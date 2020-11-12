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

import com.intellij.codeInsight.completion.CodeCompletionHandlerBase
import com.intellij.codeInsight.completion.CompletionInitializationContext
import com.intellij.codeInsight.lookup.*
import com.intellij.icons.AllIcons
import com.intellij.openapi.module.Module
import com.intellij.psi.PsiElement
import org.eclipse.scout.sdk.core.s.ISdkConstants
import org.eclipse.scout.sdk.core.s.nls.ITranslationEntry
import org.eclipse.scout.sdk.core.util.Strings
import org.eclipse.scout.sdk.s2i.EclipseScoutBundle.message
import org.eclipse.scout.sdk.s2i.nls.PsiTranslationPatterns
import org.eclipse.scout.sdk.s2i.nls.TranslationStoreStackLoader.createStack
import org.eclipse.scout.sdk.s2i.nls.inspection.AddMissingTranslationQuickFix
import org.eclipse.scout.sdk.s2i.nlsDependencyScope
import org.eclipse.scout.sdk.s2i.settings.ScoutSettings
import java.util.stream.Collectors.toList

object NlsCompletionHelper {

    private val RENDERER = object : LookupElementRenderer<LookupElement>() {
        override fun renderElement(element: LookupElement, presentation: LookupElementPresentation) {
            renderLookupElement(element, presentation)
        }
    }

    fun computeLookupElements(module: Module, psiElement: PsiElement, includeNewTranslationEntry: Boolean = true, lookupStringProvider: (String) -> String = { it }): List<LookupElementBuilder> {
        val stack = createStack(module, psiElement.nlsDependencyScope(), true) ?: return emptyList()
        val elements = stack.allEntries()
                .map { lookupElementFor(it, module, lookupStringProvider) }
                .collect(toList())
        if (includeNewTranslationEntry) {
            createNewTranslationLookupElement(psiElement, lookupStringProvider)?.let { elements.add(it) }
        }
        return elements
    }

    private fun createNewTranslationLookupElement(psiElement: PsiElement, lookupStringProvider: (String) -> String): LookupElementBuilder? {
        val key = PsiTranslationPatterns.getTranslationKeyOf(psiElement)
                ?.let {
                    Strings.replaceEach(it,
                            arrayOf(CompletionInitializationContext.DUMMY_IDENTIFIER_TRIMMED + ';', CompletionInitializationContext.DUMMY_IDENTIFIER_TRIMMED),
                            arrayOf("", "")).toString()
                }
                ?.takeIf { Strings.hasText(it) }
                ?: return null
        val builder = LookupElementBuilder.create(NewTranslationLookupObject(psiElement, key), message("new.translation") + "...")
                .withCaseSensitivity(false)
                .withInsertHandler { _, element -> createNewTranslation(element) }
                .withLookupStrings(listOf(lookupStringProvider.invoke(key)))
                .withIcon(AllIcons.General.Add)
        builder.withAutoCompletionPolicy(AutoCompletionPolicy.NEVER_AUTOCOMPLETE)
        builder.putUserData(CodeCompletionHandlerBase.DIRECT_INSERTION, false)
        return builder
    }

    private fun createNewTranslation(element: LookupElement) {
        val lookupObj = element.getObject() as NewTranslationLookupObject
        AddMissingTranslationQuickFix(lookupObj.key).applyFix(lookupObj.psiElement)
    }

    private fun lookupElementFor(translation: ITranslationEntry, module: Module, lookupStringProvider: (String) -> String) =
            LookupElementBuilder.create(TranslationLookupObject(translation, module), lookupStringProvider.invoke(translation.key()))
                    .withCaseSensitivity(true)
                    .withRenderer(RENDERER)

    private fun renderLookupElement(element: LookupElement, presentation: LookupElementPresentation) {
        val lookupObj = element.getObject() as TranslationLookupObject
        val translation = lookupObj.translationEntry
        val store = translation.store()
        val isReadOnly = !store.isEditable

        presentation.itemText = translation.key()
        presentation.isItemTextItalic = isReadOnly
        presentation.icon = AllIcons.Nodes.ResourceBundle

        val requestedLanguage = ScoutSettings.getTranslationLanguage(lookupObj.module.project)
        translation.bestText(requestedLanguage).ifPresent { presentation.appendTailText("=$it", true) }

        var storeName = store.service().type().elementName()
        if (storeName.endsWith(ISdkConstants.SUFFIX_TEXT_PROVIDER_SERVICE)) {
            storeName = storeName.substring(0, storeName.length - ISdkConstants.SUFFIX_TEXT_PROVIDER_SERVICE.length)
        }
        presentation.typeText = storeName
    }

    private data class NewTranslationLookupObject(val psiElement: PsiElement, val key: String)

    private data class TranslationLookupObject(val translationEntry: ITranslationEntry, val module: Module)
}