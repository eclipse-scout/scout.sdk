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

import com.intellij.lang.ecmascript6.psi.ES6ImportDeclaration
import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.jsdoc.JSDocComment
import com.intellij.lang.javascript.psi.resolve.JSTypeEvaluator
import com.intellij.lang.javascript.psi.types.JSLiteralType
import com.intellij.lang.javascript.psi.types.JSUtilType
import com.intellij.lang.javascript.psi.types.JSWrapperType
import com.intellij.lang.javascript.psi.types.evaluable.JSQualifiedReferenceType
import com.intellij.psi.util.PsiTreeUtil
import org.eclipse.scout.sdk.core.typescript.model.api.IConstantValue
import org.eclipse.scout.sdk.core.typescript.model.spi.DataTypeSpi
import org.eclipse.scout.sdk.s2i.model.typescript.IdeaConstantValue
import org.eclipse.scout.sdk.s2i.model.typescript.IdeaNodeModules

class IdeaDataTypeSpiFactory(val ideaNodeModules: IdeaNodeModules) {

    fun createDataType(element: JSElement, constantValue: () -> IConstantValue? = { null }): DataTypeSpi? {
        if (element is JSAssignmentExpression) {
            createDataType(element)?.let { return it }
        }
        if (element is JSTypeOwner) {
            createDataType(element as JSTypeOwner, constantValue)?.let { return it }
        }
        if (element is JSType) {
            return createDataType(element as JSType)
        }
        if (element is JSObjectLiteralExpression) {
            createDataType(element)?.let { return it }
        }
        return getDataType(constantValue)
    }

    private fun createDataType(assignment: JSAssignmentExpression): DataTypeSpi? {
        val expression = assignment.parent as? JSExpressionStatement ?: return null
        val comment = expression.children.firstNotNullOfOrNull { it as? JSDocComment } ?: return null
        return createJSDocCommentTypeDataType(comment)
    }

    private fun createDataType(typeOwner: JSTypeOwner, constantValue: () -> IConstantValue?): DataTypeSpi? {
        if (typeOwner.jsType is JSWrapperType || typeOwner.jsType is JSQualifiedReferenceType) {
            getDataType(constantValue)?.let { return it }
        }
        return typeOwner.jsType?.let { createDataType(it) }
    }

    private fun createDataType(type: JSType): DataTypeSpi {
        if (type is JSWrapperType) return createDataType(type.originalType)
        (type as? JSUtilType)?.let { return ideaNodeModules.spiFactory.createJavaScriptType(it) }

        (type.sourceElement as? JSElement)
            ?.let { (ideaNodeModules.resolveReferencedElement(it) as? DataTypeSpi) }
            ?.let { return it }

        return ideaNodeModules.spiFactory.createJavaScriptType(type)
    }

    private fun createDataType(objectLiteral: JSObjectLiteralExpression): DataTypeSpi? {
        val name = when (val parent = objectLiteral.parent) {
            is JSFieldVariable -> parent.name
            is JSProperty -> parent.name
            else -> null
        } ?: return null
        return ideaNodeModules.spiFactory.createObjectLiteralDataType(name, objectLiteral)
    }

    fun createDataType(property: JSRecordType.PropertySignature): DataTypeSpi? {
        return property.jsType?.let { createDataType(it) }
    }

    fun createJSDocCommentDataType(comment: JSDocComment, getDataType: (JSDocComment) -> String?): DataTypeSpi? {
        val dataType = getDataType(comment) ?: return null

        PsiTreeUtil.getChildrenOfType(comment.containingFile, ES6ImportDeclaration::class.java)
            ?.asSequence()
            ?.flatMap { it.importSpecifiers.asSequence() }
            ?.firstOrNull { it.declaredName == dataType }
            ?.let { ideaNodeModules.resolveImport(it) as? DataTypeSpi }
            ?.let { return it }

        return ideaNodeModules.spiFactory.createJavaScriptDocCommentAsDataType(dataType)
    }

    fun createJSDocCommentTypeDataType(comment: JSDocComment) = createJSDocCommentDataType(comment) { it.type }

    fun createDataType(constantValue: IdeaConstantValue): DataTypeSpi? {
        constantValue.referencedConstantValue()
            ?.let { it.element?.parent as? JSProperty }
            ?.let { it.parent as? JSObjectLiteralExpression }
            ?.let { createDataType(it) }
            ?.let { return it }

        constantValue.referencedConstantValue()?.let { return it.dataType().orElse(null)?.spi() }

        return when (constantValue.type()) {
            IConstantValue.ConstantValueType.Boolean,
            IConstantValue.ConstantValueType.Numeric,
            IConstantValue.ConstantValueType.String -> (constantValue.unwrappedElement() as? JSExpression)
                ?.let { JSTypeEvaluator.getTypeFromConstant(it) }
                ?.let { if (it is JSLiteralType) it.asPrimitiveType() else it }
                ?.let { createDataType(it) }

            IConstantValue.ConstantValueType.ES6Class -> constantValue.asES6Class().orElse(null)?.spi()

            IConstantValue.ConstantValueType.ObjectLiteral,
            IConstantValue.ConstantValueType.Array,
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