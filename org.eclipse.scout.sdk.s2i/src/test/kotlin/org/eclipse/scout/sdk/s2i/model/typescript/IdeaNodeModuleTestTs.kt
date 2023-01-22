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

import org.eclipse.scout.sdk.s2i.model.AbstractModelTest

class IdeaNodeModuleTestTs : AbstractModelTest("typescript/moduleWithExports") {

    fun testExportsWithTypeScript() {
        val exports = myIdeaNodeModule.exports().stream()
            .map { it.referencedElement().name() + " as " + it.name() }
            .toList()
        assertEquals(
            listOf(
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
                "SampleWidgetMap as SampleWidgetMap"
            ), exports
        )
    }

    fun testExportAliasForExportFrom() {
        assertEquals("NamedClazz", myIdeaNodeModule.export("NamedClazz").orElseThrow().exportAlias().orElseThrow())
    }

    fun testExportAliasForInterface() {
        assertEquals("NamedInterface", myIdeaNodeModule.export("NamedInterface").orElseThrow().referencedElement().exportAlias().orElseThrow())
    }

    fun testExportAliasForVariable() {
        assertEquals("NamedObj", myIdeaNodeModule.export("NamedObj").orElseThrow().referencedElement().exportAlias().orElseThrow())
        assertEquals("wildcardVar", myIdeaNodeModule.export("wildcardVar").orElseThrow().referencedElement().exportAlias().orElseThrow())
    }

    fun testExportAliasForModule() {
        assertEquals("@eclipse-scout/sdk-export-ts", myIdeaNodeModule.exportAlias().orElseThrow())
    }

    fun testSourceOfInterface() {
        val namedInterfaceSourceRange = myIdeaNodeModule.export("NamedInterface").orElseThrow().referencedElement().source().orElseThrow()
        assertEquals("export interface NamedInterface {\n  myVar: boolean\n}", namedInterfaceSourceRange.asCharSequence().toString())
    }

    fun testSourceOfFunction() {
        val namedFuncSource = myIdeaNodeModule.export("namedFunc").orElseThrow().referencedElement().source().orElseThrow().asCharSequence().toString()
        assertEquals("function namedDefaultFunc() {\n}", namedFuncSource)
        val wildcardFuncSource = myIdeaNodeModule.export("wildcardFunc").orElseThrow().referencedElement().source().orElseThrow().asCharSequence().toString()
        assertEquals("export function wildcardFunc() {\n}", wildcardFuncSource)
    }

    fun testSourceOfType() {
        val namedTypeSourceRange = myIdeaNodeModule.export("NamedType").orElseThrow().referencedElement().source().orElseThrow().asCharSequence().toString()
        assertEquals("export type NamedType = {\n  myProperty: boolean\n};", namedTypeSourceRange)
        val wildcardType = myIdeaNodeModule.export("WildcardType").orElseThrow().referencedElement().source().orElseThrow().asCharSequence().toString()
        assertEquals("export type WildcardType = {\n  myProperty: boolean\n};", wildcardType)
    }
}