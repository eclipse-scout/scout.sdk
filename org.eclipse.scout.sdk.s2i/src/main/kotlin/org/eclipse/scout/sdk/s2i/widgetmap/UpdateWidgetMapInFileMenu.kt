/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2i.widgetmap

import com.intellij.lang.javascript.psi.JSFile
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.psi.PsiDocumentManager
import org.eclipse.scout.sdk.s2i.DataContextHelper
import org.eclipse.scout.sdk.s2i.EclipseScoutBundle.message

class UpdateWidgetMapInFileMenu : AnAction(message("update.widgetMap.in.file"), message("update.widgetMap.in.file.desc"), null) {

    override fun update(event: AnActionEvent) {
        event.presentation.isEnabled = getActiveFile(event) != null
    }

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun actionPerformed(event: AnActionEvent) {
        val file = getActiveFile(event) ?: return
        FileDocumentManager.getInstance().saveAllDocuments() // save all documents so that the update can see the latest changes
        WidgetMapUpdater.update(file.virtualFile, file.project)
    }

    private fun getActiveFile(event: AnActionEvent): JSFile? {
        val data = DataContextHelper(event.dataContext)
        val editor = data.editor() ?: return null
        val project = editor.project ?: return null
        return PsiDocumentManager.getInstance(project).getPsiFile(editor.document) as? JSFile
    }
}