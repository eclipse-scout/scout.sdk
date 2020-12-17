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
package org.eclipse.scout.sdk.s2i.nls

import com.intellij.codeInsight.AnnotationUtil
import com.intellij.codeInsight.AnnotationUtil.isAnnotated
import com.intellij.lang.java.JavaLanguage
import com.intellij.lang.xml.XMLLanguage
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
import org.eclipse.scout.sdk.core.log.SdkLog
import org.eclipse.scout.sdk.core.s.apidef.ScoutApi
import org.eclipse.scout.sdk.core.s.nls.query.TranslationPatterns

/**
 * The JS patterns are in [org.eclipse.scout.sdk.s2i.nls.completion.NlsCompletionContributorForJs] because the dependency to the JS module is optional!
 */
object PsiTranslationPatterns {

    /**
     * A pattern selecting <scout:message> tags and the containing keys attributes. This pattern is valid for xml files (which includes html files).
     */
    val HTML_KEY_PATTERN = htmlKeyPattern()

    /**
     * A pattern selecting all arguments passed to Java methods having the @NlsKey annotation. This pattern is valid for java files.
     */
    val JAVA_NLS_KEY_PATTERN = javaNlsKeyArgumentPattern()

    /**
     * An optional function selecting text keys used in session.text() methods or '${textKey:}' placeholders. This pattern is valid for js files.
     */
    private val JS_TRANSLATION_KEY_SUPPLIER = getJsTranslationKeySupplierIfAvailable()

    /**
     * Gets the Scout translation key referenced by the [PsiElement] given.
     * @param element The [PsiElement] to check.
     * @return The Scout translation key referenced by the [PsiElement] given or null if the element does not point to a translation key.
     */
    fun getTranslationKeyOf(element: PsiElement?): String? {
        val elementLang = element?.language ?: return null
        return when {
            elementLang.isKindOf(JavaLanguage.INSTANCE) -> getTranslationKeyFromJava(element)
            elementLang.isKindOf("JavaScript" /* do not use class here */) -> JS_TRANSLATION_KEY_SUPPLIER?.invoke(element)
            elementLang.isKindOf(XMLLanguage.INSTANCE) -> getTranslationKeyFromHtml(element)
            else -> null
        }
    }

    private fun getTranslationKeyFromJava(element: PsiElement?): String? {
        if (element is PsiLiteralExpressionImpl) {
            return asStringLiteral(element)
                    .takeIf { JAVA_NLS_KEY_PATTERN.accepts(it) }
                    ?.value as? String
        }

        // reference to variable or constant
        val variable = (element as? PsiReference)
                ?.takeIf { JAVA_NLS_KEY_PATTERN.accepts(it) }
                ?.resolve() as? PsiVariable ?: return null
        val constant = variable.computeConstantValue() as? String
        if (constant != null) {
            return constant
        }
        return asStringLiteral(variable.initializer)?.value as? String
    }

    private fun asStringLiteral(element: PsiElement?) = (element as? PsiLiteralExpressionImpl)
            ?.takeIf { ElementType.STRING_LITERALS.contains(it.literalElementType) }

    private fun getJsTranslationKeySupplierIfAvailable(): ((PsiElement?) -> String?)? {
        try {
            PsiTranslationPatternsForJs.getTranslationKeyOf(null) // check if class can be loaded. fails in IJ community edition
            return { PsiTranslationPatternsForJs.getTranslationKeyOf(it) }
        } catch (e: Throwable) {
            SdkLog.debug("Skipping registration of JavaScript translation pattern.", e)
            return null
        }
    }

    private fun getTranslationKeyFromHtml(element: PsiElement?): String? {
        val takeIf = (element as? XmlAttributeValue)
                ?.takeIf { HTML_KEY_PATTERN.accepts(it) }
        return takeIf
                ?.value
    }

    private fun htmlKeyPattern(): PsiElementPattern.Capture<PsiElement> = PlatformPatterns.psiElement()
            .withParent(xmlAttribute(TranslationPatterns.HtmlScoutMessagePattern.ATTRIBUTE_NAME)
                    .withParent(xmlTag().withName(TranslationPatterns.HtmlScoutMessagePattern.SCOUT_MESSAGE_TAG_NAME)))

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
}
