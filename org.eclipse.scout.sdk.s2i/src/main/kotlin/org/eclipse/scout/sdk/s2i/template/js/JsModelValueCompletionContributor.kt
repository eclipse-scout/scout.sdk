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
import org.eclipse.scout.sdk.core.s.model.js.ScoutJsModel
import org.eclipse.scout.sdk.core.s.model.js.ScoutJsProperty
import org.eclipse.scout.sdk.s2i.template.js.JsModelCompletionHelper.PropertyCompletionInfo
import org.eclipse.scout.sdk.s2i.template.js.JsModelCompletionHelper.SELECTED_ELEMENT
import org.eclipse.scout.sdk.s2i.template.js.JsModelCompletionHelper.createLookupElement
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
            if (completionInfo.objectType == null && ScoutJsModel.OBJECT_TYPE_PROPERTY_NAME != completionInfo.propertyName) return

            val elements = getPropertyValueElements(completionInfo) ?: return
            if (elements.isNotEmpty()) {
                result.addAllElements(elements)
                result.stopHere()
            }
        }

        private fun getPropertyValueElements(completionInfo: PropertyCompletionInfo): List<LookupElement>? {
            val scoutJsModel = completionInfo.scoutJsModel() ?: return null
            val scoutJsProperty = if (ScoutJsModel.OBJECT_TYPE_PROPERTY_NAME == completionInfo.propertyName) {
                getPropertyValueInfo(completionInfo.property, "")
                    ?.let { scoutJsModel.scoutObject(it.objectType)?.property(it.propertyName) }
                    ?.takeIf { it.isWidget(scoutJsModel) } // only accept widgets filter. for all other types use the default: all known top-level objects
                    ?: scoutJsModel.scoutObject(ScoutJsModel.WIDGET_CLASS_NAME).property(ScoutJsModel.OBJECT_TYPE_PROPERTY_NAME)
            } else {
                scoutJsModel.scoutObject(completionInfo.objectType).property(completionInfo.propertyName)
            } ?: return emptyList()

            return scoutJsProperty.values(scoutJsModel).map {
                val lookupElement = createLookupElement(it.name(), it, scoutJsProperty, completionInfo)
                if (!completionInfo.isInLiteral) {
                    withJsImportIfNecessary(lookupElement, completionInfo.property.containingFile.originalFile, scoutJsProperty, scoutJsModel)
                } else {
                    lookupElement
                }
            }
        }

        private fun withJsImportIfNecessary(lookupElement: LookupElementBuilder, place: PsiElement, scoutJsProperty: ScoutJsProperty, scoutJsModel: ScoutJsModel): LookupElementBuilder {
            val targetScoutJsModel = scoutJsProperty.scoutJsModel()
            if (!scoutJsProperty.isEnum && (!targetScoutJsModel.supportsClassReference() || (!scoutJsProperty.isWidget(scoutJsModel) && scoutJsProperty.isObjectType))) {
                return lookupElement
            }

            val type = if (targetScoutJsModel.supportsClassReference() && (scoutJsProperty.isWidget(scoutJsModel) || scoutJsProperty.isObjectType)) {
                (lookupElement.getUserData(SELECTED_ELEMENT) as? JsModelCompletionHelper.ScoutJsObjectLookupElement)?.scoutJsObject?.declaringClass()
            } else {
                // FIXME model: add enum support
//                targetJsModel.element(scoutJsProperty.dataType.type)
                null
            } ?: return lookupElement

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
    }
}