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
import com.intellij.lang.javascript.psi.*
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import org.eclipse.scout.sdk.core.s.nls.TranslationStoreStack
import org.eclipse.scout.sdk.core.s.nls.query.TranslationPatterns.JsonTextKeyPattern
import org.eclipse.scout.sdk.s2i.nls.PsiTranslationPatternsForJs

class NlsFoldingBuilderForJs : AbstractNlsFoldingBuilder() {

    override fun buildFoldRegions(root: PsiElement, stack: TranslationStoreStack): List<FoldingDescriptor> {
        val folds = ArrayList<FoldingDescriptor>()
        root.accept(object : JSRecursiveWalkingElementVisitor() {
            override fun visitJSLiteralExpression(expression: JSLiteralExpression) {
                visitElement(expression)
            }

            override fun visitJSReferenceExpression(node: JSReferenceExpression) {
                visitElement(node)
            }

            private fun visitElement(element: JSElement) {
                val translationKey = PsiTranslationPatternsForJs.getTranslationKeyOf(element) ?: return
                createFoldingDescriptor(translationKey, element, stack)?.let { folds.add(it) }
            }
        })
        return folds
    }

    private fun createFoldingDescriptor(key: String, element: PsiElement, stack: TranslationStoreStack): FoldingDescriptor? {
        val isJsonTextKey = element is JSLiteralExpression && element.stringValue?.startsWith(JsonTextKeyPattern.JSON_TEXT_KEY_PREFIX) == true
        val psiElement = if (isJsonTextKey) element else PsiTreeUtil.getParentOfType(element, JSCallExpression::class.java) ?: return null
        return createFoldingDescriptor(psiElement, key, stack)
    }
}