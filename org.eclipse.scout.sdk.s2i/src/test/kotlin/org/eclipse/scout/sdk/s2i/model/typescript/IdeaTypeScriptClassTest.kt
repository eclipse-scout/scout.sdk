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
import org.eclipse.scout.sdk.s2i.model.AbstractES6ClassTest

class IdeaTypeScriptClassTest : AbstractES6ClassTest("SomeClass") {
    fun testTypeParameters() {
        val someClass = myIdeaNodeModule.export(es6ClassName).orElseThrow() as IES6Class
        val params = someClass.typeParameters().toList()
        assertEquals(3, params.size)

        assertSame(someClass, params[0].declaringClass())
        assertTrue(params[0].constraint().isEmpty)
        assertTrue(params[0].defaultConstraint().isEmpty)
        assertEquals("TArg0", params[0].name())
        assertEquals("TArg0", params[0].source().orElseThrow().asCharSequence().toString())

        assertSame(someClass, params[1].declaringClass())
        assertEquals("Promise<string> | string", params[1].constraint().orElseThrow().name())
        assertTrue(params[1].defaultConstraint().isEmpty)
        assertEquals("TArg1", params[1].name())
        assertEquals("TArg1 extends Promise<string> | string", params[1].source().orElseThrow().asCharSequence().toString())

        assertSame(someClass, params[2].declaringClass())
        assertEquals("object", params[2].constraint().orElseThrow().name())
        assertEquals("WildcardClass", params[2].defaultConstraint().orElseThrow().name())
        assertEquals("TArg2", params[2].name())
        assertEquals("TArg2 extends object = WildcardClassAlias", params[2].source().orElseThrow().asCharSequence().toString())
    }
}