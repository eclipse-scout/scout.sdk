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


class IdeaNodeModulesTest : AbstractModelTest("javascript/moduleWithExternalImports") {

    fun testResolveReferencedElement() {
        val testClass = myIdeaNodeModule.export("TestClass").orElseThrow().referencedElement()
        assertEquals("@eclipse-scout/sdk-external-imports-js", testClass.containingModule().name())

        // TODO [fsh]: Use field access of testClass as soon as implemented instead of using the psi directly
        val testClassPsi = (testClass.spi() as IdeaJavaScriptClass).javaScriptClass
        val externalField = testClassPsi.fields.first { it.name == "external" }
        val localField = testClassPsi.fields.first { it.name == "local" }
        val wildField = testClassPsi.fields.first { it.name == "wild" }
        val aliasField = testClassPsi.fields.first { it.name == "alias" }

        val externalElement = myNodeModules.resolveReferencedElement(externalField) as IES6Class
        assertEquals("NamedDefaultClass", externalElement.name())
        assertEquals("NamedClazz", externalElement.exportAlias().orElseThrow())
        assertEquals("@eclipse-scout/sdk-export-js", externalElement.containingModule().name())

        val localElement = myNodeModules.resolveReferencedElement(localField) as IES6Class
        assertEquals("LocalClass", localElement.name())
        assertEquals("LocalClass", localElement.exportAlias().orElseThrow())
        assertEquals("@eclipse-scout/sdk-external-imports-js", localElement.containingModule().name())
        assertSame(testClass.containingModule(), localElement.containingModule())

        val wildElement = myNodeModules.resolveReferencedElement(wildField) as IES6Class
        assertEquals("WildcardClass", wildElement.name())
        assertEquals("WildcardClass", wildElement.exportAlias().orElseThrow())
        assertEquals("@eclipse-scout/sdk-export-js", wildElement.containingModule().name())
        assertSame(externalElement.containingModule(), wildElement.containingModule())

        val aliasElement = myNodeModules.resolveReferencedElement(aliasField) as IES6Class
        assertEquals("AnotherClass", aliasElement.name())
        assertEquals("AnotherClass", aliasElement.exportAlias().orElseThrow())
        assertEquals("@eclipse-scout/sdk-export-js", aliasElement.containingModule().name())
        assertSame(externalElement.containingModule(), aliasElement.containingModule())
    }
}