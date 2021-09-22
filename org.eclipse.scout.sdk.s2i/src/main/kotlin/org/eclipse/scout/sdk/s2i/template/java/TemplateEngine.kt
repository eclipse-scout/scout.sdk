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
package org.eclipse.scout.sdk.s2i.template.java

import com.intellij.openapi.module.Module
import com.intellij.psi.PsiClass
import org.eclipse.scout.sdk.core.apidef.Api
import org.eclipse.scout.sdk.core.s.apidef.IScoutApi
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

    data class TemplateContext(val declaringClass: PsiClass, val module: Module, val scoutApi: IScoutApi, val fileInsertPosition: Int)

    companion object {
        const val VALUE_OPTION_BOX = "box"
        const val VALUE_OPTION_UNBOX = "unbox"
        const val VALUE_OPTION_DEFAULT_VAL = "default"
        const val VALUE_OPTION_UNCHANGED = "unchanged"

        private val DECLARING_TYPE_ARG_REGEX = Pattern.compile("\\\$declaringTypeArg\\(([\\w.]+),\\s*(\\d+),\\s*($VALUE_OPTION_BOX|$VALUE_OPTION_UNBOX|$VALUE_OPTION_DEFAULT_VAL|$VALUE_OPTION_UNCHANGED)\\)\\\$")
        private val ENCLOSING_INSTANCE_FQN_REGEX = Pattern.compile("\\\$enclosingInstanceInScopeFqn\\((.*)\\)\\\$")
        private val CLASS_ID_REGEX = Pattern.compile("\\\$newClassId\\(\\)\\\$")
        private val UNIQUE_ID_REGEX = Pattern.compile("\\\$newUniqueId\\((.+)\\)\\\$")
    }

    private val m_resolvedTypeArgs = HashMap<Pair<String, Int>, String>()
    private val m_superClassBase = templateDescriptor.superClassInfo()
            ?.baseFqn
            ?.let { context.module.findTypeByName(it) }
    val menuTypes: List<String> = menuTypeMapping().entries
            .firstOrNull { context.declaringClass.isInstanceOf(it.key) }
            ?.value ?: emptyList()
    val keyStrokes = getKeyStrokeOptions()

    fun buildTemplate(): String {
        val templateSource = VelocityRunner()
                .withProperties(buildScoutRtConstants())
                .withProperty(TemplateDescriptor.PREDEFINED_CONSTANT_IN_EXTENSION, context.declaringClass.isInstanceOf(context.scoutApi.IExtension()))
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

    /**
     * Builds the Scout RT constants containing all elements of the [IScoutApi].
     *
     * Form:
     *
     * "TEXTS"="org.eclipse.scout.rt.platform.text.TEXTS"
     *
     * "TEXTS_getMethodName"="get"
     *
     * "ICodeType_codeIdTypeParamIndex"="1"
     */
    private fun buildScoutRtConstants(): Map<String, String> {
        val result = HashMap<String, String>()
        Api.dump(context.scoutApi)
                .map { flattenRtConstants(it.key, it.value.values) }
                .forEach { result.putAll(it) }
        return result
    }

    private fun flattenRtConstants(fqn: String, children: Collection<Map<String, String>>): Map<String, String> {
        val simpleName = JavaTypes.simpleName(fqn)
        val result = HashMap<String, String>()
        result[simpleName] = fqn
        children.forEach { it.forEach { (k, v) -> result[simpleName + '_' + k] = v } }
        return result
    }

    private fun getKeyStrokeOptions(): List<String> {
        val iKeyStroke = context.scoutApi.IKeyStroke().fqn()
        val fKeys = (1..12).map { "$iKeyStroke.F$it" }
        val actionKeys = listOf("$iKeyStroke.ENTER", "$iKeyStroke.INSERT", "$iKeyStroke.DELETE",
                "$iKeyStroke.ESCAPE", "$iKeyStroke.SPACE", "$iKeyStroke.TAB", "$iKeyStroke.BACKSPACE")
        val cursors = listOf("$iKeyStroke.LEFT", "$iKeyStroke.RIGHT", "$iKeyStroke.UP", "$iKeyStroke.DOWN")
        val ctrl = "$iKeyStroke.CONTROL"
        val shift = "$iKeyStroke.SHIFT"
        val alt = "$iKeyStroke.ALT"
        val combinedSamples = listOf(
                "$ctrl, \"C\"",
                "$ctrl, $shift, \"E\"",
                "$alt, $iKeyStroke.F11")
                .map { "${context.scoutApi.AbstractAction().fqn()}.${context.scoutApi.AbstractAction().combineKeyStrokesMethodName()}($it)" }
        return fKeys + actionKeys + cursors + combinedSamples
    }

    private fun menuTypeMapping(): Map<String, List<String>> {
        val scoutApi = context.scoutApi
        val tableMenuType = scoutApi.TableMenuType()
        val treeMenuType = scoutApi.TreeMenuType()
        val calendarMenuType = scoutApi.CalendarMenuType()
        val valueFieldMenuType = scoutApi.ValueFieldMenuType()
        val imageFieldMenuType = scoutApi.ImageFieldMenuType()
        val tileGridMenuType = scoutApi.TileGridMenuType()
        val tabBoxMenuType = scoutApi.TabBoxMenuType()

        val tableEmpty = "${tableMenuType.fqn()}.${tableMenuType.EmptySpace()}"
        val tableHeader = "${tableMenuType.fqn()}.${tableMenuType.Header()}"
        val tableMulti = "${tableMenuType.fqn()}.${tableMenuType.MultiSelection()}"
        val tableSingle = "${tableMenuType.fqn()}.${tableMenuType.SingleSelection()}"
        val treeEmpty = "${treeMenuType.fqn()}.${treeMenuType.EmptySpace()}"
        val treeHeader = "${treeMenuType.fqn()}.${treeMenuType.Header()}"
        val treeMulti = "${treeMenuType.fqn()}.${treeMenuType.MultiSelection()}"
        val treeSingle = "${treeMenuType.fqn()}.${treeMenuType.SingleSelection()}"
        val calendarComponent = "${calendarMenuType.fqn()}.${calendarMenuType.CalendarComponent()}"
        val calendarEmpty = "${calendarMenuType.fqn()}.${calendarMenuType.EmptySpace()}"
        val valueFieldEmpty = "${valueFieldMenuType.fqn()}.${valueFieldMenuType.Null()}"
        val valueFieldNotEmpty = "${valueFieldMenuType.fqn()}.${valueFieldMenuType.NotNull()}"
        val imageFieldEmpty = "${imageFieldMenuType.fqn()}.${imageFieldMenuType.Null()}"
        val imageFieldImageId = "${imageFieldMenuType.fqn()}.${imageFieldMenuType.ImageId()}"
        val imageFieldImageUrl = "${imageFieldMenuType.fqn()}.${imageFieldMenuType.ImageUrl()}"
        val imageFieldImage = "${imageFieldMenuType.fqn()}.${imageFieldMenuType.Image()}"
        val tileGridEmpty = "${tileGridMenuType.fqn()}.${tileGridMenuType.EmptySpace()}"
        val tileGridMulti = "${tileGridMenuType.fqn()}.${tileGridMenuType.MultiSelection()}"
        val tileGridSingle = "${tileGridMenuType.fqn()}.${tileGridMenuType.SingleSelection()}"

        val treeMappings = listOf("$treeSingle, $treeMulti", "$treeEmpty, $treeHeader", "$treeSingle, $treeMulti, $treeHeader, $treeEmpty")
        val calendarMappings = listOf(calendarComponent, calendarEmpty, "$calendarComponent, $calendarEmpty")

        val mapping = HashMap<String, List<String>>()
        mapping[scoutApi.ITable().fqn()] = listOf("$tableSingle, $tableMulti", "$tableEmpty, $tableHeader", "$tableSingle, $tableMulti, $tableHeader, $tableEmpty")
        mapping[scoutApi.ITree().fqn()] = treeMappings
        mapping[scoutApi.ITreeNode().fqn()] = treeMappings
        mapping[scoutApi.ITabBox().fqn()] = listOf("${tabBoxMenuType.fqn()}.${tabBoxMenuType.Header()}")
        mapping[scoutApi.ICalendarItemProvider().fqn()] = calendarMappings
        mapping[scoutApi.ICalendar().fqn()] = calendarMappings
        mapping[scoutApi.IValueField().fqn()] = listOf(valueFieldNotEmpty, valueFieldEmpty, "$valueFieldNotEmpty, $valueFieldEmpty")
        mapping[scoutApi.IImageField().fqn()] = listOf("$imageFieldImageId, $imageFieldImageUrl, $imageFieldImage", imageFieldEmpty, "$imageFieldImageId, $imageFieldImageUrl, $imageFieldImage, $imageFieldEmpty")
        mapping[scoutApi.ITileGrid().fqn()] = listOf("$tileGridSingle, $tileGridMulti", tileGridEmpty, "$tileGridSingle, $tileGridMulti, $tileGridEmpty")
        return mapping
    }

    private fun resolveEnclosingInstanceFqn(match: MatchResult): String {
        val queryType = match.group(1)
        return context.declaringClass.findEnclosingClass(queryType, context.scoutApi, true)
                ?.qualifiedName ?: ""
    }

    private fun resolveDeclaringTypeArgument(match: MatchResult): String {
        val typeArgDeclaringClassFqn = match.group(1)
        val typeArgIndex = Integer.valueOf(match.group(2))
        val postProcessing = match.group(3)
        val type = m_resolvedTypeArgs.computeIfAbsent(typeArgDeclaringClassFqn to typeArgIndex) {
            val owner = context.declaringClass.findEnclosingClass(typeArgDeclaringClassFqn, context.scoutApi, true)
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
        val project = context.module.project
        val first = OrderAnnotation.valueOf(siblings[0], project, context.scoutApi)
        val second = OrderAnnotation.valueOf(siblings[1], project, context.scoutApi)
        val orderValue = org.eclipse.scout.sdk.core.s.annotation.OrderAnnotation.convertToJavaSource(org.eclipse.scout.sdk.core.s.annotation.OrderAnnotation.getNewViewOrderValue(first, second))
        val orderAnnotationFqn = context.scoutApi.Order().fqn()
        return "@$orderAnnotationFqn($orderValue)"
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
        val classIdFqn = context.scoutApi.ClassId().fqn()
        return "@$classIdFqn($classIdValue)"
    }

    private fun isOrderSupported() = m_superClassBase?.isInstanceOf(context.scoutApi.IOrdered()) ?: false

    private fun isClassIdSupported() = m_superClassBase?.isInstanceOf(context.scoutApi.ITypeWithClassId()) ?: false
}