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

import com.intellij.analysis.AnalysisUIOptions
import com.intellij.analysis.BaseAnalysisActionDialog
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileEditor.FileDocumentManager
import org.eclipse.scout.sdk.s2i.DataContextHelper
import org.eclipse.scout.sdk.s2i.EclipseScoutBundle

class UpdateAllDerivedResourcesMenu : AnAction(EclipseScoutBundle.message("update.derived.resources") + "...", EclipseScoutBundle.message("update.derived.resources.desc"), null) {

    override fun actionPerformed(event: AnActionEvent) {
        val data = DataContextHelper(event.dataContext)
        val project = data.project() ?: return

        val options = AnalysisUIOptions.getInstance(project)
        val initialAnalysisScope = data.scope()!!
        val items = BaseAnalysisActionDialog.standardItems(project, initialAnalysisScope, data.module(), data.psiElement())
        val dialog = BaseAnalysisActionDialog(EclipseScoutBundle.message("update.derived.resources"),
                EclipseScoutBundle.message("select.scope.to.update"),
                project, items, options, true)
        if (dialog.showAndGet()) {
            val scopeToTrigger = dialog.getScope(initialAnalysisScope)
            val manager = EclipseScoutBundle.derivedResourceManager(project)
            FileDocumentManager.getInstance().saveAllDocuments() // save all documents so that the handlers for sure see the latest changes
            manager.trigger(scopeToTrigger.toSearchScope())
        }
    }
}
