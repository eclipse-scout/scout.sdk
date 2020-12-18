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
import com.intellij.codeInsight.template.impl.TemplateImpl
import com.intellij.util.ProcessingContext
import icons.JavaScriptPsiIcons
import org.eclipse.scout.sdk.core.s.IWebConstants
import org.eclipse.scout.sdk.s2i.model.js.JsModel
import org.eclipse.scout.sdk.s2i.template.js.JsModelCompletionHelper.createLookupElement
import org.eclipse.scout.sdk.s2i.template.js.JsModelCompletionHelper.propertyElementPattern

class JsModelNameCompletionContributor : CompletionContributor() {

    init {
        extend(CompletionType.BASIC, propertyElementPattern(), JsModelNameCompletionProvider())
    }

    private class JsModelNameCompletionProvider : CompletionProvider<CompletionParameters>() {
        override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
            val completionInfo = JsModelCompletionHelper.getPropertyNameInfo(parameters, result) ?: return
            val elements = if (completionInfo.objectType == null) getUnknownObjectTypeElements(completionInfo) else getPropertyNameElements(completionInfo)
            if (elements.isNotEmpty()) {
                result.addAllElements(elements)
                result.stopHere()
            }
        }

        private fun getPropertyNameElements(completionInfo: JsModelCompletionHelper.PropertyCompletionInfo) = JsModel().build(completionInfo.module)
                .properties(completionInfo.objectType).values
                .filter { !completionInfo.siblingPropertyNames.contains(it.name) }
                .map { createLookupElement(it.name, it, it, completionInfo) }

        private fun getUnknownObjectTypeElements(completionInfo: JsModelCompletionHelper.PropertyCompletionInfo): List<LookupElement> {
            return listOf(JsModel.ID_PROPERTY_NAME, JsModel.OBJECT_TYPE_PROPERTY_NAME)
                    .filter { !completionInfo.siblingPropertyNames.contains(it) }
                    .map { lookupElementForUnknownObjectType(it, completionInfo) }
        }

        private fun lookupElementForUnknownObjectType(propertyName: String, completionInfo: JsModelCompletionHelper.PropertyCompletionInfo) =
                createLookupElement(propertyName, null, IWebConstants.SCOUT_JS_CORE_MODULE_NAME, JsModel.WIDGET_CLASS_NAME, JavaScriptPsiIcons.Classes.Alias, completionInfo.searchPrefix, true) {
                    buildUnknownObjectTemplate(propertyName, completionInfo)
                }

        private fun buildUnknownObjectTemplate(propertyName: String, completionInfo: JsModelCompletionHelper.PropertyCompletionInfo): TemplateImpl {
            val variable = if (JsModel.ID_PROPERTY_NAME == propertyName) JsModelCompletionHelper.TEXT_VARIABLE_SRC else JsModelCompletionHelper.COMPLETE_VARIABLE_SRC
            var source = "$propertyName: '$variable'"
            if (!completionInfo.isLast) {
                source += ','
            }
            source += JsModelCompletionHelper.END_VARIABLE_SRC
            return JsModelCompletionHelper.buildTemplate("Scout.unknownObject.$propertyName", source) {
                if (JsModel.ID_PROPERTY_NAME == it) JsModelCompletionHelper.ID_DEFAULT_TEXT else ""
            }
        }
    }
}