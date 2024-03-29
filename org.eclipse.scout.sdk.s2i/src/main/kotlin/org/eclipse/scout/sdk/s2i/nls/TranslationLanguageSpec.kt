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

import com.intellij.lang.Language
import com.intellij.lang.java.JavaLanguage
import com.intellij.lang.xml.XMLLanguage
import com.intellij.psi.PsiElement
import org.eclipse.scout.sdk.core.s.nls.Translations.DependencyScope

abstract class TranslationLanguageSpec(val element: PsiElement, val translationDependencyScope: DependencyScope, val stringDelimiter: CharSequence,
                                       protected val translationKeyResolver: (PsiElement) -> String?) {

    companion object {

        /**
         * @return The best matching [DependencyScope] for this [PsiElement] or null if the language of this [PsiElement] does not support translations.
         */
        fun PsiElement.translationDependencyScope() = translationSpec()?.translationDependencyScope

        fun PsiElement.translationSpec() = forElement(this)

        fun forElement(element: PsiElement?) = element?.let { create(it.language, it) }

        private fun create(language: Language, element: PsiElement): TranslationLanguageSpec? = when {
            language.isKindOf(JavaLanguage.INSTANCE) -> PsiTranslationPatterns.JavaTranslationSpec(element)
            language.isKindOf("JavaScript" /* do not use class here */) -> PsiTranslationPatternsForJs.JsTranslationSpec(element)
            language.isKindOf(XMLLanguage.INSTANCE) -> PsiTranslationPatterns.HtmlTranslationSpec(element)
            else -> null
        }
    }

    fun resolveTranslationKey() = translationKeyResolver(element)

    abstract fun createNewLiteral(text: String): PsiElement

    open fun decorateTranslationKey(nlsKey: String): String = nlsKey
}

