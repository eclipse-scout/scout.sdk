/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
import com.intellij.openapi.util.Key
import com.intellij.patterns.PlatformPatterns.psiElement
import com.intellij.patterns.StandardPatterns.or
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import com.intellij.util.ThrowableRunnable
import icons.JavaScriptPsiIcons
import org.eclipse.scout.sdk.core.java.JavaUtils
import org.eclipse.scout.sdk.core.s.model.js.ScoutJsCoreConstants
import org.eclipse.scout.sdk.core.s.model.js.prop.*
import org.eclipse.scout.sdk.core.s.nls.query.TranslationPatterns
import org.eclipse.scout.sdk.core.typescript.model.api.IConstantValue
import org.eclipse.scout.sdk.s2i.containingModule
import org.eclipse.scout.sdk.s2i.model.js.JsModelManager
import org.eclipse.scout.sdk.s2i.template.BoolVariableAdapter
import org.eclipse.scout.sdk.s2i.template.TemplateHelper
import org.eclipse.scout.sdk.s2i.template.VariableDescriptor
import javax.swing.Icon

object JsModelCompletionHelper {

    const val ID_DEFAULT_TEXT = "MyId"
    const val STRING_DELIM = "'"
    val SELECTED_ELEMENT = Key.create<ScoutJsModelLookupElement?>("ScoutLookupJsModelElement")

    val BOOL_VARIABLE = BoolVariableAdapter<Unit?>("BOOL", false.toString()).invoke(null)
    val TEXT_VARIABLE = VariableDescriptor("TXT", null)
    val COMPLETE_VARIABLE = VariableDescriptor("COMP", "complete()")
    val ALL_VARIABLES = listOf(BOOL_VARIABLE, TEXT_VARIABLE, COMPLETE_VARIABLE)
        .associateBy { it.name }

    const val END_VARIABLE_SRC = "\$${TemplateImpl.END}$"
    val BOOL_VARIABLE_SRC = "\$${BOOL_VARIABLE.name}$"
    val TEXT_VARIABLE_SRC = "\$${TEXT_VARIABLE.name}$"
    val COMPLETE_VARIABLE_SRC = "\$${COMPLETE_VARIABLE.name}$"

    fun propertyElementPattern() = or(
        psiElement().withSuperParent(2, jsProperty()),
        psiElement().withParent(jsProperty())
    )

    fun getPropertyNameInfo(parameters: CompletionParameters, result: CompletionResultSet) = getPropertyInfo(parameters.position.parent, result.prefixMatcher.prefix, true)

    fun getPropertyValueInfo(parameters: CompletionParameters, result: CompletionResultSet) = getPropertyInfo(parameters.position.parent, result.prefixMatcher.prefix, false)

    fun getPropertyValueInfo(element: PsiElement, prefix: String) = getPropertyInfo(element, prefix, false)

    private fun getPropertyInfo(element: PsiElement?, prefix: String, requireNameInfo: Boolean): JsModelCompletionInfo? {
        if (element == null) return null
        val property = PsiTreeUtil.getParentOfType(element, JSProperty::class.java, false) ?: return null
        var propertyName = property.name ?: return null
        val isNameCompletion = propertyName.endsWith(CompletionInitializationContext.DUMMY_IDENTIFIER_TRIMMED)
        if (requireNameInfo != isNameCompletion) return null // early abort if not requested type
        val objectLiteral = PsiTreeUtil.getParentOfType(property, JSObjectLiteralExpression::class.java) ?: return null
        val module = property.containingModule() ?: return null
        val scoutJsModel = JsModelManager.getOrCreateScoutJsModel(module) ?: return null

        if (isNameCompletion) {
            if (propertyName.length == CompletionInitializationContext.DUMMY_IDENTIFIER_TRIMMED.length) {
                PsiTreeUtil.getParentOfType(property, JSProperty::class.java)?.name?.let { propertyName = it }
            } else {
                propertyName = propertyName.take(propertyName.length - CompletionInitializationContext.DUMMY_IDENTIFIER_TRIMMED.length)
            }
        }

        val usedPropertyNames = objectLiteral.properties.mapNotNull { it.name }.filter { it != CompletionInitializationContext.DUMMY_IDENTIFIER_TRIMMED }.toSet()
        val contextElement = if (isNameCompletion) element.parent else element // on property completion there is one more element (a jsProperty with DUMMY_IDENTIFIER name)
        val contextParent = contextElement.parent
        val isInArray = contextParent.elementType == JSStubElementTypes.ARRAY_LITERAL_EXPRESSION
        val siblings = contextParent.children
        val isLast = siblings.indexOf(contextElement) == siblings.size - 1
        val isInLiteral = contextElement is JSLiteralExpression

        val extendedPrefix = contextElement
            .takeIf { !isNameCompletion && !isInLiteral }
            ?.text?.split(CompletionInitializationContext.DUMMY_IDENTIFIER_TRIMMED)?.firstOrNull()
            ?.takeIf { it != prefix }

        return JsModelCompletionInfo(property, propertyName, objectLiteral, module, scoutJsModel, isLast, isNameCompletion, usedPropertyNames, prefix, extendedPrefix, isInArray, isInLiteral)
    }

    fun createPropertyNameLookupElement(property: ScoutJsProperty, completionInfo: JsModelCompletionInfo) =
        createLookupElement(JsNameLookupElement(property), completionInfo)

    fun createPropertyValueLookupElement(propertyValue: IScoutJsPropertyValue, completionInfo: JsModelCompletionInfo): LookupElementBuilder {
        val element = when (propertyValue) {
            is ScoutJsObjectPropertyValue -> JsObjectValueLookupElement(propertyValue)
            else -> JsValueLookupElement(propertyValue)
        }
        return createLookupElement(element, completionInfo)
    }

    private fun createLookupElement(modelElement: ScoutJsModelLookupElement, completionInfo: JsModelCompletionInfo): LookupElementBuilder {
        val name = modelElement.name()
        val presentableName = modelElement.presentableName()
        val lookupString = createLookupString(modelElement, completionInfo)

        var result = LookupElementBuilder.create(name, lookupString)
            .withLookupString(presentableName)
            .withCaseSensitivity(true)
            .withPresentableText(presentableName)
            .withIcon(modelElement.icon())
        modelElement.tailText()?.let { result = result.withTailText(it, true) }
        modelElement.typeText()?.let { result = result.withTypeText(it, true) }

        val isTemplateRequired = completionInfo.isPropertyNameCompletion || (completionInfo.propertyName != ScoutJsCoreConstants.PROPERTY_NAME_OBJECT_TYPE && modelElement.property().type().isChildModelSupported)
        if (isTemplateRequired) {
            result = result.withInsertHandler { context, _ -> startTemplate(context.editor, completionInfo.searchPrefix, buildPropertyTemplate(modelElement, completionInfo)) }
            result.putUserData(CodeCompletionHandlerBase.DIRECT_INSERTION, true)
        }

        result.putUserData(SELECTED_ELEMENT, modelElement)
        result.putUserData(TemplateHelper.SCOUT_LOOKUP_ELEMENT_MARKER, true) // to identify scout LookupElements. Used for testing.
        return result
    }

    private fun createLookupString(modelElement: ScoutJsModelLookupElement, completionInfo: JsModelCompletionInfo): String {
        val name = modelElement.name()
        val presentableName = modelElement.presentableName()
        var lookupString = when (modelElement) {
            is JsObjectValueLookupElement -> if (completionInfo.isInLiteral) name else presentableName
            else -> name
        }

        val isValueCompletionOutsideLiteral = !completionInfo.isPropertyNameCompletion && !completionInfo.isInLiteral
        val isStringObjectTypeCompletion = completionInfo.propertyName == ScoutJsCoreConstants.PROPERTY_NAME_OBJECT_TYPE && !completionInfo.scoutJsModel.supportsClassReference()
        val isEnumWithConstantStringValue = modelElement.property().type().isEnumLike && modelElement is JsValueLookupElement
                && (modelElement.propertyValue as? ScoutJsConstantValuePropertyValue)?.value?.type() == IConstantValue.ConstantValueType.String


        if (isValueCompletionOutsideLiteral && (isStringObjectTypeCompletion || isEnumWithConstantStringValue)) {
            lookupString = "$STRING_DELIM$name$STRING_DELIM"
        } else if (completionInfo.extendedSearchPrefix != null) {
            lookupString = lookupString.removePrefix(completionInfo.extendedSearchPrefix.replaceAfterLast('.', ""))
        }

        return lookupString
    }

    private fun startTemplate(editor: Editor, prefix: CharSequence, template: Template) {
        val project = editor.project
        writeCommandAction(project).run(ThrowableRunnable<RuntimeException> {
            TemplateHelper.removePrefix(editor, prefix)
            TemplateManager.getInstance(project).startTemplate(editor, template)
        })
    }

    private fun buildPropertyTemplate(selectedElement: ScoutJsModelLookupElement, completionInfo: JsModelCompletionInfo): TemplateImpl {
        val property = selectedElement.property()
        val name = property.name()

        val src = StringBuilder()
        if (completionInfo.isPropertyNameCompletion) {
            src.append(name).append(": ")
        }
        src.append(buildPropertyValueSource(selectedElement, completionInfo))
        if (!completionInfo.isLast) {
            src.append(',')
        }
        if (!src.contains(END_VARIABLE_SRC)) {
            src.append(END_VARIABLE_SRC)
        }

        return buildTemplate("Scout.jsModel.property.$name", src.toString()) {
            val isIdProperty = it == TEXT_VARIABLE.name && property.type().isChildModelSupported
            if (isIdProperty) ID_DEFAULT_TEXT else ""
        }
    }

    private fun buildPropertyValueSource(selectedElement: ScoutJsModelLookupElement, completionInfo: JsModelCompletionInfo): String {
        val property = selectedElement.property()
        val propertyType = property.type()

        // wrappings
        val valueSrc = WrappingStringBuilder()
        if (propertyType.isArray && !completionInfo.isInArray) {
            valueSrc.appendWrapping("[", "]")
        }
        if (propertyType.isString && !property.isObjectType) {
            valueSrc.appendWrapping(STRING_DELIM, STRING_DELIM)
            if (propertyType.subType() == ScoutJsPropertySubType.TEXT_KEY) {
                valueSrc.appendWrapping(
                    TranslationPatterns.JsModelTextKeyPattern.MODEL_TEXT_KEY_PREFIX,
                    TranslationPatterns.JsModelTextKeyPattern.MODEL_TEXT_KEY_SUFFIX
                )
            }
        } else if (propertyType.isChildModelSupported) {
            valueSrc.appendWrapping("{", "}")
        }

        // property value
        if (propertyType.isBoolean) {
            valueSrc.append(BOOL_VARIABLE_SRC)
        } else if (propertyType.subType() == ScoutJsPropertySubType.TEXT_KEY || property.isObjectType || propertyType.isEnumLike) {
            valueSrc.append(COMPLETE_VARIABLE_SRC)
        } else if (propertyType.isChildModelSupported) {
            valueSrc.append(buildModelPropertyValueSource(selectedElement, completionInfo))
        } else if (propertyType.isClassType) { // must be after isChildModelSupported
            valueSrc.append(COMPLETE_VARIABLE_SRC) // e.g. for class references (like lookupCall)
        } else {
            valueSrc.append(TEXT_VARIABLE_SRC)
        }
        return valueSrc.toString()
    }

    fun buildModelPropertyValueSource(selectedElement: ScoutJsModelLookupElement, completionInfo: JsModelCompletionInfo): String {
        val rootModel = completionInfo.scoutJsModel
        val hasProperty = when (selectedElement) {
            is JsObjectValueLookupElement -> { propertyName: String -> selectedElement.propertyValue.scoutJsObject.hasProperty(propertyName) }
            is JsNameLookupElement -> { propertyName: String -> selectedElement.property().type().possibleChildProperties().anyMatch { propertyName == it.name() } }
            else -> null
        } ?: return "\n" + COMPLETE_VARIABLE_SRC
        val predefinedProperties = ArrayList<String>()

        if (hasProperty(ScoutJsCoreConstants.PROPERTY_NAME_ID)) {
            predefinedProperties.add("${ScoutJsCoreConstants.PROPERTY_NAME_ID}: $STRING_DELIM$TEXT_VARIABLE_SRC$STRING_DELIM")
        }
        if (hasProperty(ScoutJsCoreConstants.PROPERTY_NAME_OBJECT_TYPE)) {
            val objectType = when (selectedElement) {
                is JsNameLookupElement -> COMPLETE_VARIABLE_SRC
                is JsObjectValueLookupElement -> {
                    val scoutObject = selectedElement.propertyValue.scoutJsObject
                    if (rootModel.supportsClassReference()) scoutObject.name() else "$STRING_DELIM${scoutObject.shortName()}$STRING_DELIM"
                }

                else -> null
            }
            predefinedProperties.add("${ScoutJsCoreConstants.PROPERTY_NAME_OBJECT_TYPE}: $objectType")
        }
        if (predefinedProperties.isEmpty()) {
            return "\n" + COMPLETE_VARIABLE_SRC + "\n"
        }
        return "\n" + predefinedProperties.joinToString(",\n") + END_VARIABLE_SRC + "\n"
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
        val defaultValue = JavaUtils.toStringLiteral(defaultValueProvider(name)).toString()
        target.addVariable(name, descriptor.expression, defaultValue, true)
    }

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

    interface ScoutJsModelLookupElement {
        fun property(): ScoutJsProperty
        fun icon(): Icon?
        fun tailText(): String? = null
        fun typeText(): String? = property().type().displayName().orElse("")
        fun name(): String
        fun presentableName() = name()
    }

    data class JsNameLookupElement(val scoutJsProperty: ScoutJsProperty) : ScoutJsModelLookupElement {
        override fun property() = scoutJsProperty

        override fun icon(): Icon {
            val property = property()
            if (property.type().isChildModelSupported) {
                if (property.scoutJsObject().declaringClass().isTypeScript) {
                    return JavaScriptPsiIcons.Classes.TypeScriptClass
                }
                return JavaScriptPsiIcons.Classes.JavaScriptClass
            }
            if (property.type().isEnumLike) {
                return AllIcons.Nodes.Enum
            }
            return JavaScriptPsiIcons.Classes.Alias
        }

        override fun tailText(): String = " (from " + property().scoutJsObject().declaringClass().name() + ")"

        override fun name(): String = property().name()
    }

    data class JsObjectValueLookupElement(val propertyValue: ScoutJsObjectPropertyValue) : ScoutJsModelLookupElement {
        override fun property(): ScoutJsProperty = propertyValue.property()
        override fun icon(): Icon {
            if (propertyValue.scoutJsObject.declaringClass().isTypeScript) {
                return JavaScriptPsiIcons.Classes.TypeScriptClass
            }
            return JavaScriptPsiIcons.Classes.JavaScriptClass
        }

        override fun typeText(): String = propertyValue.scoutJsObject.declaringClass().containingModule().name()
        override fun name(): String = propertyValue.name()
        override fun presentableName(): String = propertyValue.scoutJsObject.name()
    }

    data class JsValueLookupElement(val propertyValue: IScoutJsPropertyValue) : ScoutJsModelLookupElement {
        override fun property(): ScoutJsProperty = propertyValue.property()
        override fun icon() = AllIcons.Nodes.Enum
        override fun name(): String = propertyValue.name()
        override fun presentableName(): String = when (propertyValue) {
            is ScoutJsEnumPropertyValue -> propertyValue.enumConstant
            else -> name()
        }
    }
}