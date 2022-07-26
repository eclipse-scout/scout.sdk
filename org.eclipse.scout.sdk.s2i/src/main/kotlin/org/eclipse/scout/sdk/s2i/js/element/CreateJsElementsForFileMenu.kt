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
package org.eclipse.scout.sdk.s2i.js.element

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.search.GlobalSearchScope.fileScope
import org.eclipse.scout.sdk.s2i.DataContextHelper
import org.eclipse.scout.sdk.s2i.EclipseScoutBundle
import org.eclipse.scout.sdk.s2i.EclipseScoutBundle.message

class CreateJsElementsForFileMenu : AnAction(message("create.js.elements.for.file") + "...", message("create.js.elements.for.file.desc"), null) {

    override fun update(event: AnActionEvent) {
        event.presentation.isEnabled = getActiveFile(event) != null
    }

    override fun actionPerformed(event: AnActionEvent) {
        val file = getActiveFile(event) ?: return
        val manager = EclipseScoutBundle.jsElementsManager(file.project)
        FileDocumentManager.getInstance().saveAllDocuments() // save all documents so that the update can see the latest changes
        manager.scheduleJsElementsCreation(fileScope(file))
    }

    private fun getActiveFile(event: AnActionEvent): PsiJavaFile? {
        val data = DataContextHelper(event.dataContext)
        val editor = data.editor() ?: return null
        val project = editor.project ?: return null
        return PsiDocumentManager.getInstance(project).getPsiFile(editor.document) as? PsiJavaFile
    }
}