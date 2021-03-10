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
package org.eclipse.scout.sdk.s2i.derived

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileEditor.FileDocumentManager
import org.eclipse.scout.sdk.s2i.EclipseScoutBundle
import org.eclipse.scout.sdk.s2i.EclipseScoutBundle.message
import org.eclipse.scout.sdk.s2i.chooseAnalysisScope

class UpdateAllDerivedResourcesMenu : AnAction(message("update.derived.resources") + "...", message("update.derived.resources.desc"), null) {

    override fun actionPerformed(event: AnActionEvent) {
        val scopeToTrigger = event.chooseAnalysisScope(message("update.derived.resources"), message("select.scope.to.update")) ?: return
        val manager = EclipseScoutBundle.derivedResourceManager(scopeToTrigger.project)
        FileDocumentManager.getInstance().saveAllDocuments() // save all documents so that the handlers for sure see the latest changes
        manager.trigger(scopeToTrigger.toSearchScope())
    }
}
