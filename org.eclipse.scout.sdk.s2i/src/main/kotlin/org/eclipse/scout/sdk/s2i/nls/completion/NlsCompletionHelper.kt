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
import com.intellij.psi.PsiManager
import com.intellij.refactoring.suggested.endOffset
import com.intellij.refactoring.suggested.startOffset
import org.eclipse.scout.sdk.core.s.ISdkConstants
import org.eclipse.scout.sdk.core.s.nls.ITranslationEntry
import org.eclipse.scout.sdk.core.s.nls.Language
import org.eclipse.scout.sdk.core.util.Strings
import org.eclipse.scout.sdk.s2i.EclipseScoutBundle.message
import org.eclipse.scout.sdk.s2i.environment.IdeaProgress
import org.eclipse.scout.sdk.s2i.environment.TransactionManager
import org.eclipse.scout.sdk.s2i.environment.TransactionMember
import org.eclipse.scout.sdk.s2i.nls.TranslationLanguageSpec
import org.eclipse.scout.sdk.s2i.nls.TranslationLanguageSpec.Companion.translationSpec
import org.eclipse.scout.sdk.s2i.nls.TranslationStoreStackLoader.createStack
import org.eclipse.scout.sdk.s2i.nls.inspection.AddMissingTranslationQuickFix
import org.eclipse.scout.sdk.s2i.resolveLocalPath
import org.eclipse.scout.sdk.s2i.resolveProperty
import org.eclipse.scout.sdk.s2i.settings.ScoutSettings
import java.nio.file.Path
import java.util.stream.Collectors.toList

object NlsCompletionHelper {

    private val RENDERER = object : LookupElementRenderer<LookupElement>() {
        override fun renderElement(element: LookupElement, presentation: LookupElementPresentation) {
            renderLookupElement(element, presentation)
        }
    }

    fun computeLookupElements(module: Module, psiElement: PsiElement, includeNewTranslationEntry: Boolean = true): List<LookupElementBuilder> {
        // compute original element so that the translation key may be modified. the completion clone will be discarded
        val psiFile = psiElement.containingFile.originalFile
        val element = psiFile.findElementAt(psiElement.startOffset)?.parent ?: return emptyList()
        val translationSpec = element.translationSpec() ?: return emptyList()
        val stack = createStack(module, translationSpec.translationDependencyScope, true) ?: return emptyList()
        val psiManager = PsiManager.getInstance(module.project)
        val elements = stack.allEntries()
                .map { createExistingTranslationLookupElement(it, module, translationSpec, psiManager) }
                .collect(toList())
        if (includeNewTranslationEntry) {
            createNewTranslationLookupElement(element, translationSpec)?.let { elements.add(it) }
        }
        return elements
    }

    private fun createNewTranslationLookupElement(psiElement: PsiElement, translationSpec: TranslationLanguageSpec): LookupElementBuilder? {
        val key = translationSpec.resolveTranslationKey()
                ?.let { removeDummyIdentifier(it) }
                ?.takeIf { Strings.hasText(it) }
                ?: return null
        val builder = LookupElementBuilder.create(NewTranslationLookupObject(psiElement, key), message("new.translation") + "...")
                .withCaseSensitivity(false)
                .withInsertHandler { _, element -> openTranslationNewDialog(element, translationSpec) }
                .withLookupStrings(listOf(translationSpec.decorateTranslationKey(key)))
                .withIcon(AllIcons.General.Add)
        builder.withAutoCompletionPolicy(AutoCompletionPolicy.NEVER_AUTOCOMPLETE)
        builder.putUserData(CodeCompletionHandlerBase.DIRECT_INSERTION, true)
        return builder
    }

    private fun removeDummyIdentifier(s: String) = Strings.replaceEach(s,
            arrayOf(CompletionInitializationContext.DUMMY_IDENTIFIER_TRIMMED + ';', CompletionInitializationContext.DUMMY_IDENTIFIER_TRIMMED),
            arrayOf("", "")).toString()

    private fun openTranslationNewDialog(element: LookupElement, translationSpec: TranslationLanguageSpec) {
        val lookupObj = element.getObject() as NewTranslationLookupObject
        AddMissingTranslationQuickFix(lookupObj.key)
                .withSaveTask { createdNlsEntry, _, _ -> updateTranslationKey(lookupObj.key, createdNlsEntry, translationSpec) }
                .applyFix(lookupObj.psiElement)
    }

    private fun updateTranslationKey(existingKey: String, createdNlsEntry: ITranslationEntry, translationSpec: TranslationLanguageSpec) {
        val createdKey = createdNlsEntry.key()
        if (createdKey == existingKey) return
        val path = translationSpec.element.containingFile.virtualFile.resolveLocalPath() ?: return
        TransactionManager.current().register(UpdateTranslationKeyMember(path, createdKey, translationSpec))
    }

    private fun createExistingTranslationLookupElement(translation: ITranslationEntry, module: Module, translationSpec: TranslationLanguageSpec, psiManager: PsiManager): LookupElementBuilder {
        val prop = translation.resolveProperty(Language.LANGUAGE_DEFAULT, psiManager)?.psiElement
        val lookupElement = LookupElementBuilder.create(TranslationLookupObject(translation, prop, module), translationSpec.decorateTranslationKey(translation.key()))
                .withCaseSensitivity(true)
                .withInsertHandler { context, item -> insertTranslationKey(context, item, translationSpec) }
                .withRenderer(RENDERER)
        lookupElement.putUserData(CodeCompletionHandlerBase.DIRECT_INSERTION, true)
        return lookupElement
    }

    private fun insertTranslationKey(context: InsertionContext, element: LookupElement, translationSpec: TranslationLanguageSpec) {
        val currentCaret = context.editor.caretModel.currentCaret
        val lookupObj = element.getObject() as TranslationLookupObject
        insertTranslationKey(lookupObj.translationEntry.key(), translationSpec, currentCaret)
    }

    private fun insertTranslationKey(nlsKey: String, translationSpec: TranslationLanguageSpec, caret: Caret?) {
        val newElement = translationSpec.createNewLiteral(nlsKey)
        WriteAction.run<RuntimeException> {
            val inserted = translationSpec.element.replace(newElement)
            caret?.moveToOffset(inserted.endOffset)
        }
    }

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

        presentation.typeText = store.service().type().elementName().removeSuffix(ISdkConstants.SUFFIX_TEXT_PROVIDER_SERVICE)
    }

    private class UpdateTranslationKeyMember(val path: Path, val createdKey: String, val translationSpec: TranslationLanguageSpec) : TransactionMember {
        override fun file() = path

        override fun commit(progress: IdeaProgress): Boolean {
            insertTranslationKey(createdKey, translationSpec, null)
            return true
        }
    }

    data class NewTranslationLookupObject(val psiElement: PsiElement, val key: String)

    data class TranslationLookupObject(val translationEntry: ITranslationEntry, val element: PsiElement?, val module: Module)
}