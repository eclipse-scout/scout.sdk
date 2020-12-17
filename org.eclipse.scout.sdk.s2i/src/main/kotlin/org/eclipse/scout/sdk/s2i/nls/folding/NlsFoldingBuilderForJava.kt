/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.s2i.nls.folding

import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.psi.*
import com.intellij.psi.util.PsiTreeUtil
import org.eclipse.scout.sdk.core.s.nls.TranslationStoreStack
import org.eclipse.scout.sdk.s2i.nls.PsiTranslationPatterns

class NlsFoldingBuilderForJava : AbstractNlsFoldingBuilder() {

    override fun buildFoldRegions(root: PsiElement, stack: TranslationStoreStack): List<FoldingDescriptor> {
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

            private fun visitElement(expression: PsiExpression) {
                val translationKey = PsiTranslationPatterns.getTranslationKeyOf(expression) ?: return
                createFoldingDescriptor(translationKey, expression, stack)?.let { folds.add(it) }
            }
        })
        return folds
    }

    private fun createFoldingDescriptor(key: String, literal: PsiExpression, stack: TranslationStoreStack): FoldingDescriptor? {
        val methodCall = PsiTreeUtil.getParentOfType(literal, PsiMethodCallExpression::class.java) ?: return null
        val element = if (methodCall.methodExpression.qualifierExpression is PsiReferenceExpression) methodCall else literal
        return createFoldingDescriptor(element, key, stack)
    }
}