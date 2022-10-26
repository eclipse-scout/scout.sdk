/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
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
import org.eclipse.scout.sdk.s2i.model.js.JsModel
import org.eclipse.scout.sdk.s2i.model.js.JsModelProperty
import org.eclipse.scout.sdk.s2i.template.js.JsModelCompletionHelper.PropertyCompletionInfo
import org.eclipse.scout.sdk.s2i.template.js.JsModelCompletionHelper.SELECTED_ELEMENT
import org.eclipse.scout.sdk.s2i.template.js.JsModelCompletionHelper.createLookupElement
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
            if (completionInfo.objectType == null && JsModel.OBJECT_TYPE_PROPERTY_NAME != completionInfo.propertyName) return

            val elements = getPropertyValueElements(completionInfo)
            if (elements.isNotEmpty()) {
                result.addAllElements(elements)
                result.stopHere()
            }
        }

        private fun getPropertyValueElements(completionInfo: PropertyCompletionInfo): List<LookupElement> {
            val jsModel = JsModel().build(completionInfo.module)
            val jsProperty = if (JsModel.OBJECT_TYPE_PROPERTY_NAME == completionInfo.propertyName) {
                getPropertyValueInfo(completionInfo.property, "")
                    ?.let { jsModel.property(it.objectType, it.propertyName) }
                    ?.takeIf { it.dataType == JsModelProperty.JsPropertyDataType.WIDGET } // only accept widgets filter. for all other types use the default: all known top-level objects
                    ?: jsModel.property(JsModel.WIDGET_CLASS_NAME, JsModel.OBJECT_TYPE_PROPERTY_NAME)
            } else {
                jsModel.property(completionInfo.objectType, completionInfo.propertyName)
            } ?: return emptyList()

            return jsModel.valuesForProperty(jsProperty).map {
                val lookupElement = createLookupElement(it.displayText, it.element, jsProperty, completionInfo)
                if (!completionInfo.isInLiteral) {
                    withJsImportIfNecessary(lookupElement, completionInfo.property.containingFile.originalFile, jsProperty)
                } else {
                    lookupElement
                }
            }
        }

        private fun withJsImportIfNecessary(lookupElement: LookupElementBuilder, place: PsiElement, jsProperty: JsModelProperty): LookupElementBuilder {
            val targetScoutJsModule = jsProperty.scoutJsModule
            if (!jsProperty.dataType.isCustomType() && (!targetScoutJsModule.useClassReference || (jsProperty.dataType != JsModelProperty.JsPropertyDataType.WIDGET && jsProperty.name != JsModel.OBJECT_TYPE_PROPERTY_NAME))) return lookupElement
            val targetJsModel = targetScoutJsModule.jsModel
            val type = if (targetScoutJsModule.useClassReference && (jsProperty.dataType == JsModelProperty.JsPropertyDataType.WIDGET || jsProperty.name == JsModel.OBJECT_TYPE_PROPERTY_NAME)) {
                lookupElement.getUserData(SELECTED_ELEMENT)
            } else {
                targetJsModel.element(jsProperty.dataType.type)
            } ?: return lookupElement
            val originalHandler = lookupElement.insertHandler
            var importName = type.name
            val firstDot = importName.indexOf('.')
            if (firstDot > 0) importName = importName.take(firstDot)

            return lookupElement.withInsertHandler { context, item ->
                originalHandler?.handleInsert(context, item)

                val targetModuleMainFile = PsiManager.getInstance(context.project).findFile(targetScoutJsModule.mainFile) ?: return@withInsertHandler
                ES6ImportPsiUtil.insertJSImport(place, importName, ES6ImportPsiUtil.ImportExportType.SPECIFIER, targetModuleMainFile, context.editor)
            }
        }
    }
}