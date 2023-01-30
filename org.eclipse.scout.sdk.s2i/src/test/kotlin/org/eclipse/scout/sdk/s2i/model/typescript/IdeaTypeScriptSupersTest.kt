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


class IdeaTypeScriptSupersTest : AbstractModelTest("typescript/moduleWithExternalImports") {

    fun testSupers() {
        val classWithSupers = myIdeaNodeModule.export("ClassWithSupers").orElseThrow().referencedElement() as IES6Class

        val superClass = classWithSupers.superClass().orElseThrow()
        assertEquals("NamedClass", superClass.name())
        assertEquals("@eclipse-scout/sdk-export-ts", superClass.containingModule().name())

        val superInterfaces = classWithSupers.superInterfaces().toList()
        assertEquals(2, superInterfaces.size)
        assertEquals("NamedInterface", superInterfaces[0].name())
        assertEquals("@eclipse-scout/sdk-export-ts", superInterfaces[0].containingModule().name())
        assertEquals("WildcardInterface", superInterfaces[1].name())
        assertEquals("@eclipse-scout/sdk-export-ts", superInterfaces[1].containingModule().name())
        assertEquals(3, classWithSupers.supers().count())
    }

}