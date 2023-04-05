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
import com.intellij.lang.javascript.psi.ecma6.TypeScriptTypeParameter
import com.intellij.lang.javascript.psi.ecma6.TypeScriptTypeofType
import com.intellij.lang.javascript.psi.ecmal4.JSClass
import com.intellij.openapi.vfs.VirtualFile
import org.eclipse.scout.sdk.core.typescript.model.api.IConstantValue
import org.eclipse.scout.sdk.core.typescript.model.spi.AbstractNodeElementFactorySpi
import org.eclipse.scout.sdk.core.typescript.model.spi.ES6ClassSpi
import org.eclipse.scout.sdk.core.typescript.model.spi.ObjectLiteralDataTypeSpi
import org.eclipse.scout.sdk.s2i.model.typescript.*

class IdeaNodeElementFactory(val ideaModule: IdeaNodeModule) : AbstractNodeElementFactorySpi(ideaModule) {

    fun createJavaScriptClass(jsClass: JSClass): IdeaJavaScriptClass =
        getOrCreate(jsClass) { IdeaJavaScriptClass(ideaModule, it) }

    fun createJavaScriptFunction(jsFunction: JSFunction): IdeaJavaScriptFunction =
        getOrCreate(jsFunction) { IdeaJavaScriptFunction(ideaModule, it) }

    fun createObjectLiteralExpression(jsObjectLiteral: JSObjectLiteralExpression): IdeaJavaScriptObjectLiteral =
        getOrCreate(jsObjectLiteral) { IdeaJavaScriptObjectLiteral(ideaModule, it) }

    fun createJavaScriptField(jsField: JSField, declaringClass: ES6ClassSpi): IdeaJavaScriptField =
        getOrCreate(jsField) { IdeaJavaScriptField(ideaModule, it, declaringClass) }

    fun createJavaScriptVariable(jsVariable: JSVariable): IdeaJavaScriptVariable =
        getOrCreate(jsVariable) { IdeaJavaScriptVariable(ideaModule, it) }

    fun createConstantValue(jsElement: JSElement?): IdeaConstantValue =
        getOrCreate(jsElement to IConstantValue::class.java) { IdeaConstantValue(ideaModule, jsElement) }

    fun createPackageJson(moduleDir: VirtualFile): IdeaPackageJson =
        getOrCreate(moduleDir) { IdeaPackageJson(ideaModule, it) }

    fun createJavaScriptAssignmentExpressionAsField(jsAssignmentExpression: JSAssignmentExpression, jsReferenceExpression: JSReferenceExpression, declaringClass: ES6ClassSpi): IdeaJavaScriptAssignmentExpressionAsField =
        getOrCreate(jsAssignmentExpression) { IdeaJavaScriptAssignmentExpressionAsField(ideaModule, it, jsReferenceExpression, declaringClass) }

    fun createJavaScriptType(jsType: JSType): IdeaJavaScriptType =
        getOrCreate(jsType) { IdeaJavaScriptType(ideaModule, it) }

    fun createObjectLiteralDataType(name: String, jsObjectLiteral: JSObjectLiteralExpression): ObjectLiteralDataTypeSpi =
        createObjectLiteralDataType(name, createObjectLiteralExpression(jsObjectLiteral))

    fun createTypeScriptTypeofType(typeOfType: TypeScriptTypeofType): IdeaTypeScriptTypeofType =
        getOrCreate(typeOfType) { IdeaTypeScriptTypeofType(ideaModule, it) }

    fun createTypeParameter(typeParameter: TypeScriptTypeParameter, ownerClass: ES6ClassSpi): IdeaTypeParameter =
        getOrCreate(typeParameter) { IdeaTypeParameter(ideaModule, it, ownerClass) }
}