/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2i.model.typescript.util

import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.ecma6.*
import com.intellij.lang.javascript.psi.ecmal4.JSClass
import com.intellij.psi.PsiElement
import kotlinx.collections.immutable.toImmutableList
import org.eclipse.scout.sdk.core.typescript.TypeScriptTypes
import org.eclipse.scout.sdk.core.typescript.model.api.IDataType
import org.eclipse.scout.sdk.core.typescript.model.spi.DataTypeSpi
import org.eclipse.scout.sdk.core.typescript.model.spi.ES6ClassSpi
import org.eclipse.scout.sdk.core.typescript.model.spi.FieldSpi
import org.eclipse.scout.sdk.s2i.model.typescript.IdeaNodeModule

object FieldSpiUtils {

    /* **************************************************************************
     * CREATE FIELDS
     * *************************************************************************/

    fun createField(element: JSElement, declaringClass: ES6ClassSpi, module: IdeaNodeModule): FieldSpi? {
        if (element is JSField) {
            return createField(element, declaringClass, module)
        }
        if (element is JSAssignmentExpression) {
            createField(element, declaringClass, module)?.let { return it }
        }

        return null
    }

    private fun createField(field: JSField, declaringClass: ES6ClassSpi, module: IdeaNodeModule): FieldSpi = module.nodeElementFactory().createJavaScriptField(field, declaringClass)

    private fun createField(assignment: JSAssignmentExpression, declaringClass: ES6ClassSpi, module: IdeaNodeModule): FieldSpi? {
        val reference: JSReferenceExpression = assignment.definitionExpression?.expression as? JSReferenceExpression ?: return null
        if (reference.qualifier !is JSThisExpression) return null
        return module.nodeElementFactory().createJavaScriptAssignmentExpressionAsField(assignment, reference, declaringClass)
    }

    /* **************************************************************************
     * CHOOSE FIELDS
     * *************************************************************************/

    private fun chooseField(field1: FieldSpi, field2: FieldSpi): FieldSpi {
        val dataType1 = field1.dataType() ?: return field2
        val dataType2 = field2.dataType() ?: return field1

        chooseField(field1 to dataType1, field2 to dataType2)?.let { return it }

        if (dataType1.flavor() === IDataType.DataTypeFlavor.Array && dataType2.flavor() === IDataType.DataTypeFlavor.Array) {
            if (dataType1.arrayDimension() != dataType2.arrayDimension()) return field1

            val componentDataType1 = dataType1.componentDataTypes().stream().findFirst().orElse(null) ?: return field2
            val componentDataType2 = dataType1.componentDataTypes().stream().findFirst().orElse(null) ?: return field1

            chooseField(field1 to componentDataType1, field2 to componentDataType2)?.let { return it }
        }

        return field1
    }

    private fun chooseField(fieldDataTypePair1: Pair<FieldSpi, DataTypeSpi>, fieldDataTypePair2: Pair<FieldSpi, DataTypeSpi>): FieldSpi? {
        val (field1, dataType1) = fieldDataTypePair1
        val (field2, dataType2) = fieldDataTypePair2

        if (dataType1.name() === TypeScriptTypes._any) return field2
        if (dataType2.name() === TypeScriptTypes._any) return field1

        if (dataType1 is ES6ClassSpi && dataType2 is ES6ClassSpi) {
            if (dataType2.api().isInstanceOf(dataType1.api())) return field2
            return field1
        }

        return null
    }

    /* **************************************************************************
     * COLLECT FIELDS
     * *************************************************************************/

    fun collectFields(element: JSElement, declaringClass: ES6ClassSpi, module: IdeaNodeModule): List<FieldSpi> {
        val collector = FieldCollector(declaringClass, module)
        if (element is TypeScriptTypeAlias) {
            collectFields(collector, element)
            return collector.fields()
        }
        if (element is JSClass) {
            collectFields(collector, element)
        }
        return collector.fields()
    }

    private fun collectFields(collector: FieldCollector, typeAlias: TypeScriptTypeAlias) {
        val typeDeclaration = typeAlias.typeDeclaration
        if (typeDeclaration !is TypeScriptObjectType) return
        // type with own fields
        collector.collectTypeScriptPropertySignatures(typeDeclaration.children)
    }

    private fun collectFields(collector: FieldCollector, clazz: JSClass) {
        when (clazz) {
            is TypeScriptClass -> collector.collectTypeScriptFields(clazz.fields)
            is TypeScriptInterface -> collector.collectTypeScriptPropertySignatures(clazz.fields)
            else -> collector.collectJSElements(clazz.fields)
        }

        if (clazz is TypeScriptInterfaceClass || clazz is TypeScriptCompileTimeType) return

        val block = clazz.constructor?.block ?: return
        block.statementListItems.asSequence()
            .filter { it is JSExpressionStatement }
            .mapNotNull { it.children.asSequence().firstNotNullOfOrNull { child -> child as? JSAssignmentExpression } }
            .forEach { collector.collect(it) }
    }

    private class FieldCollector(val declaringClass: ES6ClassSpi, val module: IdeaNodeModule) {

        val map = LinkedHashMap<String, FieldSpi>()

        fun fields(): List<FieldSpi> = map.values.toImmutableList()

        fun collect(element: JSElement) {
            createField(element, declaringClass, module)?.let { collect(it) }
        }

        fun collect(field: FieldSpi) {
            map.compute(field.name()) { _, existingField ->
                existingField?.let { chooseField(it, field) } ?: field
            }
        }

        fun collectTypeScriptFields(elements: Array<out PsiElement>) {
            collectJSElements(elements) { it as? TypeScriptField }
        }

        fun collectTypeScriptPropertySignatures(elements: Array<out PsiElement>) {
            collectJSElements(elements) { it as? TypeScriptPropertySignature }
        }

        fun collectJSElements(elements: Array<out PsiElement>, transform: (PsiElement) -> JSElement? = { it as? JSElement }) {
            collectJSElements(elements.asSequence(), transform)
        }

        fun collectJSElements(elements: Sequence<PsiElement>, transform: (PsiElement) -> JSElement? = { it as? JSElement }) {
            elements.mapNotNull(transform).forEach { collect(it) }
        }
    }
}