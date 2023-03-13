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

import com.intellij.util.containers.isEmpty
import org.eclipse.scout.sdk.core.typescript.TypeScriptTypes
import org.eclipse.scout.sdk.core.typescript.model.api.IDataType
import org.eclipse.scout.sdk.core.typescript.model.api.IDataType.DataTypeFlavor
import org.eclipse.scout.sdk.core.typescript.model.api.IES6Class
import org.eclipse.scout.sdk.core.typescript.model.api.IField
import org.eclipse.scout.sdk.core.typescript.model.api.Modifier

abstract class AbstractES6ClassTest(val es6ClassName: String, fixturePath: String = "typescript/moduleWithExternalImports") : AbstractModelTest(fixturePath) {

    fun testES6Class() {
        val es6Class = myIdeaNodeModule.export(es6ClassName).orElseThrow().referencedElement() as IES6Class
        assertES6Class(es6Class)
    }

    protected fun assertES6Class(es6Class: IES6Class) {
        assertEquals(if (isAssignmentPossible()) 30 else 16, es6Class.fields().withoutModifier(Modifier.STATIC).stream().count())

        assertFieldDefAndInfer(myString(), es6Class)
        assertFieldDefAndInfer(myNumber(), es6Class)
        assertFieldDefAndInfer(myBoolean(), es6Class)
        assertFieldDefAndInfer(myUndefined(), es6Class)
        assertFieldDefAndInfer(myNull(), es6Class)
        assertFieldDefAndInfer(myObject(), es6Class)
        assertFieldDefAndInfer(myAny(), es6Class)
        assertFieldDefAndInfer(myRef(), es6Class)
        assertFieldDefAndInfer(myStringArray(), es6Class)
        assertFieldDefAndInfer(myNumberArray(), es6Class)
        assertFieldDef(myStringNumberUnion(), es6Class)
        assertFieldDefAndInfer(myStringNumberUnionArray(), es6Class)
        assertFieldDef(myStringArrayNumberUnion(), es6Class)
        assertFieldDef(myAbBcIntersection(), es6Class)
        assertFieldDef(myAbBcIntersectionArray(), es6Class)
        assertFieldDef(myAbBcArrayIntersection(), es6Class)

        if (!isAssignmentPossible()) return

        assertFieldInfer(myArray(), es6Class)
        assertFieldInfer(myStaticStringRef(), es6Class)
        assertFieldInfer(myEnumRef(), es6Class)

        assertEquals(3, es6Class.fields().withModifier(Modifier.STATIC).stream().count())

        assertFieldDefAndInfer(myStaticString(), es6Class)
        assertFieldInfer(myEnum(), es6Class)
    }

    protected fun assertFieldDefAndInfer(expectedField: ExpectedField, es6Class: IES6Class) {
        assertFieldDef(expectedField, es6Class)

        if (!isAssignmentPossible()) return

        assertFieldInfer(expectedField, es6Class)
    }

    protected fun assertFieldDef(expectedField: ExpectedField, es6Class: IES6Class) {
        expectedField.infer = false
        assertField(expectedField, es6Class)
    }

    protected fun assertFieldInfer(expectedField: ExpectedField, es6Class: IES6Class) {
        expectedField.infer = true
        assertField(expectedField, es6Class)
    }

    protected fun assertField(expectedField: ExpectedField, es6Class: IES6Class) {
        val field = es6Class.field(expectedField.name()).orElse(null)
        assertNotNull(field)
        assertEquals(expectedField.optional(), field.isOptional)
        assertDataType(expectedField, field)
    }

    protected fun assertDataType(expectedField: ExpectedField, field: IField) {
        if (!expectedField.isAssertDataType()) {
            return
        }

        assertDataType(expectedField.dataType, field.dataType().orElseThrow())
    }

    protected fun assertDataType(expectedDataType: ExpectedDataType, dataType: IDataType) {
        expectedDataType.name?.let {
            assertEquals(it, dataType.name())
        }
        assertEquals(TypeScriptTypes.isPrimitive(expectedDataType.name), dataType.isPrimitive)
        assertEquals(expectedDataType.flavor, dataType.flavor())
        val remainingExpectedComponentDataTypes = expectedDataType.componentDataTypes.toMutableSet()
        assertTrue(dataType.componentDataTypes()
            .map {
                remainingExpectedComponentDataTypes.removeIf { expectedComponentDataType ->
                    try {
                        assertDataType(expectedComponentDataType, it)
                        true
                    } catch (e: AssertionError) {
                        false
                    }
                }
            }
            .filter { !it }
            .isEmpty())

        assertEquals(expectedDataType.arrayDimension, dataType.arrayDimension())

        if (expectedDataType.isES6Class) {
            assertTrue(dataType is IES6Class)
        }
    }

    protected open fun isAssignmentPossible() = true

    protected open fun isOptionalPossible() = true

    protected open fun isInferPossible(field: ExpectedField) =
        when (field.dataType.flavor) {
            DataTypeFlavor.Single -> when (field.dataType.name) {
                TypeScriptTypes._string,
                TypeScriptTypes._number,
                TypeScriptTypes._boolean,
                "WildcardClass" -> true

                else -> false
            }

            DataTypeFlavor.Array,
            DataTypeFlavor.Union,
            DataTypeFlavor.Intersection -> true
        }

    protected open fun myString() = ExpectedField(
        "myString",
        ExpectedDataType(TypeScriptTypes._string),
        optional = true
    )

    protected open fun myNumber() = ExpectedField(
        "myNumber",
        ExpectedDataType(TypeScriptTypes._number)
    )

    protected open fun myBoolean() = ExpectedField(
        "myBoolean",
        ExpectedDataType(TypeScriptTypes._boolean)
    )

    protected open fun myUndefined() = ExpectedField(
        "myUndefined",
        ExpectedDataType(TypeScriptTypes._undefined)
    )

    protected open fun myNull() = ExpectedField(
        "myNull",
        ExpectedDataType(TypeScriptTypes._null)
    )

    protected open fun myObject() = ExpectedField(
        "myObject",
        ExpectedDataType(TypeScriptTypes._object)
    )

    protected open fun myAny() = ExpectedField(
        "myAny",
        ExpectedDataType(TypeScriptTypes._any),
        optional = true
    )

    protected open fun myRef() = ExpectedField(
        "myRef",
        ExpectedDataType(
            "WildcardClass",
            isES6Class = true
        )
    )

    protected open fun myStaticString() = ExpectedField(
        "myStaticString",
        ExpectedDataType(TypeScriptTypes._string)
    )

    protected open fun myEnum() = ExpectedField(
        "myEnum",
        ExpectedDataType(TypeScriptTypes._object)
    )

    protected open fun myStaticStringRef() = ExpectedField(
        "myStaticStringRef",
        ExpectedDataType(TypeScriptTypes._string)
    )

    protected open fun myEnumRef() = ExpectedField(
        "myEnumRef",
        ExpectedDataType("myEnum")
    )

    protected open fun myStringArray() = ExpectedField(
        "myStringArray",
        ExpectedDataType(
            null,
            flavor = DataTypeFlavor.Array,
            componentDataTypes = setOf(
                ExpectedDataType(TypeScriptTypes._string)
            ),
            arrayDimension = 2
        ),
        optional = true
    )

    protected open fun myNumberArray() = ExpectedField(
        "myNumberArray",
        ExpectedDataType(
            null,
            flavor = DataTypeFlavor.Array,
            componentDataTypes = setOf(
                ExpectedDataType(TypeScriptTypes._number)
            ),
            arrayDimension = 1
        )
    )

    protected open fun myArray() = ExpectedField(
        "myArray",
        ExpectedDataType(
            null,
            flavor = DataTypeFlavor.Array,
            componentDataTypes = emptySet(),
            arrayDimension = 1
        )
    )

    protected open fun myStringNumberUnion() = ExpectedField(
        "myStringNumberUnion",
        ExpectedDataType(
            null,
            flavor = DataTypeFlavor.Union,
            componentDataTypes = setOf(
                ExpectedDataType(TypeScriptTypes._string),
                ExpectedDataType(TypeScriptTypes._number)
            )
        )
    )

    protected open fun myStringNumberUnionArray() = ExpectedField(
        "myStringNumberUnionArray",
        ExpectedDataType(
            null,
            flavor = DataTypeFlavor.Array,
            componentDataTypes = setOf(
                ExpectedDataType(
                    null,
                    flavor = DataTypeFlavor.Union,
                    componentDataTypes = setOf(
                        ExpectedDataType(TypeScriptTypes._string),
                        ExpectedDataType(TypeScriptTypes._number)
                    )
                )
            ),
            arrayDimension = 1
        )
    )

    protected open fun myStringArrayNumberUnion() = ExpectedField(
        "myStringArrayNumberUnion",
        ExpectedDataType(
            null,
            flavor = DataTypeFlavor.Union,
            componentDataTypes = setOf(
                ExpectedDataType(
                    null,
                    flavor = DataTypeFlavor.Array,
                    componentDataTypes = setOf(
                        ExpectedDataType(TypeScriptTypes._string)
                    ),
                    arrayDimension = 1
                ),
                ExpectedDataType(TypeScriptTypes._number)
            )
        )
    )

    protected open fun myAbBcIntersection() = ExpectedField(
        "myAbBcIntersection",
        ExpectedDataType(
            null,
            flavor = DataTypeFlavor.Intersection,
            componentDataTypes = setOf(
                ExpectedDataType("AB"),
                ExpectedDataType("BC")
            )
        )
    )

    protected open fun myAbBcIntersectionArray() = ExpectedField(
        "myAbBcIntersectionArray",
        ExpectedDataType(
            null,
            flavor = DataTypeFlavor.Array,
            componentDataTypes = setOf(
                ExpectedDataType(
                    null,
                    flavor = DataTypeFlavor.Intersection,
                    componentDataTypes = setOf(
                        ExpectedDataType("AB"),
                        ExpectedDataType("BC")
                    ),
                ),
            ),
            arrayDimension = 1
        )
    )

    protected open fun myAbBcArrayIntersection() = ExpectedField(
        "myAbBcArrayIntersection",
        ExpectedDataType(
            null,
            flavor = DataTypeFlavor.Intersection,
            componentDataTypes = setOf(
                ExpectedDataType("AB"),
                ExpectedDataType(
                    null,
                    flavor = DataTypeFlavor.Array,
                    componentDataTypes = setOf(
                        ExpectedDataType("BC")
                    ),
                    arrayDimension = 1
                )
            )
        )
    )

    protected inner class ExpectedField(
        val name: String,
        val dataType: ExpectedDataType,
        val optional: Boolean = false
    ) {

        var infer = false

        fun name() = if (infer) name + "Infer" else name + "Def"

        fun optional() = optional && isOptionalPossible()

        fun isAssertDataType() = !(infer && !isInferPossible(this))
    }

    protected inner class ExpectedDataType(
        val name: String?,
        val isES6Class: Boolean = false,
        val flavor: DataTypeFlavor = DataTypeFlavor.Single,
        val componentDataTypes: Set<ExpectedDataType> = emptySet(),
        val arrayDimension: Int = 0
    )
}