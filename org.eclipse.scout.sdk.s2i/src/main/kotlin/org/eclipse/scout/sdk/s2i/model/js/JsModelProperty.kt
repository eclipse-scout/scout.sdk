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

import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.impl.JSPsiElementFactory

/**
 * Represents a property of a [JsModelElement]
 */
class JsModelProperty(name: String, scoutJsModule: JsModule, val dataType: JsPropertyDataType, val isArray: Boolean) : JsModelElement(name, emptyList(), scoutJsModule) {

    companion object {

        private val ENUM_REGEX = "([\\w\\.]+)\\.[A-Z_]+".toRegex()
        private val CONSTANT_REGEX = "[A-Z_]+".toRegex()

        fun parse(property: JSAssignmentExpression, scoutJsModule: JsModule, propertyTypesByName: List<JsModelClass.JsModelPropertyRecorder>): JsModelProperty? {
            val lhs = property.definitionExpression?.expression as? JSReferenceExpression ?: return null
            val rhs = property.rOperand ?: return null
            if (lhs.qualifier !is JSThisExpression) return null
            val name = lhs.referenceName ?: return null
            if (isPrivateOrJQueryLikeName(name)) return null
            if (name.contains('.')) return null // complex fields or expressions
            if (name.matches(CONSTANT_REGEX)) return null // constants

            val isArray = rhs is JSArrayLiteralExpression
            val propertyTypedByName = propertyTypesByName
                    .mapNotNull { it.use(name, isArray) }
                    .firstOrNull()
            if (propertyTypedByName != null) {
                return propertyTypedByName
            }

            val elementToDetectDataType = if (rhs is JSArrayLiteralExpression) rhs.expressions.firstOrNull() else rhs
            val dataType = elementToDetectDataType?.let { parseDataType(it, scoutJsModule) }
            return JsModelProperty(name, scoutJsModule, dataType ?: JsPropertyDataType.UNKNOWN, isArray)
        }

        private fun parseDataType(valueExpression: JSExpression, scoutJsModule: JsModule): JsPropertyDataType? {
            if (valueExpression is JSLiteralExpression) {
                return parseDataTypeFromLiteral(valueExpression)
            }

            if (valueExpression is JSReferenceExpression) {
                val literal = resolveReferenceToLiteral(valueExpression)
                if (literal != null) {
                    val dataTypeFromConstant = parseDataTypeFromLiteral(literal)
                    if (dataTypeFromConstant != JsPropertyDataType.UNKNOWN) {
                        return dataTypeFromConstant
                    }
                }

                val matchResult = ENUM_REGEX.matchEntire(valueExpression.text)
                if (matchResult != null) {
                    // match enum type
                    return JsPropertyDataType(scoutJsModule.namespace + '.' + matchResult.groupValues[1])
                }
            }

            return null // cannot parse
        }

        private fun resolveReferenceToLiteral(referenceExpression: JSReferenceExpression): JSLiteralExpression? {
            var ref: JSReferenceExpression? = referenceExpression
            var literal: JSLiteralExpression? = null
            while (ref != null && literal == null) {
                val refText = ref.text
                // treat these JS constants as the corresponding number literal (to detect it is number type)
                if ("Number.MIN_SAFE_INTEGER" == refText || "Number.MAX_SAFE_INTEGER" == ref.text) {
                    return JSPsiElementFactory.createJSExpression("9007199254740991", ref) as? JSLiteralExpression
                }
                val refToField = ref.resolve() as? JSField
                var initializer = refToField?.initializer
                if (initializer is JSPrefixExpression) initializer = initializer.expression
                when (initializer) {
                    is JSLiteralExpression -> literal = initializer // stop
                    is JSReferenceExpression -> ref = initializer // resolve next reference
                    else -> ref = null // stop
                }
            }
            return literal
        }

        private fun parseDataTypeFromLiteral(literalExpression: JSLiteralExpression) = when {
            literalExpression.isBooleanLiteral -> JsPropertyDataType.BOOL
            literalExpression.isStringLiteral -> JsPropertyDataType.STRING
            literalExpression.isNumericLiteral -> JsPropertyDataType.NUMERIC
            literalExpression.isBigInteger -> JsPropertyDataType.BIG_INTEGER
            literalExpression.isNullLiteral -> JsPropertyDataType.OBJECT
            else -> JsPropertyDataType.UNKNOWN
        }
    }

    data class JsPropertyDataType(val type: String) {
        companion object {
            val BOOL = JsPropertyDataType("boolean")
            val BIG_INTEGER = JsPropertyDataType("big_integer")
            val NUMERIC = JsPropertyDataType("numeric")
            val STRING = JsPropertyDataType("string")
            val WIDGET = JsPropertyDataType("widget")
            val TEXT_KEY = JsPropertyDataType("text-key")
            val OBJECT = JsPropertyDataType("object")
            val UNKNOWN = JsPropertyDataType("unknown")
        }

        fun isCustomType() =
                this != BOOL
                        && this != BIG_INTEGER
                        && this != NUMERIC
                        && this != STRING
                        && this != WIDGET
                        && this != TEXT_KEY
                        && this != OBJECT
                        && this != UNKNOWN
    }

    override fun toString(): String {
        return name + "=" + dataType.type + if (isArray) "[]" else ""
    }
}