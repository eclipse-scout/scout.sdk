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
        val exports = myIdeaNodeModule.exports().stream()
            .map { it.referencedElement().name() + " as " + it.name() }
            .toList()
        assertEquals(
            listOf(
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

        // named export: only the single element is the source of the export
        val utilsExportFrom = myIdeaNodeModule.exports().withName("utils").first().orElseThrow()
        assertEquals("default as utils", utilsExportFrom.source().orElseThrow().asCharSequence().toString())

        // wildcard export: the full export declaration is the source
        val wildcardDefaultClassExportFrom = myIdeaNodeModule.exports().withName("WildcardDefaultClass").first().orElseThrow()
        assertEquals("export * from './WildcardExported';", wildcardDefaultClassExportFrom.source().orElseThrow().asCharSequence().toString())
    }

    fun testExportAliasForExportFrom() {
        assertEquals("NamedClazz", myIdeaNodeModule.export("NamedClazz").orElseThrow().exportAlias().orElseThrow())
    }

    fun testExportAliasForClass() {
        assertEquals("WildcardDefaultClass", myIdeaNodeModule.export("WildcardDefaultClass").orElseThrow().referencedElement().exportAlias().orElseThrow())
    }

    fun testExportAliasForVariable() {
        assertEquals("namedVar", myIdeaNodeModule.export("namedVar").orElseThrow().referencedElement().exportAlias().orElseThrow())
        assertEquals("utils", myIdeaNodeModule.export("utils").orElseThrow().referencedElement().exportAlias().orElseThrow())
    }

    fun testExportAliasForModule() {
        assertEquals("@eclipse-scout/sdk-export-js", myIdeaNodeModule.exportAlias().orElseThrow())
    }

    fun testSourceOfClass() {
        val namedClazzSourceRange = myIdeaNodeModule.export("NamedClazz").orElseThrow().referencedElement().source().orElseThrow()
        assertEquals("class NamedDefaultClass {\n}", namedClazzSourceRange.asCharSequence().toString())
        assertEquals(350, namedClazzSourceRange.start())
        assertEquals(376, namedClazzSourceRange.end())
        assertEquals(27, namedClazzSourceRange.length())
    }

    fun testFunctions() {
        val classWithFunctions = myIdeaNodeModule.export("ClassWithFunctions").orElseThrow().referencedElement() as IES6Class
        assertEquals("constructor", classWithFunctions.function("constructor").orElseThrow().name())
        assertEquals("_init", classWithFunctions.function("_init").orElseThrow().name())
        assertEquals("myOtherFunction", classWithFunctions.function("myOtherFunction").orElseThrow().name())
    }

    fun testSourceOfFunction() {
        val namedFuncSource = myIdeaNodeModule.export("namedFunc").orElseThrow().referencedElement().source().orElseThrow().asCharSequence().toString()
        assertEquals("export function namedFunc() {\n}", namedFuncSource)

        val wildcardFuncSource = myIdeaNodeModule.export("wildcardFunc").orElseThrow().referencedElement().source().orElseThrow().asCharSequence().toString()
        assertEquals("export function wildcardFunc() {\n  let x = 3;\n}", wildcardFuncSource)
    }

    fun testSourceOfVariable() {
        val namedVarSource = myIdeaNodeModule.export("namedVar").orElseThrow().referencedElement().source().orElseThrow().asCharSequence().toString()
        assertEquals("namedVar = {}", namedVarSource)

        val wildcardVarSource = myIdeaNodeModule.export("wildcardVar").orElseThrow().referencedElement().source().orElseThrow().asCharSequence().toString()
        assertEquals("wildcardVar = {}", wildcardVarSource)
    }
}