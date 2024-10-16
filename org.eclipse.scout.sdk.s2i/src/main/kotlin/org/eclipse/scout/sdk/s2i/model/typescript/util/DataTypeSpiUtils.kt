/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2i.model.typescript.util

import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.ecma6.JSTypeDeclaration
import com.intellij.lang.javascript.psi.ecma6.TypeScriptAsExpression
import com.intellij.lang.javascript.psi.ecma6.TypeScriptLiteralType
import com.intellij.lang.javascript.psi.ecma6.TypeScriptTypeofType
import com.intellij.lang.javascript.psi.jsdoc.JSDocComment
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil
import com.intellij.lang.javascript.psi.resolve.JSTypeEvaluator
import com.intellij.lang.javascript.psi.types.*
import com.intellij.lang.javascript.psi.types.evaluable.JSEvaluableOnlyType
import com.intellij.lang.javascript.psi.types.primitives.JSNullType
import com.intellij.lang.javascript.psi.types.primitives.JSUndefinedType
import org.eclipse.scout.sdk.core.typescript.model.api.IConstantValue
import org.eclipse.scout.sdk.core.typescript.model.spi.DataTypeSpi
import org.eclipse.scout.sdk.core.typescript.model.spi.ES6ClassSpi
import org.eclipse.scout.sdk.core.typescript.model.spi.FieldSpi
import org.eclipse.scout.sdk.s2i.model.typescript.IdeaConstantValue
import org.eclipse.scout.sdk.s2i.model.typescript.IdeaNodeModule
import org.eclipse.scout.sdk.s2i.util.ES6ImportUtils

object DataTypeSpiUtils {

    fun createDataType(element: JSElement, module: IdeaNodeModule, constantValue: () -> IConstantValue? = { null }): DataTypeSpi? {
        if (element is JSAssignmentExpression) {
            createDataType(element, module)?.let { return it }
        }
        if (element is JSTypeOwner) {
            createDataType(element as JSTypeOwner, module, constantValue)?.let { return it }
        }
        if (element is JSType) {
            return createDataType(element as JSType, module)
        }
        if (element is JSObjectLiteralExpression) {
            createDataType(element, module)?.let { return it }
        }
        getDataType(constantValue)?.let { return it }
        return JSResolveUtil.getElementJSType(element)
            ?.let { createDataType(it, module) }
    }

    private fun createDataType(assignment: JSAssignmentExpression, module: IdeaNodeModule): DataTypeSpi? {
        val expression = assignment.parent as? JSExpressionStatement ?: return null
        val comment = expression.children.firstNotNullOfOrNull { it as? JSDocComment } ?: return null
        return createJSDocCommentTypeDataType(comment, module)
    }

    private fun createDataType(typeOwner: JSTypeOwner, module: IdeaNodeModule, constantValue: () -> IConstantValue?): DataTypeSpi? {
        if (typeOwner.jsType is JSWrapperType || typeOwner.jsType is JSEvaluableOnlyType) {
            getDataType(constantValue)?.let { return it }
        }
        return typeOwner.jsType?.let { createDataType(it, module) }
    }

    fun createDataType(type: JSType, module: IdeaNodeModule): DataTypeSpi? {
        if (type is JSWrapperType) return createDataType(type.originalType, module)
        if (type.isJavaScript && (type is JSNullType || type is JSUndefinedType)) return null
        if (type is JSArrayType) {
            var arrayDimension = 0
            var currentType: JSType? = type
            do {
                arrayDimension++
                currentType = (currentType as JSArrayType).type
            } while (currentType is JSArrayType)
            return module.nodeElementFactory().createArrayDataType(currentType?.let { createDataType(it, module) }, arrayDimension)
        }
        if (type is JSUnionOrIntersectionType) {
            return type.types.map { createDataType(it, module) }
                .let {
                    if (type.isUnionType)
                        module.nodeElementFactory().createUnionDataType(it)
                    else
                        module.nodeElementFactory().createIntersectionDataType(it)
                }
        }
        if (type is TypeScriptTypeOfJSTypeImpl) {
            return (type.sourceElement as? TypeScriptTypeofType)?.let { module.nodeElementFactory().createTypeScriptTypeofType(it) }
        }
        if (type.isTypeScript && type is JSLiteralType) {
            (type.sourceElement as? TypeScriptLiteralType)?.expression
                ?.let { module.nodeElementFactory().createConstantValue(it) }
                ?.let { module.nodeElementFactory().createConstantValueDataType(it) }
                ?.let { return it }
        }
        if (type is JSUtilType) return module.nodeElementFactory().createJavaScriptType(type)

        resolveAsReferencedType(type, module)?.let { return it }

        return module.nodeElementFactory().createJavaScriptType(type)
    }

    private fun resolveAsReferencedType(type: JSType, module: IdeaNodeModule): DataTypeSpi? {
        val sourceElement = type.sourceElement as? JSElement ?: return null
        val reference = module.resolveReferencedElement(sourceElement) as? DataTypeSpi ?: return null

        if (reference !is ES6ClassSpi) return reference
        if (sourceElement !is JSTypeArgumentsOwner) return reference
        return resolveTypeArguments(reference, sourceElement.typeArguments, module)
    }

    fun resolveTypeArguments(es6Class: ES6ClassSpi, typeArguments: Array<out JSTypeDeclaration>, module: IdeaNodeModule): ES6ClassSpi? {
        val typeArgumentClasses = typeArguments
            .mapNotNull { createDataType(it.jsType, module) }
        if (typeArgumentClasses.isEmpty()) return es6Class
        return module.nodeElementFactory().createClassWithTypeArgumentsDataType(es6Class, typeArgumentClasses)
    }

    private fun createDataType(objectLiteral: JSObjectLiteralExpression, module: IdeaNodeModule): DataTypeSpi? {
        var parent = objectLiteral.parent
        if (parent is TypeScriptAsExpression) parent = parent.parent
        val name = when (parent) {
            is JSFieldVariable -> parent.name
            is JSProperty -> parent.name
            else -> null
        } ?: return null
        return module.nodeElementFactory().createObjectLiteralDataType(name, objectLiteral)
    }

    private val ILLEGAL_DATA_TYPE_CHARS = "[|&\\(\\)\\[\\]]".toRegex()

    fun createDataType(dataType: String, scope: JSElement, module: IdeaNodeModule): DataTypeSpi? {
        if (dataType.isEmpty() || dataType.contains(ILLEGAL_DATA_TYPE_CHARS)) return null

        val source = JSTypeSourceFactory.createTypeSource(scope, true)
        val type = JSNamedTypeFactory.createType(dataType, source, JSTypeContext.INSTANCE, false)
        return module.nodeElementFactory().createJavaScriptType(type)
    }

    private fun createJSDocCommentDataType(comment: JSDocComment, module: IdeaNodeModule, getDataType: (JSDocComment) -> String?): DataTypeSpi? {
        val dataType = getDataType(comment) ?: return null

        ES6ImportUtils.importsInFileOf(comment).asSequence()
            .flatMap { it.importSpecifiers.asSequence() }
            .firstOrNull { it.declaredName == dataType }
            ?.let { module.resolveImport(it) as? DataTypeSpi }
            ?.let { return it }

        return createDataType(dataType, comment, module)
    }

    private fun createJSDocCommentTypeDataType(comment: JSDocComment, module: IdeaNodeModule) = createJSDocCommentDataType(comment, module) { it.type }

    fun createDataType(constantValue: IdeaConstantValue): DataTypeSpi? {
        constantValue.referencedElement()
            ?.let { it as? FieldSpi }
            ?.declaringClass()
            ?.takeIf { it.isEnum }
            ?.let { return it }

        constantValue.referencedConstantValue()
            ?.let { referencedConstantValue ->
                (referencedConstantValue.element?.parent as? JSProperty)
                    ?.let { it.parent as? JSObjectLiteralExpression }
                    ?.let { createDataType(it, referencedConstantValue.ideaModule) }
            }
            ?.let { return it }

        constantValue.referencedConstantValue()?.let { return it.dataType().orElse(null)?.spi() }

        return when (constantValue.type()) {
            IConstantValue.ConstantValueType.Boolean,
            IConstantValue.ConstantValueType.Numeric,
            IConstantValue.ConstantValueType.String -> (constantValue.unwrappedElement() as? JSExpression)
                ?.let { JSTypeEvaluator.getTypeFromConstant(it) }
                ?.let { if (it is JSLiteralType) it.asPrimitiveType() else it }
                ?.let { createDataType(it, constantValue.ideaModule) }

            IConstantValue.ConstantValueType.ES6Class -> constantValue.asES6Class().orElse(null)?.spi()

            IConstantValue.ConstantValueType.Array -> {
                var arrayDimension = 0
                var currentConstantValues: Collection<IConstantValue> = listOf(constantValue)
                do {
                    arrayDimension++
                    currentConstantValues = currentConstantValues.asSequence()
                        .flatMap { currentConstantValue ->
                            currentConstantValue.asArray()
                                .map { it.asSequence() }
                                .orElse(emptySequence())
                        }
                        .toList()
                } while (currentConstantValues
                        .mapNotNull { it.type() }
                        .let { types -> types.isNotEmpty() && types.all { it == IConstantValue.ConstantValueType.Array } }
                )
                val union = constantValue.ideaModule.nodeElementFactory().createUnionDataType(currentConstantValues.map { getDataType(it) })
                constantValue.ideaModule.nodeElementFactory().createArrayDataType(union, arrayDimension)
            }

            IConstantValue.ConstantValueType.ObjectLiteral -> (constantValue.unwrappedElement() as? JSObjectLiteralExpression)?.let { createDataType(it, constantValue.ideaModule) }
            IConstantValue.ConstantValueType.Unknown -> null
        }
    }

    private fun getDataType(constantValue: () -> IConstantValue?): DataTypeSpi? {
        return constantValue()?.let { getDataType(it) }
    }

    private fun getDataType(constantValue: IConstantValue): DataTypeSpi? {
        return constantValue.dataType().orElse(null)?.spi()
    }
}