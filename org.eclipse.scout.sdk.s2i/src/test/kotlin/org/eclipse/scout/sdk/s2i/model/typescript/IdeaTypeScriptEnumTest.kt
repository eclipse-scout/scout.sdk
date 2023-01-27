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
import org.eclipse.scout.sdk.s2i.model.AbstractModelTest


class IdeaTypeScriptEnumTest : AbstractModelTest("typescript/moduleWithEnums") {

    fun testSingleEnumTs() {
        val logLevel = myIdeaNodeModule.export("LogLevel").orElseThrow().referencedElement() as IES6Class
        assertTrue(logLevel.isEnum)
        assertEquals("TRACE", logLevel.field("TRACE").orElseThrow().name())
    }

    fun testEnumLikeTypeAlias() {
        val constEnumType = myIdeaNodeModule.export("ConstEnumType").orElseThrow().referencedElement() as IES6Class
        assertEquals("FIRST", constEnumType.field("FIRST").orElseThrow().name())
        assertEquals("FIRST: 'a'", constEnumType.field("FIRST").orElseThrow().source().orElseThrow().asCharSequence().toString())
        assertFalse(constEnumType.field("SECOND").orElseThrow().isOptional)
        assertEquals("c", constEnumType.field("THIRD").orElseThrow().constantValue().asString().orElseThrow())
        assertEquals("\"c\"", constEnumType.field("THIRD").orElseThrow().dataType().orElseThrow().name())

        val noConstEnumType = myIdeaNodeModule.export("NoConstEnumType").orElseThrow().referencedElement() as IES6Class
        assertEquals("first", noConstEnumType.field("first").orElseThrow().name())
        assertEquals("first: 1", noConstEnumType.field("first").orElseThrow().source().orElseThrow().asCharSequence().toString())
        assertFalse(noConstEnumType.field("second").orElseThrow().isOptional)
        assertEquals(3, noConstEnumType.field("third").orElseThrow().constantValue().convertTo(Int::class.java).orElseThrow())
        assertEquals("3", noConstEnumType.field("third").orElseThrow().dataType().orElseThrow().name())
    }
}