/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2i.template

import org.eclipse.scout.sdk.core.util.Strings

open class EnumVariableAdapter<C>(val name: String, val enumMacroName: CharSequence? = null, val optionsSupplier: (C) -> Iterable<CharSequence>) : (C) -> VariableDescriptor? {

    constructor(name: String, enumMacroName: CharSequence?, options: Iterable<CharSequence>) : this(name, enumMacroName, { options })

    override fun invoke(context: C): VariableDescriptor {
        val options = optionsSupplier(context)
        val defaultValue = Strings.toStringLiteral(options.firstOrNull())?.toString() ?: ""
        return VariableDescriptor(name, toEnum(options, enumMacroName ?: "enum"), defaultValue)
    }

    protected fun toEnum(options: Iterable<CharSequence>, enumMacroName: CharSequence) = options.joinToString(", ", "$enumMacroName(", ")") { Strings.toStringLiteral(it) }
}