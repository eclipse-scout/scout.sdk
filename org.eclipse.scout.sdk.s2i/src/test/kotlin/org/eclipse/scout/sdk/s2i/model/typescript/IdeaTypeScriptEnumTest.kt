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

import org.eclipse.scout.sdk.core.typescript.model.api.IES6Class
import org.eclipse.scout.sdk.core.typescript.model.api.ITypeOf
import org.eclipse.scout.sdk.s2i.model.AbstractModelTest


class IdeaTypeScriptEnumTest : AbstractModelTest("typescript/moduleWithEnums") {

    fun testSingleEnumTs() {
        val logLevel = myIdeaNodeModule.export("LogLevel").orElseThrow().referencedElement() as IES6Class
        assertTrue(logLevel.isEnum)
        assertEquals("TRACE", logLevel.field("TRACE").orElseThrow().name())
    }

    fun testEnumLikeTypeAlias() {
        val constEnumType = myIdeaNodeModule.export("ConstEnumType").orElseThrow().referencedElement() as IES6Class
        assertTrue(constEnumType.isTypeAlias)
        val constEnumAliased = constEnumType.aliasedDataType().orElseThrow()
        assertEquals("EnumObject", constEnumAliased.name())
        assertTrue(constEnumAliased is IES6Class)
        assertEquals(1, constEnumAliased.typeArguments().count())

        val constEnumTypeArgument = constEnumAliased.typeArguments().findFirst().orElseThrow()
        assertTrue(constEnumTypeArgument is ITypeOf)

        val constEnumComponentType = (constEnumTypeArgument as ITypeOf).dataType().orElseThrow()
        assertEquals("Const", constEnumComponentType.name())
        val constEnumObjectLiteral = constEnumComponentType.objectLiteral().orElseThrow()
        assertNotNull("FIRST", constEnumObjectLiteral.property("FIRST").orElseThrow())
        assertEquals("a", constEnumObjectLiteral.property("FIRST").orElseThrow().asString().orElseThrow())
        assertEquals("c", constEnumObjectLiteral.property("THIRD").orElseThrow().asString().orElseThrow())
        assertEquals("string", constEnumObjectLiteral.property("THIRD").orElseThrow().dataType().orElseThrow().name())

        val noConstEnumType = myIdeaNodeModule.export("NoConstEnumType").orElseThrow().referencedElement() as IES6Class
        assertTrue(noConstEnumType.isTypeAlias)
        val noConstEnumAliased = noConstEnumType.aliasedDataType().orElseThrow()
        assertEquals("EnumObject", noConstEnumAliased.name())
        assertTrue(noConstEnumAliased is IES6Class)
        assertEquals(1, noConstEnumAliased.typeArguments().count())

        val noConstEnumTypeArgument = noConstEnumAliased.typeArguments().findFirst().orElseThrow()
        assertTrue(noConstEnumTypeArgument is ITypeOf)

        val noConstEnumComponentType = (noConstEnumTypeArgument as ITypeOf).dataType().orElseThrow()
        assertEquals("NoConst", noConstEnumComponentType.name())
        val noConstEnumObjectLiteral = noConstEnumComponentType.objectLiteral().orElseThrow()
        assertNotNull("first", noConstEnumObjectLiteral.property("first").orElseThrow())
        assertEquals(1, noConstEnumObjectLiteral.property("first").orElseThrow().convertTo(Int::class.java).orElseThrow())
        assertEquals(3, noConstEnumObjectLiteral.property("third").orElseThrow().convertTo(Int::class.java).orElseThrow())
        assertEquals("number", noConstEnumObjectLiteral.property("third").orElseThrow().dataType().orElseThrow().name())
    }
}