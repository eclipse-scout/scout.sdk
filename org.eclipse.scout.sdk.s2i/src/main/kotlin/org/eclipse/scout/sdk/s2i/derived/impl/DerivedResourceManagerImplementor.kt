package org.eclipse.scout.sdk.s2i.derived.impl

import com.intellij.AppTopics
import com.intellij.openapi.components.ProjectComponent
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileDocumentManagerListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.GlobalSearchScope.fileScope
import com.intellij.util.concurrency.AppExecutorUtil
import com.intellij.util.messages.MessageBusConnection
import org.eclipse.scout.sdk.core.log.SdkLog
import org.eclipse.scout.sdk.core.s.environment.Future
import org.eclipse.scout.sdk.core.s.environment.IEnvironment
import org.eclipse.scout.sdk.core.s.environment.IFuture
import org.eclipse.scout.sdk.core.s.environment.IProgress
import org.eclipse.scout.sdk.s2i.derived.DerivedResourceHandlerFactory
import org.eclipse.scout.sdk.s2i.derived.DerivedResourceManager
import org.eclipse.scout.sdk.s2i.environment.IdeaEnvironment
import org.eclipse.scout.sdk.s2i.environment.IdeaProgress
import org.eclipse.scout.sdk.s2i.environment.TransactionManager
import org.eclipse.scout.sdk.s2i.settings.ScoutSettings
import org.eclipse.scout.sdk.s2i.settings.SettingsChangedListener
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import java.util.function.BiFunction

open class DerivedResourceManagerImplementor(private val project: Project) : ProjectComponent, DerivedResourceManager, SettingsChangedListener {

    private val m_updateHandlerFactories = ArrayList<DerivedResourceHandlerFactory>()
    private val m_eventBuffer = ArrayList<GlobalSearchScope>()
    private val m_pendingFutures = ConcurrentLinkedQueue<ScheduledFuture<*>>()
    private val m_executorService = AppExecutorUtil.getAppScheduledExecutorService()

    private var m_busConnection: MessageBusConnection? = null

    override fun initComponent() = synchronized(this) {
        addDerivedResourceHandlerFactory(DtoUpdateHandlerFactory())
        ScoutSettings.addListener(this)
        updateSubscription()
    }

    override fun disposeComponent(): Unit = synchronized(this) {
        unsubscribe()
        ScoutSettings.removeListener(this)
        m_updateHandlerFactories.clear()
        consumeAllBufferedEvents()
    }

    override fun changed(key: String, oldVal: String?, newVal: String?) {
        if (ScoutSettings.autoUpdateDerivedResourcesKey == key) {
            updateSubscription()
        }
    }

    protected fun subscribe() = synchronized(this) {
        if (m_busConnection != null) {
            return@synchronized
        }
        m_busConnection = project.messageBus.connect()
        m_busConnection?.subscribe(AppTopics.FILE_DOCUMENT_SYNC, DocumentSyncListener())
    }

    protected fun unsubscribe() = synchronized(this) {
        m_busConnection?.disconnect()
        m_busConnection = null
    }

    protected fun updateSubscription() {
        if (isAutoUpdateDerivedResources()) {
            subscribe()
        } else {
            unsubscribe()
        }
    }

    override fun isAutoUpdateDerivedResources(): Boolean = ScoutSettings.isAutoUpdateDerivedResources(project)

    override fun trigger(scope: GlobalSearchScope) {
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

    private fun registerEvent(scope: GlobalSearchScope) = synchronized(m_eventBuffer) {
        m_eventBuffer.add(scope)
    }

    private fun consumeAllBufferedEvents(): GlobalSearchScope = synchronized(m_eventBuffer) {
        if (m_eventBuffer.isEmpty()) {
            return GlobalSearchScope.EMPTY_SCOPE
        }
        val unionScope = GlobalSearchScope.union(m_eventBuffer)
        m_eventBuffer.clear()
        m_pendingFutures.clear() // there is no more work. also ensures that the current future is removed
        return unionScope
    }

    private fun scheduleHandlerCreation() {
        val scope = consumeAllBufferedEvents()
        SdkLog.debug("Check for derived resource updates in scope $scope")
        IdeaEnvironment.callInIdeaEnvironment({ env, progress ->
            val handlers = synchronized(this) {
                m_updateHandlerFactories.flatMap { it.createHandlersFor(scope, project) }
            }
            executeHandlers(handlers, env, progress)
        }, project, "Update derived resources")
    }

    override fun addDerivedResourceHandlerFactory(factory: DerivedResourceHandlerFactory) = synchronized(this) {
        m_updateHandlerFactories += factory
    }

    override fun removeDerivedResourceHandlerFactory(factory: DerivedResourceHandlerFactory) = synchronized(this) {
        m_updateHandlerFactories -= factory
    }

    protected fun executeHandlers(handlers: List<BiFunction<IEnvironment, IProgress, Collection<IFuture<*>>>>, env: IdeaEnvironment, progress: IdeaProgress) {
        progress.init("Update derived resources", handlers.size)
        val indicator = IdeaEnvironment.toIdeaProgress(progress).indicator
        val fileWrites = ArrayList<IFuture<*>>()
        val transactionManager = TransactionManager.current()
        for (handler in handlers) {
            indicator.text2 = handler.toString()
            val start = System.currentTimeMillis()
            try {
                fileWrites.addAll(handler.apply(env, progress.newChild(1)))
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

    protected inner class DocumentSyncListener : FileDocumentManagerListener {
        override fun fileContentReloaded(file: VirtualFile, document: Document) {
            trigger(fileScope(project, file))
        }

        override fun beforeDocumentSaving(document: Document) {
            val file = FileDocumentManager.getInstance().getFile(document) ?: return
            fileContentReloaded(file, document)
        }
    }
}