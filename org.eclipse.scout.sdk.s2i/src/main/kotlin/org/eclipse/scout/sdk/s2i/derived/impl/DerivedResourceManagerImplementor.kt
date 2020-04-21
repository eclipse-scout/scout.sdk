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
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.GlobalSearchScope.fileScope
import com.intellij.psi.search.SearchScope
import com.intellij.util.concurrency.AppExecutorUtil
import com.intellij.util.messages.MessageBusConnection
import org.eclipse.scout.sdk.core.log.SdkLog
import org.eclipse.scout.sdk.core.s.environment.IEnvironment
import org.eclipse.scout.sdk.core.s.environment.IFuture
import org.eclipse.scout.sdk.core.s.environment.IProgress
import org.eclipse.scout.sdk.core.s.environment.SdkFuture
import org.eclipse.scout.sdk.core.util.JavaTypes
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
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import java.util.function.BiFunction
import kotlin.streams.toList

class DerivedResourceManagerImplementor(val project: Project) : DerivedResourceManager, SettingsChangedListener {

    private val m_updateHandlerFactories = HashMap<Class<*>, DerivedResourceHandlerFactory>() // use a map so that there is always only one factory of the same type
    private val m_eventBuffer = ArrayList<SearchScope>()
    private val m_pendingFutures = ConcurrentLinkedQueue<ScheduledFuture<*>>()
    private val m_executorService = AppExecutorUtil.getAppScheduledExecutorService()
    private var m_busConnection: MessageBusConnection? = null

    init {
        addDerivedResourceHandlerFactory(DtoUpdateHandlerFactory())
        ScoutSettings.addListener(this)
        updateSubscription()
    }

    override fun dispose() {
        unsubscribe()
        ScoutSettings.removeListener(this)
        m_updateHandlerFactories.clear()
        consumeAllBufferedEvents()
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
        cancelPendingFutures() // already scheduled but not started futures are no longer necessary because a new one is scheduled that will handle all scopes in the buffer
        registerEvent(scope)
        m_pendingFutures.add(m_executorService.schedule(this::scheduleHandlerCreation, 2, TimeUnit.SECONDS))
    }

    private fun cancelPendingFutures() {
        val it = m_pendingFutures.iterator()
        while (it.hasNext()) {
            it.next().cancel(false) // do not interrupt running task these must complete otherwise derived resources might not be updated
            it.remove()
        }
    }

    private fun registerEvent(scope: SearchScope) = synchronized(m_eventBuffer) {
        m_eventBuffer.add(scope)
    }

    private fun consumeAllBufferedEvents(): SearchScope = synchronized(m_eventBuffer) {
        if (m_eventBuffer.isEmpty()) {
            return GlobalSearchScope.EMPTY_SCOPE
        }

        var union = GlobalSearchScope.EMPTY_SCOPE
        for (scope in m_eventBuffer) {
            union = union.union(scope)
        }

        m_eventBuffer.clear()
        m_pendingFutures.clear() // there is no more work. also ensures that the current future is removed
        return union
    }

    private fun scheduleHandlerCreation() {
        val scope = consumeAllBufferedEvents()
        SdkLog.debug("Check for derived resource updates in scope $scope")
        callInIdeaEnvironment(project, "Update derived resources") { env, progress ->
            val start = System.currentTimeMillis()
            val factories = synchronized(this) { ArrayList(m_updateHandlerFactories.values) }
            val handlers = factories
                    .parallelStream()
                    .flatMap { executeDerivedResourceHandlerFactory(it, scope) }
                    .toList()
            SdkLog.debug("Derived resource handler creation took {}ms. Number of created handlers: {}", System.currentTimeMillis() - start, handlers.size)
            if (handlers.isNotEmpty() && !progress.indicator.isCanceled) {
                executeAllHandlersAndWait(handlers, env, progress)
            }
        }
    }

    private fun executeDerivedResourceHandlerFactory(factory: DerivedResourceHandlerFactory, scope: SearchScope) = computeInReadAction(project) {
        factory.createHandlersFor(scope, project).toList().stream() // create a list first (terminal operation) so that the factory is executed here!
    }

    override fun addDerivedResourceHandlerFactory(factory: DerivedResourceHandlerFactory) = synchronized(this) {
        m_updateHandlerFactories[factory::class.java] = factory
    }

    override fun removeDerivedResourceHandlerFactory(factory: DerivedResourceHandlerFactory): Boolean = synchronized(this) {
        return m_updateHandlerFactories.keys.remove(factory::class.java)
    }

    private fun executeAllHandlersAndWait(handlers: List<BiFunction<IEnvironment, IProgress, Collection<IFuture<*>>>>, env: IdeaEnvironment, progress: IdeaProgress) {
        val workForHandler = 1000
        val start = System.currentTimeMillis()
        val transaction = TransactionManager.current()
        val runningFileWrites = ArrayList<IFuture<*>>()
        progress.init("Update derived resources", handlers.size * workForHandler)

        handlers.parallelStream().forEach {
            if (progress.indicator.isCanceled) {
                return@forEach
            }
            runningFileWrites.addAll(executeHandler(it, transaction, env))
            commitTransactionIfNecessary(transaction)
            progress.worked(workForHandler)
        }

        // wait for the remaining file writes to participate in the current transaction.
        // The failure of a single file does not fail the whole transaction, therefore only log the failed writes.
        SdkFuture.awaitAllLoggingOnError(runningFileWrites)

        if (handlers.size > 1) {
            SdkLog.debug("All derived resource handlers took {}ms", System.currentTimeMillis() - start)
        }
    }

    private fun executeHandler(handler: BiFunction<IEnvironment, IProgress, Collection<IFuture<*>>>, transaction: TransactionManager, env: IdeaEnvironment): Collection<IFuture<*>> {
        val start = System.currentTimeMillis()
        try {
            return TransactionManager.callInExistingTransaction(transaction) {
                SdkLog.debug("About to execute derived resource handler: {}", handler)
                return@callInExistingTransaction handler.apply(env, IdeaEnvironment.toIdeaProgress(null))
            }
        } catch (e: ProcessCanceledException) {
            throw e // so that it is not logged below and cancels the transaction. In the end its handles by the IntelliJ framework
        } catch (e: Exception) {
            // log the exception but continue processing. The failure of one handler does not abort the transaction
            SdkLog.error("Error while: {}", handler, e)
        } finally {
            SdkLog.info("Derived resource handler took {}ms to execute: {}", System.currentTimeMillis() - start, handler)
        }
        return emptyList()
    }

    private fun commitTransactionIfNecessary(transaction: TransactionManager) {
        if (transaction.size() < 1000) {
            return
        }
        // to save memory the running transaction is committed in chunk blocks of 1000 items
        // bigger chunks are faster (less events in the IDE and less IO) but require more memory to store the transaction members
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
