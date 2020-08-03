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
package org.eclipse.scout.sdk.s2i.template.variable

import org.eclipse.scout.sdk.core.util.Strings
import org.eclipse.scout.sdk.s2i.template.TemplateEngine

open class EnumVariableAdapter(val name: String, val enumMacroName: CharSequence? = null, val optionsSupplier: (TemplateEngine) -> Iterable<CharSequence>) : (TemplateEngine) -> VariableDescriptor? {

    constructor(name: String, enumMacroName: CharSequence?, options: Iterable<CharSequence>) : this(name, enumMacroName, { options })

    override fun invoke(context: TemplateEngine): VariableDescriptor? {
        val options = optionsSupplier.invoke(context)
        val defaultValue = Strings.toStringLiteral(options.firstOrNull())?.toString() ?: ""
        return VariableDescriptor(name, toEnum(options, enumMacroName ?: "enum"), defaultValue)
    }

    protected fun toEnum(options: Iterable<CharSequence>, enumMacroName: CharSequence) = options.joinToString(", ", "$enumMacroName(", ")") { Strings.toStringLiteral(it) }
}