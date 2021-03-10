/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.s2i.dataobject

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileEditor.FileDocumentManager
import org.eclipse.scout.sdk.s2i.EclipseScoutBundle
import org.eclipse.scout.sdk.s2i.EclipseScoutBundle.message
import org.eclipse.scout.sdk.s2i.chooseAnalysisScope

class UpdateDoConvenienceMethodsInScopeMenu : AnAction(message("update.dataobject.in.scope") + "...", message("update.dataobject.in.scope.desc"), null) {

    override fun actionPerformed(event: AnActionEvent) {
        val scopeToTrigger = event.chooseAnalysisScope(message("update.dataobject.in.scope"), message("select.scope.to.update")) ?: return
        val manager = EclipseScoutBundle.dataObjectManager(scopeToTrigger.project)
        FileDocumentManager.getInstance().saveAllDocuments() // save all documents so that the update can see the latest changes
        manager.scheduleConvenienceMethodsUpdate(scopeToTrigger.toSearchScope())
    }
}