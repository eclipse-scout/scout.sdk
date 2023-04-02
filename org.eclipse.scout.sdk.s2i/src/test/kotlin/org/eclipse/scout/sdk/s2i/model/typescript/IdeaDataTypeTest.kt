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

import org.eclipse.scout.sdk.core.typescript.model.api.IDataType.DataTypeFlavor
import org.eclipse.scout.sdk.core.typescript.model.api.IES6Class
import org.eclipse.scout.sdk.core.typescript.model.api.IField
import org.eclipse.scout.sdk.core.typescript.model.api.ITypeOf
import org.eclipse.scout.sdk.core.typescript.model.api.IVariable
import org.eclipse.scout.sdk.s2i.model.AbstractModelTest
import java.util.stream.Collectors

class IdeaDataTypeTest : AbstractModelTest("typescript/moduleWithExternalImports") {

    fun testAlignment() {
        val alignment = field("alignment")

        val alignmentDataType = alignment.dataType().orElseThrow()
        assertEquals("Alignment", alignmentDataType.name())
        assertTrue(alignmentDataType is IES6Class)
        assertTrue((alignmentDataType as IES6Class).isTypeAlias)

        val aliasedDataType = alignmentDataType.aliasedDataType().orElseThrow()
        assertEquals(DataTypeFlavor.Union, aliasedDataType.flavor())
        assertEquals(setOf(-1, 0, 1), aliasedDataType.childTypes()
            .flatMap { it.constantValue().stream() }
            .flatMap { cv -> cv.convertTo(Int::class.java).stream() }
            .collect(Collectors.toSet()))
    }

    fun testOrientation() {
        val orientation = field("orientation")

        val orientationDataType = orientation.dataType().orElseThrow()
        assertEquals(DataTypeFlavor.Union, orientationDataType.flavor())
        assertEquals(setOf("top", "right", "bottom", "left"), orientationDataType.childTypes()
            .flatMap { it.constantValue().stream() }
            .flatMap { cv -> cv.asString().stream() }
            .collect(Collectors.toSet()))
    }

    fun testNestedEnum() {
        val nestedEnum = field("nestedEnum")

        val nestedLevelEnumDataType = nestedEnum.dataType().orElseThrow()
        assertEquals("NestedEnum", nestedLevelEnumDataType.name())
        assertTrue(nestedLevelEnumDataType is IES6Class)
        assertTrue((nestedLevelEnumDataType as IES6Class).isTypeAlias)

        val aliasedDataType = nestedLevelEnumDataType.aliasedDataType().orElseThrow()
        assertEquals("EnumObject", aliasedDataType.name())
        assertTrue(aliasedDataType is IES6Class)
        assertEquals(1, aliasedDataType.typeArguments().count())

        val typeArgument = aliasedDataType.typeArguments().findFirst().orElseThrow()
        assertTrue(typeArgument is ITypeOf)

        val componentType = (typeArgument as ITypeOf).dataType().orElseThrow()
        assertEquals("NestedEnumLike", componentType.name())
        val objectLiteral = componentType.objectLiteral().orElseThrow()
        assertEquals(setOf("A", "B", "C"), objectLiteral.properties().keys)
        assertEquals(componentType, myIdeaNodeModule.export("Foo")
            .map { it as IES6Class }
            .flatMap { it.field("NestedEnumLike") }
            .flatMap { it.dataType() }
            .orElseThrow())
    }

    fun testTopLevelEnum() {
        val topLevelEnum = field("topLevelEnum")

        val topLevelEnumDataType = topLevelEnum.dataType().orElseThrow()
        assertEquals("TopLevelEnum", topLevelEnumDataType.name())
        assertTrue(topLevelEnumDataType is IES6Class)
        assertTrue((topLevelEnumDataType as IES6Class).isTypeAlias)

        val aliasedDataType = topLevelEnumDataType.aliasedDataType().orElseThrow()
        assertEquals("EnumObject", aliasedDataType.name())
        assertTrue(aliasedDataType is IES6Class)
        assertEquals(1, aliasedDataType.typeArguments().count())

        val typeArgument = aliasedDataType.typeArguments().findFirst().orElseThrow()
        assertTrue(typeArgument is ITypeOf)

        val componentType = (typeArgument as ITypeOf).dataType().orElseThrow()
        assertEquals("TopLevelEnumLike", componentType.name())
        val objectLiteral = componentType.objectLiteral().orElseThrow()
        assertEquals(setOf("FOO", "BAR"), objectLiteral.properties().keys)
        assertEquals(componentType, myIdeaNodeModule.export("TopLevelEnumLike")
            .map { it as IVariable }
            .flatMap { it.dataType() }
            .orElseThrow())
    }

    fun testObjectLiteralTypeEnum() {
        val objectLiteralTypeEnum = field("objectLiteralTypeEnum")

        val objectLiteralTypeEnumDataType = objectLiteralTypeEnum.dataType().orElseThrow()
        assertEquals("ObjectLiteralTypeEnum", objectLiteralTypeEnumDataType.name())
        assertTrue(objectLiteralTypeEnumDataType is IES6Class)
        assertTrue((objectLiteralTypeEnumDataType as IES6Class).isTypeAlias)

        val aliasedDataType = objectLiteralTypeEnumDataType.aliasedDataType().orElseThrow()
        assertEquals("EnumObject", aliasedDataType.name())
        assertTrue(aliasedDataType is IES6Class)
        assertEquals(1, aliasedDataType.typeArguments().count())

        val typeArgument = aliasedDataType.typeArguments().findFirst().orElseThrow()
        assertEquals("ObjectLiteralType", typeArgument.name())
        assertTrue(typeArgument is IES6Class)
        assertTrue((typeArgument as IES6Class).isTypeAlias)
        assertEquals(setOf("DEFAULT", "ALTERNATIVE"), typeArgument.fields().stream().map { it.name() }.collect(Collectors.toSet()))
        assertSame(typeArgument, myIdeaNodeModule.export("ObjectLiteralType").orElseThrow())
    }

    fun testRealEnum() {
        val realEnum = field("realEnum")

        val realEnumDataType = realEnum.dataType().orElseThrow()
        assertEquals("RealEnum", realEnumDataType.name())
        assertTrue(realEnumDataType is IES6Class)
        assertEquals(setOf("X", "Y", "Z"), (realEnumDataType as IES6Class).fields().stream().map { it.name() }.collect(Collectors.toSet()))
        assertSame(realEnumDataType, myIdeaNodeModule.export("RealEnum").orElseThrow())
    }

    private fun field(name: String): IField {
        val foo = myIdeaNodeModule.export("Foo").orElseThrow() as IES6Class
        return foo.field(name).orElseThrow()
    }
}