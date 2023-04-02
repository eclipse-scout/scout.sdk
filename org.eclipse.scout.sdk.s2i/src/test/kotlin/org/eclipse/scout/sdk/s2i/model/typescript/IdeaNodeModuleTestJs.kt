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

class IdeaNodeModuleTestJs : AbstractModelTest("javascript/moduleWithExports") {

    fun testExports() {
        val exports = myIdeaNodeModule.elements().stream()
            .flatMap { it.exportNames().stream().map { export -> it.name() + " as " + export } }
            .toList().toSet()
        assertEquals(
            setOf(
                "NamedDefaultClass as NamedClazz",
                "namedFunc as namedFunc",
                "namedVar as namedVar",
                " as utils",
                "WildcardClass as WildcardClass",
                "WildcardDefaultClass as WildcardDefaultClass",
                "wildcardFunc as wildcardFunc",
                "wildcardVar as wildcardVar",
                "AnotherClass as AnotherClass",
                "ClassWithFunctions as ClassWithFunctions"
            ), exports
        )
    }

    fun testNodeModule() {
        assertSame(myIdeaNodeModule, myIdeaNodeModule.containingModule())
        assertEquals("23.1.0-snapshot", myIdeaNodeModule.packageJson().version())
        assertEquals("@eclipse-scout/sdk-export-js@23.1.0-snapshot", myIdeaNodeModule.toString())
        assertTrue(myIdeaNodeModule.source().orElseThrow().asCharSequence().contains("JSUnusedGlobalSymbols"))
    }

    fun testExportAliasForExportFrom() {
        assertEquals(listOf("NamedClazz"), myIdeaNodeModule.export("NamedClazz").orElseThrow().exportNames())
    }

    fun testExportAliasForClass() {
        assertEquals(listOf("WildcardDefaultClass"), myIdeaNodeModule.export("WildcardDefaultClass").orElseThrow().exportNames())
    }

    fun testExportAliasForVariable() {
        assertEquals(listOf("namedVar"), myIdeaNodeModule.export("namedVar").orElseThrow().exportNames())
        assertEquals(listOf("utils"), myIdeaNodeModule.export("utils").orElseThrow().exportNames())
    }

    fun testExportAliasForModule() {
        assertEquals(listOf("@eclipse-scout/sdk-export-js"), myIdeaNodeModule.exportNames())
    }

    fun testSourceOfClass() {
        val namedClazzSourceRange = myIdeaNodeModule.export("NamedClazz").orElseThrow().source().orElseThrow()
        assertEquals("class NamedDefaultClass {\n}", namedClazzSourceRange.asCharSequence().toString())
        assertEquals(350, namedClazzSourceRange.start())
        assertEquals(376, namedClazzSourceRange.end())
        assertEquals(27, namedClazzSourceRange.length())
    }

    fun testFunctions() {
        val classWithFunctions = myIdeaNodeModule.export("ClassWithFunctions").orElseThrow() as IES6Class
        assertEquals("constructor", classWithFunctions.function("constructor").orElseThrow().name())
        assertEquals("_init", classWithFunctions.function("_init").orElseThrow().name())
        assertEquals("myOtherFunction", classWithFunctions.function("myOtherFunction").orElseThrow().name())
    }

    fun testSourceOfFunction() {
        val namedFuncSource = myIdeaNodeModule.export("namedFunc").orElseThrow().source().orElseThrow().asCharSequence().toString()
        assertEquals("export function namedFunc() {\n}", namedFuncSource)

        val wildcardFuncSource = myIdeaNodeModule.export("wildcardFunc").orElseThrow().source().orElseThrow().asCharSequence().toString()
        assertEquals("export function wildcardFunc() {\n  let x = 3;\n}", wildcardFuncSource)
    }

    fun testSourceOfVariable() {
        val namedVarSource = myIdeaNodeModule.export("namedVar").orElseThrow().source().orElseThrow().asCharSequence().toString()
        assertEquals("namedVar = {}", namedVarSource)

        val wildcardVarSource = myIdeaNodeModule.export("wildcardVar").orElseThrow().source().orElseThrow().asCharSequence().toString()
        assertEquals("wildcardVar = {}", wildcardVarSource)
    }
}