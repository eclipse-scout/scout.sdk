/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2i.model.typescript.factory

import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass
import com.intellij.lang.javascript.psi.ecma6.TypeScriptFunction
import com.intellij.lang.javascript.psi.ecma6.TypeScriptInterface
import com.intellij.lang.javascript.psi.ecma6.TypeScriptTypeAlias
import com.intellij.lang.javascript.psi.ecmal4.JSClass
import com.intellij.openapi.vfs.VirtualFile
import org.eclipse.scout.sdk.core.typescript.model.api.IConstantValue
import org.eclipse.scout.sdk.core.typescript.model.api.IObjectLiteral
import org.eclipse.scout.sdk.core.typescript.model.spi.*
import org.eclipse.scout.sdk.s2i.model.typescript.*
import java.util.concurrent.ConcurrentHashMap

class IdeaSpiFactory(val ideaModule: IdeaNodeModule) {

    private val m_elements = ConcurrentHashMap<Any?, Any?>()

    fun createTypeScriptClass(tsClass: TypeScriptClass) = getOrCreate(tsClass) { IdeaTypeScriptClass(ideaModule, it) }

    fun createTypeScriptInterface(tsInterface: TypeScriptInterface) = getOrCreate(tsInterface) { IdeaTypeScriptInterface(ideaModule, it) }

    fun createTypeScriptFunction(tsFunction: TypeScriptFunction) = getOrCreate(tsFunction) { IdeaTypeScriptFunction(ideaModule, it) }

    fun createTypeScriptTypeAlias(tsTypeAlias: TypeScriptTypeAlias) = getOrCreate(tsTypeAlias) { IdeaTypeScriptType(ideaModule, it) }

    fun createJavaScriptClass(jsClass: JSClass) = getOrCreate(jsClass) { IdeaJavaScriptClass(ideaModule, it) }

    fun createJavaScriptFunction(jsFunction: JSFunction) = getOrCreate(jsFunction) { IdeaJavaScriptFunction(ideaModule, it) }

    fun createObjectLiteralExpression(jsObjectLiteral: JSObjectLiteralExpression) = getOrCreate(jsObjectLiteral) { IdeaJavaScriptObjectLiteral(ideaModule, it) }

    fun createJavaScriptField(jsField: JSField) = getOrCreate(jsField) { IdeaJavaScriptField(ideaModule, it) }

    fun createRecordField(property: JSRecordType.PropertySignature) = getOrCreate(property) { IdeaRecordField(ideaModule, it) }

    fun createJavaScriptVariable(jsVariable: JSVariable) = getOrCreate(jsVariable) { IdeaJavaScriptVariable(ideaModule, it) }

    fun createConstantValue(jsElement: JSElement?) = getOrCreate(jsElement to IConstantValue::class.java) { IdeaConstantValue(ideaModule, jsElement) }

    fun createExportFrom(exportDeclaration: JSElement, exportName: String, referencedElement: NodeElementSpi) = getOrCreate(exportDeclaration to exportName) {
        IdeaExportFrom(ideaModule, exportDeclaration, exportName, referencedElement)
    }

    fun createPackageJson(moduleDir: VirtualFile) = getOrCreate(moduleDir) { IdeaPackageJson(ideaModule, it) }

    fun createJavaScriptAssignmentExpressionAsField(jsAssignmentExpression: JSAssignmentExpression, jsReferenceExpression: JSReferenceExpression) = getOrCreate(jsAssignmentExpression) {
        IdeaJavaScriptAssignmentExpressionAsField(ideaModule, it, jsReferenceExpression)
    }

    fun createSimpleDataType(type: String) = getOrCreate(type) { SimpleDataTypeSpi(ideaModule, type) }

    fun createJavaScriptType(jsType: JSType) = getOrCreate(jsType) { IdeaJavaScriptType(ideaModule, it) }

    fun createObjectLiteralDataType(name: String, jsObjectLiteral: JSObjectLiteralExpression) = createObjectLiteralDataType(name, createObjectLiteralExpression(jsObjectLiteral).api())

    fun createObjectLiteralDataType(name: String, objectLiteral: IObjectLiteral) = getOrCreate(name to objectLiteral) { ObjectLiteralDataTypeSpi(ideaModule, name, objectLiteral) }

    fun createArrayDataType(componentDataType: DataTypeSpi?, arrayDimension: Int): SimpleCompositeDataTypeSpi =
        getOrCreate(componentDataType to arrayDimension) { SimpleCompositeDataTypeSpi.createArray(ideaModule, componentDataType, arrayDimension) }

    @Suppress("UNCHECKED_CAST")
    private fun <ID, R> getOrCreate(identifier: ID, factory: (ID) -> R) = m_elements.computeIfAbsent(identifier) { factory(identifier) } as R
}