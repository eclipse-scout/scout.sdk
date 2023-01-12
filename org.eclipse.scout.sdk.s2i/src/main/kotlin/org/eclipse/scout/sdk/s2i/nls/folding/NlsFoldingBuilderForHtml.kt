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

import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.psi.PsiElement
import com.intellij.psi.XmlRecursiveElementWalkingVisitor
import com.intellij.psi.xml.XmlAttributeValue
import org.eclipse.scout.sdk.core.s.nls.manager.TranslationManager
import org.eclipse.scout.sdk.s2i.nls.TranslationLanguageSpec.Companion.translationSpec

class NlsFoldingBuilderForHtml : AbstractNlsFoldingBuilder() {

    override fun buildFoldRegions(root: PsiElement, manager: TranslationManager): List<FoldingDescriptor> {
        val folds = ArrayList<FoldingDescriptor>()
        root.accept(object : XmlRecursiveElementWalkingVisitor() {
            override fun visitXmlAttributeValue(attributeValue: XmlAttributeValue) {
                val translationKey = attributeValue.translationSpec()?.resolveTranslationKey() ?: return
                createFoldingDescriptor(attributeValue, translationKey, manager)?.let { folds.add(it) }
            }
        })
        return folds
    }
}