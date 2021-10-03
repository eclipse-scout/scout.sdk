/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.s2i.nls.doc

import com.intellij.lang.documentation.AbstractDocumentationProvider
import com.intellij.lang.documentation.DocumentationMarkup
import com.intellij.lang.properties.psi.Property
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import org.eclipse.scout.sdk.core.s.ISdkConstants
import org.eclipse.scout.sdk.core.s.nls.manager.IStackedTranslation
import org.eclipse.scout.sdk.core.util.Strings.escapeHtml
import org.eclipse.scout.sdk.core.util.Strings.trim
import org.eclipse.scout.sdk.s2i.containingModule
import org.eclipse.scout.sdk.s2i.nls.TranslationLanguageSpec.Companion.translationDependencyScope
import org.eclipse.scout.sdk.s2i.nls.TranslationManagerLoader.createManager
import org.eclipse.scout.sdk.s2i.nls.completion.NlsCompletionHelper
import java.util.stream.Collectors.joining

abstract class AbstractNlsDocumentationProvider : AbstractDocumentationProvider() {

    override fun getCustomDocumentationElement(editor: Editor, file: PsiFile, contextElement: PsiElement?, targetOffset: Int) =
            contextElement?.takeIf { translationKeyOf(it) != null }

    override fun generateDoc(element: PsiElement?, originalElement: PsiElement?): String? {
        val module = element?.containingModule() ?: return null
        val (key, dependencyScope) = if (element is Property) {
            // in case the documentation is triggered on a property in a .properties file or on a lookup element (see getDocumentationElementForLookupItem below).
            element.key to originalElement?.translationDependencyScope()
        } else {
            translationKeyOf(element) to element.translationDependencyScope()
        }
        val manager = createManager(module, dependencyScope) ?: return null
        val translation = manager.translation(key).orElse(null) ?: return null
        return generateDoc(translation)
    }

    override fun getDocumentationElementForLookupItem(psiManager: PsiManager, lookupObject: Any?, elementUnderCursor: PsiElement?): PsiElement? {
        // this method is required so that the quick documentation (ctrl+q) works for lookup elements
        val translationLookupObject = lookupObject as? NlsCompletionHelper.TranslationLookupObject
        return translationLookupObject?.element
    }

    private fun generateDoc(translation: IStackedTranslation): String {
        val storeNames = translation.stores()
                .map { it.service().type().elementName().removeSuffix(ISdkConstants.SUFFIX_TEXT_PROVIDER_SERVICE) }
                .collect(joining(", "))
        val doc = StringBuilder()
                .append(DocumentationMarkup.CONTENT_START)
                .append(escapeHtml("'${translation.key()}' defined in [$storeNames]"))
                .append(DocumentationMarkup.CONTENT_END)
                .append("<font size='1'>&nbsp;</font>")
                .append(DocumentationMarkup.SECTIONS_START)
        translation.texts().entries.forEach {
            doc.append(DocumentationMarkup.SECTION_HEADER_START).append(escapeHtml(it.key.displayName() + ":")).append("</p>")
                    .append(DocumentationMarkup.SECTION_SEPARATOR).append(escapeHtml(trim(it.value))).append(DocumentationMarkup.SECTION_END).append("</tr>")
        }
        doc.append(DocumentationMarkup.SECTIONS_END)
        return doc.toString()
    }

    protected abstract fun translationKeyOf(element: PsiElement?): String?
}