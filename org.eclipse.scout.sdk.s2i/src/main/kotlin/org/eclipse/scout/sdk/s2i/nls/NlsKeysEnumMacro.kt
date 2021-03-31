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
package org.eclipse.scout.sdk.s2i.nls

import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.template.Expression
import com.intellij.codeInsight.template.ExpressionContext
import com.intellij.codeInsight.template.Macro
import com.intellij.codeInsight.template.Result
import com.intellij.psi.PsiDocumentManager
import org.eclipse.scout.sdk.s2i.containingModule
import org.eclipse.scout.sdk.s2i.nls.completion.NlsCompletionHelper

/**
 * A Macro providing all accessible NLS keys in the containing Module.
 */
class NlsKeysEnumMacro : Macro() {

    companion object {
        const val NAME = "nlsKeysEnum"
    }

    override fun getName() = NAME

    override fun getPresentableName() = "$NAME()"

    override fun calculateResult(params: Array<out Expression>, context: ExpressionContext?): Result? = null

    override fun calculateLookupItems(params: Array<out Expression>, context: ExpressionContext?): Array<LookupElement>? {
        if (context == null) return null
        val document = context.editor?.document ?: return null
        val psiFile = PsiDocumentManager.getInstance(context.project).getPsiFile(document) ?: return null
        // do not use context.getPsiElementAtStartOffset() because it uses context.getTemplateStartOffset() but here context.getStartOffset() is required
        val psiElement = psiFile.findElementAt(context.startOffset) ?: return null
        val module = psiElement.containingModule() ?: return null
        return NlsCompletionHelper.computeLookupElements(module, psiElement).toTypedArray()
    }
}