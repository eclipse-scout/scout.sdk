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

import com.intellij.analysis.AnalysisScope
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileEditor.FileDocumentManager
import org.eclipse.scout.sdk.s2i.EclipseScoutBundle.message
import org.eclipse.scout.sdk.s2i.chooseAnalysisScope

class UpdateWidgetMapInScopeMenu : AnAction(message("update.widgetMap.in.scope") + "...", message("update.widgetMap.in.scope.desc"), null) {

    companion object {
        private val UNSUPPORTED_SCOPE_TYPES: List<Int> = listOf(AnalysisScope.MODULE, AnalysisScope.MODULES)
    }

    override fun actionPerformed(event: AnActionEvent) {
        val scopeToTrigger = event.chooseAnalysisScope(message("select.scope.to.update"), message("select.scope.to.update")) {
            it.scope?.scopeType !in UNSUPPORTED_SCOPE_TYPES
        } ?: return
        FileDocumentManager.getInstance().saveAllDocuments() // save all documents so that the update can see the latest changes
        WidgetMapUpdater.updateAsync(scopeToTrigger.toSearchScope(), scopeToTrigger.project)
    }
}