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

import com.intellij.application.options.CodeStyle
import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.template.Template
import com.intellij.codeInsight.template.TemplateEditingAdapter
import com.intellij.codeInsight.template.TemplateManager
import com.intellij.codeInsight.template.impl.TemplateImpl
import com.intellij.codeInsight.template.impl.TemplateImplUtil
import com.intellij.codeInsight.template.impl.TemplateState
import com.intellij.openapi.command.WriteCommandAction.writeCommandAction
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.module.Module
import com.intellij.openapi.util.Conditions.alwaysTrue
import com.intellij.psi.*
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.codeStyle.CodeStyleSettingsManager
import com.intellij.psi.codeStyle.JavaCodeStyleManager
import com.intellij.psi.codeStyle.JavaCodeStyleSettings
import com.intellij.psi.util.InheritanceUtil.findEnclosingInstanceInScope
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ThrowableRunnable
import org.eclipse.scout.sdk.core.log.SdkLog
import org.eclipse.scout.sdk.core.log.SdkLog.onTrace
import org.eclipse.scout.sdk.core.model.api.PropertyBean
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes
import org.eclipse.scout.sdk.core.s.annotation.OrderAnnotation.convertToJavaSource
import org.eclipse.scout.sdk.core.s.annotation.OrderAnnotation.getNewViewOrderValue
import org.eclipse.scout.sdk.core.s.classid.ClassIds
import org.eclipse.scout.sdk.core.s.uniqueid.UniqueIds
import org.eclipse.scout.sdk.core.util.Ensure.newFail
import org.eclipse.scout.sdk.core.util.FinalValue
import org.eclipse.scout.sdk.core.util.JavaTypes
import org.eclipse.scout.sdk.core.util.Strings
import org.eclipse.scout.sdk.core.util.Strings.toStringLiteral
import org.eclipse.scout.sdk.s2i.*
import org.eclipse.scout.sdk.s2i.nls.NlsKeysEnumMacro
import java.lang.reflect.Method
import java.util.regex.MatchResult
import java.util.regex.Pattern

/**
 * Handler that inserts a selected [TemplateDescriptor].
 */
class TemplateInsertHandler(val templateDescriptor: TemplateDescriptor, val prefix: String) : InsertHandler<LookupElement> {

    companion object {

        const val VALUE_OPTION_BOX = "box"
        const val VALUE_OPTION_UNBOX = "unbox"
        const val VALUE_OPTION_DEFAULT_VAL = "default"
        const val VALUE_OPTION_UNCHANGED = "unchanged"
        const val CONSTANT_IDENTIFIER = "#" // marker for constants in IScoutRuntimeTypes that may be used in the template
        private const val VARIABLE_MENU_TYPES = "menuTypes"
        private val CREATE_TEMP_SETTINGS_METHOD = FinalValue<Method>()

        private val DECLARING_TYPE_ARG_REGEX = Pattern.compile("\\\$declaringTypeArg\\(([\\w.]+),\\s*(\\d+),\\s*($VALUE_OPTION_BOX|$VALUE_OPTION_UNBOX|$VALUE_OPTION_DEFAULT_VAL|$VALUE_OPTION_UNCHANGED)\\)\\\$")
        private val ENCLOSING_INSTANCE_FQN_REGEX = Pattern.compile("\\\$enclosingInstanceInScopeFqn\\((.*)\\)\\\$")
        private val CLASS_ID_REGEX = Pattern.compile("\\\$newClassId\\(\\)\\\$")
        private val UNIQUE_ID_REGEX = Pattern.compile("\\\$newUniqueId\\((.+)\\)\\\$")
        private val SCOUT_RT_CONSTANTS = buildScoutRtConstants()
        private val MENU_TYPE_MAPPING = buildMenuTypeMapping()

        private fun buildScoutRtConstants(): Pair<Array<String>, Array<String>> {
            val map = IScoutRuntimeTypes::class.java.fields
                    .map { CONSTANT_IDENTIFIER + it.name + CONSTANT_IDENTIFIER to it.get(null).toString() }
                    .toMap()
            val keyArray = map.keys.toTypedArray()
            val valArray = map.values.toTypedArray()
            return keyArray to valArray
        }

        private fun buildMenuTypeMapping(): Map<String, List<String>> {
            val mapping = HashMap<String, List<String>>()

            val tableEmpty = "${IScoutRuntimeTypes.TableMenuType}.${IScoutRuntimeTypes.TableMenuType_EmptySpace}"
            val tableHeader = "${IScoutRuntimeTypes.TableMenuType}.${IScoutRuntimeTypes.TableMenuType_Header}"
            val tableMulti = "${IScoutRuntimeTypes.TableMenuType}.${IScoutRuntimeTypes.TableMenuType_MultiSelection}"
            val tableSingle = "${IScoutRuntimeTypes.TableMenuType}.${IScoutRuntimeTypes.TableMenuType_SingleSelection}"
            val treeEmpty = "${IScoutRuntimeTypes.TreeMenuType}.${IScoutRuntimeTypes.TreeMenuType_EmptySpace}"
            val treeHeader = "${IScoutRuntimeTypes.TreeMenuType}.${IScoutRuntimeTypes.TreeMenuType_Header}"
            val treeMulti = "${IScoutRuntimeTypes.TreeMenuType}.${IScoutRuntimeTypes.TreeMenuType_MultiSelection}"
            val treeSingle = "${IScoutRuntimeTypes.TreeMenuType}.${IScoutRuntimeTypes.TreeMenuType_SingleSelection}"
            val calendarComponent = "${IScoutRuntimeTypes.CalendarMenuType}.${IScoutRuntimeTypes.CalendarMenuType_CalendarComponent}"
            val calendarEmpty = "${IScoutRuntimeTypes.CalendarMenuType}.${IScoutRuntimeTypes.CalendarMenuType_EmptySpace}"
            val valueFieldEmpty = "${IScoutRuntimeTypes.ValueFieldMenuType}.${IScoutRuntimeTypes.ValueFieldMenuType_Null}"
            val valueFieldNotEmpty = "${IScoutRuntimeTypes.ValueFieldMenuType}.${IScoutRuntimeTypes.ValueFieldMenuType_NotNull}"
            val imageFieldEmpty = "${IScoutRuntimeTypes.ImageFieldMenuType}.${IScoutRuntimeTypes.ImageFieldMenuType_Null}"
            val imageFieldImageId = "${IScoutRuntimeTypes.ImageFieldMenuType}.${IScoutRuntimeTypes.ImageFieldMenuType_ImageId}"
            val imageFieldImageUrl = "${IScoutRuntimeTypes.ImageFieldMenuType}.${IScoutRuntimeTypes.ImageFieldMenuType_ImageUrl}"
            val imageFieldImage = "${IScoutRuntimeTypes.ImageFieldMenuType}.${IScoutRuntimeTypes.ImageFieldMenuType_Image}"
            val tileGridEmpty = "${IScoutRuntimeTypes.TileGridMenuType}.${IScoutRuntimeTypes.TileGridMenuType_EmptySpace}"
            val tileGridMulti = "${IScoutRuntimeTypes.TileGridMenuType}.${IScoutRuntimeTypes.TileGridMenuType_MultiSelection}"
            val tileGridSingle = "${IScoutRuntimeTypes.TileGridMenuType}.${IScoutRuntimeTypes.TileGridMenuType_SingleSelection}"

            val treeMappings = listOf("$treeSingle, $treeMulti", "$treeEmpty, $treeHeader", "$treeSingle, $treeMulti, $treeHeader, $treeEmpty")
            val calendarMappings = listOf(calendarComponent, calendarEmpty, "$calendarComponent, $calendarEmpty")

            mapping[IScoutRuntimeTypes.ITable] = listOf("$tableSingle, $tableMulti", "$tableEmpty, $tableHeader", "$tableSingle, $tableMulti, $tableHeader, $tableEmpty")
            mapping[IScoutRuntimeTypes.ITree] = treeMappings
            mapping[IScoutRuntimeTypes.ITreeNode] = treeMappings
            mapping[IScoutRuntimeTypes.ITabBox] = listOf("${IScoutRuntimeTypes.TabBoxMenuType}.${IScoutRuntimeTypes.TabBoxMenuType_Header}")
            mapping[IScoutRuntimeTypes.ICalendarItemProvider] = calendarMappings
            mapping[IScoutRuntimeTypes.ICalendar] = calendarMappings
            mapping[IScoutRuntimeTypes.IValueField] = listOf(valueFieldNotEmpty, valueFieldEmpty, "$valueFieldNotEmpty, $valueFieldEmpty")
            mapping[IScoutRuntimeTypes.IImageField] = listOf("$imageFieldImageId, $imageFieldImageUrl, $imageFieldImage", imageFieldEmpty, "$imageFieldImageId, $imageFieldImageUrl, $imageFieldImage, $imageFieldEmpty")
            mapping[IScoutRuntimeTypes.ITileGrid] = listOf("$tileGridSingle, $tileGridMulti", tileGridEmpty, "$tileGridSingle, $tileGridMulti, $tileGridEmpty")
            return mapping
        }

        private fun createTempSettings(origSettings: CodeStyleSettings, settingsManager: CodeStyleSettingsManager): CodeStyleSettings {
            val createTemporarySettings = CREATE_TEMP_SETTINGS_METHOD.computeIfAbsentAndGet { createTemporarySettingsMethod() }
            if (createTemporarySettings != null) {
                // use createTemporarySettings() factory method in IJ 2020.2 and newer
                val tempSettings = createTemporarySettings.invoke(settingsManager) as CodeStyleSettings
                tempSettings.copyFrom(origSettings)
                return tempSettings
            }

            // use clone method until IJ 2020.1
            // Can be removed if the supported min. IJ version is 2020.2
            return CodeStyleSettings::class.java.getMethod("clone").invoke(origSettings) as CodeStyleSettings
        }

        private fun createTemporarySettingsMethod() =
                try {
                    CodeStyleSettingsManager::class.java.getMethod("createTemporarySettings")
                } catch (e: NoSuchMethodException) {
                    SdkLog.debug("Using legacy temporary CodeStyleSettings creation.", onTrace(e))
                    null
                }
    }

    private lateinit var m_declaringClass: PsiClass
    private lateinit var m_containingModule: Module
    private val m_resolvedTypeArgs = HashMap<Pair<String, Int>, String>()
    private var m_superClassInfo: TemplateDescriptor.SuperClassInfo? = null
    private var m_superClassBase: PsiClass? = null
    private var m_insertPosition: Int = -1
    private var m_menuTypes: List<String>? = null

    override fun handleInsert(context: InsertionContext, item: LookupElement) {
        val editor = context.editor
        m_declaringClass = item.getObject() as PsiClass
        m_containingModule = m_declaringClass.containingModule() ?: return
        m_superClassInfo = templateDescriptor.superClassInfo()
        m_superClassBase = m_superClassInfo?.baseFqn?.let { m_containingModule.findTypeByName(it) }
        m_insertPosition = editor.caretModel.offset
        m_menuTypes = MENU_TYPE_MAPPING.entries
                .firstOrNull { m_declaringClass.isInstanceOf(it.key) }
                ?.value

        startTemplateWithTempSettings(buildTemplate(), editor)
    }

    /**
     * The templates do not work if the setting "InsertInnerClassImports" is active.
     * Therefore execute the template with temporary settings (see [CodeStyleSettingsManager.setTemporarySettings]).
     * The temporary settings will be removed again in the [TemplateListener].
     */
    private fun startTemplateWithTempSettings(template: TemplateImpl, editor: Editor) {
        val project = editor.project
        val settingsManager = CodeStyleSettingsManager.getInstance(project)
        val origTempSettings = settingsManager.temporarySettings
        val tempSettings = createTempSettings(CodeStyle.getSettings(editor), settingsManager)
        val tempJavaSettings = tempSettings.getCustomSettings(JavaCodeStyleSettings::class.java)
        tempJavaSettings.isInsertInnerClassImports = false

        writeCommandAction(project).run(ThrowableRunnable<RuntimeException> {
            val templateListener = TemplateListener(templateDescriptor, settingsManager, origTempSettings)
            removePrefix(editor)
            settingsManager.setTemporarySettings(tempSettings)
            TemplateManager.getInstance(project).startTemplate(editor, template, templateListener)
        })
    }

    private fun removePrefix(editor: Editor) {
        if (Strings.isEmpty(prefix)) {
            return
        }
        val document = editor.document
        val offset = editor.caretModel.offset
        var start = offset - prefix.length - 1
        val limit = 0.coerceAtLeast(start - 5)
        val chars = document.immutableCharSequence
        // reduce start index of removal to any preceding alphabet characters
        // this is required for fast typing where the prefix is "older" than the current content of the document
        while (start >= limit && isAlphaChar(chars[start])) {
            start--
        }
        document.replaceString(start + 1, offset, "")
    }

    private fun isAlphaChar(char: Char) = char in 'a'..'z' || char in 'A'..'Z'

    private fun buildTemplate(): TemplateImpl {
        val source = buildTemplateSource()
        val template = TemplateImpl(templateDescriptor.id, source, "Scout")
        template.id = this.templateDescriptor.id
        template.description = templateDescriptor.description()
        template.isToShortenLongNames = true
        template.isToReformat = true
        template.isDeactivated = false
        template.isToIndent = true
        template.setValue(Template.Property.USE_STATIC_IMPORT_IF_POSSIBLE, false)
        TemplateImplUtil.parseVariableNames(source).forEach { addVariable(it, template) }
        template.parseSegments()
        return template
    }

    private fun buildTemplateSource(): String {
        val templateSource = replaceConstants(templateDescriptor.source())
        val classId = createClassIdAnnotationIfNecessary() ?: ""
        val order = createOrderAnnotationIfNecessary() ?: ""
        return "$order $classId $templateSource"
    }

    /**
     * Replace constants in the template. The values depend on the execution context (the declaring class in which the template is invoked) and must therefore be evaluated at insertion time.
     * But the values are constant from the perspective of a single template execution. The user cannot modify these constants.
     */
    private fun replaceConstants(src: CharSequence): String {
        val staticIfExtension = if (m_declaringClass.isInstanceOf(IScoutRuntimeTypes.IExtension)) "static" else ""
        val getConfiguredMenuTypes = if (isMenuTypesSupported()) {
            "@${Override::class.java.name} protected ${Set::class.java.name}<? extends ${IScoutRuntimeTypes.IMenuType}> getConfiguredMenuTypes() { return \$$VARIABLE_MENU_TYPES\$; }"
        } else {
            ""
        }

        val stage1 = Strings.replaceEach(src,
                arrayOf("\$${TemplateDescriptor.PREDEFINED_VARIABLE_STATIC_IN_EXTENSION}\$", "\$${TemplateDescriptor.PREDEFINED_VARIABLE_CONFIGURED_MENU_TYPES}\$"),
                arrayOf(staticIfExtension, getConfiguredMenuTypes))
        val stage2 = Strings.replaceEach(stage1, SCOUT_RT_CONSTANTS.first, SCOUT_RT_CONSTANTS.second)
        val stage3 = DECLARING_TYPE_ARG_REGEX.replaceAll(stage2) { resolveDeclaringTypeArgument(it) }
        val stage4 = UNIQUE_ID_REGEX.replaceAll(stage3) { UniqueIds.next(it.group(1)) }
        val stage5 = ENCLOSING_INSTANCE_FQN_REGEX.replaceAll(stage4) { resolveEnclosingInstanceFqn(it) }
        return CLASS_ID_REGEX.replaceAll(stage5) { ClassIds.next(m_declaringClass.qualifiedName) }
    }

    private fun resolveEnclosingInstanceFqn(match: MatchResult): String {
        val queryType = match.group(1)
        return m_declaringClass.findEnclosingClass(queryType, true)
                ?.qualifiedName ?: ""
    }

    private fun resolveDeclaringTypeArgument(match: MatchResult): String {
        val typeArgDeclaringClassFqn = match.group(1)
        val typeArgIndex = Integer.valueOf(match.group(2))
        val postProcessing = match.group(3)
        val type = m_resolvedTypeArgs.computeIfAbsent(typeArgDeclaringClassFqn to typeArgIndex) {
            val owner = m_declaringClass.findEnclosingClass(typeArgDeclaringClassFqn, true)
            owner?.resolveTypeArgument(typeArgIndex, typeArgDeclaringClassFqn)
                    ?.getCanonicalText(false)
                    ?: Object::class.java.name
        }
        return when (postProcessing) {
            VALUE_OPTION_BOX -> JavaTypes.boxPrimitive(type)
            VALUE_OPTION_UNBOX -> JavaTypes.unboxToPrimitive(type)
            VALUE_OPTION_DEFAULT_VAL -> JavaTypes.defaultValueOf(type)
            else -> type
        }
    }

    private fun createOrderAnnotationIfNecessary(): String? {
        if (!isOrderSupported()) {
            return null
        }
        val siblings = findOrderSiblings()
        val first = OrderAnnotation.valueOf(siblings[0])
        val second = OrderAnnotation.valueOf(siblings[1])
        val orderValue = convertToJavaSource(getNewViewOrderValue(first, second))
        return "@${IScoutRuntimeTypes.Order}($orderValue)"
    }

    private fun findOrderSiblings(): Array<PsiClass?> {
        val orderDefinitionType = templateDescriptor.orderDefinitionType()
                ?: throw newFail("Super class supports the Order annotation but no order annotation definition type has been specified.")
        val candidates = m_declaringClass.innerClasses.filter { it.isInstanceOf(orderDefinitionType) }

        var prev: PsiClass? = null
        for (t in candidates) {
            if (t.textOffset > m_insertPosition) {
                return arrayOf(prev, t)
            }
            prev = t
        }
        return arrayOf(prev, null)
    }

    private fun createClassIdAnnotationIfNecessary(): String? {
        if (!isClassIdSupported() || !ClassIds.isAutomaticallyCreateClassIdAnnotation()) {
            return null
        }
        val classIdValue = toStringLiteral(ClassIds.next(m_declaringClass.qualifiedName)) ?: return null
        return "@${IScoutRuntimeTypes.ClassId}($classIdValue)"
    }

    private fun isOrderSupported() = m_superClassBase?.isInstanceOf(IScoutRuntimeTypes.IOrdered) ?: false

    private fun isClassIdSupported() = m_superClassBase?.isInstanceOf(IScoutRuntimeTypes.ITypeWithClassId) ?: false

    private fun isMenuTypesSupported() = m_menuTypes != null

    private fun addVariable(name: String, target: TemplateImpl) {
        if (TemplateImpl.INTERNAL_VARS_SET.contains(name)) {
            return
        }
        if (VARIABLE_MENU_TYPES == name) {
            val options = m_menuTypes!!.map { "${IScoutRuntimeTypes.CollectionUtility}.hashSet($it)" }
            target.addVariable(name, toEnum(options, PsiExpressionEnumMacro.NAME), toStringLiteral(options[0]).toString(), true)
            return
        }
        if (TemplateDescriptor.PREDEFINED_VARIABLE_KEYSTROKES == name) {
            val options = getKeyStrokeOptions()
            target.addVariable(name, toEnum(options, PsiExpressionEnumMacro.NAME), toStringLiteral(options[0]).toString(), true)
            return
        }
        if (TemplateDescriptor.PREDEFINED_VARIABLE_SUPER == name) {
            val baseFqn = toStringLiteral(m_superClassInfo?.baseFqn) ?: throw newFail("Variable '{}' is used in the template but no valid super class base was specified.", TemplateDescriptor.PREDEFINED_VARIABLE_SUPER)
            val defaultValue = toStringLiteral(m_superClassInfo?.defaultValue) ?: throw newFail("Variable '{}' is used in the template but no valid super class default was specified.", TemplateDescriptor.PREDEFINED_VARIABLE_SUPER)
            target.addVariable(name, "${DescendantAbstractClassesEnumMacro.NAME}($baseFqn, $defaultValue)", defaultValue.toString(), true)
            return
        }
        if (TemplateDescriptor.PREDEFINED_VARIABLE_COMPLETE == name) {
            target.addVariable(name, "complete()", null, true)
            return
        }
        addVariableFromDescriptorDefinition(name, target)
    }

    private fun toEnum(options: Iterable<String>, enumMacroName: String = "enum") = options.joinToString(", ", "$enumMacroName(", ")") { toStringLiteral(it) }

    private fun addVariableFromDescriptorDefinition(name: String, target: TemplateImpl) {
        val variableDescriptor = templateDescriptor.variable(name) ?: throw newFail("Variable '{}' is used in the template but not declared in the template descriptor.", name)
        var expression: String? = null
        var defaultValue = variableDescriptor.values.firstOrNull() ?: ""

        if (name.startsWith(TemplateDescriptor.VARIABLE_PREFIX_NLS)) {
            expression = "${NlsKeysEnumMacro.NAME}()"
        } else if (name.startsWith(TemplateDescriptor.VARIABLE_PREFIX_BOOL)) {
            val other = if ("true" == defaultValue) "false" else "true"
            expression = toEnum(listOf(defaultValue, other))
        } else {
            val options = variableDescriptor.values
            defaultValue = options.firstOrNull() ?: ""
            if (options.size > 1) {
                expression = toEnum(options)
            }
        }
        target.addVariable(name, expression, toStringLiteral(defaultValue).toString(), true)
    }

    private fun getKeyStrokeOptions(): List<String> {
        val fKeys = (1..12).map { "${IScoutRuntimeTypes.IKeyStroke}.F$it" }
        val cursors = listOf("${IScoutRuntimeTypes.IKeyStroke}.LEFT", "${IScoutRuntimeTypes.IKeyStroke}.RIGHT", "${IScoutRuntimeTypes.IKeyStroke}.UP", "${IScoutRuntimeTypes.IKeyStroke}.DOWN")
        val actionKeys = listOf("${IScoutRuntimeTypes.IKeyStroke}.ENTER", "${IScoutRuntimeTypes.IKeyStroke}.INSERT", "${IScoutRuntimeTypes.IKeyStroke}.DELETE",
                "${IScoutRuntimeTypes.IKeyStroke}.ESCAPE", "${IScoutRuntimeTypes.IKeyStroke}.SPACE", "${IScoutRuntimeTypes.IKeyStroke}.TAB", "${IScoutRuntimeTypes.IKeyStroke}.BACKSPACE")
        val ctrl = "${IScoutRuntimeTypes.IKeyStroke}.CONTROL"
        val shift = "${IScoutRuntimeTypes.IKeyStroke}.SHIFT"
        val alt = "${IScoutRuntimeTypes.IKeyStroke}.ALT"
        val combinedSamples = listOf(
                "$ctrl, \"C\"",
                "$ctrl, $shift, \"E\"",
                "$alt, ${IScoutRuntimeTypes.IKeyStroke}.F11")
                .map { "${IScoutRuntimeTypes.KeyStroke}.combineKeyStrokes($it)" }
        return fKeys + actionKeys + cursors + combinedSamples
    }

    private class TemplateListener(private val templateDescriptor: TemplateDescriptor, private val settingsManager: CodeStyleSettingsManager, private val origSettings: CodeStyleSettings?) : TemplateEditingAdapter() {

        override fun templateCancelled(template: Template?) {
            resetTemporarySettings()
        }

        override fun beforeTemplateFinished(state: TemplateState, template: Template) {
            try {
                insertInnerTypeGetter(state)
            } finally {
                resetTemporarySettings()
            }
        }

        private fun resetTemporarySettings() {
            if (origSettings != null) {
                settingsManager.setTemporarySettings(origSettings)
            } else {
                settingsManager.dropTemporarySettings()
            }
        }

        private fun insertInnerTypeGetter(state: TemplateState) {
            val innerTypeGetterContainerInfo = templateDescriptor.innerTypeGetterContainer() ?: return
            val editor = state.editor
            val project = editor.project ?: return
            val document = editor.document
            val nameRange = state.getVariableRange(TemplateDescriptor.VARIABLE_NAME) ?: return
            val psiDocumentManager = PsiDocumentManager.getInstance(project)
            val file = psiDocumentManager.getPsiFile(document) ?: return
            val element = file.findElementAt(nameRange.startOffset) ?: return
            val createdClass = PsiTreeUtil.getParentOfType(element, PsiClass::class.java) ?: return
            val containingModule = file.containingModule() ?: return
            val innerTypeGetterBase = containingModule.findTypeByName(innerTypeGetterContainerInfo.definitionClassFqn) ?: return
            val innerTypeGetterContainer = findEnclosingInstanceInScope(innerTypeGetterBase, element, alwaysTrue(), false) ?: return

            val createdClassFqn = createdClass.qualifiedName
            val createdClassSimpleName = Strings.ensureStartWithUpperCase(createdClass.name)
            val psiElementFactory = JavaPsiFacade.getElementFactory(project)
            val methodName = PropertyBean.GETTER_PREFIX + createdClassSimpleName
            val innerTypeGetterMethodName = innerTypeGetterContainerInfo.methodName
            val innerTypeGetter = psiElementFactory.createMethodFromText("public $createdClassFqn $methodName() { return $innerTypeGetterMethodName($createdClassFqn.class);}", innerTypeGetterContainer)
            if (innerTypeGetterContainer.findMethodBySignature(innerTypeGetter, false) != null) {
                return // method already exists
            }

            val anchorInfo = getInsertAnchor(innerTypeGetterContainer, methodName, innerTypeGetterMethodName, document)

            JavaCodeStyleManager.getInstance(project).shortenClassReferences(innerTypeGetter)
            writeCommandAction(project).run(ThrowableRunnable<RuntimeException> {
                if (anchorInfo == null) {
                    innerTypeGetterContainer.add(innerTypeGetter)
                } else {
                    if (anchorInfo.second) {
                        innerTypeGetterContainer.addAfter(innerTypeGetter, anchorInfo.first)
                    } else {
                        innerTypeGetterContainer.addBefore(innerTypeGetter, anchorInfo.first)
                    }
                }
                psiDocumentManager.doPostponedOperationsAndUnblockDocument(document)
            })
        }

        private fun getInsertAnchor(innerTypeGetterContainer: PsiClass, methodName: String, innerTypeGetterMethodName: String, document: Document): Pair<PsiElement, Boolean>? {
            val gettersWithSource = innerTypeGetterContainer.methods
                    .filter { it.name.startsWith(PropertyBean.GETTER_PREFIX) }
                    .map { it to document.getText(it.textRange) }
            val otherInnerTypeGetters = gettersWithSource
                    .filter { it.second.contains(innerTypeGetterMethodName) }
                    .map { it.first }
            return getInsertAnchorFrom(otherInnerTypeGetters, methodName)  // find position within other inner-type-getters first
                    ?: getInsertAnchorFrom(gettersWithSource.map { it.first }, methodName) // find position within all getters
                    ?: getInsertAnchorFrom(innerTypeGetterContainer.methods.filter { !it.isConstructor }, methodName) // find position within all methods
        }

        private fun getInsertAnchorFrom(candidates: Iterable<PsiMethod>, methodName: String) = candidates
                .lastOrNull { it.name < methodName }
                ?.let { it to true } // sort ascending, add after
                ?: candidates
                        .firstOrNull()
                        ?.let { it to false } // all must be after, add before the first
    }
}