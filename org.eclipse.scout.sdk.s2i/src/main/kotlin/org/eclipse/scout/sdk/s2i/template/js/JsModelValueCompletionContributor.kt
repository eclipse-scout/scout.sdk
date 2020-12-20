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
import com.intellij.lang.ecmascript6.psi.impl.ES6ImportPsiUtil.CreateImportExportInfo
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
import org.eclipse.scout.sdk.s2i.util.compat.CompatibilityMethodCaller
import org.eclipse.scout.sdk.s2i.util.compat.CompatibilityMethodCaller.ResolvedMethod

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
                val executor = ES6AddImportExecutor(context.editor, place)
                createImportOrUseExisting(importName, moduleOrNamespaceName, executor)
            }
        }

        /**
         * CreateImportExportInfo creation changed with IJ 2020.3
         * Can be removed if the minimal supported IJ version is >= 2020.3
         */
        private val m_createImportOrUseExisting = CompatibilityMethodCaller<CreateImportExportInfo>()
                .withCandidate("com.intellij.lang.ecmascript6.psi.impl.ES6ImportPsiUtil\$CreateImportExportInfo", CompatibilityMethodCaller.CONSTRUCTOR_NAME,
                        String::class.java.name, "com.intellij.lang.javascript.modules.imports.JSImportExportType") {
                    val moduleOrNamespaceName = it.args[1] as String
                    // IJ 2020.3: module must not be quoted
                    createImportOrUseExisting(moduleOrNamespaceName, it)
                }
                .withCandidate(CreateImportExportInfo::class.java, CompatibilityMethodCaller.CONSTRUCTOR_NAME, String::class.java, ES6ImportPsiUtil.ImportExportType::class.java) {
                    val moduleOrNamespaceName = it.args[1] as String
                    // before IJ 2020.3: module must be quoted
                    createImportOrUseExisting(Strings.toStringLiteral(moduleOrNamespaceName).toString(), it)
                }

        private fun createImportOrUseExisting(moduleOrNamespaceName: String, constr: ResolvedMethod<CreateImportExportInfo>): CreateImportExportInfo {
            val importedName = constr.args[0] as String
            val executor = constr.args[2] as ES6AddImportExecutor
            val info = constr.invokeStatic(importedName, ES6ImportPsiUtil.ImportExportType.SPECIFIER)
            executor.createImportOrUseExisting(info, null, moduleOrNamespaceName)
            return info
        }

        fun createImportOrUseExisting(importedName: String, moduleOrNamespaceName: String, executor: ES6AddImportExecutor) = m_createImportOrUseExisting.invoke(importedName, moduleOrNamespaceName, executor)
    }
}