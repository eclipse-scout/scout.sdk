/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2i.nls.completion

import com.intellij.codeInsight.completion.*
import com.intellij.patterns.PsiJavaPatterns.psiElement
import com.intellij.psi.PsiLiteralExpression
import com.intellij.util.ProcessingContext
import org.eclipse.scout.sdk.s2i.containingModule
import org.eclipse.scout.sdk.s2i.nls.PsiTranslationPatterns
import org.eclipse.scout.sdk.s2i.nls.completion.NlsCompletionHelper.computeLookupElements

class NlsCompletionContributorForJava : CompletionContributor() {

    init {
        extend(CompletionType.BASIC, psiElement()
                .withParent(psiElement(PsiLiteralExpression::class.java)
                        .and(PsiTranslationPatterns.JAVA_NLS_KEY_PATTERN)), DefaultNlsCompletionProvider())
    }

    class DefaultNlsCompletionProvider : CompletionProvider<CompletionParameters>() {
        override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
            val module = parameters.position.containingModule() ?: return
            result.addAllElements(computeLookupElements(module, parameters.position.parent))
            result.restartCompletionOnAnyPrefixChange() // required that the "new translation" element is always available
            result.stopHere()
        }
    }
}