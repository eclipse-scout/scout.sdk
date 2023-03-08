/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2i.model.typescript.util

import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.types.JSRecursiveTypeVisitor

object JSTypeUtils {

    fun <T : JSType> firstChildOfType(startType: JSType, t: Class<T>): T? {
        var result: T? = null
        val visitor: JSRecursiveTypeVisitor = object : JSRecursiveTypeVisitor(false) {
            override fun visitJSType(type: JSType) {
                if (result == null && t.isAssignableFrom(type::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    result = type as T
                }
                if (result == null) {
                    super.visitJSType(type)
                }
            }
        }
        visitor.visitJSType(startType)
        return result
    }
}