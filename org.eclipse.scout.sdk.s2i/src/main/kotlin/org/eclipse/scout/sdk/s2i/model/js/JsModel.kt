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
package org.eclipse.scout.sdk.s2i.model.js

import com.intellij.javascript.nodejs.NodeModuleSearchUtil.collectVisibleNodeModules
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.guessModuleDir
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import org.eclipse.scout.sdk.core.log.SdkLog
import org.eclipse.scout.sdk.core.s.IWebConstants
import org.eclipse.scout.sdk.s2i.EclipseScoutBundle.jsModuleCache
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/**
 * Represents the full Scout JavaScript model of a Node module.
 * This includes the JavaScript code of the module itself and all direct dependencies of the module.
 */
class JsModel {

    companion object {
        const val OBJECT_TYPE_PROPERTY_NAME = "objectType"
        const val ID_PROPERTY_NAME = "id"
        const val SCOUT_JS_NAMESPACE = "scout"
        const val WIDGET_CLASS_NAME = "Widget"
    }

    private val m_modules = ArrayList<JsModule>()

    /**
     * Creates the model for the [Module] given.
     * @param module The [Module] for which the model should be created.
     * @return This instance
     */
    fun build(module: Module): JsModel {
        m_modules.clear()
        val moduleRoot = module.guessModuleDir() ?: return this
        val start = System.currentTimeMillis()
        val project = module.project
        val jsModuleCache = jsModuleCache(project)
        val psiManager = PsiManager.getInstance(project)

        // find all module candidate roots
        val jsModuleRoots = sequenceOf(moduleRoot) + collectVisibleNodeModules(HashMap(), project, moduleRoot)
                .asSequence()
                .mapNotNull { it.virtualFile }

        // Pass 1: get or create the module meta data (name, namespace, containing files, etc.)
        // this pass is necessary because the next pass (parse) needs the mapping which file belongs to which module (it will call containingModule())
        jsModuleRoots
                .mapNotNull { getOrCreateModule(jsModuleCache, it) }
                .forEach { m_modules.add(it) }

        // Pass 2: parse files of modules into AbstractJsModelElements and store in cache
        m_modules
                .asSequence()
                .filter { !it.isParsed() } // only the new ones. the others are from the cache
                .forEach {
                    it.parseModelElements(psiManager)
                    jsModuleCache.putModule(it.moduleRoot, it)
                }

        SdkLog.debug("JS model creation took {}ms.", System.currentTimeMillis() - start)
        return this
    }

    /**
     * @param file The [VirtualFile] for which the containing [JsModule] should be returned. Only files having a [IWebConstants.JS_FILE_SUFFIX] can be passed.
     * @return The [JsModule] that contains the [VirtualFile] given.
     */
    fun containingModule(file: VirtualFile?) = file?.canonicalFile?.let { f ->
        m_modules.firstOrNull { it.files.contains(f) }
    }

    /**
     * @return A [Sequence] containing all [JsModelClass] instances as well as top-level and nested [JsModelEnum] instances of this [JsModel].
     */
    fun elements(): Sequence<AbstractJsModelElement> = m_modules.asSequence().flatMap { it.elements() }

    /**
     * @param objectType The objectType (e.g. 'scout.GroupBox'). The default 'scout' namespace may be omitted. All other namespaces are required.
     * @return The [AbstractJsModelElement] that corresponds to the given [objectType]. May be a [JsModelClass] or a top-level or nested [JsModelEnum].
     */
    fun element(objectType: String?) = m_modules.asSequence()
            .mapNotNull { it.element(objectType) }
            .firstOrNull()

    /**
     * @param objectType The objectType (e.g. 'scout.GroupBox'). The default 'scout' namespace may be omitted. All other namespaces are required.
     * @param propertyName The name of the property. E.g. 'label'.
     * @return The [JsModelProperty] of the given [objectType] having the given [propertyName]. Also returns the property if it is not directly declared on the given [objectType] but is inherited instead.
     */
    fun property(objectType: String?, propertyName: String?): JsModelProperty? {
        val root = element(objectType) ?: return null
        var result: JsModelProperty? = null
        visitElementAndParents(root) { element ->
            val p = element.properties.firstOrNull { it.name == propertyName }
            if (p != null) {
                result = chooseProperty(p, result)
            }
            true // continue visiting
        }
        return result
    }

    /**
     * @param objectType The objectType (e.g. 'scout.GroupBox'). The default 'scout' namespace may be omitted. All other namespaces are required.
     * @return All [JsModelProperty] instances available for the given [objectType]. Also includes inherited properties.
     */
    fun properties(objectType: String?): Map<String, JsModelProperty> {
        val result = HashMap<String, JsModelProperty>()
        val root = element(objectType) ?: return emptyMap()
        visitElementAndParents(root) { element ->
            element.properties.forEach { result[it.name] = chooseProperty(it, result[it.name]) }
            true // continue visiting
        }
        return result
    }

    /**
     * @param property The [JsModelProperty] for which possible values should be returned.
     * @return Possible values for the given [property] or an empty list if no values are known to this [JsModel].
     * Returns the enum values if the [property] points to a [JsModelEnum], all known Widget [JsModelClass] instances if the property is a Widget property or boolean literals if it is a boolean property.
     */
    fun valuesForProperty(property: JsModelProperty): List<PropertyValue> {
        if (property.dataType.isCustomType()) {
            // the property is a custom data type
            // if it is an enum: get its elements
            val element = element(property.dataType.type) as? JsModelEnum ?: return emptyList()
            return element.properties
                    .map { PropertyValue(element.name + '.' + it.name, it) }
        }
        if (property.name == OBJECT_TYPE_PROPERTY_NAME) {
            // object types property: get all top level classes
            return elements()
                    .filter { it.name.isNotEmpty() }
                    .filter { Character.isUpperCase(it.name[0]) } // only classes
                    .filter { !it.name.contains('.') } // only top-level elements
                    .map { PropertyValue(it.shortName(), it) }
                    .toList()
        }
        return when (property.dataType) {
            JsModelProperty.JsPropertyDataType.WIDGET -> resolveWidgetProposals().toList()
            JsModelProperty.JsPropertyDataType.BOOL -> listOf(PropertyValue(true.toString()), PropertyValue(false.toString()))
            else -> emptyList() // we cannot know
        }
    }

    /**
     * @param element The [AbstractJsModelElement] to check.
     * @return true if the [element] is a [JsModelClass] that inherits from scout.Widget.
     */
    fun isWidget(element: AbstractJsModelElement?): Boolean {
        val modelClass = element as? JsModelClass ?: return false
        return visitElementAndParents(modelClass) {
            it.name != WIDGET_CLASS_NAME || it.scoutJsModule.name != IWebConstants.SCOUT_JS_CORE_MODULE_NAME
        }
    }

    private fun visitElementAndParents(modelElement: AbstractJsModelElement, visitor: (AbstractJsModelElement) -> Boolean): Boolean {
        val superElements = ArrayDeque<AbstractJsModelElement>()
        superElements.addLast(modelElement)
        while (superElements.isNotEmpty()) {
            val element = superElements.removeFirst()
            val continueVisiting = visitor.invoke(element)
            if (!continueVisiting) {
                return true // early abort
            }
            if (element is JsModelClass) {
                element.superClassNames.mapNotNull { element(it) }.forEach { superElements.addLast(it) }
            }
        }
        return false
    }

    private fun resolveWidgetProposals() = elements()
            .filter { isWidget(it) }
            .map { PropertyValue(it.shortName(), it) }

    /**
     * Properties might be declared on several levels in the class hierarchy. E.g. a property 'x' may be declared on Widget and on FormField.
     * In that case basically the FormField declaration (lower in the hierarchy) should win (might be narrowed) unless the specification of the Widget element (higher in the hierarchy) is more specific.
     */
    private fun chooseProperty(higher: JsModelProperty, lower: JsModelProperty?): JsModelProperty {
        if (lower == null) {
            return higher // first occurrence
        }
        if (lower.dataType == JsModelProperty.JsPropertyDataType.UNKNOWN && higher.dataType != JsModelProperty.JsPropertyDataType.UNKNOWN) {
            // higher level is more specific
            return higher
        }
        return lower
    }

    private fun getOrCreateModule(jsModuleCache: JsModuleCacheImplementor, moduleRoot: VirtualFile) = jsModuleCache.getModule(moduleRoot) ?: JsModule.parse(moduleRoot, this)

    /**
     * Represents a possible [JsModelProperty] value to insert into the source. Can be obtained using [JsModel.valuesForProperty]
     */
    data class PropertyValue(val displayText: String, val element: AbstractJsModelElement? = null)
}