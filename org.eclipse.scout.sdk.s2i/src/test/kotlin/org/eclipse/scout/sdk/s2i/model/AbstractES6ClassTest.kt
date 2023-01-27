/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2i.model

import org.eclipse.scout.sdk.core.typescript.TypeScriptTypes
import org.eclipse.scout.sdk.core.typescript.model.api.IES6Class
import org.eclipse.scout.sdk.core.typescript.model.api.IField
import org.eclipse.scout.sdk.core.typescript.model.api.Modifier

abstract class AbstractES6ClassTest(val es6ClassName: String, fixturePath: String = "typescript/moduleWithExternalImports") : AbstractModelTest(fixturePath) {

    fun testES6Class() {
        val es6Class = myIdeaNodeModule.export(es6ClassName).orElseThrow().referencedElement() as IES6Class
        assertES6Class(es6Class)
    }

    protected fun assertES6Class(es6Class: IES6Class) {
        assertEquals(if (isAssignmentPossible()) 16 else 8, es6Class.fields().withoutModifier(Modifier.STATIC).stream().count())

        assertField(myString(), es6Class)
        assertField(myNumber(), es6Class)
        assertField(myBoolean(), es6Class)
        assertField(myUndefined(), es6Class)
        assertField(myNull(), es6Class)
        assertField(myObject(), es6Class)
        assertField(myAny(), es6Class)
        assertField(myRef(), es6Class)
    }

    protected fun assertField(expectedField: ExpectedField, es6Class: IES6Class) {
        expectedField.infer = false

        var field = es6Class.field(expectedField.name()).orElse(null)
        assertNotNull(field)
        assertEquals(expectedField.optional(), field.isOptional)
        assertDataType(expectedField, field)

        if (!isAssignmentPossible()) return

        expectedField.infer = true

        field = es6Class.field(expectedField.name()).orElse(null)
        assertNotNull(field)
        assertEquals(expectedField.optional(), field.isOptional)
        assertDataType(expectedField, field)
    }

    protected fun assertDataType(expectedField: ExpectedField, field: IField) {
        if (expectedField.dataTypeName() == null) {
            return
        }

        val dataType = field.dataType().orElseThrow()
        assertEquals(expectedField.dataTypeName, dataType.name())
        assertEquals(TypeScriptTypes.isPrimitive(expectedField.dataTypeName), dataType.isPrimitive)

        if (expectedField.isES6Class) {
            assertTrue(dataType is IES6Class)
        }
    }

    protected open fun isAssignmentPossible() = true

    protected open fun isOptionalPossible() = true

    protected open fun isInferPossible(dataTypeName: String) = when (dataTypeName) {
        TypeScriptTypes._string,
        TypeScriptTypes._number,
        TypeScriptTypes._boolean,
        "WildcardClass" -> true

        else -> false
    }

    protected open fun myString() = ExpectedField("myString", true, TypeScriptTypes._string)

    protected open fun myNumber() = ExpectedField("myNumber", false, TypeScriptTypes._number)

    protected open fun myBoolean() = ExpectedField("myBoolean", false, TypeScriptTypes._boolean)

    protected open fun myUndefined() = ExpectedField("myUndefined", false, TypeScriptTypes._undefined)

    protected open fun myNull() = ExpectedField("myNull", false, TypeScriptTypes._null)

    protected open fun myObject() = ExpectedField("myObject", false, TypeScriptTypes._object)

    protected open fun myAny() = ExpectedField("myAny", true, TypeScriptTypes._any)

    protected open fun myRef() = ExpectedField("myRef", false, "WildcardClass", true)

    protected inner class ExpectedField(val name: String, val optional: Boolean, val dataTypeName: String, val isES6Class: Boolean = false) {

        var infer = false

        fun name() = if (infer) name + "Infer" else name + "Def"

        fun optional() = optional && isOptionalPossible()

        fun dataTypeName() = if (infer && !isInferPossible(dataTypeName)) null else dataTypeName
    }
}