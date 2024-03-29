/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2i.nls

import com.intellij.lang.javascript.patterns.JSPatterns.*
import com.intellij.lang.javascript.psi.JSCallExpression
import com.intellij.lang.javascript.psi.JSInitializerOwner
import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.lang.javascript.psi.impl.JSPsiElementFactory
import com.intellij.patterns.ElementPattern
import com.intellij.patterns.PatternCondition
import com.intellij.patterns.StandardPatterns.and
import com.intellij.patterns.StandardPatterns.or
import com.intellij.patterns.StringPattern
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.util.ProcessingContext
import org.eclipse.scout.sdk.core.java.JavaUtils
import org.eclipse.scout.sdk.core.s.nls.Translations
import org.eclipse.scout.sdk.core.s.nls.query.TranslationPatterns.JsModelTextKeyPattern
import org.eclipse.scout.sdk.core.util.Strings

/**
 * These patterns are here and not in [org.eclipse.scout.sdk.s2i.nls.PsiTranslationPatterns] because the dependency to the JS module is optional!
 */
object PsiTranslationPatternsForJs {

    val SESSION_TEXT_PATTERN = sessionTextPattern()
    val TEXT_KEY_PATTERN = textKeyPattern()
    val ANY_JS_PATTERN = or(SESSION_TEXT_PATTERN, TEXT_KEY_PATTERN)

    private fun getTranslationKeyFromJs(element: PsiElement?): String? {
        val literal = if (element is JSLiteralExpression)
            element.takeIf { it.isStringLiteral }.takeIf { ANY_JS_PATTERN.accepts(it) }
        else
            resolveConstant(element)

        return literal?.let { getTranslationKeyOfLiteral(it) }
    }

    private fun resolveConstant(element: PsiElement?): JSLiteralExpression? {
        val constant = (element as? PsiReference)
            ?.takeIf { ANY_JS_PATTERN.accepts(it) }
            ?.resolve() as? JSInitializerOwner
        val literal = constant?.initializer as? JSLiteralExpression
        return literal?.takeIf { it.isStringLiteral }
    }

    private fun sessionTextPattern() = jsExpression().and(isJsCallArgument("session", "text", 0))

    private fun textKeyPattern() = jsLiteralExpression().withText(surroundedByIgnoringQuotes(JsModelTextKeyPattern.MODEL_TEXT_KEY_PREFIX, JsModelTextKeyPattern.MODEL_TEXT_KEY_SUFFIX, true))

    private fun surroundedByIgnoringQuotes(prefix: CharSequence, suffix: CharSequence, unescape: Boolean): StringPattern {
        return string().with(object : PatternCondition<String>("surroundedBy=$prefix$suffix") {
            override fun accepts(str: String, context: ProcessingContext): Boolean {
                val text = if (unescape) JavaUtils.fromStringLiteral(str) else Strings.withoutQuotes(str)
                return Strings.startsWith(text, prefix) && Strings.endsWith(text, suffix)
            }
        })
    }

    private fun getTranslationKeyOfLiteral(element: JSLiteralExpression): String? {
        var text = element.stringValue ?: return null
        val jsonPrefix = JsModelTextKeyPattern.MODEL_TEXT_KEY_PREFIX
        val jsonSuffix = JsModelTextKeyPattern.MODEL_TEXT_KEY_SUFFIX
        if (text.startsWith(jsonPrefix) && text.endsWith(jsonSuffix)) {
            text = text.substring(jsonPrefix.length, text.length - jsonSuffix.length)
        }
        if (Strings.hasText(text)) {
            return text
        }
        return null
    }

    private fun isJsCallArgument(qualifierName: String, functionName: String, argumentIndex: Int): ElementPattern<PsiElement> {
        return and(
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

    class JsTranslationSpec(element: PsiElement) : TranslationLanguageSpec(element, Translations.DependencyScope.NODE, "'", PsiTranslationPatternsForJs::getTranslationKeyFromJs) {

        override fun decorateTranslationKey(nlsKey: String) =
            if (TEXT_KEY_PATTERN.accepts(element) || TEXT_KEY_PATTERN.accepts(element.parent))
                JsModelTextKeyPattern.MODEL_TEXT_KEY_PREFIX + nlsKey + JsModelTextKeyPattern.MODEL_TEXT_KEY_SUFFIX
            else
                nlsKey

        override fun createNewLiteral(text: String): PsiElement {
            val literalValue = JavaUtils.toStringLiteral(decorateTranslationKey(text), stringDelimiter, true).toString()
            return JSPsiElementFactory.createJSExpression(literalValue, element)
        }
    }
}