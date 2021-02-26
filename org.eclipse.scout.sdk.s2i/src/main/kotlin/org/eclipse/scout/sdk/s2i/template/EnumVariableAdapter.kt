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