/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
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
import com.intellij.lang.ecmascript6.actions.ES6AddImportExecutor
import com.intellij.lang.ecmascript6.psi.impl.ES6ImportPsiUtil
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.psi.PsiElement
import com.intellij.util.ProcessingContext
import org.eclipse.scout.sdk.core.s.IWebConstants
import org.eclipse.scout.sdk.core.util.Strings
import org.eclipse.scout.sdk.s2i.model.js.JsModel
import org.eclipse.scout.sdk.s2i.model.js.JsModelProperty
import org.eclipse.scout.sdk.s2i.template.js.JsModelCompletionHelper.PropertyCompletionInfo
import org.eclipse.scout.sdk.s2i.template.js.JsModelCompletionHelper.createLookupElement
import org.eclipse.scout.sdk.s2i.template.js.JsModelCompletionHelper.getPropertyValueInfo
import org.eclipse.scout.sdk.s2i.template.js.JsModelCompletionHelper.propertyElementPattern

class JsModelValueCompletionContributor : CompletionContributor() {

    init {
        extend(CompletionType.BASIC, propertyElementPattern(), JsModelValueCompletionProvider())
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
                withJsImportIfNecessary(lookupElement, completionInfo.property.containingFile.originalFile, jsProperty)
            }
        }

        private fun withJsImportIfNecessary(lookupElement: LookupElementBuilder, place: PsiElement, jsProperty: JsModelProperty): LookupElementBuilder {
            if (!jsProperty.dataType.isCustomType()) return lookupElement
            val jsModel = jsProperty.scoutJsModule.jsModel
            val type = jsModel.element(jsProperty.dataType.type) ?: return lookupElement
            val containingFile = place.containingFile.virtualFile
            val thisModule = jsModel.containingModule(containingFile) ?: return lookupElement
            val targetModule = type.scoutJsModule
            val originalHandler = lookupElement.insertHandler

            return lookupElement.withInsertHandler { context, item ->
                originalHandler?.handleInsert(context, item)
                val moduleOrNamespaceName = if (thisModule == targetModule) {
                    val relPath = VfsUtilCore.findRelativePath(containingFile, thisModule.mainFile, VfsUtilCore.VFS_SEPARATOR_CHAR) ?: targetModule.name
                    relPath.removeSuffix(IWebConstants.JS_FILE_SUFFIX)
                } else {
                    targetModule.name
                }
                var importName = type.name
                val firstDot = importName.indexOf('.')
                if (firstDot > 0) importName = importName.take(firstDot)
                val info = ES6ImportPsiUtil.CreateImportExportInfo(importName, ES6ImportPsiUtil.ImportExportType.SPECIFIER)
                ES6AddImportExecutor(context.editor, place).createImportOrUseExisting(info, null, Strings.toStringLiteral(moduleOrNamespaceName).toString())
            }
        }
    }
}