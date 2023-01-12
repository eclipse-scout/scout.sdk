/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2i.nls.folding

import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.psi.*
import com.intellij.psi.util.PsiTreeUtil
import org.eclipse.scout.sdk.core.s.nls.manager.TranslationManager
import org.eclipse.scout.sdk.s2i.nls.TranslationLanguageSpec.Companion.translationSpec

class NlsFoldingBuilderForJava : AbstractNlsFoldingBuilder() {

    override fun buildFoldRegions(root: PsiElement, manager: TranslationManager): List<FoldingDescriptor> {
        val folds = ArrayList<FoldingDescriptor>()
        root.accept(object : JavaRecursiveElementWalkingVisitor() {
            override fun visitLiteralExpression(expression: PsiLiteralExpression) {
                super.visitLiteralExpression(expression)
                visitElement(expression)
            }

            override fun visitReferenceExpression(expression: PsiReferenceExpression) {
                super.visitReferenceExpression(expression)
                visitElement(expression)
            }

            override fun visitMethodCallExpression(expression: PsiMethodCallExpression) {
                super.visitMethodCallExpression(expression)
                visitElement(expression)
            }

            private fun visitElement(expression: PsiExpression) {
                val translationKey = expression.translationSpec()?.resolveTranslationKey() ?: return
                createFoldingDescriptor(translationKey, expression, manager)?.let { folds.add(it) }
            }
        })
        return folds
    }

    private fun createFoldingDescriptor(key: String, literal: PsiExpression, manager: TranslationManager): FoldingDescriptor? {
        val methodCall = PsiTreeUtil.getParentOfType(literal, PsiMethodCallExpression::class.java) ?: return null
        val element = if (methodCall.methodExpression.qualifierExpression is PsiReferenceExpression) methodCall else literal
        return createFoldingDescriptor(element, key, manager)
    }
}