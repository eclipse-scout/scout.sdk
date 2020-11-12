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
package org.eclipse.scout.sdk.s2i.nls.editor

import com.intellij.codeHighlighting.BackgroundEditorHighlighter
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorLocation
import com.intellij.openapi.fileEditor.FileEditorState
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.UserDataHolderBase
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.components.JBLabel
import org.eclipse.scout.sdk.core.s.nls.TranslationStores
import org.eclipse.scout.sdk.core.util.Strings
import org.eclipse.scout.sdk.s2i.EclipseScoutBundle.message
import org.eclipse.scout.sdk.s2i.nls.TranslationStoreStackLoader
import java.awt.BorderLayout
import java.awt.Graphics
import java.beans.PropertyChangeListener
import javax.swing.JPanel

class NlsEditor(val project: Project, private val vFile: VirtualFile) : UserDataHolderBase(), FileEditor {

    private val m_root: JPanel = object : JPanel(BorderLayout()) {
        private var m_first = true

        override fun paint(g: Graphics) {
            if (m_first) {
                m_first = false
                // do not schedule directly. instead schedule after this paint. otherwise there might be ArrayIndexOutOfBoundsExceptions in swing.
                ApplicationManager.getApplication().invokeLater {
                    FileDocumentManager.getInstance().saveAllDocuments() // ensures all changes are visible to the loader.
                    TranslationStoreStackLoader.createModalLoader(vFile, project, TranslationStores.DependencyScope.ALL)
                            .withErrorHandler { onLoadError(it) }
                            .withStackCreatedHandler { onStackCreated(it) }
                            .queue()
                }
            }
            super.paint(g)
        }
    }
    private var m_content: NlsEditorContent? = null

    private fun onStackCreated(result: TranslationStoreStackLoader.TranslationStoreStackLoaderResult?) {
        if (result?.primaryStore == null) {
            ApplicationManager.getApplication().invokeLater { m_root.add(JBLabel(message("no.translations.found"))) }
            return
        }

        ApplicationManager.getApplication().invokeLater {
            val content = NlsEditorContent(project, result.stack, result.primaryStore)
            m_content = content
            m_root.add(content)
            content.textFilterField().requestFocus()
        }
    }

    private fun onLoadError(e: Throwable) = ApplicationManager.getApplication().invokeLater {
        m_root.add(JBLabel(toLabelString(e)))
    }

    private fun toLabelString(e: Throwable): String {
        val msg = Strings.escapeHtml(e.message)
        val stackTrace = Strings.fromThrowable(e).replace("\r", "").replace("\n", "<br>")
        return "<html>$msg<br>$stackTrace</html>"
    }

    override fun getFile() = vFile

    override fun isModified() = false

    override fun getName() = "Scout NLS"

    override fun isValid() = file.isValid

    override fun getComponent() = m_root

    override fun getPreferredFocusedComponent() = m_root

    override fun setState(state: FileEditorState) {
    }

    override fun selectNotify() {
    }

    override fun deselectNotify() {
    }

    override fun getCurrentLocation(): FileEditorLocation? = null

    override fun getBackgroundHighlighter(): BackgroundEditorHighlighter? = null

    override fun removePropertyChangeListener(listener: PropertyChangeListener) {
    }

    override fun addPropertyChangeListener(listener: PropertyChangeListener) {
    }

    override fun dispose() {
    }
}