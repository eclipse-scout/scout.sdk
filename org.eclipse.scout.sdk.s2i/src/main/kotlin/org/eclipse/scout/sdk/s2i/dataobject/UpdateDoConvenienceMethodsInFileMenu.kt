/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2i.dataobject

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.search.GlobalSearchScope.fileScope
import org.eclipse.scout.sdk.s2i.DataContextHelper
import org.eclipse.scout.sdk.s2i.EclipseScoutBundle
import org.eclipse.scout.sdk.s2i.EclipseScoutBundle.message

class UpdateDoConvenienceMethodsInFileMenu : AnAction(message("update.dataobject.in.file") + "...", message("update.dataobject.in.file.desc"), null) {

    override fun update(event: AnActionEvent) {
        event.presentation.isEnabled = getActiveFile(event) != null
    }

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun actionPerformed(event: AnActionEvent) {
        val file = getActiveFile(event) ?: return
        val manager = EclipseScoutBundle.dataObjectManager(file.project)
        FileDocumentManager.getInstance().saveAllDocuments() // save all documents so that the update can see the latest changes
        manager.scheduleConvenienceMethodsUpdate(fileScope(file))
    }

    private fun getActiveFile(event: AnActionEvent): PsiJavaFile? {
        val data = DataContextHelper(event.dataContext)
        val editor = data.editor() ?: return null
        val project = editor.project ?: return null
        return PsiDocumentManager.getInstance(project).getPsiFile(editor.document) as? PsiJavaFile
    }
}