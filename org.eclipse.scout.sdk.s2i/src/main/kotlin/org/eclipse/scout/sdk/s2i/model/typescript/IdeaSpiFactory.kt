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
import org.eclipse.scout.sdk.core.typescript.model.api.IConstantValue
import org.eclipse.scout.sdk.core.typescript.model.spi.*
import java.util.concurrent.ConcurrentHashMap

class IdeaSpiFactory(val ideaNodeModule: IdeaNodeModule) {

    private val m_elements = ConcurrentHashMap<Pair<Class<*>, Any?>, Any?>()

    fun createTypeScriptClass(tsClass: TypeScriptClass) = getOrCreate(tsClass, ES6ClassSpi::class.java) { IdeaTypeScriptClass(ideaNodeModule, it) }

    fun createTypeScriptInterface(tsInterface: TypeScriptInterface) = getOrCreate(tsInterface, ES6ClassSpi::class.java) { IdeaTypeScriptInterface(ideaNodeModule, it) }

    fun createTypeScriptFunction(tsFunction: TypeScriptFunction) = getOrCreate(tsFunction, FunctionSpi::class.java) { IdeaTypeScriptFunction(ideaNodeModule, it) }

    fun createTypeScriptTypeAlias(tsTypeAlias: TypeScriptTypeAlias) = getOrCreate(tsTypeAlias, ES6ClassSpi::class.java) { IdeaTypeScriptType(ideaNodeModule, it) }

    fun createJavaScriptClass(jsClass: JSClass) = getOrCreate(jsClass, ES6ClassSpi::class.java) { IdeaJavaScriptClass(ideaNodeModule, it) }

    fun createJavaScriptFunction(jsFunction: JSFunction) = getOrCreate(jsFunction, FunctionSpi::class.java) { IdeaJavaScriptFunction(ideaNodeModule, it) }

    fun createObjectLiteralExpression(jsObjectLiteral: JSObjectLiteralExpression) = getOrCreate(jsObjectLiteral, ObjectLiteralSpi::class.java) { IdeaJavaScriptObjectLiteral(ideaNodeModule, it) }

    fun createJavaScriptField(jsField: JSField) = getOrCreate(jsField, FieldSpi::class.java) { IdeaJavaScriptField(ideaNodeModule, it) }

    fun createRecordField(property: JSRecordType.PropertySignature) = getOrCreate(property, FieldSpi::class.java) { IdeaRecordField(ideaNodeModule, it) }

    fun createJavaScriptVariable(jsVariable: JSVariable) = getOrCreate(jsVariable, VariableSpi::class.java) { IdeaJavaScriptVariable(ideaNodeModule, it) }

    fun createConstantValue(jsElement: JSElement?) = getOrCreate(jsElement, IConstantValue::class.java) { IdeaConstantValue(ideaNodeModule, it) }

    fun createExportFrom(exportDeclaration: JSElement, exportName: String, referencedElement: NodeElementSpi) = getOrCreate(referencedElement, ExportFromSpi::class.java) {
        IdeaExportFrom(ideaNodeModule, exportDeclaration, exportName, it)
    }

    fun createPackageJson(moduleDir: VirtualFile) = getOrCreate(moduleDir, PackageJsonSpi::class.java) { IdeaPackageJson(ideaNodeModule, it) }

    fun createJavaScriptAssignmentExpressionAsField(jsAssignmentExpression: JSAssignmentExpression, jsReferenceExpression: JSReferenceExpression) = getOrCreate(jsAssignmentExpression, FieldSpi::class.java) {
        IdeaJavaScriptAssignmentExpressionAsField(ideaNodeModule, it, jsReferenceExpression)
    }

    fun createJavaScriptDocCommentTypeAsDataType(type: String) = getOrCreate(type, DataTypeSpi::class.java) { IdeaJavaScriptDocCommentTypeAsDataType(it) }

    fun createJavaScriptType(jsType: JSType) = getOrCreate(jsType, DataTypeSpi::class.java) { IdeaJavaScriptType(it) }

    private fun <ID, R> getOrCreate(identifier: ID, typeClass: Class<*>, factory: (ID) -> R): R {
        val key = typeClass to identifier
        @Suppress("UNCHECKED_CAST")
        return m_elements.computeIfAbsent(key) { factory(identifier) } as R
    }
}