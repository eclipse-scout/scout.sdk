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
package org.eclipse.scout.sdk.s2i.nls.intention

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import org.eclipse.scout.sdk.s2i.EclipseScoutBundle.message
import org.eclipse.scout.sdk.s2i.containingModule
import org.eclipse.scout.sdk.s2i.environment.IdeaEnvironment.Factory.callInIdeaEnvironment
import org.eclipse.scout.sdk.s2i.nls.TranslationLanguageSpec.Companion.translationSpec
import org.eclipse.scout.sdk.s2i.nls.TranslationStoreStackLoader
import org.eclipse.scout.sdk.s2i.nls.editor.TranslationEditDialog

class TranslationEditIntention : PsiElementBaseIntentionAction() {

    override fun isAvailable(project: Project, editor: Editor?, element: PsiElement) = resolveTranslationSpec(element)?.resolveTranslationKey() != null

    override fun invoke(project: Project, editor: Editor?, element: PsiElement) {
        val spec = resolveTranslationSpec(element) ?: return
        val module = element.containingModule() ?: return
        val key = spec.resolveTranslationKey() ?: return
        val stack = TranslationStoreStackLoader.createStack(module, spec.translationDependencyScope) ?: return
        val translationToEdit = stack.translation(key).orElse(null) ?: return

        val dialog = TranslationEditDialog(project, translationToEdit, stack)
        val ok = dialog.showAndGet()
        if (ok) {
            callInIdeaEnvironment(project, message("edit.translation.x", key)) { env, progress ->
                stack.flush(env, progress)
            }
        }
    }

    fun resolveTranslationSpec(element: PsiElement) = element.parent.translationSpec()

    override fun startInWriteAction() = false

    override fun getFamilyName() = "Scout"

    override fun getText() = message("edit.translation")
}