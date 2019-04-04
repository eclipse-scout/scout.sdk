package org.eclipse.scout.sdk.s2i.derived.impl

import com.intellij.AppTopics
import com.intellij.openapi.components.ProjectComponent
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileDocumentManagerListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.GlobalSearchScope.fileScope
import com.intellij.psi.search.SearchScope
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
import org.eclipse.scout.sdk.s2i.settings.ScoutSettings
import org.eclipse.scout.sdk.s2i.settings.SettingsChangedListener
import java.util.function.BiFunction

open class DerivedResourceManagerImplementor(private val project: Project) : ProjectComponent, DerivedResourceManager, SettingsChangedListener {


    private val m_updateHandlerFactories = ArrayList<DerivedResourceHandlerFactory>()
    private var m_busConnection: MessageBusConnection? = null

    override fun initComponent() {
        addDerivedResourceHandlerFactory(DtoUpdateHandlerFactory())
        ScoutSettings.addListener(this)
        updateSubscription()
    }

    override fun disposeComponent() {
        unsubscribe()
        ScoutSettings.removeListener(this)
        m_updateHandlerFactories.clear()
    }

    override fun changed(key: String, oldVal: String?, newVal: String?) {
        if (ScoutSettings.autoUpdateDerivedResourcesKey == key) {
            updateSubscription()
        }
    }

    protected fun subscribe() {
        synchronized(this) {
            if (m_busConnection != null) {
                return
            }
            m_busConnection = project.messageBus.connect()
            m_busConnection?.subscribe(AppTopics.FILE_DOCUMENT_SYNC, DocumentSyncListener())
        }
    }

    protected fun unsubscribe() {
        synchronized(this) {
            m_busConnection?.disconnect()
            m_busConnection = null
        }
    }

    protected fun updateSubscription() {
        if (isAutoUpdateDerivedResources()) {
            subscribe()
        } else {
            unsubscribe()
        }
    }

    override fun isAutoUpdateDerivedResources(): Boolean = ScoutSettings.isAutoUpdateDerivedResources(project)

    override fun trigger(scope: SearchScope) {
        IdeaEnvironment.callInIdeaEnvironment({ env, progress ->
            executeHandlers(m_updateHandlerFactories.flatMap { it.createHandlersFor(scope, project) }, env, progress)
        }, project, "Update derived resources")
    }

    override fun addDerivedResourceHandlerFactory(factory: DerivedResourceHandlerFactory) {
        m_updateHandlerFactories += factory
    }

    override fun removeDerivedResourceHandlerFactory(factory: DerivedResourceHandlerFactory) {
        m_updateHandlerFactories -= factory
    }

    protected fun executeHandlers(handlers: List<BiFunction<IEnvironment, IProgress, Collection<IFuture<*>>>>, env: IdeaEnvironment, progress: IdeaProgress) {
        progress.init("Update derived resources", handlers.size)
        val indicator = IdeaEnvironment.toIdeaProgress(progress).indicator
        val fileWrites = ArrayList<IFuture<*>>()
        for (handler in handlers) {
            indicator.text2 = handler.toString()
            try {
                fileWrites.addAll(handler.apply(env, progress))
            } catch (e: Throwable) {
                SdkLog.error("Error while: {}", indicator.text2, e)
            } finally {
                progress.worked(1)
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