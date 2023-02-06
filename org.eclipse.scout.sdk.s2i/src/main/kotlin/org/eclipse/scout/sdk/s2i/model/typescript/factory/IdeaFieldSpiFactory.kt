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
import com.intellij.lang.javascript.psi.ecma6.*
import com.intellij.lang.javascript.psi.ecmal4.JSClass
import com.intellij.lang.javascript.psi.types.TypeScriptTypeOfJSTypeImpl
import com.intellij.psi.PsiElement
import kotlinx.collections.immutable.toImmutableList
import org.eclipse.scout.sdk.core.typescript.model.spi.FieldSpi
import org.eclipse.scout.sdk.s2i.model.typescript.IdeaNodeModule
import org.eclipse.scout.sdk.s2i.model.typescript.IdeaNodeModules
import org.eclipse.scout.sdk.s2i.model.typescript.util.JSTypeUtils

class IdeaFieldSpiFactory(val ideaNodeModules: IdeaNodeModules) {

    /* **************************************************************************
     * CREATE FIELDS
     * *************************************************************************/

    fun createField(element: JSElement): FieldSpi? {
        if (element is JSField) {
            return createField(element)
        }
        if (element is JSAssignmentExpression) {
            createField(element)?.let { return it }
        }

        return null
    }

    private fun createField(field: JSField): FieldSpi = ideaNodeModules.spiFactory.createJavaScriptField(field)

    private fun createField(assignment: JSAssignmentExpression): FieldSpi? {
        val reference: JSReferenceExpression = assignment.definitionExpression?.expression as? JSReferenceExpression ?: return null
        if (reference.qualifier !is JSThisExpression) return null
        return ideaNodeModules.spiFactory.createJavaScriptAssignmentExpressionAsField(assignment, reference)
    }

    private fun createField(property: JSRecordType.PropertySignature, module: IdeaNodeModule): FieldSpi = ideaNodeModules.spiFactory.createRecordField(property, module)

    /* **************************************************************************
     * COLLECT FIELDS
     * *************************************************************************/

    fun collectFields(element: JSElement): List<FieldSpi> {
        val collector = FieldCollector()
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
        if (typeDeclaration is TypeScriptObjectType) {
            // type with own fields
            collector.collectTypeScriptPropertySignatures(typeDeclaration.children)
        } else {
            // type alias with referenced fields (enum like)
            val parsedType = typeAlias.parsedTypeDeclaration ?: return
            val recordType = JSTypeUtils.firstChildOfType(parsedType, TypeScriptTypeOfJSTypeImpl::class.java)?.substitute() as? JSRecordType ?: return
            val module = ideaNodeModules.findContainingModule(typeAlias) ?: return
            recordType.properties
                .forEach { collector.collect(createField(it, module)) }
        }
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

    private inner class FieldCollector {

        val map = HashMap<String, FieldSpi>()

        fun fields(): List<FieldSpi> = map.values.toImmutableList()

        fun collect(element: JSElement) {
            createField(element)?.let { collect(it) }
        }

        fun collect(field: FieldSpi) {
            map.compute(field.name()) { _, existingField ->
                existingField?.let {
                    it.addAdditionalField(field)
                    it
                } ?: field
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