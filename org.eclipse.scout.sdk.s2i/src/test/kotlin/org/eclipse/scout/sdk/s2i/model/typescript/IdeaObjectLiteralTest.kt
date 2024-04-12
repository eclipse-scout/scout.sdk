/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2i.model.typescript

import org.eclipse.scout.sdk.core.typescript.TypeScriptTypes
import org.eclipse.scout.sdk.core.typescript.model.api.*
import org.eclipse.scout.sdk.s2i.model.AbstractModelTest


class IdeaObjectLiteralTest : AbstractModelTest("typescript/moduleWithExternalImports") {

    fun testSampleModel() {
        val arrow = myIdeaNodeModule.export("SampleModel").orElseThrow() as IFunction
        val literal = arrow.resultingObjectLiteral().orElseThrow()

        assertEquals("WildcardClass", literal.propertyAsES6Class("objectType").orElseThrow().name())

        val fields = literal.propertyAs("fields", Array<IObjectLiteral>::class.java).orElseThrow()
        assertEquals(4, fields.size)

        val genericsNumber = fields[0]
        assertEquals("GenericsNumber", genericsNumber.propertyAsString("id").orElseThrow())
        val genericsNumberObjectType = genericsNumber.propertyAsES6Class("objectType").orElseThrow()
        assertEquals("Generics", genericsNumberObjectType.name())
        assertEquals(1, genericsNumberObjectType.typeArguments().count())
        assertEquals(TypeScriptTypes._number, genericsNumberObjectType.typeArguments().findFirst().orElseThrow().name())

        val genericsBoolean = fields[1]
        assertEquals("GenericsBoolean", genericsBoolean.propertyAsString("id").orElseThrow())
        val genericsBooleanObjectType = genericsBoolean.propertyAsES6Class("objectType").orElseThrow()
        assertEquals("Generics", genericsBooleanObjectType.name())
        assertEquals(1, genericsBooleanObjectType.typeArguments().count())
        assertEquals(TypeScriptTypes._boolean, genericsBooleanObjectType.typeArguments().findFirst().orElseThrow().name())

        val genericsString = fields[2]
        assertEquals("GenericsString", genericsString.propertyAsString("id").orElseThrow())
        val genericsStringObjectType = genericsString.propertyAsES6Class("objectType").orElseThrow()
        assertEquals("Generics", genericsStringObjectType.name())
        assertEquals(0, genericsStringObjectType.typeArguments().count())

        val genericsNew = fields[3]
        assertEquals("GenericsNew", genericsNew.propertyAsString("id").orElseThrow())
        val genericsNewObjectType = genericsNew.propertyAsES6Class("objectType").orElseThrow()
        assertEquals("Generics", genericsNewObjectType.name())
        assertEquals(1, genericsNewObjectType.typeArguments().count())
        val firstTypeArg = genericsNewObjectType.typeArguments().findFirst().orElseThrow()
        assertTrue(firstTypeArg is IES6Class)
        assertEquals("Generics", firstTypeArg.name())
        val firstTypeArgNested = firstTypeArg.typeArguments().findFirst().orElseThrow()
        assertTrue(firstTypeArgNested is IES6Class)
        assertEquals("WildcardClass", firstTypeArgNested.name())
    }
}