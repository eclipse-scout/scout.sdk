/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.s2i.derived.impl

import com.intellij.AppTopics
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileDocumentManagerListener
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.GlobalSearchScope.fileScope
import com.intellij.psi.search.SearchScope
import com.intellij.util.concurrency.AppExecutorUtil
import com.intellij.util.messages.MessageBusConnection
import org.eclipse.scout.sdk.core.log.SdkLog
import org.eclipse.scout.sdk.core.log.SdkLog.onTrace
import org.eclipse.scout.sdk.core.s.derived.IDerivedResourceHandler
import org.eclipse.scout.sdk.core.s.environment.IFuture
import org.eclipse.scout.sdk.core.s.environment.IProgress
import org.eclipse.scout.sdk.core.s.environment.SdkFuture
import org.eclipse.scout.sdk.core.util.JavaTypes
import org.eclipse.scout.sdk.s2i.EclipseScoutBundle.Companion.message
import org.eclipse.scout.sdk.s2i.derived.DerivedResourceHandlerFactory
import org.eclipse.scout.sdk.s2i.derived.DerivedResourceManager
import org.eclipse.scout.sdk.s2i.environment.IdeaEnvironment
import org.eclipse.scout.sdk.s2i.environment.IdeaEnvironment.Factory.callInIdeaEnvironment
import org.eclipse.scout.sdk.s2i.environment.IdeaEnvironment.Factory.computeInReadAction
import org.eclipse.scout.sdk.s2i.environment.IdeaProgress
import org.eclipse.scout.sdk.s2i.environment.TransactionManager
import org.eclipse.scout.sdk.s2i.settings.ScoutSettings
import org.eclipse.scout.sdk.s2i.settings.SettingsChangedListener
import java.util.Collections.emptyList
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.streams.toList


class DerivedResourceManagerImplementor(val project: Project) : DerivedResourceManager, SettingsChangedListener {

    private val m_updateHandlerFactories = HashMap<Class<*>, DerivedResourceHandlerFactory>() // use a map so that there is always only one factory of the same type
    private val m_eventBuffer = ArrayList<SearchScope>()
    private val m_lock = ReentrantLock()
    private val m_dataAvailable = m_lock.newCondition()

    @Volatile
    private var m_busConnection: MessageBusConnection? = null
    @Volatile
    private var m_workerEnabled = false

    init {
        Disposer.register(project, this) // ensure it is disposed when the project closes
    }

    override fun start() {
        addDerivedResourceHandlerFactory(DtoUpdateHandlerFactory())
        ScoutSettings.addListener(this)
        updateSubscription()
        m_workerEnabled = true // must be before the schedule. otherwise it ends immediately.
        AppExecutorUtil.getAppExecutorService().submit(this::dispatchWork)
    }

    override fun dispose() {
        m_workerEnabled = false
        unsubscribe()
        ScoutSettings.removeListener(this)
        m_updateHandlerFactories.clear()
    }

    override fun changed(key: String, oldVal: String?, newVal: String?) {
        if (ScoutSettings.KEY_AUTO_UPDATE_DERIVED_RESOURCES == key) {
            updateSubscription()
        }
    }

    private fun subscribe() = synchronized(this) {
        if (m_busConnection != null) {
            return@synchronized
        }
        m_busConnection = project.messageBus.connect()
        m_busConnection?.subscribe(AppTopics.FILE_DOCUMENT_SYNC, DocumentSyncListener())
    }

    private fun unsubscribe() = synchronized(this) {
        m_busConnection?.disconnect()
        m_busConnection = null
    }

    private fun updateSubscription() {
        if (isAutoUpdateDerivedResources()) {
            subscribe()
        } else {
            unsubscribe()
        }
    }

    override fun isAutoUpdateDerivedResources(): Boolean = ScoutSettings.isAutoUpdateDerivedResources(project)

    override fun trigger(scope: SearchScope) {
        SdkLog.debug("Derived resource update event for scope $scope")
        m_lock.withLock {
            m_eventBuffer.add(scope)
            m_dataAvailable.signalAll()
        }
    }

    private fun dispatchWork() {
        while (m_workerEnabled) {
            val scope = getWork()
            if (scope != GlobalSearchScope.EMPTY_SCOPE) {
                try {
                    performUpdateAsync(scope).awaitDoneThrowingOnError()
                } catch (e: Throwable) { // use throwable here otherwise the worker will be killed an will not start again. This way it has the chance to recover.
                    SdkLog.warning("Derived resource update for scope '{}' failed.", scope, e)
                }
            }
        }
    }

    private fun getWork(): SearchScope {
        waitUntilDataAvailable() // wait until some data arrived
        if (m_workerEnabled) {
            Thread.sleep(TimeUnit.SECONDS.toMillis(2)) // wait for more work to come
        }
        return consumeAllBufferedEvents() // the complete work
    }

    private fun waitUntilDataAvailable() = m_lock.withLock {
        while (m_eventBuffer.isEmpty()) {
            m_dataAvailable.await()
        }
    }

    private fun consumeAllBufferedEvents(): SearchScope = m_lock.withLock {
        if (m_eventBuffer.isEmpty()) {
            return GlobalSearchScope.EMPTY_SCOPE
        }
        var union = GlobalSearchScope.EMPTY_SCOPE
        for (scope in m_eventBuffer) {
            union = union.union(scope)
        }
        m_eventBuffer.clear()
        return union
    }

    private fun performUpdateAsync(scope: SearchScope): IFuture<Unit> = callInIdeaEnvironment(project, message("update.derived.resources")) { env, progress ->
        SdkLog.debug("Check for derived resource updates in scope $scope")
        val start = System.currentTimeMillis()
        val factories = synchronized(m_updateHandlerFactories) { ArrayList(m_updateHandlerFactories.values) }
        val handlers = factories
                .parallelStream()
                .flatMap { executeDerivedResourceHandlerFactory(it, scope) }
                .toList()
        SdkLog.debug("Derived resource handler creation took {}ms. Number of created handlers: {}", System.currentTimeMillis() - start, handlers.size)
        if (handlers.isNotEmpty() && !progress.indicator.isCanceled) {
            executeAllHandlersAndWait(handlers, env, progress)
        }
    }

    private fun executeDerivedResourceHandlerFactory(factory: DerivedResourceHandlerFactory, scope: SearchScope) = computeInReadAction(project) {
        factory.createHandlersFor(scope, project).toList().stream() // create a list first (terminal operation) so that the factory is executed here!
    }

    override fun addDerivedResourceHandlerFactory(factory: DerivedResourceHandlerFactory) = synchronized(m_updateHandlerFactories) {
        m_updateHandlerFactories[factory::class.java] = factory
    }

    override fun removeDerivedResourceHandlerFactory(factory: DerivedResourceHandlerFactory): Boolean = synchronized(m_updateHandlerFactories) {
        return m_updateHandlerFactories.keys.remove(factory::class.java)
    }

    private fun executeAllHandlersAndWait(handlers: List<IDerivedResourceHandler>, env: IdeaEnvironment, progress: IdeaProgress) {
        val workForHandler = 1000
        val start = System.currentTimeMillis()
        val transaction = TransactionManager.current()
        val runningFileWrites = ConcurrentLinkedQueue<IFuture<*>>()
        val indicator = progress.indicator
        progress.init(handlers.size * workForHandler, message("update.derived.resources"))

        handlers.forEach {
            try {
                if (indicator.isCanceled) {
                    return@forEach
                }
                indicator.text2 = it.toString()
                runningFileWrites.addAll(executeHandler(it, transaction, env, progress.newChild(workForHandler)))
                commitTransactionIfNecessary(transaction, runningFileWrites, indicator)
            } catch (t: Throwable) {
                // catch throwable here because parallel streams return early: the first thread having this throwable would report to the caller which quits the transaction and kills the java environment. but other threads may still try to use it -> boom.
                indicator.cancel() // fatal error: abort all
                SdkLog.error("Fatal error while: {}", it, t)
            }
        }

        // wait for the remaining file writes to participate in the current transaction.
        // The failure of a single file does not fail the whole transaction, therefore only log the failed writes.
        SdkFuture.awaitAllLoggingOnError(runningFileWrites)

        if (handlers.size > 1) {
            SdkLog.debug("All derived resource handlers took {}ms", System.currentTimeMillis() - start)
        }
    }

    private fun executeHandler(handler: IDerivedResourceHandler, transaction: TransactionManager, env: IdeaEnvironment, progress: IProgress): Collection<IFuture<*>> {
        val start = System.currentTimeMillis()

        try {
            return TransactionManager.callInExistingTransaction(transaction) {
                SdkLog.debug("About to execute derived resource handler: {}", handler)
                return@callInExistingTransaction handler.apply(env, progress)
            }
        } catch (e: ProcessCanceledException) {
            SdkLog.debug("Derived resources update canceled.", onTrace(e))
        } catch (e: RuntimeException) {
            // log the exception but continue processing. The failure of one handler does not abort the transaction
            SdkLog.error("Error while: {}", handler, e)
        } finally {
            SdkLog.info("Derived resource handler took {}ms to execute: {}", System.currentTimeMillis() - start, handler)
        }
        return emptyList()
    }

    private fun commitTransactionIfNecessary(transaction: TransactionManager, runningFileWrites: MutableCollection<IFuture<*>>, indicator: ProgressIndicator) {
        if (indicator.isCanceled) {
            return
        }

        // to save memory the running transaction is committed in chunk blocks
        // bigger chunks are faster (less events in the IDE) but require more memory to store the transaction members
        val chunkSize = TransactionManager.BULK_UPDATE_LIMIT
        if (transaction.size() < chunkSize) {
            return
        }

        SdkFuture.awaitAllLoggingOnError(runningFileWrites)
        runningFileWrites.clear()

        SdkLog.debug("Derived resource update transaction chunk size reached. Performing intermediate commit.")
        transaction.checkpoint(null)
    }

    private inner class DocumentSyncListener : FileDocumentManagerListener {
        override fun fileContentReloaded(file: VirtualFile, document: Document) {
            if (JavaTypes.JAVA_FILE_EXTENSION == file.extension) {
                trigger(fileScope(project, file))
            }
        }

        override fun beforeDocumentSaving(document: Document) {
            val file = FileDocumentManager.getInstance().getFile(document) ?: return
            fileContentReloaded(file, document)
        }
    }
}
