/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2i.template.js

import com.intellij.lang.documentation.AbstractDocumentationProvider
import com.intellij.lang.javascript.psi.JSField
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import org.eclipse.scout.sdk.core.s.model.js.prop.ScoutJsProperty
import org.eclipse.scout.sdk.s2i.model.typescript.IdeaJavaScriptField

class JsModelPropertyDocumentationProvider : AbstractDocumentationProvider() {

    override fun getCustomDocumentationElement(editor: Editor, file: PsiFile, contextElement: PsiElement?, targetOffset: Int): PsiElement? {
        val element = contextElement ?: return null
        val info = JsModelCompletionHelper.getPropertyValueInfo(element, "") ?: return contextElement
        val scoutObject = info.declaringScoutObjectByObjectType() ?: return contextElement
        val property = scoutObject
            .findProperties()
            .withSupers(true)
            .withName(info.propertyName)
            .first().orElse(null) ?: return contextElement
        return propertyToPsi(property) ?: contextElement
    }

    override fun getDocumentationElementForLookupItem(psiManager: PsiManager, lookupObject: Any?, elementUnderCursor: PsiElement?): PsiElement? {
        val jsNameLookupElement = lookupObject as? JsModelCompletionHelper.JsNameLookupElement ?: return null
        return propertyToPsi(jsNameLookupElement.scoutJsProperty)
    }

    private fun propertyToPsi(property: ScoutJsProperty): JSField? {
        val spi = property.field().spi() as? IdeaJavaScriptField ?: return null
        return spi.javaScriptField
    }
}