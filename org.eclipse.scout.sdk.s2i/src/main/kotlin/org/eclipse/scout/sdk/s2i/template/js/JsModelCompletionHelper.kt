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
package org.eclipse.scout.sdk.s2i.template.js

import com.intellij.codeInsight.completion.CodeCompletionHandlerBase
import com.intellij.codeInsight.completion.CompletionInitializationContext
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.codeInsight.template.Template
import com.intellij.codeInsight.template.TemplateManager
import com.intellij.codeInsight.template.impl.TemplateImpl
import com.intellij.codeInsight.template.impl.TemplateImplUtil
import com.intellij.icons.AllIcons
import com.intellij.lang.javascript.JSStubElementTypes
import com.intellij.lang.javascript.patterns.JSPatterns.jsProperty
import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.lang.javascript.psi.JSProperty
import com.intellij.openapi.command.WriteCommandAction.writeCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.module.Module
import com.intellij.openapi.util.Key
import com.intellij.patterns.PlatformPatterns.psiElement
import com.intellij.patterns.StandardPatterns.or
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import com.intellij.util.ThrowableRunnable
import icons.JavaScriptPsiIcons
import org.eclipse.scout.sdk.core.s.nls.query.TranslationPatterns
import org.eclipse.scout.sdk.core.util.Strings
import org.eclipse.scout.sdk.s2i.containingModule
import org.eclipse.scout.sdk.s2i.model.js.AbstractJsModelElement
import org.eclipse.scout.sdk.s2i.model.js.JsModel
import org.eclipse.scout.sdk.s2i.model.js.JsModelClass
import org.eclipse.scout.sdk.s2i.model.js.JsModelProperty
import org.eclipse.scout.sdk.s2i.template.BoolVariableAdapter
import org.eclipse.scout.sdk.s2i.template.TemplateHelper
import org.eclipse.scout.sdk.s2i.template.VariableDescriptor
import javax.swing.Icon

object JsModelCompletionHelper {

    const val ID_DEFAULT_TEXT = "MyId"
    val SELECTED_ELEMENT = Key.create<AbstractJsModelElement?>("ScoutLookupJsModelElement")

    val BOOL_VARIABLE = BoolVariableAdapter<Unit?>("BOOL", false.toString()).invoke(null)
    val TEXT_VARIABLE = VariableDescriptor("TXT", null)
    val COMPLETE_VARIABLE = VariableDescriptor("COMP", "complete()")
    val ALL_VARIABLES = listOf(BOOL_VARIABLE, TEXT_VARIABLE, COMPLETE_VARIABLE)
            .map { it.name to it }
            .toMap()

    const val END_VARIABLE_SRC = "\$${TemplateImpl.END}$"
    val BOOL_VARIABLE_SRC = "\$${BOOL_VARIABLE.name}$"
    val TEXT_VARIABLE_SRC = "\$${TEXT_VARIABLE.name}$"
    val COMPLETE_VARIABLE_SRC = "\$${COMPLETE_VARIABLE.name}$"

    fun propertyElementPattern() = or(
            psiElement().withSuperParent(2, jsProperty()),
            psiElement().withParent(jsProperty()))

    fun getPropertyNameInfo(parameters: CompletionParameters, result: CompletionResultSet) = getPropertyInfo(parameters.position.parent, result.prefixMatcher.prefix, true)

    fun getPropertyValueInfo(parameters: CompletionParameters, result: CompletionResultSet) = getPropertyInfo(parameters.position.parent, result.prefixMatcher.prefix, false)

    fun getPropertyValueInfo(element: PsiElement, prefix: String) = getPropertyInfo(element, prefix, false)

    private fun getPropertyInfo(element: PsiElement, prefix: String, requireNameInfo: Boolean): PropertyCompletionInfo? {
        val property = PsiTreeUtil.getParentOfType(element, JSProperty::class.java) ?: return null
        var propertyName = property.name ?: return null
        val isPropertyNameCompletion = propertyName.endsWith(CompletionInitializationContext.DUMMY_IDENTIFIER_TRIMMED)
        if (requireNameInfo != isPropertyNameCompletion) return null // early abort if not requested type
        val objectLiteral = PsiTreeUtil.getParentOfType(property, JSObjectLiteralExpression::class.java) ?: return null
        val module = property.containingModule() ?: return null

        if (isPropertyNameCompletion && propertyName.length == CompletionInitializationContext.DUMMY_IDENTIFIER_TRIMMED.length) {
            PsiTreeUtil.getParentOfType(property, JSProperty::class.java)
                    ?.name
                    ?.let { propertyName = it }
        } else if (isPropertyNameCompletion) {
            propertyName = propertyName.take(propertyName.length - CompletionInitializationContext.DUMMY_IDENTIFIER_TRIMMED.length)
        }
        val usedPropertyNames = objectLiteral.properties.mapNotNull { it.name }.toSet()
        val contextElement = if (isPropertyNameCompletion) element.parent else element // on property completion there is one more element (a jsProperty with DUMMY_IDENTIFIER name)
        val contextParent = contextElement.parent
        val isInArray = contextParent.elementType == JSStubElementTypes.ARRAY_LITERAL_EXPRESSION
        val siblings = contextParent.children
        val isLast = siblings.indexOf(contextElement) == siblings.size - 1
        return PropertyCompletionInfo(property, propertyName, module, isLast, isPropertyNameCompletion,
                findObjectType(objectLiteral), usedPropertyNames, prefix, isInArray)
    }

    private fun findObjectType(objectLiteral: JSObjectLiteralExpression): String? {
        val objectTypeProperty = objectLiteral.findProperty(JsModel.OBJECT_TYPE_PROPERTY_NAME) ?: return null
        val literal = objectTypeProperty.value as? JSLiteralExpression ?: return null
        val objectType = literal.takeIf { it.isStringLiteral }?.stringValue ?: return null
        return if (Strings.isBlank(objectType)) null else objectType
    }

    fun createLookupElement(elementText: String, element: AbstractJsModelElement?, property: JsModelProperty, completionInfo: PropertyCompletionInfo): LookupElementBuilder {
        val icon = when (element) {
            is JsModelClass -> JavaScriptPsiIcons.Classes.JavaScriptClass
            is JsModelProperty -> when (element.dataType) {
                JsModelProperty.JsPropertyDataType.BOOL,
                JsModelProperty.JsPropertyDataType.BIG_INTEGER,
                JsModelProperty.JsPropertyDataType.NUMERIC,
                JsModelProperty.JsPropertyDataType.TEXT_KEY,
                JsModelProperty.JsPropertyDataType.UNKNOWN,
                JsModelProperty.JsPropertyDataType.OBJECT,
                JsModelProperty.JsPropertyDataType.STRING -> JavaScriptPsiIcons.Classes.Alias
                JsModelProperty.JsPropertyDataType.WIDGET -> JavaScriptPsiIcons.Classes.JavaScriptClass
                else -> AllIcons.Nodes.Enum
            }
            else -> AllIcons.Nodes.Enum
        }

        val tailText = if (completionInfo.isPropertyNameCompletion && element is JsModelProperty) element.owner.shortName() else null
        val nodeModuleName = element?.scoutJsModule?.name
        val isTemplateRequired = completionInfo.isPropertyNameCompletion || (property.dataType == JsModelProperty.JsPropertyDataType.WIDGET && completionInfo.propertyName != JsModel.OBJECT_TYPE_PROPERTY_NAME)
        return createLookupElement(elementText, element, nodeModuleName, tailText, icon, completionInfo.searchPrefix, isTemplateRequired) {
            buildPropertyTemplate(property, element, completionInfo)
        }
    }

    fun createLookupElement(name: String, modelElement: AbstractJsModelElement?, nodeModuleName: String?, tailText: String?, icon: Icon, prefix: CharSequence, isTemplateRequired: Boolean, templateProvider: () -> Template): LookupElementBuilder {
        var element = LookupElementBuilder.create(name, name)
                .withCaseSensitivity(true)
                .withPresentableText(name)
                .withTailText(tailText, true)
                .withTypeText(nodeModuleName)
                .withIcon(icon)
        tailText?.let { element = element.withTailText(" ($it)", true) }
        if (isTemplateRequired) {
            element = element.withInsertHandler { context, _ -> startTemplate(context.editor, prefix, templateProvider.invoke()) }
            element.putUserData(CodeCompletionHandlerBase.DIRECT_INSERTION, true)
        }
        element.putUserData(SELECTED_ELEMENT, modelElement)
        element.putUserData(TemplateHelper.SCOUT_LOOKUP_ELEMENT_MARKER, true) // to identify scout LookupElements. Used for testing.
        return element
    }

    private fun startTemplate(editor: Editor, prefix: CharSequence, template: Template) {
        val project = editor.project
        writeCommandAction(project).run(ThrowableRunnable<RuntimeException> {
            TemplateHelper.removePrefix(editor, prefix)
            TemplateManager.getInstance(project).startTemplate(editor, template)
        })
    }

    private fun buildPropertyTemplate(property: JsModelProperty, selectedElement: AbstractJsModelElement?, completionInfo: PropertyCompletionInfo): TemplateImpl {
        val src = StringBuilder()
        if (completionInfo.isPropertyNameCompletion) {
            src.append(property.name).append(": ")
        }

        val valueSrc = WrappingStringBuilder()
        // wrappings
        if (property.isArray && !completionInfo.isInArray) {
            valueSrc.appendWrapping("[", "]")
        }
        val stringLiteralDelimiter = "'"
        when (property.dataType) {
            JsModelProperty.JsPropertyDataType.STRING -> valueSrc.appendWrapping(stringLiteralDelimiter, stringLiteralDelimiter)
            JsModelProperty.JsPropertyDataType.WIDGET -> valueSrc.appendWrapping("{", "}")
            JsModelProperty.JsPropertyDataType.TEXT_KEY -> valueSrc.appendWrapping(stringLiteralDelimiter + TranslationPatterns.JsonTextKeyPattern.JSON_TEXT_KEY_PREFIX,
                    TranslationPatterns.JsonTextKeyPattern.JSON_TEXT_KEY_SUFFIX + stringLiteralDelimiter)
        }
        // property value
        if (property.dataType.isCustomType()) {
            valueSrc.append(COMPLETE_VARIABLE_SRC)
        } else {
            when (property.dataType) {
                JsModelProperty.JsPropertyDataType.BOOL -> valueSrc.append(BOOL_VARIABLE_SRC)
                JsModelProperty.JsPropertyDataType.TEXT_KEY -> valueSrc.append(COMPLETE_VARIABLE_SRC)
                JsModelProperty.JsPropertyDataType.BIG_INTEGER,
                JsModelProperty.JsPropertyDataType.STRING,
                JsModelProperty.JsPropertyDataType.OBJECT,
                JsModelProperty.JsPropertyDataType.UNKNOWN,
                JsModelProperty.JsPropertyDataType.NUMERIC -> valueSrc.append(TEXT_VARIABLE_SRC)
                JsModelProperty.JsPropertyDataType.WIDGET -> {
                    val objectType = if (property.scoutJsModule.jsModel.isWidget(selectedElement)) selectedElement?.shortName() else null ?: COMPLETE_VARIABLE_SRC
                    valueSrc.append("\n${JsModel.ID_PROPERTY_NAME}: '${TEXT_VARIABLE_SRC}',\n${JsModel.OBJECT_TYPE_PROPERTY_NAME}: '${objectType}'${END_VARIABLE_SRC}\n")
                }
            }
        }
        src.append(valueSrc.toString())

        if (!completionInfo.isLast) {
            src.append(',')
        }
        if (!src.contains(END_VARIABLE_SRC)) {
            src.append(END_VARIABLE_SRC)
        }

        return buildTemplate("Scout.jsModel.property.${property.name}", src.toString()) {
            if (it == TEXT_VARIABLE.name && property.dataType == JsModelProperty.JsPropertyDataType.WIDGET) ID_DEFAULT_TEXT else ""
        }
    }

    fun buildTemplate(id: String, source: String, defaultValueProvider: (String) -> String): TemplateImpl {
        val template = TemplateImpl(id, source, "Scout")
        template.id = id
        template.isToShortenLongNames = true
        template.isToReformat = true
        template.isDeactivated = false
        template.isToIndent = true
        template.setValue(Template.Property.USE_STATIC_IMPORT_IF_POSSIBLE, false)
        template.parseSegments()
        TemplateImplUtil.parseVariableNames(source)
                .mapNotNull { ALL_VARIABLES[it] }
                .forEach { addVariable(it, template, defaultValueProvider) }
        return template
    }

    private fun addVariable(descriptor: VariableDescriptor, target: TemplateImpl, defaultValueProvider: (String) -> String) {
        val name = descriptor.name
        // com.intellij.codeInsight.Template internal variables (like "END")
        if (TemplateImpl.INTERNAL_VARS_SET.contains(name)) {
            return
        }
        val defaultValue = Strings.toStringLiteral(defaultValueProvider.invoke(name)).toString()
        target.addVariable(name, descriptor.expression, defaultValue, true)
    }

    data class PropertyCompletionInfo(val property: JSProperty, val propertyName: String, val module: Module, val isLast: Boolean, val isPropertyNameCompletion: Boolean,
                                      val objectType: String?, val siblingPropertyNames: Set<String>, val searchPrefix: String, val isInArray: Boolean)

    private class WrappingStringBuilder {
        private val m_builder = StringBuilder()
        private val m_wrappings = ArrayList<Pair<String, String>>()

        fun append(s: String) = apply { m_builder.append(s) }

        fun appendWrapping(prefix: String, suffix: String) = apply { m_wrappings.add(prefix to suffix) }

        override fun toString(): String {
            val s = StringBuilder()
            m_wrappings.map { it.first }.forEach { s.append(it) }
            s.append(m_builder)
            m_wrappings.reversed().map { it.second }.forEach { s.append(it) }
            return s.toString()
        }
    }
}