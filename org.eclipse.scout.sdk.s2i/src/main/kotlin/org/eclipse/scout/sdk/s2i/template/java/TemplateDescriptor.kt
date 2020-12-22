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
package org.eclipse.scout.sdk.s2i.template.java

import com.intellij.codeInsight.template.impl.TemplateImpl
import org.eclipse.scout.sdk.core.util.Ensure
import org.eclipse.scout.sdk.core.util.Ensure.newFail
import org.eclipse.scout.sdk.core.util.FinalValue
import org.eclipse.scout.sdk.core.util.Strings
import org.eclipse.scout.sdk.core.util.Strings.toStringLiteral
import org.eclipse.scout.sdk.s2i.EclipseScoutBundle.message
import org.eclipse.scout.sdk.s2i.nls.NlsKeysEnumMacro
import org.eclipse.scout.sdk.s2i.template.BoolVariableAdapter
import org.eclipse.scout.sdk.s2i.template.EnumVariableAdapter
import org.eclipse.scout.sdk.s2i.template.VariableDescriptor

class TemplateDescriptor(val id: String, private val resourceLoader: ClassLoader = TemplateDescriptor::class.java.classLoader) {

    companion object {
        const val VARIABLE_NAME = "name"

        const val PREDEFINED_VARIABLE_SUPER = "super"
        const val PREDEFINED_VARIABLE_MENU_TYPES = "menuTypes"
        const val PREDEFINED_VARIABLE_KEYSTROKES = "keystrokes"
        const val PREDEFINED_VARIABLE_COMPLETE = "complete"

        const val PREDEFINED_CONSTANT_IN_EXTENSION = "inExtension"
        const val PREDEFINED_CONSTANT_MENU_SUPPORTED = "menuSupported"
    }

    private var m_name: String? = null
    private var m_description: String? = null
    private var m_superClassInfo: SuperClassInfo? = null
    private var m_orderDefinitionType: String? = null
    private val m_source = FinalValue<String>()
    private var m_innerTypeGetterInfos = LinkedHashSet<InnerTypeGetterInfo>()
    private val m_variables = HashMap<String, (TemplateEngine) -> VariableDescriptor?>()
    private val m_alias = HashSet<String>()

    init {
        // append predefined variables
        withVariable(PREDEFINED_VARIABLE_MENU_TYPES, EnumVariableAdapter(PREDEFINED_VARIABLE_MENU_TYPES, PsiExpressionEnumMacro.NAME) {
            it.menuTypes.map { candidate -> "${it.context.scoutApi.CollectionUtility().fqn()}.${it.context.scoutApi.CollectionUtility().hashSetMethodName()}($candidate)" }
        })
        withVariable(PREDEFINED_VARIABLE_KEYSTROKES, EnumVariableAdapter(PREDEFINED_VARIABLE_KEYSTROKES, PsiExpressionEnumMacro.NAME) {
            it.keyStrokes
        })
        withVariable(PREDEFINED_VARIABLE_SUPER, DescendantAbstractClassesEnumVariableAdapter(PREDEFINED_VARIABLE_SUPER))
        withVariable(PREDEFINED_VARIABLE_COMPLETE) { VariableDescriptor(PREDEFINED_VARIABLE_COMPLETE, "complete()") }
    }

    constructor(original: TemplateDescriptor) : this(original.id, original.resourceLoader) {
        m_name = original.m_name
        m_description = original.m_description
        m_superClassInfo = original.m_superClassInfo
        m_orderDefinitionType = original.m_orderDefinitionType
        m_innerTypeGetterInfos.addAll(original.m_innerTypeGetterInfos)
        m_variables.putAll(original.m_variables)
        m_alias.addAll(original.m_alias)
    }

    fun withName(name: String) = apply { m_name = name }
    fun name() = m_name ?: ""

    fun withDescription(description: String) = apply { m_description = description }
    fun description() = m_description ?: message("template.desc", name())

    fun withEnumVariable(name: String, values: Iterable<String>, macroName: String? = null) = withVariable(name, EnumVariableAdapter(name, macroName, values))
    fun withBoolVariable(name: String, defaultValue: Boolean) = withVariable(name, BoolVariableAdapter(name, defaultValue.toString()))

    fun withNlsVariable(name: String, defaultValue: String? = null) = withVariable(name) { VariableDescriptor(name, "${NlsKeysEnumMacro.NAME}()", toStringLiteral(defaultValue)?.toString()) }
    fun withVariable(name: String, value: String) = withVariable(name) { VariableDescriptor(name, null, toStringLiteral(value)?.toString()) }
    fun withVariable(name: String, variableAdapter: (TemplateEngine) -> VariableDescriptor?) = apply {
        Ensure.isFalse(TemplateImpl.INTERNAL_VARS_SET.contains(name), "Variable name '{}' is reserved for internal use.", name)
        m_variables[name] = variableAdapter
    }

    fun variable(name: String) = m_variables[name]

    fun withAliasName(alias: String) = apply { m_alias.add(alias) }
    fun withAliasNames(vararg aliases: String) = apply { aliases.filter { Strings.hasText(it) }.forEach { withAliasName(it) } }
    fun aliasNames(): Set<String> = m_alias

    fun withSuperClassInfo(baseFqn: String, defaultValue: String) = apply { m_superClassInfo = SuperClassInfo(baseFqn, defaultValue) }
    fun superClassInfo() = m_superClassInfo

    fun withOrderDefinitionType(fqn: String) = apply { m_orderDefinitionType = fqn }
    fun orderDefinitionType() = m_orderDefinitionType

    fun withInnerTypeGetterInfo(containerFqn: String, methodName: String, lookupType: InnerTypeGetterLookupType = InnerTypeGetterLookupType.CLOSEST) = apply { m_innerTypeGetterInfos.add(InnerTypeGetterInfo(containerFqn, methodName, lookupType)) }
    fun innerTypeGetterInfos(): Set<InnerTypeGetterInfo> = m_innerTypeGetterInfos

    fun source(): String = m_source.computeIfAbsentAndGet {
        val templatePath = id.replace('.', '/') + ".txt"
        resourceLoader.getResource(templatePath)?.readText() ?: throw newFail("Template source could not be found on classpath '{}' in classloader '{}'.", templatePath, resourceLoader)
    }

    fun copy() = TemplateDescriptor(this)

    data class SuperClassInfo(val baseFqn: String, val defaultValue: String)

    data class InnerTypeGetterInfo(val definitionClassFqn: String, val methodName: String, val lookupType: InnerTypeGetterLookupType = InnerTypeGetterLookupType.CLOSEST)

    enum class InnerTypeGetterLookupType { CLOSEST, FARTHEST }
}
