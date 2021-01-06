/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.s2i.model.js

import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.impl.JSPsiElementFactory

/**
 * Represents a property of a [AbstractJsModelElement]
 */
class JsModelProperty(name: String, val owner: AbstractJsModelElement, val dataType: JsPropertyDataType, val isArray: Boolean) : AbstractJsModelElement(name, owner.scoutJsModule) {

    init {
        properties = emptyList()
    }

    companion object {

        private val ENUM_REGEX = "([\\w\\.]+)\\.[A-Z_]+".toRegex()
        private val CONSTANT_REGEX = "[A-Z_]+".toRegex()
        private const val WIDGET_QUALIFIED_CLASS_NAME = "${JsModel.SCOUT_JS_NAMESPACE}.${JsModel.WIDGET_CLASS_NAME}"
        private const val GROUP_BOX_QUALIFIED_CLASS_NAME = JsModel.SCOUT_JS_NAMESPACE + ".GroupBox"
        private const val TAG_FIELD_QUALIFIED_CLASS_NAME = JsModel.SCOUT_JS_NAMESPACE + ".TagField"
        private const val SMART_FIELD_QUALIFIED_CLASS_NAME = JsModel.SCOUT_JS_NAMESPACE + ".SmartField"
        private const val TABLE_QUALIFIED_CLASS_NAME = JsModel.SCOUT_JS_NAMESPACE + ".Table"
        private const val TREE_QUALIFIED_CLASS_NAME = JsModel.SCOUT_JS_NAMESPACE + ".Tree"
        private const val TILE_GRID_QUALIFIED_CLASS_NAME = JsModel.SCOUT_JS_NAMESPACE + ".TileGrid"
        private const val BUTTON_QUALIFIED_CLASS_NAME = JsModel.SCOUT_JS_NAMESPACE + ".Button"
        private const val DATE_FIELD_QUALIFIED_CLASS_NAME = JsModel.SCOUT_JS_NAMESPACE + ".DateField"
        private const val MENU_QUALIFIED_CLASS_NAME = JsModel.SCOUT_JS_NAMESPACE + ".Menu"
        private const val VIEW_MENU_TAB_QUALIFIED_CLASS_NAME = JsModel.SCOUT_JS_NAMESPACE + ".ViewMenuTab"

        private val EXCLUDED_PROPERTIES = setOf(
                WIDGET_QUALIFIED_CLASS_NAME to "enabledComputed",
                WIDGET_QUALIFIED_CLASS_NAME to "events",
                WIDGET_QUALIFIED_CLASS_NAME to "attached",
                WIDGET_QUALIFIED_CLASS_NAME to "children",
                WIDGET_QUALIFIED_CLASS_NAME to "cloneOf",
                WIDGET_QUALIFIED_CLASS_NAME to "destroyed",
                WIDGET_QUALIFIED_CLASS_NAME to "destroying",
                WIDGET_QUALIFIED_CLASS_NAME to "eventDelegators",
                WIDGET_QUALIFIED_CLASS_NAME to "htmlComp",
                WIDGET_QUALIFIED_CLASS_NAME to "initialized",
                WIDGET_QUALIFIED_CLASS_NAME to "removalPending",
                WIDGET_QUALIFIED_CLASS_NAME to "removing",
                WIDGET_QUALIFIED_CLASS_NAME to "rendered",
                WIDGET_QUALIFIED_CLASS_NAME to "rendering",
                GROUP_BOX_QUALIFIED_CLASS_NAME to "controls",
                GROUP_BOX_QUALIFIED_CLASS_NAME to "processButtons",
                GROUP_BOX_QUALIFIED_CLASS_NAME to "processMenus",
                GROUP_BOX_QUALIFIED_CLASS_NAME to "systemButtons",
                TAG_FIELD_QUALIFIED_CLASS_NAME to "fieldHtmlComp",
                TAG_FIELD_QUALIFIED_CLASS_NAME to "popup",
                SMART_FIELD_QUALIFIED_CLASS_NAME to "lookupSeqNo",
                SMART_FIELD_QUALIFIED_CLASS_NAME to "popup",
                TABLE_QUALIFIED_CLASS_NAME to "rootRows",
                TABLE_QUALIFIED_CLASS_NAME to "rowBorderLeftWidth",
                TABLE_QUALIFIED_CLASS_NAME to "rowBorderRightWidth",
                TABLE_QUALIFIED_CLASS_NAME to "rowBorderWidth",
                TABLE_QUALIFIED_CLASS_NAME to "rowHeight",
                TABLE_QUALIFIED_CLASS_NAME to "rowsMap",
                TABLE_QUALIFIED_CLASS_NAME to "rowWidth",
                TABLE_QUALIFIED_CLASS_NAME to "visibleRows",
                TABLE_QUALIFIED_CLASS_NAME to "visibleRowsMap",
                TABLE_QUALIFIED_CLASS_NAME to "columnLayoutDirty",
                TABLE_QUALIFIED_CLASS_NAME to "viewRangeDirty",
                TABLE_QUALIFIED_CLASS_NAME to "viewRangeRendered",
                TABLE_QUALIFIED_CLASS_NAME to "contextMenu",
                TREE_QUALIFIED_CLASS_NAME to "groupedNodes",
                TREE_QUALIFIED_CLASS_NAME to "runningAnimationsFinishFunc",
                TREE_QUALIFIED_CLASS_NAME to "startAnimationFunc",
                TREE_QUALIFIED_CLASS_NAME to "visibleNodesFlat",
                TREE_QUALIFIED_CLASS_NAME to "visibleNodesMap",
                TREE_QUALIFIED_CLASS_NAME to "maxNodeWidth",
                TREE_QUALIFIED_CLASS_NAME to "nodeHeight",
                TREE_QUALIFIED_CLASS_NAME to "nodesMap",
                TREE_QUALIFIED_CLASS_NAME to "nodeWidth",
                TREE_QUALIFIED_CLASS_NAME to "nodeWidthDirty",
                TREE_QUALIFIED_CLASS_NAME to "viewRangeDirty",
                TREE_QUALIFIED_CLASS_NAME to "viewRangeRendered",
                TREE_QUALIFIED_CLASS_NAME to "contextMenu",
                TILE_GRID_QUALIFIED_CLASS_NAME to "tileRemovalPendingCount",
                TILE_GRID_QUALIFIED_CLASS_NAME to "filteredTiles",
                TILE_GRID_QUALIFIED_CLASS_NAME to "filteredTilesDirty",
                TILE_GRID_QUALIFIED_CLASS_NAME to "viewRangeRendered",
                TILE_GRID_QUALIFIED_CLASS_NAME to "contextMenu",
                BUTTON_QUALIFIED_CLASS_NAME to "popup",
                DATE_FIELD_QUALIFIED_CLASS_NAME to "popup",
                MENU_QUALIFIED_CLASS_NAME to "popup",
                VIEW_MENU_TAB_QUALIFIED_CLASS_NAME to "popup"
        )

        fun parse(property: JSAssignmentExpression, owner: AbstractJsModelElement, propertyTypesByName: List<JsModelClass.JsModelPropertyRecorder>): JsModelProperty? {
            val lhs = property.definitionExpression?.expression as? JSReferenceExpression ?: return null
            val rhs = property.rOperand ?: return null
            if (lhs.qualifier !is JSThisExpression) return null
            val name = lhs.referenceName ?: return null

            if (isPrivateOrJQueryLikeName(name)) return null
            if (name.contains('.')) return null // complex fields or expressions
            if (isInternalProperty(owner.qualifiedName(), name)) return null // internal properties which have no "private" marker prefix ("_")
            if (name.matches(CONSTANT_REGEX)) return null // constants

            val isArray = rhs is JSArrayLiteralExpression
            val propertyTypedByName = propertyTypesByName
                    .mapNotNull { it.use(name, isArray) }
                    .firstOrNull()
            if (propertyTypedByName != null) {
                return propertyTypedByName
            }

            val elementToDetectDataType = if (rhs is JSArrayLiteralExpression) rhs.expressions.firstOrNull() else rhs
            val dataType = elementToDetectDataType?.let { parseDataType(it, owner.scoutJsModule) }
            return JsModelProperty(name, owner, dataType ?: JsPropertyDataType.UNKNOWN, isArray)
        }

        fun isInternalProperty(ownerFqn: String, propertyName: String) = EXCLUDED_PROPERTIES.contains(ownerFqn to propertyName)

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