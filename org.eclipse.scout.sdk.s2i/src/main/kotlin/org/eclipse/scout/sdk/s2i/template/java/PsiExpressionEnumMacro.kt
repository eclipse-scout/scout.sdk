/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2i.template.java

import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.codeInsight.template.*
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiElement
import com.intellij.psi.codeStyle.JavaCodeStyleManager

/**
 * Macro that converts all input parameters into [com.intellij.psi.PsiExpression]
 */
class PsiExpressionEnumMacro : Macro() {

    companion object {
        const val NAME = "psiExpressionEnum"
    }

    override fun getName() = NAME

    override fun getPresentableName() = "$NAME(...)"

    override fun calculateResult(params: Array<out Expression>, context: ExpressionContext?) =
            firstExpression(params, context)
                    ?.let { PsiElementResult(it.second, it.first) }

    override fun calculateLookupItems(params: Array<out Expression>, context: ExpressionContext?) =
            allExpressions(params, context)
                    .map { LookupElementBuilder.create(it.second, it.first).withPresentableText(it.second.text) }
                    .toTypedArray()

    override fun isAcceptableInContext(context: TemplateContextType?) = context is JavaCodeContextType

    private fun firstExpression(params: Array<out Expression>, context: ExpressionContext?): Pair<String, PsiElement>? {
        val firstExpression = params.firstOrNull() ?: return null
        return allExpressions(arrayOf(firstExpression), context).firstOrNull()
    }

    private fun allExpressions(params: Array<out Expression>, context: ExpressionContext?): List<Pair<String, PsiElement>> {
        val ctx = context ?: return emptyList()
        val psiElementFactory = JavaPsiFacade.getElementFactory(ctx.project)
        val codeStyle = JavaCodeStyleManager.getInstance(ctx.project)
        return params
                .mapNotNull { it.calculateResult(context) }
                .map { it.toString() }
                .map { it to codeStyle.shortenClassReferences(psiElementFactory.createExpressionFromText(it, ctx.psiElementAtStartOffset)) }
    }

    open class PsiElementResult(val psiElement: PsiElement, val text: String) : JavaPsiElementResult(psiElement) {
        override fun toString() = text
    }
}
