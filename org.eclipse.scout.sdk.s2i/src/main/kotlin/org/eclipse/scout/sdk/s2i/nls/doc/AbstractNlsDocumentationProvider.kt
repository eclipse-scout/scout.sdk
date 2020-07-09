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
package org.eclipse.scout.sdk.s2i.nls.doc

import com.intellij.lang.documentation.AbstractDocumentationProvider
import com.intellij.lang.documentation.DocumentationMarkup
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import org.eclipse.scout.sdk.core.s.nls.ITranslationEntry
import org.eclipse.scout.sdk.core.util.Strings.escapeHtml
import org.eclipse.scout.sdk.s2i.containingModule
import org.eclipse.scout.sdk.s2i.nls.TranslationStoreStackLoader.createStack

abstract class AbstractNlsDocumentationProvider : AbstractDocumentationProvider() {
    override fun getCustomDocumentationElement(editor: Editor, file: PsiFile, contextElement: PsiElement?): PsiElement? {
        if (accept(contextElement)) {
            return contextElement
        }
        return null
    }

    override fun generateDoc(element: PsiElement?, originalElement: PsiElement?): String? {
        if (element == null || !accept(element)) {
            return null
        }
        val translation = findTranslationFor(element) ?: return null
        return generateDoc(translation)
    }

    private fun findTranslationFor(element: PsiElement): ITranslationEntry? {
        val module = element.containingModule() ?: return null
        val stack = createStack(module) ?: return null
        val key = psiElementToKey(element) ?: return null
        return stack.translation(key).orElse(null)
    }

    protected abstract fun psiElementToKey(element: PsiElement): String?

    private fun generateDoc(translation: ITranslationEntry): String {
        val doc = StringBuilder()
                .append(DocumentationMarkup.CONTENT_START)
                .append(escapeHtml("'${translation.key()}' defined in '${translation.store().service().type().elementName()}'"))
                .append(DocumentationMarkup.CONTENT_END)
                .append("<font size='1'>&nbsp;</font>")
                .append(DocumentationMarkup.SECTIONS_START)
        translation.texts().entries.forEach {
            doc.append(DocumentationMarkup.SECTION_HEADER_START).append(escapeHtml(it.key.displayName() + ":")).append("</p>")
                    .append(DocumentationMarkup.SECTION_SEPARATOR).append(escapeHtml(it.value.trim())).append(DocumentationMarkup.SECTION_END).append("</tr>")
        }
        doc.append(DocumentationMarkup.SECTIONS_END)
        return doc.toString()
    }

    protected abstract fun accept(element: PsiElement?): Boolean
}