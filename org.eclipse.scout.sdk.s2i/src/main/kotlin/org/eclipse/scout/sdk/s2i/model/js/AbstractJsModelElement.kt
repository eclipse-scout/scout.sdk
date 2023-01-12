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

/**
 * Base class for JavaScript model elements
 */
abstract class AbstractJsModelElement(val name: String, val scoutJsModule: JsModule) {

    /**
     * All properties of this element
     */
    lateinit var properties: List<JsModelProperty>

    companion object {
        /**
         * @param name The name to check
         * @return true if it is a private or jQuery name
         */
        fun isPrivateOrJQueryLikeName(name: String) = name.startsWith('$') || name.startsWith('_')
    }

    /**
     * @return The element name including a namespace prefix (e.g. scout.GroupBox).
     */
    fun qualifiedName() = scoutJsModule.namespace + '.' + name

    /**
     * @return The short name is [AbstractJsModelElement.qualifiedName] for custom namespaces and the simple [AbstractJsModelElement.name] for the 'scout' namespace. E.g. 'helloworld.Person' but only 'GroupBox' for 'scout.GroupBox'.
     */
    fun shortName() = if (scoutJsModule.namespace == JsModel.SCOUT_JS_NAMESPACE) name else qualifiedName()

    override fun toString(): String {
        return javaClass.simpleName + " $name" + (if (properties.isEmpty()) "" else " $properties")
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AbstractJsModelElement
        if (name != other.name) return false
        if (scoutJsModule != other.scoutJsModule) return false
        return properties == other.properties
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + scoutJsModule.hashCode()
        result = 31 * result + properties.hashCode()
        return result
    }
}