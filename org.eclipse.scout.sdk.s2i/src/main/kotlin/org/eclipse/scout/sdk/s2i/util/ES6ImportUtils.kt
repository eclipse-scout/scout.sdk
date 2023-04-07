/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2i.util

import com.intellij.lang.ecmascript6.psi.ES6ImportDeclaration
import com.intellij.lang.javascript.psi.impl.JSPsiElementFactory
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.refactoring.extractMethod.newImpl.ExtractMethodHelper.addSiblingAfter
import com.intellij.util.ThrowableRunnable
import org.eclipse.scout.sdk.core.typescript.IWebConstants
import org.eclipse.scout.sdk.core.typescript.model.api.INodeElement
import org.eclipse.scout.sdk.core.typescript.model.api.INodeModule
import org.eclipse.scout.sdk.core.util.Strings
import org.eclipse.scout.sdk.s2i.resolveLocalPath
import java.util.*

object ES6ImportUtils {

    fun createOrUpdateImport(importedElement: INodeElement, importAlias: String?, modelOfImporter: INodeModule, importerElement: PsiElement) {
        var importName = importedElement.moduleExportNames().stream().findAny().orElse(importedElement.name()) // use any of the exported names for the element
        val firstDot = importName.indexOf('.')
        if (firstDot > 0) importName = importName.take(firstDot)

        val fromFile = importerElement.containingFile?.virtualFile?.resolveLocalPath() ?: return
        val importFrom = importedElement.computeImportPathFrom(modelOfImporter, fromFile).orElse(null) ?: return

        // default import for relative path in the same module only.
        // imports to module main files are no default imports for now.
        val isDefaultImport = importedElement.containingModule() == modelOfImporter
                && !importedElement.isExportedFromModule
                && importedElement.exportType() == INodeElement.ExportType.DEFAULT
        if (!ApplicationManager.getApplication().isWriteAccessAllowed) {
            WriteCommandAction.writeCommandAction(importerElement.project).run(ThrowableRunnable<RuntimeException> {
                createOrUpdateImport(importName, importAlias, isDefaultImport, importFrom, importerElement)
            })
        } else {
            createOrUpdateImport(importName, importAlias, isDefaultImport, importFrom, importerElement)
        }
    }

    fun importsInFileOf(element: PsiElement): List<ES6ImportDeclaration> = element.containingFile?.let {
        PsiTreeUtil.getChildrenOfTypeAsList(it, ES6ImportDeclaration::class.java)
    } ?: emptyList()

    /**
     * Own implementation as JSImportAction is internal API and ES6ImportPsiUtil does not work correctly in TypeScript
     */
    fun createOrUpdateImport(importName: String, alias: String?, isDefaultImport: Boolean, importFrom: String, context: PsiElement) {
        val psiFile = context.containingFile
        val imports = importsInFileOf(psiFile)
        val importFromClean = importFrom.removeSuffix(IWebConstants.TS_FILE_SUFFIX).removeSuffix(IWebConstants.JS_FILE_SUFFIX)
        val existingImport = imports.firstOrNull { Strings.withoutQuotes(it.fromClause?.referenceText).removeSuffix(IWebConstants.TS_FILE_SUFFIX).removeSuffix(IWebConstants.JS_FILE_SUFFIX) == importFromClean }
        val addToBindings = isDefaultImport && alias == null
        val referenceName = if (isDefaultImport && alias != null) "default" else importName
        val newImportExpression = if (alias == null) importName else "$referenceName as $alias"
        if (existingImport != null) {
            // check if already imported
            val existingSpecifiers = existingImport.importSpecifiers
            if (!addToBindings && existingSpecifiers.any { it.referenceName == referenceName }) return // specifier import already exists
            val existingBindings = existingImport.importedBindings
            if (addToBindings && existingBindings.any { it.declaredName == importName }) return // binding import already exists

            // add new specifier to existing import declaration
            val specifiers = existingSpecifiers.map { it.text }.toMutableList()
            val bindings = existingBindings.map { it.text }.toMutableList()
            if (addToBindings) bindings.add(newImportExpression) else specifiers.add(newImportExpression)
            var newSpecifiers = ""
            if (specifiers.isNotEmpty()) {
                newSpecifiers = '{' + specifiers.sortedBy { it.lowercase(Locale.US) }.joinToString(", ") + '}'
            }
            val newBindings = bindings.sortedBy { it.lowercase(Locale.US) }.joinToString(", ")

            val newImports = listOf(newBindings, newSpecifiers)
                .filter { !Strings.isBlank(it) }
                .joinToString(", ")
            val newImport = JSPsiElementFactory.createJSSourceElement("import $newImports from '$importFromClean';", context, existingImport.javaClass)
            existingImport.replace(newImport)
            return
        }

        // add new import declaration
        var allImports = newImportExpression
        if (!addToBindings) {
            allImports = "{$allImports}"
        }
        val importToAdd = JSPsiElementFactory.createJSSourceElement("import $allImports from '$importFrom';", context, ES6ImportDeclaration::class.java)
        if (imports.isEmpty()) {
            val firstNotComment = psiFile.children.firstOrNull { it !is PsiComment } ?: psiFile.children.firstOrNull()
            psiFile.addBefore(importToAdd, firstNotComment)
        } else {
            imports.last().addSiblingAfter(importToAdd)
        }
    }
}