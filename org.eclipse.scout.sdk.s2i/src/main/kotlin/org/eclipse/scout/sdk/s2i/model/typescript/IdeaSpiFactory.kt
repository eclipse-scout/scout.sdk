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

import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass
import com.intellij.lang.javascript.psi.ecma6.TypeScriptFunction
import com.intellij.lang.javascript.psi.ecma6.TypeScriptInterface
import com.intellij.lang.javascript.psi.ecma6.TypeScriptTypeAlias
import com.intellij.lang.javascript.psi.ecmal4.JSClass
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import org.eclipse.scout.sdk.core.typescript.model.api.IConstantValue
import org.eclipse.scout.sdk.core.typescript.model.spi.NodeElementSpi
import org.eclipse.scout.sdk.core.util.SdkException
import java.util.concurrent.ConcurrentHashMap

class IdeaSpiFactory(val ideaNodeModules: IdeaNodeModules) {

    private val m_elements = ConcurrentHashMap<Any?, Any?>()

    fun createTypeScriptClass(tsClass: TypeScriptClass) = getOrCreate(tsClass) { module, psi -> IdeaTypeScriptClass(module, psi) }

    fun createTypeScriptInterface(tsInterface: TypeScriptInterface) = getOrCreate(tsInterface) { module, psi -> IdeaTypeScriptInterface(module, psi) }

    fun createTypeScriptFunction(tsFunction: TypeScriptFunction) = getOrCreate(tsFunction) { module, psi -> IdeaTypeScriptFunction(module, psi) }

    fun createTypeScriptTypeAlias(tsTypeAlias: TypeScriptTypeAlias) = getOrCreate(tsTypeAlias) { module, psi -> IdeaTypeScriptType(module, psi) }

    fun createJavaScriptClass(jsClass: JSClass) = getOrCreate(jsClass) { module, psi -> IdeaJavaScriptClass(module, psi) }

    fun createJavaScriptFunction(jsFunction: JSFunction) = getOrCreate(jsFunction) { module, psi -> IdeaJavaScriptFunction(module, psi) }

    fun createObjectLiteralExpression(jsObjectLiteral: JSObjectLiteralExpression) = getOrCreate(jsObjectLiteral) { module, psi -> IdeaJavaScriptObjectLiteral(module, psi) }

    fun createJavaScriptField(jsField: JSField) = getOrCreate(jsField) { module, psi -> IdeaJavaScriptField(module, psi) }

    fun createRecordField(property: JSRecordType.PropertySignature, module: IdeaNodeModule) = getOrCreate(property, module) { m, psi -> IdeaRecordField(m, psi) }

    fun createJavaScriptVariable(jsVariable: JSVariable) = getOrCreate(jsVariable) { module, psi -> IdeaJavaScriptVariable(module, psi) }

    fun createConstantValue(jsElement: JSElement?, module: IdeaNodeModule) = getOrCreate(jsElement to IConstantValue::class.java, module) { m, _ -> IdeaConstantValue(m, jsElement) }

    fun createExportFrom(exportDeclaration: JSElement, exportName: String, referencedElement: NodeElementSpi) = getOrCreate(exportDeclaration to exportName, exportDeclaration) { module, _ ->
        IdeaExportFrom(module, exportDeclaration, exportName, referencedElement)
    }

    fun createPackageJson(moduleDir: VirtualFile, module: IdeaNodeModule) = getOrCreate(moduleDir, module) { m, psi -> IdeaPackageJson(m, psi) }

    fun createJavaScriptAssignmentExpressionAsField(jsAssignmentExpression: JSAssignmentExpression, jsReferenceExpression: JSReferenceExpression) = getOrCreate(jsAssignmentExpression) { module, psi ->
        IdeaJavaScriptAssignmentExpressionAsField(module, psi, jsReferenceExpression)
    }

    fun createJavaScriptDocCommentTypeAsDataType(type: String) = getOrCreate(type) { IdeaJavaScriptDocCommentTypeAsDataType(type) }

    fun createJavaScriptType(jsType: JSType) = getOrCreate(jsType) { IdeaJavaScriptType(it) }

    private fun <ID : PsiElement, R> getOrCreate(psi: ID, factory: (IdeaNodeModule, ID) -> R) = getOrCreate(psi, psi, factory)

    private fun <ID, R> getOrCreate(identifier: ID, psi: PsiElement, factory: (IdeaNodeModule, ID) -> R) = getOrCreate(identifier) {
        val module = ideaNodeModules.findContainingModule(psi) ?: throw SdkException("Cannot find module for psi '{}'.", psi)
        factory(module, it)
    }

    private fun <ID, R> getOrCreate(identifier: ID, module: IdeaNodeModule, factory: (IdeaNodeModule, ID) -> R) = getOrCreate(identifier) { factory(module, it) }

    @Suppress("UNCHECKED_CAST")
    private fun <ID, R> getOrCreate(identifier: ID, factory: (ID) -> R) = m_elements.computeIfAbsent(identifier) { factory(identifier) } as R
}