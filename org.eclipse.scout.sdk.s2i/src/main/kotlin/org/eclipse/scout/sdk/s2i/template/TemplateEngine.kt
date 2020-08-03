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

import com.intellij.openapi.module.Module
import com.intellij.psi.PsiClass
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes
import org.eclipse.scout.sdk.core.s.classid.ClassIds
import org.eclipse.scout.sdk.core.s.uniqueid.UniqueIds
import org.eclipse.scout.sdk.core.util.Ensure
import org.eclipse.scout.sdk.core.util.JavaTypes
import org.eclipse.scout.sdk.core.util.Strings
import org.eclipse.scout.sdk.s2i.*
import org.eclipse.scout.sdk.s2i.util.VelocityRunner
import java.util.Collections.emptyList
import java.util.regex.MatchResult
import java.util.regex.Pattern

class TemplateEngine(val templateDescriptor: TemplateDescriptor, val context: TemplateContext) {

    data class TemplateContext(val declaringClass: PsiClass, val module: Module, val fileInsertPosition: Int)

    companion object {
        const val VALUE_OPTION_BOX = "box"
        const val VALUE_OPTION_UNBOX = "unbox"
        const val VALUE_OPTION_DEFAULT_VAL = "default"
        const val VALUE_OPTION_UNCHANGED = "unchanged"

        private val DECLARING_TYPE_ARG_REGEX = Pattern.compile("\\\$declaringTypeArg\\(([\\w.]+),\\s*(\\d+),\\s*(${VALUE_OPTION_BOX}|${VALUE_OPTION_UNBOX}|${VALUE_OPTION_DEFAULT_VAL}|${VALUE_OPTION_UNCHANGED})\\)\\\$")
        private val ENCLOSING_INSTANCE_FQN_REGEX = Pattern.compile("\\\$enclosingInstanceInScopeFqn\\((.*)\\)\\\$")
        private val CLASS_ID_REGEX = Pattern.compile("\\\$newClassId\\(\\)\\\$")
        private val UNIQUE_ID_REGEX = Pattern.compile("\\\$newUniqueId\\((.+)\\)\\\$")
        private val SCOUT_RT_CONSTANTS = IScoutRuntimeTypes::class.java
                .fields
                .map { it.name to it.get(null).toString() }
                .toMap()
        private val MENU_TYPE_MAPPING = buildMenuTypeMapping()

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

        fun getKeyStrokeOptions(): List<String> {
            val fKeys = (1..12).map { "${IScoutRuntimeTypes.IKeyStroke}.F$it" }
            val actionKeys = listOf("${IScoutRuntimeTypes.IKeyStroke}.ENTER", "${IScoutRuntimeTypes.IKeyStroke}.INSERT", "${IScoutRuntimeTypes.IKeyStroke}.DELETE",
                    "${IScoutRuntimeTypes.IKeyStroke}.ESCAPE", "${IScoutRuntimeTypes.IKeyStroke}.SPACE", "${IScoutRuntimeTypes.IKeyStroke}.TAB", "${IScoutRuntimeTypes.IKeyStroke}.BACKSPACE")
            val cursors = listOf("${IScoutRuntimeTypes.IKeyStroke}.LEFT", "${IScoutRuntimeTypes.IKeyStroke}.RIGHT", "${IScoutRuntimeTypes.IKeyStroke}.UP", "${IScoutRuntimeTypes.IKeyStroke}.DOWN")
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
    }

    private val m_resolvedTypeArgs = HashMap<Pair<String, Int>, String>()
    private val m_superClassBase = templateDescriptor.superClassInfo()
            ?.baseFqn
            ?.let { context.module.findTypeByName(it) }
    val menuTypes: List<String> = MENU_TYPE_MAPPING.entries
            .firstOrNull { context.declaringClass.isInstanceOf(it.key) }
            ?.value ?: emptyList()
    val keyStrokes = getKeyStrokeOptions()

    fun buildTemplate(): String {
        val templateSource = VelocityRunner()
                .withProperties(SCOUT_RT_CONSTANTS)
                .withProperty(TemplateDescriptor.PREDEFINED_CONSTANT_IN_EXTENSION, context.declaringClass.isInstanceOf(IScoutRuntimeTypes.IExtension))
                .withProperty(TemplateDescriptor.PREDEFINED_CONSTANT_MENU_SUPPORTED, menuTypes.isNotEmpty())
                .withPostProcessor(DECLARING_TYPE_ARG_REGEX, this::resolveDeclaringTypeArgument)
                .withPostProcessor(UNIQUE_ID_REGEX) { UniqueIds.next(it.group(1)) }
                .withPostProcessor(ENCLOSING_INSTANCE_FQN_REGEX, this::resolveEnclosingInstanceFqn)
                .withPostProcessor(CLASS_ID_REGEX) { ClassIds.next(context.declaringClass.qualifiedName) }
                .eval(templateDescriptor.source())

        val classId = createClassIdAnnotationIfNecessary() ?: ""
        val order = createOrderAnnotationIfNecessary() ?: ""
        return "$order $classId $templateSource"
    }

    private fun resolveEnclosingInstanceFqn(match: MatchResult): String {
        val queryType = match.group(1)
        return context.declaringClass.findEnclosingClass(queryType, true)
                ?.qualifiedName ?: ""
    }

    private fun resolveDeclaringTypeArgument(match: MatchResult): String {
        val typeArgDeclaringClassFqn = match.group(1)
        val typeArgIndex = Integer.valueOf(match.group(2))
        val postProcessing = match.group(3)
        val type = m_resolvedTypeArgs.computeIfAbsent(typeArgDeclaringClassFqn to typeArgIndex) {
            val owner = context.declaringClass.findEnclosingClass(typeArgDeclaringClassFqn, true)
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
        val orderValue = org.eclipse.scout.sdk.core.s.annotation.OrderAnnotation.convertToJavaSource(org.eclipse.scout.sdk.core.s.annotation.OrderAnnotation.getNewViewOrderValue(first, second))
        return "@${IScoutRuntimeTypes.Order}($orderValue)"
    }

    private fun findOrderSiblings(): Array<PsiClass?> {
        val orderDefinitionType = templateDescriptor.orderDefinitionType()
                ?: throw Ensure.newFail("Super class supports the Order annotation but no order annotation definition type has been specified.")
        val candidates = context.declaringClass.innerClasses.filter { it.isInstanceOf(orderDefinitionType) }

        var prev: PsiClass? = null
        for (t in candidates) {
            if (t.textOffset > context.fileInsertPosition) {
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
        val classIdValue = Strings.toStringLiteral(ClassIds.next(context.declaringClass.qualifiedName)) ?: return null
        return "@${IScoutRuntimeTypes.ClassId}($classIdValue)"
    }

    private fun isOrderSupported() = m_superClassBase?.isInstanceOf(IScoutRuntimeTypes.IOrdered) ?: false

    private fun isClassIdSupported() = m_superClassBase?.isInstanceOf(IScoutRuntimeTypes.ITypeWithClassId) ?: false
}