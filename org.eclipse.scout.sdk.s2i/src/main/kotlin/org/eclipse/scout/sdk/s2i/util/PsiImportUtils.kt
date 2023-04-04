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
import org.eclipse.scout.sdk.core.typescript.model.api.INodeElement
import org.eclipse.scout.sdk.core.typescript.model.api.INodeModule
import org.eclipse.scout.sdk.core.util.Strings
import org.eclipse.scout.sdk.s2i.resolveLocalPath

object PsiImportUtils {

    fun createOrUpdateImport(elementToImport: INodeElement, fromModel: INodeModule, fromElement: PsiElement) {
        var importName = elementToImport.moduleExportNames().stream().findAny().orElse(elementToImport.name()) // use any of the exported names for the element
        val firstDot = importName.indexOf('.')
        if (firstDot > 0) importName = importName.take(firstDot)

        val fromFile = fromElement.containingFile.virtualFile.resolveLocalPath() ?: return
        val importFrom = elementToImport.computeImportPathFrom(fromModel, fromFile).orElse(null) ?: return

        // default import for relative path in the same module only.
        // imports to module main files are no default imports for now.
        val isDefaultImport = elementToImport.containingModule() == fromModel
                && !elementToImport.isExportedFromModule
                && elementToImport.exportType() == INodeElement.ExportType.DEFAULT
        if (!ApplicationManager.getApplication().isWriteAccessAllowed) {
            WriteCommandAction.writeCommandAction(fromElement.project).run(ThrowableRunnable<RuntimeException> {
                createOrUpdateImport(importName, isDefaultImport, importFrom, fromElement)
            })
        } else {
            createOrUpdateImport(importName, isDefaultImport, importFrom, fromElement)
        }
    }

    /**
     * Own implementation as JSImportAction is internal API and ES6ImportPsiUtil does not work correctly in TypeScript
     */
    fun createOrUpdateImport(importName: String, isDefaultImport: Boolean, importFrom: String, context: PsiElement) {
        val psiFile = context.containingFile
        val imports = PsiTreeUtil.getChildrenOfTypeAsList(psiFile, ES6ImportDeclaration::class.java)
        val existingImport = imports.firstOrNull { Strings.withoutQuotes(it.fromClause?.referenceText) == importFrom }
        if (existingImport != null) {
            // check if already imported
            val existingSpecifiers = existingImport.importSpecifiers
            if (!isDefaultImport && existingSpecifiers.any { it.declaredName == importName }) return // named import already exists
            val existingBindings = existingImport.importedBindings
            if (isDefaultImport && existingBindings.any { it.declaredName == importName }) return // default import already exists

            // add new specifier to existing import declaration
            val specifiers = existingSpecifiers.map { it.text }.toMutableList()
            val bindings = existingBindings.map { it.text }.toMutableList()
            if (isDefaultImport) bindings.add(importName) else specifiers.add(importName)
            var newSpecifiers = ""
            if (specifiers.isNotEmpty()) {
                newSpecifiers = '{' + specifiers.sorted().joinToString(", ") + '}'
            }
            val newBindings = bindings.sorted().joinToString(", ")

            val newImports = listOf(newBindings, newSpecifiers)
                .filter { !Strings.isBlank(it) }
                .joinToString(", ")

            val newImport = JSPsiElementFactory.createJSSourceElement("import $newImports from '$importFrom';", context, existingImport.javaClass)
            existingImport.replace(newImport)
            return
        }

        // add new import declaration
        var importSpecifier = importName
        if (!isDefaultImport) {
            importSpecifier = "{$importSpecifier}"
        }
        val importToAdd = JSPsiElementFactory.createJSSourceElement("import $importSpecifier from '$importFrom';", context, ES6ImportDeclaration::class.java)
        if (imports.isEmpty()) {
            val firstNotComment = psiFile.children.firstOrNull { it !is PsiComment } ?: psiFile.children.firstOrNull()
            psiFile.addBefore(importToAdd, firstNotComment)
        } else {
            imports.last().addSiblingAfter(importToAdd)
        }
    }
}