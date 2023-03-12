/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2i.template.js

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.lang.javascript.modules.imports.JSImportAction
import com.intellij.lang.javascript.modules.imports.JSImportCandidateWithExecutor
import com.intellij.lang.javascript.modules.imports.JSImportElementFilter
import com.intellij.lang.javascript.patterns.JSPatterns.jsProperty
import com.intellij.lang.javascript.psi.JSArrayLiteralExpression
import com.intellij.patterns.PlatformPatterns.psiElement
import com.intellij.psi.PsiElement
import com.intellij.util.ProcessingContext
import org.eclipse.scout.sdk.core.s.model.js.ScoutJsCoreConstants
import org.eclipse.scout.sdk.core.typescript.model.api.IES6Class
import org.eclipse.scout.sdk.s2i.template.js.JsModelCompletionHelper.PropertyCompletionInfo
import org.eclipse.scout.sdk.s2i.template.js.JsModelCompletionHelper.SELECTED_ELEMENT
import org.eclipse.scout.sdk.s2i.template.js.JsModelCompletionHelper.getPropertyValueInfo
import org.eclipse.scout.sdk.s2i.template.js.JsModelCompletionHelper.propertyElementPattern

class JsModelValueCompletionContributor : CompletionContributor() {

    init {
        // normal value completion
        extend(CompletionType.BASIC, propertyElementPattern(), JsModelValueCompletionProvider())

        // value completion when within an array (to add an array element)
        extend(CompletionType.BASIC, psiElement().withSuperParent(2, psiElement(JSArrayLiteralExpression::class.java).withParent(jsProperty())), JsModelValueCompletionProvider())
    }

    private class JsModelValueCompletionProvider : CompletionProvider<CompletionParameters>() {
        override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
            val completionInfo = getPropertyValueInfo(parameters, result) ?: return

            // require an objectType to be set or the completion of the object type itself
            if (completionInfo.objectTypeScoutObject() == null && ScoutJsCoreConstants.PROPERTY_NAME_OBJECT_TYPE != completionInfo.propertyName) return

            val elements = getPropertyValueElements(completionInfo) ?: return
            if (elements.isNotEmpty()) {
                result.addAllElements(elements)
                result.stopHere()
            }
        }

        private fun getPropertyValueElements(completionInfo: PropertyCompletionInfo): List<LookupElement>? {
            var infoForPropertyLookup = completionInfo
            if (ScoutJsCoreConstants.PROPERTY_NAME_OBJECT_TYPE == completionInfo.propertyName) {
                infoForPropertyLookup = getPropertyValueInfo(completionInfo.property, completionInfo.searchPrefix) ?: completionInfo
            }
            val scoutObject = infoForPropertyLookup.objectTypeScoutObject() ?: return emptyList()
            val scoutJsProperty = scoutObject
                .findProperties()
                .withSuperClasses(true)
                .withName(infoForPropertyLookup.propertyName)
                .stream()
                .findAny()
                .orElse(null) ?: return emptyList()

            return scoutJsProperty.computePossibleValues(completionInfo.scoutJsModel).map {
                val lookupElement = JsModelCompletionHelper.createPropertyValueLookupElement(it, completionInfo)
                if (!completionInfo.isInLiteral) {
                    withJsImportIfNecessary(lookupElement, completionInfo.property.containingFile.originalFile)
                } else {
                    lookupElement
                }
            }.toList()
        }

        private fun withJsImportIfNecessary(lookupElement: LookupElementBuilder, place: PsiElement): LookupElementBuilder {
            val originalInsertHandler = lookupElement.insertHandler
            return lookupElement.withInsertHandler { context, item ->
                originalInsertHandler?.handleInsert(context, item)

                val type = findTypeToImport(lookupElement) ?: return@withInsertHandler
                var importName = type.exportAlias().orElse(type.name())
                val firstDot = importName.indexOf('.')
                if (firstDot > 0) importName = importName.take(firstDot)
                object : JSImportAction(context.editor, place, importName, JSImportElementFilter.EMPTY) {
                    override fun shouldShowPopup(candidates: List<JSImportCandidateWithExecutor>) = false
                }.execute()
            }
        }

        private fun findTypeToImport(lookupElement: LookupElementBuilder): IES6Class? {
            val lookupElementViewModel = lookupElement.getUserData(SELECTED_ELEMENT) ?: return null
            val property = lookupElementViewModel.property()
            if (property.scoutJsObject().scoutJsModel().supportsClassReference() && (property.type().hasClasses() || property.isObjectType)) {
                val objectLookupElement = lookupElementViewModel as? JsModelCompletionHelper.JsObjectValueLookupElement ?: return null
                return objectLookupElement.propertyValue.scoutJsObject.declaringClass()
            }
            if (property.type().isEnumLike) {
                // FIXME model: add enum support
                return null
            }
            return null
        }
    }
}