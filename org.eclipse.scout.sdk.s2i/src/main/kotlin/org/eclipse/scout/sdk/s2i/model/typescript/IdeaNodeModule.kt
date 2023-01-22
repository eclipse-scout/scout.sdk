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

import com.intellij.lang.ecmascript6.psi.ES6ExportDeclaration
import com.intellij.lang.ecmascript6.psi.ES6ExportDefaultAssignment
import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.lang.javascript.inspections.JSRecursiveWalkingElementSkippingNestedFunctionsVisitor
import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass
import com.intellij.lang.javascript.psi.ecma6.TypeScriptFunction
import com.intellij.lang.javascript.psi.ecma6.TypeScriptInterface
import com.intellij.lang.javascript.psi.ecma6.TypeScriptTypeAlias
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList
import com.intellij.lang.javascript.psi.ecmal4.JSClass
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import org.eclipse.scout.sdk.core.log.SdkLog
import org.eclipse.scout.sdk.core.typescript.model.api.INodeModule
import org.eclipse.scout.sdk.core.typescript.model.api.internal.NodeModuleImplementor
import org.eclipse.scout.sdk.core.typescript.model.spi.*
import org.eclipse.scout.sdk.core.util.FinalValue
import org.eclipse.scout.sdk.core.util.SourceRange
import java.nio.CharBuffer
import java.util.*
import java.util.Collections.unmodifiableMap

open class IdeaNodeModule(val project: Project, private val nodeModuleDir: VirtualFile) : AbstractNodeElementSpi<INodeModule>(null), NodeModuleSpi {

    private val m_mainFile = FinalValue<VirtualFile>()
    private val m_mainPsi = FinalValue<JSFile>()
    private val m_packageJsonSpi = FinalValue<PackageJsonSpi>()
    private val m_exports = FinalValue<Map<String, ExportFromSpi>>()

    override fun containingModule() = this

    override fun packageJson(): PackageJsonSpi = m_packageJsonSpi.computeIfAbsentAndGet { IdeaPackageJson(this, nodeModuleDir) }

    override fun createApi() = NodeModuleImplementor(this, packageJson())

    override fun exports(): Map<String, ExportFromSpi> = m_exports.computeIfAbsentAndGet(this::computeExports)

    internal fun computeExports(): Map<String, ExportFromSpi> {
        val mainPsi = mainPsi()
        if (mainPsi == null) {
            SdkLog.warning("No entry point found for module '{}'. Module will not export anything.", packageJson().containingDir())
            return emptyMap()
        }

        val result = LinkedHashMap<String, ExportFromSpi>()
        mainPsi.accept(object : JSRecursiveWalkingElementSkippingNestedFunctionsVisitor() {
            override fun skipLambdas() = true
            override fun visitES6ExportDeclaration(exportDeclaration: ES6ExportDeclaration) {
                super.visitES6ExportDeclaration(exportDeclaration)
                parseES6ExportDeclaration(exportDeclaration).forEach {
                    val old = result.put(it.name(), it)
                    if (old != null) {
                        SdkLog.warning("Duplicate export '{}' in '{}'.", it.name(), packageJson().api().location())
                    }
                }
            }
        })
        return unmodifiableMap(result)
    }

    internal fun parseES6ExportDeclaration(exportDeclaration: ES6ExportDeclaration): List<IdeaExportFrom> {
        if (exportDeclaration.isExportAll) {
            // export * from './OtherFile';
            // Returns all elements exported in the target file
            return exportDeclaration.fromClause
                ?.resolveReferencedElements()
                ?.mapNotNull { it as? JSFile }
                ?.flatMap { findAllExportedElements(it) }
                ?.distinct()
                ?.mapNotNull { createIdeaExportFrom(exportDeclaration, it) }
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
                    .mapNotNull { createIdeaExportFrom(specifier, it, specifier.declaredName) }
            }
    }

    internal fun createIdeaExportFrom(exportDeclaration: JSElement, referencedElement: JSElement, exportAlias: String? = referencedElement.name): IdeaExportFrom? {
        val exportName = exportAlias ?: return null // cannot re-export an anonymous element
        return createSpiForPsi(referencedElement)?.let { IdeaExportFrom(this, exportDeclaration, exportName, it) }
    }

    internal fun findAllExportedElements(file: JSFile): Set<JSElement> {
        val result = LinkedHashSet<JSElement>()
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
                return attributeOwner.variables.asList()
            }
            candidate = attributeOwner
        }
        if (candidate is ES6ExportDefaultAssignment) {
            candidate = if (candidate.namedElement == null) candidate.expression else candidate.namedElement
        }
        return candidate?.let { listOf(it) } ?: emptyList()
    }

    internal fun createSpiForPsi(psi: JSElement): NodeElementSpi? {
        if (psi is TypeScriptClass) {
            return IdeaTypeScriptClass(this, psi)
        }
        if (psi is TypeScriptInterface) {
            return IdeaTypeScriptInterface(this, psi)
        }
        if (psi is TypeScriptFunction) {
            return IdeaTypeScriptFunction(this, psi)
        }
        if (psi is TypeScriptTypeAlias) {
            return IdeaTypeScriptType(this, psi)
        }
        if (psi is JSClass) {
            return IdeaJavaScriptClass(this, psi)
        }
        if (psi is JSFunction) {
            return IdeaJavaScriptFunction(this, psi)
        }
        if (psi is JSObjectLiteralExpression) {
            return IdeaJavaScriptObjectLiteral(this, psi)
        }
        if (psi is JSVariable) {
            return IdeaJavaScriptVariable(this, psi)
        }
        SdkLog.warning("Unsupported type: '" + psi::class.java.name + "' called '" + psi.name + "'.")
        return null
    }

    fun mainFile(): VirtualFile? = m_mainFile.computeIfAbsentAndGet {
        val packageJson = packageJson().api()
        val main = packageJson.main().orElse(null)
        if (main == null) {
            SdkLog.warning("'{}' does not contain an entry point.", packageJson.location())
            return@computeIfAbsentAndGet null
        }
        val mainFile = nodeModuleDir.findFileByRelativePath(main)?.takeIf { it.isValid && it.exists() }
        if (mainFile == null) {
            SdkLog.warning("Entry point '{}' declared in '{}' could not be found.", main, packageJson.location())
            return@computeIfAbsentAndGet null
        }
        return@computeIfAbsentAndGet mainFile
    }

    fun mainPsi(): JSFile? = m_mainPsi.computeIfAbsentAndGet {
        return@computeIfAbsentAndGet mainFile()?.let {
            PsiManager.getInstance(project).findFile(it) as? JSFile // TypeScript files are also JSFiles
        }
    }

    override fun source() = sourceFor(mainPsi())

    internal fun sourceFor(element: PsiElement?): Optional<SourceRange> {
        val sourceRange = element
            ?.containingFile
            ?.let { PsiDocumentManager.getInstance(project).getDocument(it) }
            ?.let {
                val range = element.textRange
                val elementSrc = CharBuffer.wrap(it.charsSequence, range.startOffset, range.endOffset)
                SourceRange(elementSrc, range.startOffset)
            }
        return Optional.ofNullable(sourceRange)
    }
}