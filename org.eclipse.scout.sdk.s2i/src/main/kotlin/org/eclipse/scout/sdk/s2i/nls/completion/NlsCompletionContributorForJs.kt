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
import com.intellij.lang.javascript.patterns.JSPatterns.*
import com.intellij.lang.javascript.psi.JSCallExpression
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.patterns.ElementPattern
import com.intellij.patterns.PatternCondition
import com.intellij.patterns.StandardPatterns.and
import com.intellij.patterns.StandardPatterns.or
import com.intellij.psi.PsiElement
import com.intellij.util.ProcessingContext
import org.eclipse.scout.sdk.core.s.nls.query.TranslationPatterns
import org.eclipse.scout.sdk.s2i.containingModule
import org.eclipse.scout.sdk.s2i.nls.PsiNlsPatterns.Companion.startsWithIgnoringQuotes
import kotlin.streams.toList

class NlsCompletionContributorForJs : CompletionContributor() {

    init {
        extend(CompletionType.BASIC, sessionTextPattern(), NlsCompletionContributorForJava.DefaultNlsCompletionProvider())
        extend(CompletionType.BASIC, textKeyPattern(), NlsCompletionProviderForTextKey())
    }

    private class NlsCompletionProviderForTextKey : CompletionProvider<CompletionParameters>() {
        override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
            val module = parameters.position.containingModule() ?: return
            val elements = NlsCompletionHelper.computeLookupElements(module) {
                TranslationPatterns.JsonTextKeySearch.JSON_TEXT_KEY_PREFIX + it.key() + TranslationPatterns.JsonTextKeySearch.JSON_TEXT_KEY_SUFFIX
            }.toList()
            result.addAllElements(elements)
            result.stopHere()
        }
    }


    /**
     * These patterns are here and not in [org.eclipse.scout.sdk.s2i.nls.PsiNlsPatterns] because the dependency to the JS module is optional!
     */
    companion object {
        fun sessionTextPattern() = psiElement().withParent(isJsCallWithLiteralArgument("session", "text", 0))

        fun textKeyPattern() = psiElement().withParent(jsLiteralExpression().withText(startsWithIgnoringQuotes(TranslationPatterns.JsonTextKeySearch.JSON_TEXT_KEY_PREFIX, true)))

        fun allNlsPatternsInJs() = or(sessionTextPattern(), textKeyPattern())

        fun isJsCallWithLiteralArgument(qualifierName: String, functionName: String, argumentIndex: Int): ElementPattern<PsiElement> {
            return and(
                    jsLiteralExpression(),
                    jsArgument(functionName, argumentIndex),
                    psiElement().withSuperParent(2, jsCallExpression().with(MethodExpressionQualifier(qualifierName)))
            )
        }

        private class MethodExpressionQualifier(val methodQualifier: String) : PatternCondition<JSCallExpression>("methodQualifier=$methodQualifier") {
            override fun accepts(t: JSCallExpression, context: ProcessingContext): Boolean {
                val expression = t.methodExpression
                return expression is JSReferenceExpression && jsReferenceExpression().withReferenceName(methodQualifier).accepts(expression.qualifier)
            }
        }
    }
}