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
import com.intellij.lang.ecmascript6.psi.impl.ES6ImportPsiUtil
import com.intellij.lang.javascript.patterns.JSPatterns.jsProperty
import com.intellij.lang.javascript.psi.JSArrayLiteralExpression
import com.intellij.patterns.PlatformPatterns.psiElement
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.util.ProcessingContext
import org.eclipse.scout.sdk.core.s.model.js.ScoutJsCoreConstants
import org.eclipse.scout.sdk.core.typescript.model.api.IES6Class
import org.eclipse.scout.sdk.s2i.template.js.JsModelCompletionHelper.PropertyCompletionInfo
import org.eclipse.scout.sdk.s2i.template.js.JsModelCompletionHelper.SELECTED_ELEMENT
import org.eclipse.scout.sdk.s2i.template.js.JsModelCompletionHelper.getPropertyValueInfo
import org.eclipse.scout.sdk.s2i.template.js.JsModelCompletionHelper.propertyElementPattern
import org.eclipse.scout.sdk.s2i.toVirtualFile

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
            if (completionInfo.objectTypeDeclaringScoutObject() == null && ScoutJsCoreConstants.PROPERTY_NAME_OBJECT_TYPE != completionInfo.propertyName) return

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
            val scoutObject = infoForPropertyLookup.objectTypeDeclaringScoutObject() ?: return emptyList()
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
            val type = findTypeToImport(lookupElement) ?: return lookupElement
            val originalHandler = lookupElement.insertHandler
            var importName = type.name()
            val firstDot = importName.indexOf('.')
            if (firstDot > 0) importName = importName.take(firstDot)

            return lookupElement.withInsertHandler { context, item ->
                originalHandler?.handleInsert(context, item)

                val targetPackageJson = type.containingModule()?.packageJson() ?: return@withInsertHandler
                val targetModuleMainPath = targetPackageJson.main().map { targetPackageJson.directory().resolve(it) }.orElse(null) ?: return@withInsertHandler
                val targetModuleMainFile = targetModuleMainPath.toVirtualFile()?.let { PsiManager.getInstance(context.project).findFile(it) } ?: return@withInsertHandler
                ES6ImportPsiUtil.insertJSImport(place, importName, ES6ImportPsiUtil.ImportExportType.SPECIFIER, targetModuleMainFile, context.editor)
            }
        }

        private fun findTypeToImport(lookupElement: LookupElementBuilder): IES6Class? {
            val lookupElementViewModel = lookupElement.getUserData(SELECTED_ELEMENT) ?: return null
            val property = lookupElementViewModel.property()
            if (property.scoutJsObject.scoutJsModel().supportsClassReference() && (property.type.hasLeafClasses() || property.isObjectType)) {
                val objectLookupElement = lookupElementViewModel as? JsModelCompletionHelper.ScoutJsObjectLookupElement ?: return null
                return objectLookupElement.propertyValue.scoutJsObject.declaringClass()
            }
            if (property.type.isEnumLike) {
                // FIXME model: add enum support
                return null
            }
            return null
        }
    }
}