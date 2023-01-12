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
import com.intellij.util.ProcessingContext
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
            if (completionInfo.objectType == null) return
            val elements = getPropertyNameElements(completionInfo)
            if (elements.isNotEmpty()) {
                result.addAllElements(elements)
                result.stopHere()
            }
        }

        private fun getPropertyNameElements(completionInfo: JsModelCompletionHelper.PropertyCompletionInfo) = JsModel().build(completionInfo.module)
            .properties(completionInfo.objectType).values
            .filter { !completionInfo.siblingPropertyNames.contains(it.name) }
            .map { createLookupElement(it.name, it, it, completionInfo) }
    }
}