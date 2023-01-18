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

import com.intellij.codeInsight.AnnotationUtil
import com.intellij.codeInsight.AnnotationUtil.isAnnotated
import com.intellij.patterns.*
import com.intellij.patterns.PsiJavaPatterns.psiExpression
import com.intellij.patterns.XmlPatterns.xmlAttribute
import com.intellij.patterns.XmlPatterns.xmlTag
import com.intellij.psi.*
import com.intellij.psi.impl.source.tree.ElementType
import com.intellij.psi.impl.source.tree.java.PsiLiteralExpressionImpl
import com.intellij.psi.xml.XmlAttributeValue
import com.intellij.util.ArrayUtil
import com.intellij.util.ProcessingContext
import org.eclipse.scout.sdk.core.java.JavaUtils
import org.eclipse.scout.sdk.core.s.java.apidef.ScoutApi
import org.eclipse.scout.sdk.core.s.nls.Translations
import org.eclipse.scout.sdk.core.s.nls.query.TranslationPatterns

/**
 * The JS patterns are in [org.eclipse.scout.sdk.s2i.nls.completion.NlsCompletionContributorForJs] because the dependency to the JS module is optional!
 */
object PsiTranslationPatterns {

    /**
     * A pattern selecting <scout:message> tags and the containing keys attributes. This pattern is valid for .xml files (which includes .html files).
     */
    val HTML_KEY_PATTERN = htmlKeyPattern()

    /**
     * A pattern selecting all arguments passed to Java methods having the @NlsKey annotation. This pattern is valid for .java files.
     */
    val JAVA_NLS_KEY_PATTERN = javaNlsKeyArgumentPattern()

    private fun getTranslationKeyFromJava(element: PsiElement?): String? {
        val key = resolveExpressionToString(element) {
            JAVA_NLS_KEY_PATTERN.accepts(it)
        }
        if (key != null) {
            return key
        }

        // a method call directly returning the nls key
        return (element as? PsiCall)
            ?.takeIf { JAVA_NLS_KEY_PATTERN.accepts(it) }
            ?.resolveMethod()
            ?.body
            ?.children
            ?.mapNotNull { it as? PsiReturnStatement }
            ?.map { it.returnValue }
            ?.firstNotNullOfOrNull { resolveExpressionToString(it) }
    }

    private fun resolveExpressionToString(element: PsiElement?, locationFilter: ((Any?) -> Boolean)? = null): String? {
        if (element is PsiLiteralExpressionImpl) {
            return asStringLiteral(element)
                .takeIf { locationFilter == null || locationFilter(it) }
                ?.value as? String
        }

        // reference to variable or constant
        val variable = (element as? PsiReference)
            ?.takeIf { locationFilter == null || locationFilter(it) }
            ?.resolve() as? PsiVariable ?: return null
        val constant = variable.computeConstantValue() as? String
        if (constant != null) {
            return constant
        }
        return asStringLiteral(variable.initializer)?.value as? String
    }

    private fun asStringLiteral(element: PsiElement?) = (element as? PsiLiteralExpressionImpl)
        ?.takeIf { ElementType.STRING_LITERALS.contains(it.literalElementType) }

    private fun getTranslationKeyFromHtml(element: PsiElement?): String? {
        val takeIf = (element as? XmlAttributeValue)
            ?.takeIf { HTML_KEY_PATTERN.accepts(it) }
        return takeIf
            ?.value
    }

    private fun htmlKeyPattern(): PsiElementPattern.Capture<PsiElement> = PlatformPatterns.psiElement()
        .withParent(
            xmlAttribute(TranslationPatterns.HtmlScoutMessagePattern.ATTRIBUTE_NAME)
                .withParent(xmlTag().withName(TranslationPatterns.HtmlScoutMessagePattern.SCOUT_MESSAGE_TAG_NAME))
        )

    private fun javaNlsKeyArgumentPattern(): PsiExpressionPattern.Capture<PsiExpression> {
        val patternsForAllScoutVersions = ScoutApi.allKnown()
            .map { it.NlsKey().fqn() }
            .distinct()
            .map { psiExpression().with(ArgumentToMethodParameterHavingAnnotation(it)) }
            .toArray<ElementPattern<PsiExpression>> { len -> arrayOfNulls(len) }
        if (patternsForAllScoutVersions.size == 1) {
            return psiExpression().and(patternsForAllScoutVersions[0])
        }
        return psiExpression().andOr(*patternsForAllScoutVersions)
    }

    private class ArgumentToMethodParameterHavingAnnotation(val annotationFqn: String) : PatternCondition<PsiExpression>("argumentToMethodParameterHavingAnnotation=$annotationFqn") {
        override fun accepts(expression: PsiExpression, context: ProcessingContext?): Boolean {
            val parent = expression.parent as? PsiExpressionList ?: return false
            val grandParent = parent.parent as? PsiCall ?: return false
            val parameterIndex = ArrayUtil.indexOf(parent.expressions, expression)
            if (parameterIndex < 0) return false
            val calledMethod = grandParent.resolveMethod() ?: return false
            return isMethodParameterAnnotated(calledMethod, parameterIndex)
        }

        private fun isMethodParameterAnnotated(method: PsiMethod, paramIndex: Int): Boolean {
            val params = method.parameterList.parameters
            if (paramIndex >= params.size) return false
            val param = params[paramIndex]
            return isAnnotated(param, annotationFqn, AnnotationUtil.CHECK_TYPE or AnnotationUtil.CHECK_HIERARCHY)
        }
    }

    class JavaTranslationSpec(element: PsiElement) : TranslationLanguageSpec(element, Translations.DependencyScope.JAVA, "\"", PsiTranslationPatterns::getTranslationKeyFromJava) {
        override fun createNewLiteral(text: String): PsiElement {
            val literalValue = JavaUtils.toStringLiteral(decorateTranslationKey(text), stringDelimiter, true).toString()
            return JavaPsiFacade.getElementFactory(element.project).createExpressionFromText(literalValue, element)
        }
    }

    class HtmlTranslationSpec(element: PsiElement) : TranslationLanguageSpec(element, Translations.DependencyScope.JAVA, "\"", PsiTranslationPatterns::getTranslationKeyFromHtml) {
        override fun createNewLiteral(text: String): PsiElement = XmlElementFactory.getInstance(element.project)
            .createAttribute(TranslationPatterns.HtmlScoutMessagePattern.ATTRIBUTE_NAME, decorateTranslationKey(text), element)
            .valueElement!!
    }
}
