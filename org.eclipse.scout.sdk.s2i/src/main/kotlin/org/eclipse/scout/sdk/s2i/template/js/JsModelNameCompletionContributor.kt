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
import org.eclipse.scout.sdk.core.log.SdkLog
import org.eclipse.scout.sdk.s2i.template.js.JsModelCompletionHelper.propertyElementPattern

class JsModelNameCompletionContributor : CompletionContributor() {

    init {
        extend(CompletionType.BASIC, propertyElementPattern(), JsModelNameCompletionProvider())
    }

    private class JsModelNameCompletionProvider : CompletionProvider<CompletionParameters>() {
        override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
            val start = System.currentTimeMillis()
            val completionInfo = JsModelCompletionHelper.getPropertyNameInfo(parameters, result) ?: return
            val properties = completionInfo.availableProperties()
                .filter { !completionInfo.siblingPropertyNames.contains(it.name()) } // these properties are already present
                .map { JsModelCompletionHelper.createPropertyNameLookupElement(it, completionInfo) }
                .toList()
            SdkLog.info("Scout JS model property name completion took {}ms", System.currentTimeMillis() - start)
            if (properties.isNotEmpty()) {
                result.addAllElements(properties)
                result.stopHere()
            }
        }

    }
}