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
package org.eclipse.scout.sdk.s2i.model.js

/**
 * Base class for JavaScript model elements
 */
open class JsModelElement(val name: String, val properties: List<JsModelProperty>, val scoutJsModule: JsModule) {

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
     * @return The short name is [JsModelElement.qualifiedName] for custom namespaces and the simple [JsModelElement.name] for the 'scout' namespace. E.g. 'helloworld.Person' but only 'GroupBox' for 'scout.GroupBox'.
     */
    fun shortName() = if (scoutJsModule.namespace == JsModel.DEFAULT_SCOUT_JS_NAMESPACE) name else qualifiedName()

    override fun toString(): String {
        return javaClass.simpleName + " $name" + (if (properties.isEmpty()) "" else " $properties")
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as JsModelElement
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