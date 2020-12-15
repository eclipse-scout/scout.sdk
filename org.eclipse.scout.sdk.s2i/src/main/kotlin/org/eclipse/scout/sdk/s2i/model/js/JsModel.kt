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
package org.eclipse.scout.sdk.s2i.model.js

import com.intellij.javascript.nodejs.NodeModuleSearchUtil.collectVisibleNodeModules
import com.intellij.lang.javascript.buildTools.npm.PackageJsonUtil
import com.intellij.lang.javascript.inspections.JSRecursiveWalkingElementSkippingNestedFunctionsVisitor
import com.intellij.lang.javascript.psi.JSElementVisitor
import com.intellij.lang.javascript.psi.JSFile
import com.intellij.lang.javascript.psi.JSVarStatement
import com.intellij.lang.javascript.psi.JSVariable
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList
import com.intellij.lang.javascript.psi.ecmal4.JSClass
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.guessModuleDir
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import org.eclipse.scout.sdk.core.log.SdkLog
import org.eclipse.scout.sdk.core.s.IWebConstants
import org.eclipse.scout.sdk.s2i.contentAsText
import java.util.*
import java.util.regex.Pattern
import kotlin.collections.HashMap

/**
 * Represents the full Scout JavaScript model of a Node module.
 * This includes the JavaScript code of the module itself and all direct dependencies of the module.
 */
class JsModel {

    companion object {
        const val OBJECT_TYPE_PROPERTY_NAME = "objectType"
        const val ID_PROPERTY_NAME = "id"
        const val DEFAULT_SCOUT_JS_NAMESPACE = "scout"
        const val WIDGET_CLASS_NAME = "Widget"
        private val NAMESPACE_PATTERN = Pattern.compile("window\\.([\\w._]+)\\s*=\\s*Object\\.assign\\(window\\.")
        private const val ADAPTER_FILE_SUFFIX = "Adapter${IWebConstants.JS_FILE_SUFFIX}"
        private const val MODEL_FILE_SUFFIX = "Model${IWebConstants.JS_FILE_SUFFIX}"
    }

    private val m_elements = HashMap<String /*qualified element name*/, JsModelElement>()
    private val m_moduleByFile = HashMap<VirtualFile, JsModule>()

    /**
     * Creates the model for the [Module] given.
     * @param module The [Module] for which the model should be created.
     * @return This instance
     */
    fun build(module: Module): JsModel {
        m_elements.clear()
        m_moduleByFile.clear()
        val moduleRoot = module.guessModuleDir() ?: return this

        val start = System.currentTimeMillis()

        // find all module candidate roots
        val jsModuleRootDirs = sequenceOf(moduleRoot) + collectVisibleNodeModules(HashMap(), module.project, moduleRoot)
                .asSequence()
                .mapNotNull { it.virtualFile }

        // parse all modules
        val modules = jsModuleRootDirs.mapNotNull { parseModule(it) }.toList()

        // collect all files of all modules into one map
        modules.forEach { m -> m.files.forEach { f -> f.canonicalFile?.let { m_moduleByFile[it] = m } } }

        // parse files of modules
        val psiManager = PsiManager.getInstance(module.project)
        modules.forEach { parseModuleFiles(it, psiManager) }

        SdkLog.debug("JS model creation took {}ms.", System.currentTimeMillis() - start)
        return this
    }

    /**
     * @param file The [VirtualFile] for which the containing [JsModule] should be returned. Only files having a [IWebConstants.JS_FILE_SUFFIX] can be passed.
     * @return The [JsModule] that contains the [VirtualFile] given.
     */
    fun containingModule(file: VirtualFile?) = file?.canonicalFile?.let { m_moduleByFile[it] }

    /**
     * @return A [Collection] containing [JsModelClass] instances as well as top-level and nested [JsModelEnum] instances.
     */
    fun elements(): Collection<JsModelElement> = m_elements.values

    /**
     * @param objectType The objectType (e.g. 'scout.GroupBox'). The default 'scout' namespace may be omitted. All other namespaces are required.
     * @return The [JsModelElement] that corresponds to the given [objectType]. May be a [JsModelClass] or a top-level or nested [JsModelEnum].
     */
    fun element(objectType: String?) = m_elements[objectType] ?: m_elements["$DEFAULT_SCOUT_JS_NAMESPACE.$objectType"]

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
            return m_elements.values
                    .filter { it.name.isNotEmpty() }
                    .filter { Character.isUpperCase(it.name[0]) } // only classes
                    .filter { !it.name.contains('.') } // only top-level elements
                    .map { PropertyValue(it.shortName(), it) }
        }
        return when (property.dataType) {
            JsModelProperty.JsPropertyDataType.WIDGET -> resolveWidgetProposals()
            JsModelProperty.JsPropertyDataType.BOOL -> listOf(PropertyValue(true.toString()), PropertyValue(false.toString()))
            else -> emptyList() // we cannot know
        }
    }

    /**
     * @param element The [JsModelElement] to check.
     * @return true if the [element] is a [JsModelClass] that inherits from scout.Widget.
     */
    fun isWidget(element: JsModelElement?): Boolean {
        val modelClass = element as? JsModelClass ?: return false
        return visitElementAndParents(modelClass) {
            it.name != WIDGET_CLASS_NAME || it.scoutJsModule.name != IWebConstants.SCOUT_JS_CORE_MODULE_NAME
        }
    }

    private fun visitElementAndParents(modelElement: JsModelElement, visitor: (JsModelElement) -> Boolean): Boolean {
        val superElements = ArrayDeque<JsModelElement>()
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

    private fun resolveWidgetProposals() = m_elements
            .values
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

    private fun parseModule(moduleRoot: VirtualFile): JsModule? {
        val packageJsonData = moduleRoot.findChild(PackageJsonUtil.FILE_NAME)?.let { PackageJsonUtil.getOrCreateData(it) } ?: return null
        val main = packageJsonData.main ?: return null
        val moduleName = packageJsonData.name?.takeIf { it.startsWith('@') } ?: return null
        val mainFile = moduleRoot.findFileByRelativePath(main)?.takeIf { it.isValid } ?: return null
        val ns = detectNamespace(mainFile) ?: return null
        return JsModule(moduleName, ns, mainFile.parent, mainFile, this)
    }

    private fun detectNamespace(moduleMainFile: VirtualFile): String? {
        val fileContent = moduleMainFile
                .takeIf { it.isValid }
                ?.takeIf { it.isInLocalFileSystem }
                ?.contentAsText() ?: return null
        val matcher = NAMESPACE_PATTERN.matcher(fileContent)
        var ns: String? = null
        while (matcher.find()) {
            ns = matcher.group(1)
        }
        return ns
    }

    private fun parseModuleFiles(module: JsModule, psiManager: PsiManager) = module.files
            .filter { !it.name.endsWith(ADAPTER_FILE_SUFFIX) }
            .filter { !it.name.endsWith(MODEL_FILE_SUFFIX) }
            .forEach { child ->
                val jsFile = psiManager.findFile(child) as? JSFile
                jsFile?.let { parseJsFile(it, module) }
            }

    private fun parseJsFile(jsFile: JSFile, module: JsModule) {
        jsFile.accept(object : JSRecursiveWalkingElementSkippingNestedFunctionsVisitor() {
            override fun visitJSClass(aClass: JSClass) = parseJsClass(aClass, module)
            override fun visitJSVariable(node: JSVariable) = parseTopLevelEnum(node, module)
        })
    }

    private fun parseJsClass(jsClass: JSClass, module: JsModule) {
        parseClass(jsClass, module)

        // nested enum
        jsClass.acceptChildren(object : JSElementVisitor() {
            override fun visitJSVarStatement(node: JSVarStatement) {
                if (node.attributeList?.hasModifier(JSAttributeList.ModifierType.STATIC) == true) {
                    node.stubSafeVariables.forEach { parseNestedEnum(it, jsClass, module) }
                }
            }
        })
    }

    private fun parseTopLevelEnum(jsVariable: JSVariable, module: JsModule) = registerElement(JsModelEnum.parse(jsVariable, null, module))

    private fun parseNestedEnum(staticField: JSVariable, parentClass: JSClass, module: JsModule) = registerElement(JsModelEnum.parse(staticField, parentClass, module))

    private fun parseClass(jsClass: JSClass, module: JsModule) = registerElement(JsModelClass.parse(jsClass, module))

    private fun registerElement(toRegister: JsModelElement?) {
        if (toRegister == null) return

        val qualifiedName = toRegister.qualifiedName()
        val previous = m_elements.put(qualifiedName, toRegister)
        if (previous != null) {
            m_elements[qualifiedName] = chooseElement(toRegister, previous)
            SdkLog.info("Duplicate JS element '{}' in module '{}'.", qualifiedName, toRegister.scoutJsModule.name)
        }
    }

    private fun chooseElement(a: JsModelElement, b: JsModelElement) = if (a.properties.size > b.properties.size) a else b

    /**
     * Represents a possible [JsModelProperty] value to insert into the source. Can be obtained using [JsModel.valuesForProperty]
     */
    data class PropertyValue(val displayText: String, val element: JsModelElement? = null)
}