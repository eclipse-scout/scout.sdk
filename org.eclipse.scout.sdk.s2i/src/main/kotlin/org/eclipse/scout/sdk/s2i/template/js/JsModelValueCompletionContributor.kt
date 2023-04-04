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
import com.intellij.lang.javascript.patterns.JSPatterns.jsProperty
import com.intellij.lang.javascript.psi.JSArrayLiteralExpression
import com.intellij.patterns.PlatformPatterns.psiElement
import com.intellij.psi.PsiElement
import com.intellij.util.ProcessingContext
import org.eclipse.scout.sdk.core.log.SdkLog
import org.eclipse.scout.sdk.core.s.model.js.ScoutJsCoreConstants
import org.eclipse.scout.sdk.core.s.model.js.ScoutJsModel
import org.eclipse.scout.sdk.core.s.model.js.prop.ScoutJsEnumPropertyValue
import org.eclipse.scout.sdk.core.typescript.model.api.INodeElement
import org.eclipse.scout.sdk.s2i.template.js.JsModelCompletionHelper.SELECTED_ELEMENT
import org.eclipse.scout.sdk.s2i.template.js.JsModelCompletionHelper.getPropertyValueInfo
import org.eclipse.scout.sdk.s2i.template.js.JsModelCompletionHelper.propertyElementPattern
import org.eclipse.scout.sdk.s2i.util.PsiImportUtils

class JsModelValueCompletionContributor : CompletionContributor() {

    init {
        // normal value completion
        extend(CompletionType.BASIC, propertyElementPattern(), JsModelValueCompletionProvider())

        // value completion when within an array (to add an array element)
        extend(CompletionType.BASIC, psiElement().withSuperParent(2, psiElement(JSArrayLiteralExpression::class.java).withParent(jsProperty())), JsModelValueCompletionProvider())
    }

    private class JsModelValueCompletionProvider : CompletionProvider<CompletionParameters>() {
        override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
            val start = System.currentTimeMillis()
            val completionInfo = getPropertyValueInfo(parameters, result) ?: return
            val elements = getPropertyValueElements(completionInfo) ?: return
            SdkLog.info("Scout JS model value completion took {}ms", System.currentTimeMillis() - start)
            if (elements.isNotEmpty()) {
                result.addAllElements(elements)
                result.stopHere()
            }
        }

        private fun getPropertyValueElements(completionInfo: JsModelCompletionInfo): List<LookupElement>? {
            val scoutJsProperty = if (ScoutJsCoreConstants.PROPERTY_NAME_OBJECT_TYPE == completionInfo.propertyName) {
                completionInfo.parentScoutProperty()
            } else {
                completionInfo.availableProperties()
                    .filter { it.name() == completionInfo.propertyName }
                    .findAny().orElse(null)
            } ?: return emptyList()

            val place = completionInfo.propertyPsi.containingFile.originalFile
            return scoutJsProperty.computePossibleValues(completionInfo.scoutJsModel).map {
                val lookupElement = JsModelCompletionHelper.createPropertyValueLookupElement(it, completionInfo)
                if (!completionInfo.isInLiteral) {
                    withJsImportIfNecessary(lookupElement, completionInfo.scoutJsModel, place)
                } else {
                    lookupElement
                }
            }.toList()
        }

        private fun withJsImportIfNecessary(lookupElement: LookupElementBuilder, editModel: ScoutJsModel, place: PsiElement): LookupElementBuilder {
            val originalInsertHandler = lookupElement.insertHandler
            return lookupElement.withInsertHandler { context, item ->
                originalInsertHandler?.handleInsert(context, item)
                val elementToImport = findElementToImport(lookupElement) ?: return@withInsertHandler
                PsiImportUtils.createOrUpdateImport(elementToImport, editModel.nodeModule(), place)
            }
        }


        private fun findElementToImport(lookupElement: LookupElementBuilder): INodeElement? {
            val lookupElementViewModel = lookupElement.getUserData(SELECTED_ELEMENT) ?: return null
            val property = lookupElementViewModel.property()
            if (property.type().isEnumLike && lookupElementViewModel is JsModelCompletionHelper.JsValueLookupElement && lookupElementViewModel.propertyValue is ScoutJsEnumPropertyValue) {
                return lookupElementViewModel.propertyValue.scoutJsEnum.topLevelReference()
            }
            if (property.scoutJsObject().scoutJsModel().supportsClassReference() && (lookupElementViewModel is JsModelCompletionHelper.JsObjectValueLookupElement || property.isObjectType)) {
                val objectLookupElement = lookupElementViewModel as? JsModelCompletionHelper.JsObjectValueLookupElement ?: return null
                return objectLookupElement.propertyValue.scoutJsObject.declaringClass()
            }
            return null
        }
    }
}