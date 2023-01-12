/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2i.model.js

import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.lang.javascript.psi.JSVariable
import com.intellij.lang.javascript.psi.ecmal4.JSClass

/**
 * Represents a top-level or nested enumeration.
 */
class JsModelEnum(name: String, scoutJsModule: JsModule) : AbstractJsModelElement(name, scoutJsModule) {

    companion object {
        fun parse(variable: JSVariable, declaringClass: JSClass?, scoutJsModule: JsModule): JsModelEnum? {
            val enumName = variable.name ?: return null
            if (isPrivateOrJQueryLikeName(enumName)) return null
            val enum = variable.initializer as? JSObjectLiteralExpression ?: return null
            val name = declaringClass?.name?.let { "$it.$enumName" } ?: enumName
            val result = JsModelEnum(name, scoutJsModule)
            result.properties = enum.properties
                    .mapNotNull { it.name }
                    .map { JsModelProperty(it, result, JsModelProperty.JsPropertyDataType.UNKNOWN, false) }
            return result
        }
    }
}