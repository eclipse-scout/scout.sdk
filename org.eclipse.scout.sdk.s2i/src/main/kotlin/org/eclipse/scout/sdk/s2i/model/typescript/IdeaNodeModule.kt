/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2i.model.typescript

import com.intellij.lang.ecmascript6.psi.*
import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.lang.javascript.inspections.JSRecursiveWalkingElementSkippingNestedFunctionsVisitor
import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList
import com.intellij.lang.javascript.psi.ecmal4.JSClass
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiReference
import org.eclipse.scout.sdk.core.log.SdkLog
import org.eclipse.scout.sdk.core.typescript.IWebConstants
import org.eclipse.scout.sdk.core.typescript.model.api.INodeElement
import org.eclipse.scout.sdk.core.typescript.model.api.INodeModule
import org.eclipse.scout.sdk.core.typescript.model.api.internal.NodeModuleImplementor
import org.eclipse.scout.sdk.core.typescript.model.spi.*
import org.eclipse.scout.sdk.core.util.FinalValue
import org.eclipse.scout.sdk.core.util.SourceRange
import org.eclipse.scout.sdk.s2i.model.typescript.factory.IdeaNodeElementFactory
import java.nio.CharBuffer
import java.nio.file.Path
import java.util.*
import java.util.Collections.emptyList
import java.util.Collections.unmodifiableMap
import java.util.stream.Collectors.toMap

class IdeaNodeModule(val moduleInventory: IdeaNodeModules, internal val nodeModuleDir: VirtualFile) : AbstractNodeElementSpi<INodeModule>(null), NodeModuleSpi {

    private val m_nodeElementFactory = IdeaNodeElementFactory(this)
    private val m_mainFile = FinalValue<VirtualFile>()
    private val m_mainPsi = FinalValue<JSFile>()
    private val m_source = FinalValue<Optional<SourceRange>>()
    private val m_packageJsonSpi = FinalValue<PackageJsonSpi>()
    private val m_elements = FinalValue<Map<NodeElementSpi, List<String>>>()
    private val m_exports = FinalValue<Map<String, NodeElementSpi>>()
    private val m_elementsByPsi = FinalValue<Map<PsiElement, NodeElementSpi>>()
    private val m_classes = FinalValue<List<ES6ClassSpi>>()

    override fun containingModule() = this

    override fun exportType() = INodeElement.ExportType.NAMED

    override fun nodeElementFactory(): IdeaNodeElementFactory = m_nodeElementFactory

    override fun resolveContainingFile(): Path? = packageJson().containingFile().orElse(null)

    override fun packageJson(): PackageJsonSpi = m_packageJsonSpi.computeIfAbsentAndGet { nodeElementFactory().createPackageJson(nodeModuleDir) }

    override fun createApi() = NodeModuleImplementor(this, packageJson())

    override fun classes(): List<ES6ClassSpi> = m_classes.computeIfAbsentAndGet {
        elementsByPsi().values.asSequence()
            .mapNotNull { it as? ES6ClassSpi }
            .toList()
    }

    fun elementsByPsi(): Map<PsiElement, NodeElementSpi> = m_elementsByPsi.computeIfAbsentAndGet {
        val classesByPsi = elements().keys.asSequence()
            .mapNotNull { getPsiForSpi(it)?.let { psi -> psi to it } }
            .toMap(LinkedHashMap())
        return@computeIfAbsentAndGet unmodifiableMap(classesByPsi)
    }

    override fun elements(): Map<NodeElementSpi, List<String> /* export name */> = m_elements.computeIfAbsentAndGet(this::computeNodeElements)

    override fun exports(): Map<String, NodeElementSpi> = m_exports.computeIfAbsentAndGet {
        val exports = elements().entries.stream()
            .flatMap { it.value.map { alias -> alias to it.key }.stream() }
            .collect(toMap({ it.first }, { it.second }, { _, b ->
                SdkLog.warning("Duplicate export in '{}'.", packageJson().api().name())
                b
            }, { LinkedHashMap() }))
        return@computeIfAbsentAndGet unmodifiableMap(exports)
    }

    fun resolveReferencedElement(element: JSElement) = moduleInventory.resolveReferencedElement(element, this)

    fun resolveImport(importSpecifier: ES6ImportSpecifier) = moduleInventory.resolveImport(importSpecifier, this)

    private fun computeNodeElements(): Map<NodeElementSpi, List<String>> {
        val exportedElements = resolveModuleExports()
        val notExportedElements = resolveNotExportedElements(exportedElements.keys)

        val allElements = LinkedHashMap<NodeElementSpi, List<String>>(exportedElements.size + notExportedElements.size)
        notExportedElements.asSequence()
            .mapNotNull { createSpiForPsi(it) }
            .map { it to emptyList<String>() }
            .toMap(allElements)
        exportedElements.entries.asSequence()
            .mapNotNull { createSpiForPsi(it.key)?.let { spi -> spi to it.value } }
            .toMap(allElements)
        return unmodifiableMap(allElements)
    }

    private fun resolveNotExportedElements(exportedElements: Set<JSElement>): List<JSElement> {
        val srcRoot = nodeModuleDir.findFileByRelativePath(IWebConstants.MAIN_JS_SOURCE_FOLDER)?.takeIf { it.isValid }
            ?: nodeModuleDir.findFileByRelativePath(IWebConstants.JS_SOURCE_FOLDER)?.takeIf { it.isValid }
            ?: return emptyList()
        // search for elements not exported in the main file
        // such elements have no export alias but may still be available inside the own module
        val psiManager = PsiManager.getInstance(moduleInventory.project)
        val result = ArrayList<JSElement>()
        val mainFile = mainFile()
        VfsUtilCore.iterateChildrenRecursively(srcRoot, this::acceptFile) { child ->
            child.takeUnless { it.isDirectory }.takeUnless { it == mainFile }
                ?.let { psiManager.findFile(it) as? JSFile }
                ?.let { findAllExportedElements(it).filter { candidate -> !exportedElements.contains(candidate) } }
                ?.forEach { result.add(it) }
            true // keep on iterating
        }
        return result
    }

    private fun acceptFile(fileOrDirectory: VirtualFile): Boolean {
        if (!fileOrDirectory.isValid || !fileOrDirectory.isInLocalFileSystem) {
            return false
        }
        if (fileOrDirectory.isDirectory) {
            return true
        }
        val fileName = fileOrDirectory.name
        return fileName.endsWith(IWebConstants.JS_FILE_SUFFIX) || fileName.endsWith(IWebConstants.TS_FILE_SUFFIX)
    }

    private fun resolveModuleExports(): Map<JSElement, List<String>> {
        val mainPsi = mainPsi()
        if (mainPsi == null) {
            SdkLog.info("No entry point found for module '{}'. Module will not export anything.", packageJson().containingDir())
            return emptyMap()
        }

        val result = LinkedHashMap<JSElement, MutableList<String>>()
        mainPsi.accept(object : JSRecursiveWalkingElementSkippingNestedFunctionsVisitor() {
            override fun skipLambdas() = true
            override fun visitES6ExportDeclaration(exportDeclaration: ES6ExportDeclaration) {
                super.visitES6ExportDeclaration(exportDeclaration)
                resolveExportedElements(exportDeclaration).forEach {
                    result.computeIfAbsent(it.first) { ArrayList() }.add(it.second)
                }
            }
        })
        return result
    }

    private fun resolveExportedElements(exportDeclaration: ES6ExportDeclaration): List<Pair<JSElement, String>> {
        if (exportDeclaration.isExportAll) {
            // export * from './OtherFile';
            // Returns all elements exported in the target file
            return exportDeclaration.fromClause
                ?.resolveReferencedElements()
                ?.mapNotNull { it as? JSFile }
                ?.flatMap { findAllExportedElements(it) }
                ?.mapNotNull { psi -> psi.name?.let { psi to it } }
                ?: emptyList()
        }

        // export {default as A, MyClass as B, MyOtherElement} from './OtherFile';
        // exports the referenced elements with their alias (if present)
        return exportDeclaration.exportSpecifiers
            .flatMap { specifier ->
                specifier.resolveOverAliases()
                    .filter { it.isValidResult }
                    .mapNotNull { it.element }
                    .flatMap { resolveExportTarget(it) }
                    .mapNotNull { psi -> specifier.declaredName?.let { psi to it } }
            }
    }

    private fun findAllExportedElements(file: JSFile): List<JSElement> {
        val result = ArrayList<JSElement>()
        file.accept(object : JSRecursiveWalkingElementSkippingNestedFunctionsVisitor() {
            override fun skipLambdas() = true
            override fun visitElement(element: PsiElement) {
                super.visitElement(element)
                if (element.node.elementType == JSTokenTypes.EXPORT_KEYWORD) {
                    resolveExportTarget(element).forEach { result.add(it) }
                }
            }

            override fun visitJSFunctionDeclaration(node: JSFunction) {
                super.visitJSFunctionDeclaration(node)
                if (node.attributeList?.hasModifier(JSAttributeList.ModifierType.EXPORT) == true) {
                    result.add(node)
                }
            }
        })
        return result
    }

    internal fun resolveExportTarget(element: PsiElement): List<JSElement> {
        var candidate: JSElement? = element as? JSElement
        if (element.node.elementType == JSTokenTypes.EXPORT_KEYWORD) {
            candidate = element.parent as? JSElement
        }
        if (candidate is JSAttributeList) {
            val attributeOwner = candidate.parent as? JSElement
            if (attributeOwner is JSVarStatement) {
                return attributeOwner.variables
                    .mapNotNull { resolveReferenceTarget(it) }
            }
            candidate = attributeOwner
        }
        if (candidate is ES6ImportExportSpecifierAlias) candidate = candidate.findAliasedElement() as? JSElement
        if (candidate is ES6ExportDefaultAssignment) {
            candidate = if (candidate.namedElement == null) candidate.expression else candidate.namedElement
        }
        return resolveReferenceTarget(candidate)?.let { listOf(it) } ?: emptyList()
    }

    internal fun resolveReferenceTarget(element: JSElement?): JSElement? {
        var ref = element as? PsiReference ?: return element
        while (true) {
            var refTarget = ref.resolve()
            if (refTarget === ref) return ref as? JSElement // self-reference (e.g. 'undefined')
            if (refTarget is JSField) refTarget = refTarget.initializer
            if (refTarget is JSPrefixExpression) refTarget = refTarget.expression
            if (refTarget is ES6ImportExportSpecifierAlias) refTarget = refTarget.findAliasedElement()
            if (refTarget is JSExportAssignment) refTarget = refTarget.stubSafeElement
            if (refTarget !is PsiReference) return refTarget as? JSElement // last reference found
            ref = refTarget // resolve next reference
        }
    }

    private fun getPsiForSpi(spi: NodeElementSpi): JSElement? {
        if (spi is IdeaJavaScriptClass) {
            return spi.javaScriptClass
        }
        if (spi is IdeaJavaScriptFunction) {
            return spi.javaScriptFunction
        }
        if (spi is IdeaJavaScriptObjectLiteral) {
            return spi.jsObjectLiteral
        }
        if (spi is IdeaJavaScriptVariable) {
            return spi.javaScriptVariable
        }
        return null
    }

    private fun createSpiForPsi(psi: JSElement): NodeElementSpi? {
        if (psi is JSClass) {
            return m_nodeElementFactory.createJavaScriptClass(psi)
        }
        if (psi is JSFunction) {
            return m_nodeElementFactory.createJavaScriptFunction(psi)
        }
        if (psi is JSObjectLiteralExpression) {
            return m_nodeElementFactory.createObjectLiteralExpression(psi)
        }
        if (psi is JSVariable) {
            return m_nodeElementFactory.createJavaScriptVariable(psi)
        }
        SdkLog.debug("Unsupported type: '" + psi::class.java.name + "' called '" + psi.name + "'.")
        return null
    }


    fun mainFile(): VirtualFile? = m_mainFile.computeIfAbsentAndGet {
        val packageJson = packageJson().api()
        val main = packageJson.main().orElse(null)
        if (main == null) {
            SdkLog.info("'{}' does not contain an entry point.", packageJson.location())
            return@computeIfAbsentAndGet null
        }
        val mainFile = nodeModuleDir.findFileByRelativePath(main)?.takeIf { it.isValid && it.exists() }
        if (mainFile == null) {
            SdkLog.info("Entry point '{}' declared in '{}' could not be found.", main, packageJson.location())
            return@computeIfAbsentAndGet null
        }
        return@computeIfAbsentAndGet mainFile
    }

    fun mainPsi(): JSFile? = m_mainPsi.computeIfAbsentAndGet {
        return@computeIfAbsentAndGet mainFile()?.let {
            PsiManager.getInstance(moduleInventory.project).findFile(it) as? JSFile // TypeScript files are also JSFiles
        }
    }

    override fun source(): Optional<SourceRange> = m_source.computeIfAbsentAndGet { sourceFor(mainPsi()) }

    internal fun sourceFor(element: PsiElement?): Optional<SourceRange> {
        val sourceRange = element
            ?.containingFile
            ?.let { PsiDocumentManager.getInstance(moduleInventory.project).getDocument(it) }
            ?.let {
                val range = element.textRange
                val elementSrc = CharBuffer.wrap(it.charsSequence, range.startOffset, range.endOffset)
                SourceRange(elementSrc, range.startOffset)
            }
        return Optional.ofNullable(sourceRange)
    }
}