/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2i.nls.folding

import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.psi.CommonClassNames
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethodCallExpression
import com.intellij.psi.PsiType
import com.intellij.psi.search.GlobalSearchScope.allScope
import org.eclipse.scout.sdk.core.log.SdkLog
import org.eclipse.scout.sdk.core.s.nls.Language
import org.eclipse.scout.sdk.core.s.nls.manager.TranslationManager
import org.eclipse.scout.sdk.s2i.containingModule
import org.eclipse.scout.sdk.s2i.nls.TranslationLanguageSpec.Companion.translationDependencyScope
import org.eclipse.scout.sdk.s2i.nls.TranslationManagerLoader
import org.eclipse.scout.sdk.s2i.settings.ScoutSettings
import java.util.regex.Pattern

abstract class AbstractNlsFoldingBuilder : FoldingBuilderEx() {

    companion object {
        /**
         * Visible text content max length (without surrounding quotes)
         */
        const val FOLDING_MAX_LEN = 100

        /**
         * Visible placeholder text max length (without surrounding [PLACEHOLDER_START_CHAR] and [PLACEHOLDER_END_CHAR]
         */
        const val PLACEHOLDER_MAX_LEN = 60
        const val PLACEHOLDER_START_CHAR = '{'
        const val PLACEHOLDER_END_CHAR = '}'
        private val PLACEHOLDER_PATTERN = Pattern.compile("\\{(\\d){1,9}}")
    }

    private var m_javaLangStringType: PsiType? = null
    private var m_requestedLanguage: Language? = null

    override fun buildFoldRegions(root: PsiElement, document: Document, quick: Boolean): Array<FoldingDescriptor> {
        if (quick || !isFoldingOn()) {
            return FoldingDescriptor.EMPTY
        }
        val start = System.currentTimeMillis()
        val scope = root.translationDependencyScope() ?: return FoldingDescriptor.EMPTY
        val module = root.containingModule() ?: return FoldingDescriptor.EMPTY
        val project = root.project
        val manager = TranslationManagerLoader.createManager(module, scope, true) ?: return FoldingDescriptor.EMPTY
        m_javaLangStringType = PsiType.getTypeByName(CommonClassNames.JAVA_LANG_STRING, project, allScope(project))
        m_requestedLanguage = ScoutSettings.getTranslationLanguage(project)
        val result = buildFoldRegions(root, manager).toTypedArray()
        SdkLog.debug("Folding region creation took {}ms.", System.currentTimeMillis() - start)
        return result
    }

    abstract fun buildFoldRegions(root: PsiElement, manager: TranslationManager): List<FoldingDescriptor>

    override fun getPlaceholderText(node: ASTNode): String? = null

    override fun isCollapsedByDefault(node: ASTNode) = isFoldingOn()

    protected open fun createFoldingDescriptor(element: PsiElement, key: String, manager: TranslationManager): FoldingDescriptor? {
        val translation = manager.translation(key).orElse(null) ?: return null
        var text = translation.bestText(m_requestedLanguage).orElse(null) ?: return null

        if (element is PsiMethodCallExpression) {
            val textArgs = element.argumentList.expressions
                    .filter { it.type == m_javaLangStringType }
                    .drop(1)
                    .map { it.text }
                    .map { PLACEHOLDER_START_CHAR + limitLength(it, PLACEHOLDER_MAX_LEN) + PLACEHOLDER_END_CHAR }
                    .toTypedArray()
            if (textArgs.isNotEmpty()) {
                text = replacePlaceholders(text, textArgs)
            }
        }
        val prefixAndSuffix = textPrefixAndSuffix()
        return FoldingDescriptor(element.node, element.textRange, null, prefixAndSuffix + limitLength(text, FOLDING_MAX_LEN) + prefixAndSuffix, true, emptySet())
    }

    protected open fun textPrefixAndSuffix(): String = "\""

    protected open fun replacePlaceholders(text: String, replacements: Array<String>): String = PLACEHOLDER_PATTERN.matcher(text).replaceAll {
        val index = Integer.parseInt(it.group(1))
        if (index >= 0 && index < replacements.size) {
            return@replaceAll replacements[index]
        }
        PLACEHOLDER_START_CHAR + it.group(1) + PLACEHOLDER_END_CHAR
    }

    protected fun limitLength(text: String, maxLen: Int): String {
        if (text.length <= maxLen) return text
        val suffix = "..."
        return text.take(maxLen - suffix.length) + suffix
    }

    protected open fun isFoldingOn() = ScoutSettings.getCodeFoldingSettings().isCollapseTranslations()
}