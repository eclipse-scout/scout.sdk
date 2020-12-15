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
import org.eclipse.scout.sdk.core.s.nls.TranslationStoreStack
import org.eclipse.scout.sdk.s2i.containingModule
import org.eclipse.scout.sdk.s2i.nls.TranslationStoreStackLoader
import org.eclipse.scout.sdk.s2i.nlsDependencyScope
import org.eclipse.scout.sdk.s2i.settings.ScoutSettings
import java.util.regex.Pattern

abstract class AbstractNlsFoldingBuilder : FoldingBuilderEx() {

    companion object {
        const val FOLDING_MAX_LEN = 100
        const val PLACEHOLDER_MAX_LEN = 60
        const val PLACEHOLDER_START_CHAR = '{'
        const val PLACEHOLDER_END_CHAR = '}'
        private val PLACEHOLDER_PATTERN = Pattern.compile("\\{(\\d+){0,9}}")
    }

    private var m_javaLangStringType: PsiType? = null
    private var m_requestedLanguage: Language? = null

    override fun buildFoldRegions(root: PsiElement, document: Document, quick: Boolean): Array<FoldingDescriptor> {
        if (quick || !isFoldingOn()) {
            return FoldingDescriptor.EMPTY
        }
        val start = System.currentTimeMillis()
        val scope = root.nlsDependencyScope() ?: return FoldingDescriptor.EMPTY
        val module = root.containingModule() ?: return FoldingDescriptor.EMPTY
        val project = root.project
        val stack = TranslationStoreStackLoader.createStack(module, scope, true) ?: return FoldingDescriptor.EMPTY
        m_javaLangStringType = PsiType.getTypeByName(CommonClassNames.JAVA_LANG_STRING, project, allScope(project))
        m_requestedLanguage = ScoutSettings.getTranslationLanguage(project)
        val result = buildFoldRegions(root, stack).toTypedArray()
        SdkLog.debug("Folding region creation took {}ms.", System.currentTimeMillis() - start)
        return result
    }

    abstract fun buildFoldRegions(root: PsiElement, stack: TranslationStoreStack): List<FoldingDescriptor>

    override fun getPlaceholderText(node: ASTNode): String? = null

    override fun isCollapsedByDefault(node: ASTNode) = isFoldingOn()

    protected fun createFoldingDescriptor(element: PsiElement, key: String, stack: TranslationStoreStack): FoldingDescriptor? {
        val translation = stack.translation(key).orElse(null) ?: return null
        var text = translation.bestText(m_requestedLanguage).orElse(null) ?: return null

        if (element is PsiMethodCallExpression) {
            val textArgs = element.argumentList.expressions
                    .filter { it.type == m_javaLangStringType }
                    .drop(1)
                    .map { it.text.trim() }
                    .map { PLACEHOLDER_START_CHAR + maxLength(it, PLACEHOLDER_MAX_LEN) + PLACEHOLDER_END_CHAR }
                    .toTypedArray()
            if (textArgs.isNotEmpty()) {
                text = replacePlaceholders(text, textArgs)
            }
        }
        return FoldingDescriptor(element.node, element.textRange, null, '"' + maxLength(text, FOLDING_MAX_LEN) + '"', true, emptySet())
    }

    protected fun replacePlaceholders(text: String, replacements: Array<String>): String = PLACEHOLDER_PATTERN.matcher(text).replaceAll {
        val index = Integer.parseInt(it.group(1))
        if (index >= 0 && index < replacements.size) {
            return@replaceAll replacements[index]
        }
        PLACEHOLDER_START_CHAR + it.group(1) + PLACEHOLDER_END_CHAR
    }

    protected fun maxLength(text: String, maxLen: Int): String {
        if (text.length <= maxLen) return text
        val suffix = "..."
        return text.take(maxLen - suffix.length) + suffix
    }

    protected fun isFoldingOn() = ScoutSettings.getCodeFoldingSettings().isCollapseTranslations()
}