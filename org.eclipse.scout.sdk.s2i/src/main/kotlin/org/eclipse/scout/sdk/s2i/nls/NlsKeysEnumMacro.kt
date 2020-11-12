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
package org.eclipse.scout.sdk.s2i.nls

import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.template.Expression
import com.intellij.codeInsight.template.ExpressionContext
import com.intellij.codeInsight.template.Macro
import com.intellij.codeInsight.template.Result
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
        val psiElement = context?.psiElementAtStartOffset ?: return null
        val module = psiElement.containingModule() ?: return null
        return NlsCompletionHelper.computeLookupElements(module, psiElement, false).toTypedArray()
    }
}