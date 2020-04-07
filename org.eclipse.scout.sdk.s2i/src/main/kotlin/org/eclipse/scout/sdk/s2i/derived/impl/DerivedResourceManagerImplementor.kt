/*
 * Copyright (c) 2010-2019 BSI Business Systems Integration AG.
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
import org.eclipse.scout.sdk.core.s.environment.Future
import org.eclipse.scout.sdk.core.s.environment.IEnvironment
import org.eclipse.scout.sdk.core.s.environment.IFuture
import org.eclipse.scout.sdk.core.s.environment.IProgress
import org.eclipse.scout.sdk.core.util.JavaTypes
import org.eclipse.scout.sdk.s2i.derived.DerivedResourceHandlerFactory
import org.eclipse.scout.sdk.s2i.derived.DerivedResourceManager
import org.eclipse.scout.sdk.s2i.environment.IdeaEnvironment
import org.eclipse.scout.sdk.s2i.environment.IdeaEnvironment.Factory.callInIdeaEnvironment
import org.eclipse.scout.sdk.s2i.environment.IdeaProgress
import org.eclipse.scout.sdk.s2i.environment.TransactionManager
import org.eclipse.scout.sdk.s2i.settings.ScoutSettings
import org.eclipse.scout.sdk.s2i.settings.SettingsChangedListener
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import java.util.function.BiFunction

class DerivedResourceManagerImplementor(val project: Project) : DerivedResourceManager, SettingsChangedListener {

    private val m_updateHandlerFactories = ArrayList<DerivedResourceHandlerFactory>()
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
            val handlers = synchronized(this) {
                m_updateHandlerFactories.flatMap { it.createHandlersFor(scope, project) }
            }
            executeHandlers(handlers, env, progress)
        }
    }

    override fun addDerivedResourceHandlerFactory(factory: DerivedResourceHandlerFactory) = synchronized(this) {
        m_updateHandlerFactories += factory
    }

    override fun removeDerivedResourceHandlerFactory(factory: DerivedResourceHandlerFactory) = synchronized(this) {
        m_updateHandlerFactories -= factory
    }

    private fun executeHandlers(handlers: List<BiFunction<IEnvironment, IProgress, Collection<IFuture<*>>>>, env: IdeaEnvironment, progress: IdeaProgress) {
        progress.init("Update derived resources", handlers.size)
        val indicator = IdeaEnvironment.toIdeaProgress(progress).indicator
        val fileWrites = ArrayList<IFuture<*>>()
        val transactionManager = TransactionManager.current()
        for (handler in handlers) {
            indicator.text2 = handler.toString()
            val start = System.currentTimeMillis()
            try {
                SdkLog.debug("Executing handler '{}'", handler)
                fileWrites.addAll(handler.apply(env, progress.newChild(1)))
            } catch (e: ProcessCanceledException) {
                SdkLog.debug("{} has been cancelled.", indicator.text2, e)
            } catch (e: Exception) {
                SdkLog.error("Error while: {}", indicator.text2, e)
            } finally {
                SdkLog.info("Derived Resource Handler ($handler) took {}ms to execute.", System.currentTimeMillis() - start)
            }

            if (transactionManager.size() >= 500) {
                Future.awaitAll(fileWrites)
                fileWrites.clear()
                transactionManager.checkpoint(null)
            }
        }
        Future.awaitAll(fileWrites)
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
