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

import org.eclipse.scout.sdk.core.util.Ensure.newFail
import org.eclipse.scout.sdk.core.util.Strings
import org.eclipse.scout.sdk.s2i.template.DescendantAbstractClassesEnumMacro
import org.eclipse.scout.sdk.s2i.template.TemplateEngine

open class AbstractClassesEnumVariableAdapter(val name: String) : (TemplateEngine) -> VariableDescriptor? {

    override fun invoke(context: TemplateEngine): VariableDescriptor? {
        val superClassInfo = context.templateDescriptor.superClassInfo() ?: throw newFail("No super class info specified for variable '{}'.", name)
        val baseFqn = Strings.toStringLiteral(superClassInfo.baseFqn)
        val defaultValue = Strings.toStringLiteral(superClassInfo.defaultValue)
        return VariableDescriptor(name, "${DescendantAbstractClassesEnumMacro.NAME}($baseFqn, $defaultValue)", defaultValue.toString())
    }
}