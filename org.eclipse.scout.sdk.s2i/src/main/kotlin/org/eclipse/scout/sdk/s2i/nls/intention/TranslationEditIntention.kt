/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2i.nls.intention

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import org.eclipse.scout.sdk.core.log.SdkLog
import org.eclipse.scout.sdk.s2i.EclipseScoutBundle.message
import org.eclipse.scout.sdk.s2i.containingModule
import org.eclipse.scout.sdk.s2i.environment.IdeaEnvironment.Factory.callInIdeaEnvironment
import org.eclipse.scout.sdk.s2i.nls.TranslationLanguageSpec.Companion.translationSpec
import org.eclipse.scout.sdk.s2i.nls.TranslationManagerLoader
import org.eclipse.scout.sdk.s2i.nls.editor.TranslationEditDialog
import org.eclipse.scout.sdk.s2i.nls.editor.TranslationNewDialog

class TranslationEditIntention : PsiElementBaseIntentionAction() {

    override fun isAvailable(project: Project, editor: Editor?, element: PsiElement) = resolveTranslationSpec(element)?.resolveTranslationKey() != null

    override fun invoke(project: Project, editor: Editor?, element: PsiElement) {
        val spec = resolveTranslationSpec(element) ?: return
        val module = element.containingModule() ?: return
        val key = spec.resolveTranslationKey() ?: return
        val manager = TranslationManagerLoader.createManager(module, spec.translationDependencyScope) ?: return
        val translationToEdit = manager.translation(key).orElse(null) ?: return
        if (!manager.isEditable) {
            SdkLog.warning("Translation '{}' cannot be edited because it is read-only.", translationToEdit.key())
            return
        }

        val dialog = if (translationToEdit.hasEditableStores())
            TranslationEditDialog(project, translationToEdit, manager)
        else
            TranslationNewDialog(project, manager.primaryEditableStore().orElseThrow(), manager, translationToEdit.key()) // override

        val ok = dialog.showAndGet()
        if (ok) {
            callInIdeaEnvironment(project, message("edit.translation.x", key)) { env, progress ->
                manager.flush(env, progress)
            }
        }
    }

    fun resolveTranslationSpec(element: PsiElement) = element.parent.translationSpec()

    override fun startInWriteAction() = false

    override fun getFamilyName() = "Scout"

    override fun getText() = message("edit.translation")
}