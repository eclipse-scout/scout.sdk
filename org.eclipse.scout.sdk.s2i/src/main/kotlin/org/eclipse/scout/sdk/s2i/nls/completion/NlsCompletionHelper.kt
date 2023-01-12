/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2i.nls.completion

import com.intellij.codeInsight.completion.CodeCompletionHandlerBase
import com.intellij.codeInsight.completion.CompletionInitializationContext
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.*
import com.intellij.icons.AllIcons
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.module.Module
import com.intellij.psi.PsiElement
import com.intellij.refactoring.suggested.endOffset
import com.intellij.refactoring.suggested.startOffset
import org.eclipse.scout.sdk.core.s.ISdkConstants
import org.eclipse.scout.sdk.core.s.nls.TranslationStoreComparator
import org.eclipse.scout.sdk.core.s.nls.manager.IStackedTranslation
import org.eclipse.scout.sdk.core.util.Strings
import org.eclipse.scout.sdk.s2i.EclipseScoutBundle.message
import org.eclipse.scout.sdk.s2i.nls.TranslationLanguageSpec
import org.eclipse.scout.sdk.s2i.nls.TranslationLanguageSpec.Companion.translationSpec
import org.eclipse.scout.sdk.s2i.nls.TranslationManagerLoader.createManager
import org.eclipse.scout.sdk.s2i.nls.inspection.AddMissingTranslationQuickFix
import org.eclipse.scout.sdk.s2i.settings.ScoutSettings
import java.util.stream.Collectors.toList

object NlsCompletionHelper {

    private val RENDERER = object : LookupElementRenderer<LookupElement>() {
        override fun renderElement(element: LookupElement, presentation: LookupElementPresentation) {
            renderLookupElement(element, presentation)
        }
    }

    fun computeLookupElements(module: Module, psiElement: PsiElement): List<LookupElementBuilder> {
        // compute original element so that the translation key may be modified. the completion clone will be discarded
        val psiFile = psiElement.containingFile.originalFile
        val element = psiFile.findElementAt(psiElement.startOffset)?.parent ?: return emptyList()
        val translationSpec = element.translationSpec() ?: return emptyList()
        val manager = createManager(module, translationSpec.translationDependencyScope, true) ?: return emptyList()
        val elements = manager.allTranslations()
            .map { createExistingTranslationLookupElement(it, module, translationSpec) }
            .collect(toList())
        createNewTranslationLookupElement(element, translationSpec)?.let { elements.add(0, it) }
        return elements
    }

    private fun createNewTranslationLookupElement(psiElement: PsiElement, translationSpec: TranslationLanguageSpec): LookupElementBuilder? {
        val key = translationSpec.resolveTranslationKey()
            ?.let { removeDummyIdentifier(it) }
            ?.takeIf { Strings.hasText(it) }
            ?: return null
        val builder = LookupElementBuilder.create(NewTranslationLookupObject(psiElement, key), message("new.translation") + "...")
            .withCaseSensitivity(false)
            .withInsertHandler { _, element -> openTranslationNewDialog(element) }
            .withLookupStrings(listOf(translationSpec.decorateTranslationKey(key)))
            .withIcon(AllIcons.General.Add)
        builder.withAutoCompletionPolicy(AutoCompletionPolicy.NEVER_AUTOCOMPLETE)
        builder.putUserData(CodeCompletionHandlerBase.DIRECT_INSERTION, true)
        return builder
    }

    private fun removeDummyIdentifier(s: String) = Strings.replaceEach(
        s,
        arrayOf(CompletionInitializationContext.DUMMY_IDENTIFIER_TRIMMED + ';', CompletionInitializationContext.DUMMY_IDENTIFIER_TRIMMED),
        arrayOf("", "")
    ).toString()

    private fun openTranslationNewDialog(element: LookupElement) {
        val lookupObj = element.getObject() as NewTranslationLookupObject
        AddMissingTranslationQuickFix(lookupObj.key)
            .applyFix(lookupObj.psiElement)
    }

    private fun createExistingTranslationLookupElement(translation: IStackedTranslation, module: Module, translationSpec: TranslationLanguageSpec): LookupElementBuilder {
        val translationLookupObject = TranslationLookupObject(translation, module)
        val lookupElement = LookupElementBuilder.create(translationLookupObject, translationSpec.decorateTranslationKey(translation.key()))
            .withCaseSensitivity(true)
            .withInsertHandler { context, item -> insertTranslationKey(context, item, translationSpec) }
            .withRenderer(RENDERER)
        lookupElement.putUserData(CodeCompletionHandlerBase.DIRECT_INSERTION, true)
        return lookupElement
    }

    private fun insertTranslationKey(context: InsertionContext, element: LookupElement, translationSpec: TranslationLanguageSpec) {
        val currentCaret = context.editor.caretModel.currentCaret
        val lookupObj = element.getObject() as TranslationLookupObject
        insertTranslationKey(lookupObj.translation.key(), translationSpec, currentCaret)
    }

    private fun insertTranslationKey(nlsKey: String, translationSpec: TranslationLanguageSpec, caret: Caret) {
        val newElement = translationSpec.createNewLiteral(nlsKey)
        WriteAction.run<RuntimeException> {
            val inserted = translationSpec.element.replace(newElement)
            caret.moveToOffset(inserted.endOffset)
        }
    }

    private fun renderLookupElement(element: LookupElement, presentation: LookupElementPresentation) {
        val lookupObj = element.getObject() as TranslationLookupObject
        val translation = lookupObj.translation
        val language = ScoutSettings.getTranslationLanguage(lookupObj.module.project)
        val isInProject = translation.hasEditableStores()
        val primaryStoreName = translation.stores()
            .min(TranslationStoreComparator.INSTANCE).orElse(null)
            ?.service()?.type()?.elementName()
            ?.removeSuffix(ISdkConstants.SUFFIX_TEXT_PROVIDER_SERVICE)

        presentation.itemText = translation.key()
        presentation.isItemTextItalic = !isInProject
        presentation.icon = AllIcons.Nodes.ResourceBundle
        translation.bestText(language).ifPresent { presentation.appendTailText("=$it", true) }
        presentation.typeText = primaryStoreName
    }

    data class NewTranslationLookupObject(val psiElement: PsiElement, val key: String)

    data class TranslationLookupObject(val translation: IStackedTranslation, val module: Module)
}