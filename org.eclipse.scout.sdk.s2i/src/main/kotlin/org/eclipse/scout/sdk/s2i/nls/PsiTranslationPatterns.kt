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

import com.intellij.openapi.util.text.StringUtil.startsWith
import com.intellij.patterns.*
import com.intellij.patterns.PsiJavaPatterns.literalExpression
import com.intellij.patterns.PsiJavaPatterns.psiMethod
import com.intellij.patterns.StandardPatterns.or
import com.intellij.patterns.StandardPatterns.string
import com.intellij.patterns.XmlPatterns.xmlAttribute
import com.intellij.patterns.XmlPatterns.xmlTag
import com.intellij.psi.PsiElement
import com.intellij.psi.xml.XmlAttributeValue
import com.intellij.util.ProcessingContext
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes
import org.eclipse.scout.sdk.core.s.nls.query.TranslationPatterns
import org.eclipse.scout.sdk.core.util.Strings.fromStringLiteral
import org.eclipse.scout.sdk.core.util.Strings.withoutQuotes
import java.util.*

/**
 * The JS patterns are in [org.eclipse.scout.sdk.s2i.nls.completion.NlsCompletionContributorForJs] because the dependency to the JS module is optional!
 */
object PsiTranslationPatterns {

    val HTML_KEY_PATTERN = htmlKeyPattern()
    val JAVA_TEXTS_GET_PATTERN = javaTextsGetPattern()
    private val JAVA_PATTERNS: MutableSet<ElementPattern<out PsiElement>> = HashSet()

    init {
        registerJavaPattern(JAVA_TEXTS_GET_PATTERN)
    }

    fun registerJavaPattern(pattern: ElementPattern<out PsiElement>): Boolean = synchronized(this) {
        return JAVA_PATTERNS.add(pattern)
    }

    @Suppress("unused")
    fun removeJavaPattern(pattern: ElementPattern<out PsiElement>): Boolean = synchronized(this) {
        return JAVA_PATTERNS.remove(pattern)
    }

    fun allJavaPatterns(): List<ElementPattern<out PsiElement>> = synchronized(this) {
        return@synchronized ArrayList(JAVA_PATTERNS)
    }

    fun anyJavaPatternAccepts(element: PsiElement?) = allJavaPatterns().any { it.accepts(element) }

    fun startsWithIgnoringQuotes(s: CharSequence, unescape: Boolean): StringPattern {
        return string().with(object : PatternCondition<String>("startsWithIgnoringQuotes=$s") {
            override fun accepts(str: String, context: ProcessingContext): Boolean {
                val text = if (unescape) fromStringLiteral(str) else withoutQuotes(str)
                return startsWith(text, s)
            }
        })
    }

    private fun htmlKeyPattern(): PsiElementPattern.Capture<PsiElement> = PlatformPatterns.psiElement()
            .withParent(XmlAttributeValue::class.java)
            .withSuperParent(2, xmlAttribute(TranslationPatterns.HtmlScoutMessagePattern.ATTRIBUTE_NAME))
            .withSuperParent(3, xmlTag().withName("scout:message"))

    private fun javaTextsGetPattern(): PsiJavaElementPattern.Capture<out PsiElement> {
        val stringFqn = String::class.java.name
        val localeFqn = Locale::class.java.name
        val wildcardArgument = ".."
        val getWithoutLocale = psiMethod()
                .withName(TranslationPatterns.JavaTextsGetPattern.GET_METHOD_NAME)
                .definedInClass(IScoutRuntimeTypes.TEXTS)
                .withParameters(stringFqn, wildcardArgument)
        val getWithLocale = psiMethod()
                .withName(TranslationPatterns.JavaTextsGetPattern.GET_METHOD_NAME)
                .definedInClass(IScoutRuntimeTypes.TEXTS)
                .withParameters(localeFqn, stringFqn, wildcardArgument)
        val getWithFallbackWithoutLocale = psiMethod()
                .withName(TranslationPatterns.JavaTextsGetPattern.GET_WITH_FALLBACK_METHOD_NAME)
                .definedInClass(IScoutRuntimeTypes.TEXTS)
                .withParameters(stringFqn, stringFqn, wildcardArgument)
        val getWithFallbackWithLocale = psiMethod()
                .withName(TranslationPatterns.JavaTextsGetPattern.GET_WITH_FALLBACK_METHOD_NAME)
                .definedInClass(IScoutRuntimeTypes.TEXTS)
                .withParameters(localeFqn, stringFqn, stringFqn, wildcardArgument)
        val oneOfTextsGetOverloads = or(
                literalExpression().methodCallParameter(0, getWithoutLocale),
                literalExpression().methodCallParameter(1, getWithLocale),
                literalExpression().methodCallParameter(0, getWithFallbackWithoutLocale),
                literalExpression().methodCallParameter(1, getWithFallbackWithoutLocale),
                literalExpression().methodCallParameter(1, getWithFallbackWithLocale),
                literalExpression().methodCallParameter(2, getWithFallbackWithLocale)
        )
        return PsiJavaPatterns.psiElement().withParent(oneOfTextsGetOverloads)
    }
}
