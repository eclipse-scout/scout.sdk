/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.s2i.model.js

import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.lang.javascript.psi.JSVariable
import com.intellij.lang.javascript.psi.ecmal4.JSClass

/**
 * Represents a top-level or nested enumeration.
 */
class JsModelEnum(name: String, properties: List<JsModelProperty>, scoutJsModule: JsModule) : JsModelElement(name, properties, scoutJsModule) {

    companion object {
        fun parse(variable: JSVariable, declaringClass: JSClass?, scoutJsModule: JsModule): JsModelEnum? {
            val enumName = variable.name ?: return null
            if (isPrivateOrJQueryLikeName(enumName)) return null
            val enum = variable.initializer as? JSObjectLiteralExpression ?: return null
            val values = enum.properties
                    .mapNotNull { it.name }
                    .map { JsModelProperty(it, scoutJsModule, JsModelProperty.JsPropertyDataType.UNKNOWN, false) }
            val name = declaringClass?.name?.let { "$it.$enumName" } ?: enumName
            return JsModelEnum(name, values, scoutJsModule)
        }
    }
}