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

import org.eclipse.scout.sdk.core.typescript.model.api.*
import org.eclipse.scout.sdk.s2i.model.AbstractModelTest
import org.junit.Assert
import java.math.BigDecimal
import java.math.BigInteger


class IdeaJavaScriptVariableTest : AbstractModelTest("javascript/moduleWithEnums") {

    fun testSingleEnumJs() {
        val hAlign = myIdeaNodeModule.export("HAlign").orElseThrow() as IVariable
        val enumValue = hAlign.constantValue().asObjectLiteral().orElseThrow()

        val leftProperty = enumValue.property("LEFT").orElseThrow()
        assertEquals("left", leftProperty.asString().orElseThrow())
        assertEquals(IConstantValue.ConstantValueType.String, leftProperty.type())

        val rightProperty = enumValue.property("RIGHT").orElseThrow()
        assertTrue(rightProperty.asString().isEmpty)
        assertTrue(rightProperty.convertTo(Iterable::class.java).isEmpty) // unknown conversion
        assertTrue(rightProperty.convertTo(Double::class.java).isEmpty)
        assertTrue(rightProperty.asObjectLiteral().isEmpty)
        assertEquals(IConstantValue.ConstantValueType.Unknown, rightProperty.type())

        val centerProperty = enumValue.property("CENTER").orElseThrow()
        assertEquals(3, centerProperty.convertTo(Int::class.java).orElseThrow())
        assertEquals(3, centerProperty.convertTo(Integer::class.java).orElseThrow())
        assertEquals(3.21f, centerProperty.convertTo(Float::class.java).orElseThrow())
        assertEquals(3.21f, centerProperty.convertTo(java.lang.Float::class.java).orElseThrow())
        assertEquals(3L, centerProperty.convertTo(Long::class.java).orElseThrow())
        assertEquals(3L, centerProperty.convertTo(java.lang.Long::class.java).orElseThrow())
        assertEquals(3.21, centerProperty.convertTo(Double::class.java).orElseThrow())
        assertEquals(3.21, centerProperty.convertTo(java.lang.Double::class.java).orElseThrow())
        assertEquals(BigDecimal("3.21"), centerProperty.asBigDecimal().orElseThrow())
        assertEquals(BigInteger("3"), centerProperty.convertTo(BigInteger::class.java).orElseThrow())

        assertEquals(IConstantValue.ConstantValueType.Numeric, centerProperty.type())

        val next = enumValue.propertyAsObjectLiteral("NEXT").orElseThrow()
        assertEquals(IConstantValue.ConstantValueType.ObjectLiteral, enumValue.property("NEXT").orElseThrow().type())

        val aProperty = next.property("a").orElseThrow()
        assertEquals(11, aProperty.convertTo(Int::class.java).orElseThrow())
        assertEquals(11, aProperty.convertTo(Integer::class.java).orElseThrow())
        assertEquals(11.0f, aProperty.convertTo(Float::class.java).orElseThrow())
        assertEquals(11.0f, aProperty.convertTo(java.lang.Float::class.java).orElseThrow())
        assertEquals(11L, aProperty.convertTo(Long::class.java).orElseThrow())
        assertEquals(11L, aProperty.convertTo(java.lang.Long::class.java).orElseThrow())
        assertEquals(11.0, aProperty.convertTo(Double::class.java).orElseThrow())
        assertEquals(11.0, aProperty.convertTo(java.lang.Double::class.java).orElseThrow())
        assertEquals(BigDecimal("11"), aProperty.asBigDecimal().orElseThrow())
        assertEquals(BigInteger("11"), aProperty.convertTo(BigInteger::class.java).orElseThrow())

        val bProperty = next.property("b").orElseThrow()
        assertFalse(bProperty.asBoolean().orElseThrow())
        assertEquals(java.lang.Boolean.FALSE, bProperty.asBoolean().orElseThrow())
        assertEquals(IConstantValue.ConstantValueType.Boolean, bProperty.type())

        val undef = enumValue.property("UNDEF").orElseThrow()
        assertEquals(IConstantValue.ConstantValueType.Unknown, undef.type())
        assertTrue(undef.asObjectLiteral().isEmpty)

        val arr = enumValue.property("ARR").orElseThrow()
        assertEquals(IConstantValue.ConstantValueType.Array, arr.type())
        val arrValue = arr.convertTo(Array<Array<String>>::class.java).orElseThrow()
        Assert.assertArrayEquals(arrayOf("1", "2", "3"), arrValue[0])
        Assert.assertArrayEquals(arrayOf("a", "b", "c"), arrValue[1])
        assertEquals(Array<String>::class.java, arrValue[0]::class.java)

        val constArrValue = arr.asArray().orElseThrow()[1]
        assertEquals(IConstantValue.ConstantValueType.Array, constArrValue.type())
        assertEquals("b", constArrValue.asArray().orElseThrow()[1].convertTo(String::class.java).orElseThrow())
    }

    fun testNestedEnumJs() {
        val enumInClass = myIdeaNodeModule.export("EnumInClass").orElseThrow() as IES6Class
        val enums = enumInClass.fields()
            .withModifier(Modifier.STATIC)
            .stream()
            .filter { it.constantValue().type() == IConstantValue.ConstantValueType.ObjectLiteral }
            .toList()
        assertEquals(1, enums.size)
        val nestedEnum = enums[0]
        assertEquals("NestedEnum", nestedEnum.name())
        val valueOfC = nestedEnum.constantValue()
            .asObjectLiteral().orElseThrow()
            .property("c").orElseThrow()
            .convertTo(Int::class.java).orElseThrow()
        assertEquals(2, valueOfC)
    }

    fun testMultiEnumJs() {
        val severityField = myIdeaNodeModule.export("Severity").orElseThrow() as IVariable
        val severity = severityField.constantValue().asObjectLiteral().orElseThrow()
        assertEquals("warning", severity.propertyAsString("WARNING").orElseThrow())

        val roundingModeField = myIdeaNodeModule.export("RoundingMode").orElseThrow() as IVariable
        val roundingMode = roundingModeField.constantValue().asObjectLiteral().orElseThrow()
        assertEquals("HALF_UP", roundingMode.propertyAsString("HALF_UP").orElseThrow())
    }
}