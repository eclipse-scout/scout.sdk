/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.s2i.nls.editor

import com.intellij.codeHighlighting.BackgroundEditorHighlighter
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorLocation
import com.intellij.openapi.fileEditor.FileEditorState
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.UserDataHolderBase
import com.intellij.openapi.vfs.AsyncFileListener
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import org.eclipse.scout.sdk.core.s.nls.Translations
import org.eclipse.scout.sdk.core.s.nls.manager.TranslationManager
import org.eclipse.scout.sdk.core.s.nls.properties.AbstractTranslationPropertiesFile
import org.eclipse.scout.sdk.core.util.Strings
import org.eclipse.scout.sdk.s2i.EclipseScoutBundle.message
import org.eclipse.scout.sdk.s2i.containingModule
import org.eclipse.scout.sdk.s2i.environment.IdeaEnvironment
import org.eclipse.scout.sdk.s2i.nls.TranslationManagerLoader
import org.eclipse.scout.sdk.s2i.toScoutProgress
import java.awt.BorderLayout
import java.awt.Graphics
import java.beans.PropertyChangeListener

class NlsEditor(val project: Project, private val vFile: VirtualFile) : UserDataHolderBase(), FileEditor {

    private val m_root = RootPanel()

    @Volatile
    private var m_reloadCheckNecessary = false

    @Volatile
    private var m_editorActive = false

    private lateinit var m_manager: TranslationManager

    inner class RootPanel : JBPanel<RootPanel>(BorderLayout()) {
        private var m_first = true

        override fun paint(g: Graphics) {
            if (m_first) {
                m_first = false
                // do not schedule directly. instead, schedule after this paint. Otherwise, there might be ArrayIndexOutOfBoundsExceptions in swing.
                ApplicationManager.getApplication().invokeLater {
                    FileDocumentManager.getInstance().saveAllDocuments() // ensures all changes are visible to the loader.
                    TranslationManagerLoader.createModalLoader(vFile, project, Translations.DependencyScope.ALL)
                        .withErrorHandler { onLoadError(it) }
                        .withManagerCreatedHandler { onManagerCreated(it) }
                        .queue()
                }
            }
            super.paint(g)
        }
    }

    private var m_content: NlsEditorContent? = null

    private fun onManagerCreated(result: TranslationManagerLoader.TranslationManagerLoaderResult?) {
        if (result?.primaryStore == null) {
            ApplicationManager.getApplication().invokeLater { m_root.add(JBLabel(message("no.translations.found"))) }
            return
        }
        setReloadCheckNecessary(false) // it was just created
        VirtualFileManager.getInstance().addAsyncFileListener(VfsListener(), this)
        m_manager = result.manager
        ApplicationManager.getApplication().invokeLater {
            val content = NlsEditorContent(project, m_manager, result.primaryStore)
            m_content = content
            m_root.add(content)
            content.textFilterField().requestFocus()
        }
    }

    private fun onLoadError(e: Throwable) = ApplicationManager.getApplication().invokeLater {
        val msg = Strings.escapeHtml(e.message)
        val stackTrace = Strings.fromThrowable(e).replace("\r", "").replace("\n", "<br>")
        val labelString = "<html>$msg<br>$stackTrace</html>"
        m_root.add(JBLabel(labelString))
    }

    private fun doReloadCheck() = IdeaEnvironment.computeInReadActionAsync(project, true) {
        val newManager = vFile.containingModule(project)
            ?.let { TranslationManagerLoader.createManager(it, Translations.DependencyScope.ALL, false, null) }
            ?.manager ?: return@computeInReadActionAsync
        if (!newManager.contentEquals(m_manager)) {
            showPropertiesFilesChangedOutsideEditorMessage()
        }
    }

    private fun showPropertiesFilesChangedOutsideEditorMessage() = ApplicationManager.getApplication().invokeLater {
        val parent = m_content
        val message = message("translations.changed.reload.question")
        val title = vFile.name
        val icon = Messages.getQuestionIcon()
        val result = if (parent == null) Messages.showYesNoDialog(project, message, title, icon) else Messages.showYesNoDialog(parent, message, title, icon)
        setReloadCheckNecessary(false) // reload will be scheduled just afterwards or user not interested in the changes
        if (result != Messages.YES) return@invokeLater

        object : Task.Modal(project, parent, message("loading.translations"), true) {
            override fun run(indicator: ProgressIndicator) {
                m_manager.reload(indicator.toScoutProgress())
            }
        }.queue()
    }

    private inner class VfsListener : AsyncFileListener {
        override fun prepareChange(events: MutableList<out VFileEvent>): AsyncFileListener.ChangeApplier? {
            if (isReloadCheckNecessary()) return null // already known that this editor must check its dirty state. Not necessary to handle more events.
            val hasPropertiesFiles = events.asSequence()
                .map { it.path } // use path here because the virtual file might be invalid (deleted)
                .any { it.endsWith(AbstractTranslationPropertiesFile.FILE_SUFFIX) } // only properties files are interesting for now
            if (hasPropertiesFiles) {
                setReloadCheckNecessary(true)
                if (isEditorActive()) {
                    doReloadCheck() // editor has the focus. do the reload-check right now (async so that the listener can finish as fast as possible)
                }
            }
            return null
        }
    }

    private fun isReloadCheckNecessary() = m_reloadCheckNecessary

    private fun setReloadCheckNecessary(checkNecessary: Boolean) {
        m_reloadCheckNecessary = checkNecessary
    }

    private fun isEditorActive() = m_editorActive

    override fun getFile() = vFile

    override fun isModified() = false

    override fun getName() = "Scout NLS"

    override fun isValid() = file.isValid

    override fun getComponent() = m_root

    override fun getPreferredFocusedComponent() = m_root

    override fun setState(state: FileEditorState) {}

    override fun selectNotify() {
        m_editorActive = true
        if (!isReloadCheckNecessary()) return // nothing to do
        doReloadCheck() // check if reload is necessary (async to not block the ui thread)
    }

    override fun deselectNotify() {
        m_editorActive = false
    }

    override fun getCurrentLocation(): FileEditorLocation? = null

    override fun getBackgroundHighlighter(): BackgroundEditorHighlighter? = null

    override fun removePropertyChangeListener(listener: PropertyChangeListener) {}

    override fun addPropertyChangeListener(listener: PropertyChangeListener) {}

    override fun dispose() {}
}