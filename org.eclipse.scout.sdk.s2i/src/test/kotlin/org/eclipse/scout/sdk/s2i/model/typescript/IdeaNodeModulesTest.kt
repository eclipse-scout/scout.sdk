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
        val testClass = myIdeaNodeModule.export("TestClass").orElseThrow().referencedElement() as IES6Class
        assertEquals("@eclipse-scout/sdk-external-imports-js", testClass.containingModule().name())

        val external = testClass.field("external").orElseThrow().spi() as IdeaJavaScriptField
        val local = testClass.field("local").orElseThrow().spi() as IdeaJavaScriptField
        val wild = testClass.field("wild").orElseThrow().spi() as IdeaJavaScriptField
        val alias = testClass.field("alias").orElseThrow().spi() as IdeaJavaScriptField

        val externalElement = myNodeModules.resolveReferencedElement(external.javaScriptField) as IES6Class
        assertEquals("NamedDefaultClass", externalElement.name())
        assertEquals("NamedClazz", externalElement.exportAlias().orElseThrow())
        assertEquals("@eclipse-scout/sdk-export-js", externalElement.containingModule().name())

        val localElement = myNodeModules.resolveReferencedElement(local.javaScriptField) as IES6Class
        assertEquals("LocalClass", localElement.name())
        assertEquals("LocalClass", localElement.exportAlias().orElseThrow())
        assertEquals("@eclipse-scout/sdk-external-imports-js", localElement.containingModule().name())
        assertSame(testClass.containingModule(), localElement.containingModule())

        val wildElement = myNodeModules.resolveReferencedElement(wild.javaScriptField) as IES6Class
        assertEquals("WildcardClass", wildElement.name())
        assertEquals("WildcardClass", wildElement.exportAlias().orElseThrow())
        assertEquals("@eclipse-scout/sdk-export-js", wildElement.containingModule().name())
        assertSame(externalElement.containingModule(), wildElement.containingModule())

        val aliasElement = myNodeModules.resolveReferencedElement(alias.javaScriptField) as IES6Class
        assertEquals("AnotherClass", aliasElement.name())
        assertEquals("AnotherClass", aliasElement.exportAlias().orElseThrow())
        assertEquals("@eclipse-scout/sdk-export-js", aliasElement.containingModule().name())
        assertSame(externalElement.containingModule(), aliasElement.containingModule())
    }
}