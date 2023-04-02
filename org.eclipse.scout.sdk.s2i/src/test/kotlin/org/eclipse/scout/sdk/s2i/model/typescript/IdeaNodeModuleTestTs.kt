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

class IdeaNodeModuleTestTs : AbstractModelTest("typescript/moduleWithExports") {

    fun testExportsWithTypeScript() {
        val exports = myIdeaNodeModule.elements().stream()
            .flatMap { it.exportNames().stream().map { export -> it.name() + " as " + export } }
            .toList().toSet()
        assertEquals(
            setOf(
                "namedDefaultFunc as namedFunc",
                "NamedClass as NamedClazz",
                "NamedInterface as NamedInterface",
                "NamedType as NamedType",
                "namedVar as NamedObj",
                "WildcardClass as WildcardClass",
                "WildcardDefaultClass as WildcardDefaultClass",
                "wildcardFunc as wildcardFunc",
                "WildcardInterface as WildcardInterface",
                "WildcardType as WildcardType",
                "wildcardVar as wildcardVar",
                "SampleWidgetMap as SampleWidgetMap",
                "ClassWithFunctions as ClassWithFunctions"
            ), exports
        )
    }

    fun testExportAliasForExportFrom() {
        assertEquals(listOf("NamedClazz"), myIdeaNodeModule.export("NamedClazz").orElseThrow().exportNames())
    }

    fun testExportAliasForInterface() {
        assertEquals(listOf("NamedInterface"), myIdeaNodeModule.export("NamedInterface").orElseThrow().exportNames())
    }

    fun testExportAliasForVariable() {
        assertEquals(listOf("NamedObj"), myIdeaNodeModule.export("NamedObj").orElseThrow().exportNames())
        assertEquals(listOf("wildcardVar"), myIdeaNodeModule.export("wildcardVar").orElseThrow().exportNames())
    }

    fun testExportAliasForModule() {
        assertEquals(listOf("@eclipse-scout/sdk-export-ts"), myIdeaNodeModule.exportNames())
    }

    fun testSourceOfInterface() {
        val namedInterfaceSourceRange = myIdeaNodeModule.export("NamedInterface").orElseThrow().source().orElseThrow()
        assertEquals("export interface NamedInterface {\n  myVar: boolean\n}", namedInterfaceSourceRange.asCharSequence().toString())
    }

    fun testSourceOfFunction() {
        val namedFuncSource = myIdeaNodeModule.export("namedFunc").orElseThrow().source().orElseThrow().asCharSequence().toString()
        assertEquals("function namedDefaultFunc() {\n}", namedFuncSource)
        val wildcardFuncSource = myIdeaNodeModule.export("wildcardFunc").orElseThrow().source().orElseThrow().asCharSequence().toString()
        assertEquals("export function wildcardFunc() {\n}", wildcardFuncSource)
    }

    fun testSourceOfType() {
        val namedTypeSourceRange = myIdeaNodeModule.export("NamedType").orElseThrow().source().orElseThrow().asCharSequence().toString()
        assertEquals("export type NamedType = {\n  myProperty: boolean\n};", namedTypeSourceRange)
        val wildcardType = myIdeaNodeModule.export("WildcardType").orElseThrow().source().orElseThrow().asCharSequence().toString()
        assertEquals("export type WildcardType = {\n  myProperty: boolean\n};", wildcardType)
    }

    fun testFunctions() {
        val classWithFunctions = myIdeaNodeModule.export("ClassWithFunctions").orElseThrow() as IES6Class
        assertEquals("constructor", classWithFunctions.function("constructor").orElseThrow().name())
        assertEquals("_init", classWithFunctions.function("_init").orElseThrow().name())
        assertEquals("myOtherFunction", classWithFunctions.function("myOtherFunction").orElseThrow().name())
    }
}