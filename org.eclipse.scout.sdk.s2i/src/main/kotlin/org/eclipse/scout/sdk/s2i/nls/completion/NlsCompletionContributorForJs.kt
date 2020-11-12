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
package org.eclipse.scout.sdk.s2i.nls.completion

import com.intellij.codeInsight.completion.*
import com.intellij.patterns.PsiJavaPatterns.psiElement
import com.intellij.util.ProcessingContext
import org.eclipse.scout.sdk.core.s.nls.query.TranslationPatterns
import org.eclipse.scout.sdk.s2i.containingModule
import org.eclipse.scout.sdk.s2i.nls.PsiTranslationPatternsForJs
import org.eclipse.scout.sdk.s2i.nls.completion.NlsCompletionContributorForJava.DefaultNlsCompletionProvider

class NlsCompletionContributorForJs : CompletionContributor() {

    init {
        extend(CompletionType.BASIC, psiElement()
                .withParent(PsiTranslationPatternsForJs.SESSION_TEXT_PATTERN), DefaultNlsCompletionProvider())
        extend(CompletionType.BASIC, psiElement()
                .withParent(PsiTranslationPatternsForJs.TEXT_KEY_PATTERN), NlsCompletionProviderForTextKey())
    }

    private class NlsCompletionProviderForTextKey : CompletionProvider<CompletionParameters>() {
        override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
            val module = parameters.position.containingModule() ?: return
            val elements = NlsCompletionHelper.computeLookupElements(module, parameters.position.parent) {
                TranslationPatterns.JsonTextKeyPattern.JSON_TEXT_KEY_PREFIX + it + TranslationPatterns.JsonTextKeyPattern.JSON_TEXT_KEY_SUFFIX
            }
            result.addAllElements(elements)
            result.stopHere()
        }
    }
}