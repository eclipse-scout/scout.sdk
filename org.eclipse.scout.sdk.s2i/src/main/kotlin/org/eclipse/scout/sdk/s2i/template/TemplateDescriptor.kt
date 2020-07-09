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
package org.eclipse.scout.sdk.s2i.template

import org.eclipse.scout.sdk.core.util.Ensure.newFail
import org.eclipse.scout.sdk.core.util.FinalValue
import org.eclipse.scout.sdk.core.util.Strings
import org.eclipse.scout.sdk.s2i.EclipseScoutBundle.Companion.message

class TemplateDescriptor(val id: String, private val resourceLoader: ClassLoader = TemplateDescriptor::class.java.classLoader) {

    companion object {
        const val VARIABLE_NAME = "name"
        const val VARIABLE_PREFIX_NLS = "nls"
        const val VARIABLE_PREFIX_BOOL = "bool"
        const val PREDEFINED_VARIABLE_KEYSTROKES = "keystrokes"
        const val PREDEFINED_VARIABLE_SUPER = "super"
        const val PREDEFINED_VARIABLE_COMPLETE = "complete"
        const val PREDEFINED_VARIABLE_CONFIGURED_MENU_TYPES = "getConfiguredMenuTypes"
        const val PREDEFINED_VARIABLE_STATIC_IN_EXTENSION = "staticIfInExtension"
    }

    private var m_name: String? = null
    private var m_description: String? = null
    private var m_superClassInfo: SuperClassInfo? = null
    private var m_orderDefinitionType: String? = null
    private var m_innerTypeGetterContainer: InnerTypeGetterInfo? = null
    private val m_source = FinalValue<String>()
    private val m_variables = HashMap<String, VariableDescriptor>()
    private val m_alias = HashSet<String>()

    constructor(original: TemplateDescriptor) : this(original.id, original.resourceLoader) {
        m_name = original.m_name
        m_description = original.m_description
        m_superClassInfo = original.m_superClassInfo
        m_orderDefinitionType = original.m_orderDefinitionType
        m_innerTypeGetterContainer = original.m_innerTypeGetterContainer
        m_variables.putAll(original.m_variables)
        m_alias.addAll(original.m_alias)
    }

    fun withName(name: String) = apply { m_name = name }
    fun name() = m_name ?: ""

    @Suppress("unused")
    fun withDescription(description: String) = apply { m_description = description }
    fun description() = m_description ?: message("template.desc", name())

    fun withVariable(name: String, vararg values: String) = withVariable(name, values.toList())
    fun withVariable(name: String, values: Collection<String>) = apply { m_variables[name] = VariableDescriptor(name, values) }
    fun variable(name: String) = m_variables[name]

    fun withAliasName(alias: String) = apply { m_alias.add(alias) }
    fun withAliasNames(vararg aliases: String) = apply { aliases.filter { Strings.hasText(it) }.forEach { withAliasName(it) } }
    fun aliasNames(): Set<String> = m_alias

    fun withSuperClassInfo(baseFqn: String, defaultValue: String) = apply { m_superClassInfo = SuperClassInfo(baseFqn, defaultValue) }
    fun superClassInfo() = m_superClassInfo

    fun withOrderDefinitionType(fqn: String) = apply { m_orderDefinitionType = fqn }
    fun orderDefinitionType() = m_orderDefinitionType

    fun withInnerTypeGetterContainer(containerFqn: String, methodName: String) = apply { m_innerTypeGetterContainer = InnerTypeGetterInfo(containerFqn, methodName) }
    fun innerTypeGetterContainer() = m_innerTypeGetterContainer

    fun source(): String = m_source.computeIfAbsentAndGet {
        val templatePath = id.replace('.', '/') + ".txt"
        return@computeIfAbsentAndGet resourceLoader.getResource(templatePath)?.readText() ?: throw newFail("Template source could not be found on classpath '{}' in classloader '{}'.", templatePath, resourceLoader)
    }

    fun copy() = TemplateDescriptor(this)

    data class VariableDescriptor(val name: String, val values: Collection<String>)

    data class SuperClassInfo(val baseFqn: String, val defaultValue: String)

    data class InnerTypeGetterInfo(val definitionClassFqn: String, val methodName: String)
}
