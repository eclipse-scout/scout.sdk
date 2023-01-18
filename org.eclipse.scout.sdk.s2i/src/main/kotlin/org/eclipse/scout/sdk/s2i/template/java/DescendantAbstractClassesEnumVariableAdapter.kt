/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2i.template.java

import org.eclipse.scout.sdk.core.java.JavaUtils
import org.eclipse.scout.sdk.core.util.Ensure.newFail
import org.eclipse.scout.sdk.s2i.template.VariableDescriptor

open class DescendantAbstractClassesEnumVariableAdapter(val name: String) : (TemplateEngine) -> VariableDescriptor? {

    override fun invoke(context: TemplateEngine): VariableDescriptor? {
        val superClassInfo = context.templateDescriptor.superClassInfo() ?: throw newFail("No super class info specified for variable '{}'.", name)
        val baseFqn = JavaUtils.toStringLiteral(superClassInfo.baseFqn)
        val defaultValue = JavaUtils.toStringLiteral(superClassInfo.defaultValue)
        return VariableDescriptor(name, "${DescendantAbstractClassesEnumMacro.NAME}($baseFqn, $defaultValue)", defaultValue.toString())
    }
}