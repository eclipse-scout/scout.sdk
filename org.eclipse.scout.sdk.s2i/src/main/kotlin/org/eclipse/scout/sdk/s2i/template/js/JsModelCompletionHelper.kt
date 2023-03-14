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

import com.intellij.codeInsight.completion.*
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
import com.intellij.lang.javascript.psi.JSReferenceExpression
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
import org.eclipse.scout.sdk.core.java.JavaUtils
import org.eclipse.scout.sdk.core.s.model.js.IScoutJsObject
import org.eclipse.scout.sdk.core.s.model.js.ScoutJsCoreConstants
import org.eclipse.scout.sdk.core.s.model.js.ScoutJsModel
import org.eclipse.scout.sdk.core.s.model.js.ScoutJsModels
import org.eclipse.scout.sdk.core.s.model.js.prop.IScoutJsPropertyValue
import org.eclipse.scout.sdk.core.s.model.js.prop.ScoutJsObjectPropertyValue
import org.eclipse.scout.sdk.core.s.model.js.prop.ScoutJsProperty
import org.eclipse.scout.sdk.core.s.model.js.prop.ScoutJsPropertySubType
import org.eclipse.scout.sdk.core.s.nls.query.TranslationPatterns
import org.eclipse.scout.sdk.core.typescript.TypeScriptTypes
import org.eclipse.scout.sdk.core.typescript.model.api.IES6Class
import org.eclipse.scout.sdk.core.typescript.model.spi.ES6ClassSpi
import org.eclipse.scout.sdk.core.util.FinalValue
import org.eclipse.scout.sdk.s2i.containingModule
import org.eclipse.scout.sdk.s2i.model.js.JsModelManager
import org.eclipse.scout.sdk.s2i.model.typescript.IdeaNodeModule
import org.eclipse.scout.sdk.s2i.template.BoolVariableAdapter
import org.eclipse.scout.sdk.s2i.template.TemplateHelper
import org.eclipse.scout.sdk.s2i.template.VariableDescriptor
import java.util.*
import java.util.stream.Collectors
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

    private fun getPropertyInfo(element: PsiElement, prefix: String, requireNameInfo: Boolean): PropertyCompletionInfo? {
        val property = PsiTreeUtil.getParentOfType(element, JSProperty::class.java) ?: return null
        var propertyName = property.name ?: return null
        val isNameCompletion = propertyName.endsWith(CompletionInitializationContext.DUMMY_IDENTIFIER_TRIMMED)
        if (requireNameInfo != isNameCompletion) return null // early abort if not requested type
        val objectLiteral = PsiTreeUtil.getParentOfType(property, JSObjectLiteralExpression::class.java) ?: return null
        val module = property.containingModule() ?: return null
        val scoutJsModel = JsModelManager.getOrCreate(module) ?: return null

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
        return PropertyCompletionInfo(property, propertyName, objectLiteral, module, scoutJsModel, isLast, isNameCompletion, usedPropertyNames, prefix, isInArray, contextElement is JSLiteralExpression)
    }

    fun createPropertyNameLookupElement(property: ScoutJsProperty, completionInfo: PropertyCompletionInfo) =
        createLookupElement(JsNameLookupElement(property), completionInfo)

    fun createPropertyValueLookupElement(propertyValue: IScoutJsPropertyValue, completionInfo: PropertyCompletionInfo): LookupElementBuilder {
        val element = when (propertyValue) {
            is ScoutJsObjectPropertyValue -> JsObjectValueLookupElement(propertyValue)
            else -> JsValueLookupElement(propertyValue)
        }
        return createLookupElement(element, completionInfo)
    }

    private fun createLookupElement(modelElement: ScoutJsModelLookupElement, completionInfo: PropertyCompletionInfo): LookupElementBuilder {
        val name = modelElement.name()
        val presentableName = modelElement.presentableName()
        var result = LookupElementBuilder.create(name, if (completionInfo.isInLiteral) name else presentableName)
            .withLookupString(name)
            .withLookupString(presentableName)
            .withCaseSensitivity(true)
            .withPresentableText(presentableName)
            .withIcon(modelElement.icon())
        modelElement.tailText()?.let { result = result.withTailText(it, true) }
        modelElement.typeText()?.let { result = result.withTypeText(it, true) }

        if (!completionInfo.isPropertyNameCompletion && !completionInfo.isInLiteral && completionInfo.propertyName == ScoutJsCoreConstants.PROPERTY_NAME_OBJECT_TYPE && !completionInfo.scoutJsModel.supportsClassReference()) {
            // append missing quotes if not supporting class references for objectType (no template required)
            result = result.withInsertHandler { context, _ -> insertObjectTypeWithQuotes(name, completionInfo.searchPrefix, context) }
            result.putUserData(CodeCompletionHandlerBase.DIRECT_INSERTION, true)
        } else {
            val isTemplateRequired = completionInfo.isPropertyNameCompletion || (modelElement.property().type().hasClasses() && completionInfo.propertyName != ScoutJsCoreConstants.PROPERTY_NAME_OBJECT_TYPE)
            if (isTemplateRequired) {
                result = result.withInsertHandler { context, _ -> startTemplate(context.editor, completionInfo.searchPrefix, buildPropertyTemplate(modelElement, completionInfo)) }
                result.putUserData(CodeCompletionHandlerBase.DIRECT_INSERTION, true)
            }
        }

        result.putUserData(SELECTED_ELEMENT, modelElement)
        result.putUserData(TemplateHelper.SCOUT_LOOKUP_ELEMENT_MARKER, true) // to identify scout LookupElements. Used for testing.
        return result
    }

    private fun insertObjectTypeWithQuotes(presentableName: String, searchPrefix: String, context: InsertionContext) {
        writeCommandAction(context.project).run(ThrowableRunnable<RuntimeException> {
            val startOffset = context.editor.caretModel.currentCaret.offset
            val insertText = "$STRING_DELIM$presentableName$STRING_DELIM"
            context.document.insertString(startOffset, insertText)
            val prefixRemoveLen = TemplateHelper.removePrefix(context.editor, searchPrefix)
            context.editor.caretModel.currentCaret.moveToOffset(startOffset + insertText.length - prefixRemoveLen)
        })
    }

    private fun startTemplate(editor: Editor, prefix: CharSequence, template: Template) {
        val project = editor.project
        writeCommandAction(project).run(ThrowableRunnable<RuntimeException> {
            TemplateHelper.removePrefix(editor, prefix)
            TemplateManager.getInstance(project).startTemplate(editor, template)
        })
    }

    private fun buildPropertyTemplate(selectedElement: ScoutJsModelLookupElement, completionInfo: PropertyCompletionInfo): TemplateImpl {
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
            if (it == TEXT_VARIABLE.name && property.type().hasClasses()) ID_DEFAULT_TEXT else ""
        }
    }

    private fun buildPropertyValueSource(selectedElement: ScoutJsModelLookupElement, completionInfo: PropertyCompletionInfo): String {
        val property = selectedElement.property()
        val propertyType = property.type()

        // wrappings
        val valueSrc = WrappingStringBuilder()
        if (propertyType.isArray && !completionInfo.isInArray) {
            valueSrc.appendWrapping("[", "]")
        }
        val propertyDataTypeName = propertyType.dataType().map { it.name() }.orElse(null)
        if (TypeScriptTypes._string == propertyDataTypeName && selectedElement.property().name() != ScoutJsCoreConstants.PROPERTY_NAME_OBJECT_TYPE) {
            valueSrc.appendWrapping(STRING_DELIM, STRING_DELIM)
            if (propertyType.subType() == ScoutJsPropertySubType.TEXT_KEY) {
                valueSrc.appendWrapping(
                    TranslationPatterns.JsModelTextKeyPattern.MODEL_TEXT_KEY_PREFIX,
                    TranslationPatterns.JsModelTextKeyPattern.MODEL_TEXT_KEY_SUFFIX
                )
            }
        } else if (propertyType.hasClasses()) {
            valueSrc.appendWrapping("{", "}")
        }

        // property value
        if (propertyType.isEnumLike) {
            valueSrc.append(COMPLETE_VARIABLE_SRC)
        } else if (propertyType.subType() == ScoutJsPropertySubType.TEXT_KEY) {
            valueSrc.append(COMPLETE_VARIABLE_SRC)
        } else if (propertyType.isBoolean) {
            valueSrc.append(BOOL_VARIABLE_SRC)
        } else if (selectedElement.property().name() == ScoutJsCoreConstants.PROPERTY_NAME_OBJECT_TYPE) {
            valueSrc.append(COMPLETE_VARIABLE_SRC)
        } else if (propertyType.hasClasses()) {
            valueSrc.append(buildClassPropertyValueSource(selectedElement, completionInfo))
        } else {
            valueSrc.append(TEXT_VARIABLE_SRC)
        }
        return valueSrc.toString()
    }

    fun buildClassPropertyValueSource(selectedElement: ScoutJsModelLookupElement, completionInfo: PropertyCompletionInfo): String {
        val rootModel = completionInfo.scoutJsModel
        val propertyObject = when (selectedElement) {
            is JsObjectValueLookupElement -> selectedElement.propertyValue.scoutJsObject
            is JsNameLookupElement -> rootModel
                .findScoutObjects()
                .withIncludeDependencies(true)
                .withObjectClasses(selectedElement.property().type().classes())
                .first().orElse(null)

            else -> null
        } ?: return "\n" + COMPLETE_VARIABLE_SRC

        val predefinedProperties = ArrayList<String>()
        if (propertyObject.hasProperty(ScoutJsCoreConstants.PROPERTY_NAME_ID)) {
            predefinedProperties.add("${ScoutJsCoreConstants.PROPERTY_NAME_ID}: $STRING_DELIM$TEXT_VARIABLE_SRC$STRING_DELIM")
        }
        if (propertyObject.hasProperty(ScoutJsCoreConstants.PROPERTY_NAME_OBJECT_TYPE)) {
            val objectType = if (completionInfo.isPropertyNameCompletion) COMPLETE_VARIABLE_SRC
            else if (rootModel.supportsClassReference()) propertyObject.name()
            else "$STRING_DELIM${propertyObject.shortName()}$STRING_DELIM"
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

    data class PropertyCompletionInfo(
        val propertyPsi: JSProperty, val propertyName: String, val objectLiteral: JSObjectLiteralExpression, val module: Module, val scoutJsModel: ScoutJsModel, val isLast: Boolean,
        val isPropertyNameCompletion: Boolean, val siblingPropertyNames: Set<String>, val searchPrefix: String, val isInArray: Boolean, val isInLiteral: Boolean
    ) {
        private val m_objectTypeProperty by lazy { objectLiteral.findProperty(ScoutJsCoreConstants.PROPERTY_NAME_OBJECT_TYPE) }
        private val m_objectTypeScoutObject = FinalValue<IScoutJsObject?>()
        private val m_objectTypeClass = FinalValue<IES6Class?>()
        private val m_objectTypeModel = FinalValue<ScoutJsModel?>()
        private val m_scoutObjects = FinalValue<List<IScoutJsObject>>()
        private val m_parentScoutProperty = FinalValue<ScoutJsProperty?>()

        private fun objectTypeScoutObject() = m_objectTypeScoutObject.computeIfAbsentAndGet {
            objectTypeModel()?.findScoutObjects()?.withObjectClass(objectTypeClass())?.first()?.orElse(null)
        }

        /**
         * The [IScoutJsObject]s of the current completion. Detected from an explicit objectType property or by the datatype of the parent property.
         */
        fun scoutObjects() = m_scoutObjects.computeIfAbsentAndGet {
            val scoutObject = objectTypeScoutObject()
            if (scoutObject != null) return@computeIfAbsentAndGet Collections.singletonList(scoutObject)

            val parentProperty = parentScoutProperty() ?: return@computeIfAbsentAndGet emptyList()
            if (!parentProperty.type().hasClasses()) return@computeIfAbsentAndGet emptyList()
            return@computeIfAbsentAndGet parentProperty.scoutJsObject().scoutJsModel().findScoutObjects()
                .withIncludeDependencies(true)
                .withObjectClasses(parentProperty.type().classes())
                .stream().toList()
        }.stream()

        fun parentScoutProperty() = m_parentScoutProperty.computeIfAbsentAndGet {
            val infoForParentObject = getPropertyValueInfo(propertyPsi, searchPrefix) ?: return@computeIfAbsentAndGet null
            infoForParentObject
                .objectTypeScoutObject()
                ?.findProperties()
                ?.withName(infoForParentObject.propertyName)
                ?.withSuperClasses(true)
                ?.first()
                ?.orElse(null)
        }

        private fun objectTypeClass() = m_objectTypeClass.computeIfAbsentAndGet { findReferencedClass() }
        private fun objectTypeModel() = m_objectTypeModel.computeIfAbsentAndGet { objectTypeClass()?.containingModule().let { ScoutJsModels.create(it).orElse(null) } }

        private fun findReferencedClass(): IES6Class? {
            val ref = m_objectTypeProperty?.value ?: return null
            if (ref is JSReferenceExpression) {
                val ideaNodeModule = scoutJsModel.nodeModule().spi() as IdeaNodeModule
                val referencedClass = ideaNodeModule.moduleInventory.resolveReferencedElement(ref) as? ES6ClassSpi ?: return null
                return referencedClass.api()
            }

            if (ref is JSLiteralExpression) {
                val objectType = ref.takeIf { it.isStringLiteral }?.stringValue ?: return null
                return scoutJsModel.findScoutObjects()
                    .withObjectType(objectType)
                    .withIncludeDependencies(true)
                    .first()
                    .map {
                        m_objectTypeScoutObject.set(it)
                        m_objectTypeModel.set(it.scoutJsModel())
                        it.declaringClass()
                    }.orElse(null)
            }
            return null
        }
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
        fun typeText(): String? = null
        fun name(): String
        fun presentableName() = name()
    }

    data class JsNameLookupElement(val scoutJsProperty: ScoutJsProperty) : ScoutJsModelLookupElement {
        override fun property() = scoutJsProperty

        override fun icon(): Icon {
            val property = property()
            if (property.type().hasClasses()) {
                if (property.scoutJsObject().scoutJsModel().supportsTypeScript()) {
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

        override fun typeText(): String? {
            val propertyType = property().type()
            if (propertyType.hasClasses()) {
                return propertyType.classes().map { it.name() }.collect(Collectors.joining(" | "))
            }
            return propertyType.dataType().map { it.name() }.orElse(null)
        }

        override fun name(): String = property().name()
    }

    data class JsObjectValueLookupElement(val propertyValue: ScoutJsObjectPropertyValue) : ScoutJsModelLookupElement {
        override fun property(): ScoutJsProperty = propertyValue.property()
        override fun icon(): Icon {
            if (propertyValue.scoutJsObject.scoutJsModel().supportsTypeScript()) {
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
        override fun typeText(): String = property().type().dataType().map { it.name() }.orElse(null)
        override fun name(): String = propertyValue.name()
    }
}