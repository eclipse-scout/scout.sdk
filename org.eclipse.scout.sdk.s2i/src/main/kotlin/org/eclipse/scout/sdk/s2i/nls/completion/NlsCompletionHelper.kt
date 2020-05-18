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
package org.eclipse.scout.sdk.s2i.nls.completion

import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.codeInsight.lookup.LookupElementPresentation
import com.intellij.codeInsight.lookup.LookupElementRenderer
import com.intellij.icons.AllIcons
import com.intellij.openapi.module.Module
import org.eclipse.scout.sdk.core.s.nls.ITranslationEntry
import org.eclipse.scout.sdk.core.s.nls.Language
import org.eclipse.scout.sdk.s2i.nls.TranslationStoreStackLoader.Companion.createStack
import java.util.stream.Stream

class NlsCompletionHelper private constructor() {

    companion object {

        private val RENDERER = object : LookupElementRenderer<LookupElement>() {
            override fun renderElement(element: LookupElement, presentation: LookupElementPresentation) {
                renderLookupElement(element, presentation)
            }
        }

        fun computeLookupElements(module: Module, lookupStringProvider: (ITranslationEntry) -> String = { it.key() }) =
                createStack(module)
                        ?.allEntries()
                        ?.map { lookupElementFor(it, lookupStringProvider) } ?: Stream.empty()

        private fun lookupElementFor(translation: ITranslationEntry, lookupStringProvider: (ITranslationEntry) -> String) =
                LookupElementBuilder.create(translation, lookupStringProvider.invoke(translation))
                        .withCaseSensitivity(false)
                        .withRenderer(RENDERER)

        private fun renderLookupElement(element: LookupElement, presentation: LookupElementPresentation) {
            val translation = element.getObject() as ITranslationEntry
            val store = translation.store()
            val isReadOnly = !store.isEditable
            val serviceSuffix = "TextProviderService"

            presentation.itemText = translation.key()
            presentation.isItemTextItalic = isReadOnly
            presentation.icon = AllIcons.Nodes.ResourceBundle

            presentation.appendTailText("=" + translation.text(Language.LANGUAGE_DEFAULT).get(), true)

            var storeName = store.service().type().elementName()
            if (storeName.endsWith(serviceSuffix)) {
                storeName = storeName.substring(0, storeName.length - serviceSuffix.length)
            }
            presentation.typeText = storeName
        }
    }
}