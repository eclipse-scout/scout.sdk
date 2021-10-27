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

import com.intellij.javascript.nodejs.PackageJsonData
import com.intellij.lang.javascript.buildTools.npm.PackageJsonUtil
import com.intellij.lang.javascript.inspections.JSRecursiveWalkingElementSkippingNestedFunctionsVisitor
import com.intellij.lang.javascript.psi.JSElementVisitor
import com.intellij.lang.javascript.psi.JSFile
import com.intellij.lang.javascript.psi.JSVarStatement
import com.intellij.lang.javascript.psi.JSVariable
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList
import com.intellij.lang.javascript.psi.ecmal4.JSClass
import com.intellij.openapi.vfs.VfsUtilCore.iterateChildrenRecursively
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import org.eclipse.scout.sdk.core.log.SdkLog
import org.eclipse.scout.sdk.core.s.IWebConstants
import org.eclipse.scout.sdk.s2i.contentAsText
import java.util.regex.Pattern

/**
 * @param name The module name as declared by the "name" property of the package.json file (e.g. '@eclipse-scout/core').
 * @param namespace The namespace of the module (e.g. 'scout' or 'helloworld').
 * @param sourceRoot The directory containing the [JsModule.mainFile]
 * @param mainFile The [VirtualFile] the "main" property of the package.json points to.
 * @param jsModel The owning [JsModel]
 */
class JsModule(val name: String, val namespace: String, val sourceRoot: VirtualFile, val mainFile: VirtualFile, val moduleRoot: VirtualFile, val jsModel: JsModel) {

    companion object {
        private const val ADAPTER_FILE_SUFFIX = "Adapter${IWebConstants.JS_FILE_SUFFIX}"
        private const val MODEL_FILE_SUFFIX = "Model${IWebConstants.JS_FILE_SUFFIX}"
        private val NAMESPACE_PATTERN = Pattern.compile("window\\.([\\w._]+)\\s*=\\s*Object\\.assign\\(window\\.")

        fun parse(moduleRoot: VirtualFile, model: JsModel): JsModule? {
            val packageJsonData = moduleRoot.findChild(PackageJsonUtil.FILE_NAME)?.let { PackageJsonData.getOrCreate(it) } ?: return null
            val main = packageJsonData.main ?: return null
            val moduleName = packageJsonData.name?.takeIf { it.startsWith('@') } ?: return null
            val mainFile = moduleRoot.findFileByRelativePath(main)?.takeIf { it.isValid } ?: return null
            val ns = detectNamespace(mainFile) ?: return null
            return JsModule(moduleName, ns, mainFile.parent, mainFile, moduleRoot, model)
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
    }

    /**
     * All (canonical) js files found within the [JsModule.sourceRoot] (recursively).
     */
    val files = collectFiles()
    private val m_elements = HashMap<String /*qualified element name*/, AbstractJsModelElement>()

    @Volatile
    private var m_parsed = false

    private fun collectFiles(): Set<VirtualFile> {
        val files = HashSet<VirtualFile>()
        iterateChildrenRecursively(sourceRoot, this::acceptFile) { child ->
            child
                    .takeUnless { it.isDirectory }
                    ?.canonicalFile
                    ?.let { files.add(it) }
            true // keep on iterating
        }
        return files
    }

    private fun acceptFile(fileOrDirectory: VirtualFile): Boolean {
        if (!fileOrDirectory.isValid || !fileOrDirectory.isInLocalFileSystem) {
            return false
        }
        if (fileOrDirectory.isDirectory) {
            return true
        }
        return fileOrDirectory.name.endsWith(IWebConstants.JS_FILE_SUFFIX)
    }

    /**
     * @return A [Collection] containing [JsModelClass] instances as well as top-level and nested [JsModelEnum] instances.
     */
    fun elements(): Sequence<AbstractJsModelElement> = synchronized(m_elements) {
        m_elements.values.asSequence()
    }

    /**
     * @param objectType The objectType (e.g. 'scout.GroupBox'). The default 'scout' namespace may be omitted. All other namespaces are required.
     * @return The [AbstractJsModelElement] that corresponds to the given [objectType]. May be a [JsModelClass] or a top-level or nested [JsModelEnum].
     */
    fun element(objectType: String?) = synchronized(m_elements) {
        m_elements[objectType] ?: m_elements["${JsModel.SCOUT_JS_NAMESPACE}.$objectType"]
    }

    fun isParsed() = m_parsed

    internal fun parseModelElements(psiManager: PsiManager) = synchronized(m_elements) {
        m_elements.clear()
        files
                .filter { !it.name.endsWith(ADAPTER_FILE_SUFFIX) }
                .filter { !it.name.endsWith(MODEL_FILE_SUFFIX) }
                .mapNotNull { psiManager.findFile(it) as? JSFile }
                .forEach { parseJsFile(it) }
        m_parsed = true
        SdkLog.debug("Parsed module '{}'.", this)
    }

    private fun parseJsFile(jsFile: JSFile) {
        try {
            jsFile.accept(object : JSRecursiveWalkingElementSkippingNestedFunctionsVisitor() {
                override fun visitJSClass(aClass: JSClass) = parseJsClass(aClass)
                override fun visitJSVariable(node: JSVariable) = parseTopLevelEnum(node)
            })
        } catch (e: RuntimeException) {
            SdkLog.warning("Cannot parse file '{}'. Ignoring content.", jsFile.name, e)
        }
    }

    private fun parseJsClass(jsClass: JSClass) {
        parseClass(jsClass)

        // nested enum
        jsClass.acceptChildren(object : JSElementVisitor() {
            override fun visitJSVarStatement(node: JSVarStatement) {
                if (node.attributeList?.hasModifier(JSAttributeList.ModifierType.STATIC) == true) {
                    node.stubSafeVariables.forEach { parseNestedEnum(it, jsClass) }
                }
            }
        })
    }

    private fun parseTopLevelEnum(jsVariable: JSVariable) = registerElement(JsModelEnum.parse(jsVariable, null, this))

    private fun parseNestedEnum(staticField: JSVariable, parentClass: JSClass) = registerElement(JsModelEnum.parse(staticField, parentClass, this))

    private fun parseClass(jsClass: JSClass) = registerElement(JsModelClass.parse(jsClass, this))

    private fun registerElement(toRegister: AbstractJsModelElement?) {
        if (toRegister == null) return

        val qualifiedName = toRegister.qualifiedName()
        val previous = m_elements.put(qualifiedName, toRegister)
        if (previous != null) {
            m_elements[qualifiedName] = chooseElement(toRegister, previous)
            SdkLog.info("Duplicate JS element '{}' in module '{}'.", qualifiedName, this)
        }
    }

    private fun chooseElement(a: AbstractJsModelElement, b: AbstractJsModelElement) = if (a.properties.size > b.properties.size) a else b

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as JsModule
        return name == other.name
    }

    override fun hashCode() = name.hashCode()

    override fun toString() = name
}