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

import com.intellij.openapi.vfs.VirtualFile
import org.eclipse.scout.sdk.core.typescript.model.api.*
import org.eclipse.scout.sdk.s2i.model.AbstractModelTest


class IdeaNodeModulesTest : AbstractModelTest("javascript/moduleWithExternalImports") {

    fun testResolveReferencedElement() {
        val moduleSpi = myIdeaNodeModule.spi() as IdeaNodeModule
        val testClass = myIdeaNodeModule.export("TestClass").orElseThrow() as IES6Class
        assertEquals("@eclipse-scout/sdk-external-imports-js", testClass.containingModule().name())

        val external = testClass.field("external").orElseThrow().spi() as IdeaJavaScriptField
        val local = testClass.field("local").orElseThrow().spi() as IdeaJavaScriptField
        val wild = testClass.field("wild").orElseThrow().spi() as IdeaJavaScriptField
        val alias = testClass.field("alias").orElseThrow().spi() as IdeaJavaScriptField

        val externalElement = moduleSpi.resolveReferencedElement(external.javaScriptField)?.api() as IES6Class
        assertEquals("NamedDefaultClass", externalElement.name())
        assertEquals(setOf("NamedClazz"), externalElement.moduleExportNames())
        assertEquals("@eclipse-scout/sdk-export-js", externalElement.containingModule().name())

        val localElement = moduleSpi.resolveReferencedElement(local.javaScriptField)?.api() as IES6Class
        assertEquals("LocalClass", localElement.name())
        assertEquals(setOf("LocalClass"), localElement.moduleExportNames())
        assertEquals("@eclipse-scout/sdk-external-imports-js", localElement.containingModule().name())
        assertSame(testClass.containingModule(), localElement.containingModule())

        val wildElement = moduleSpi.resolveReferencedElement(wild.javaScriptField)?.api() as IES6Class
        assertEquals("WildcardClass", wildElement.name())
        assertEquals(setOf("WildcardClass"), wildElement.moduleExportNames())
        assertEquals("@eclipse-scout/sdk-export-js", wildElement.containingModule().name())
        assertSame(externalElement.containingModule(), wildElement.containingModule())

        val aliasElement = moduleSpi.resolveReferencedElement(alias.javaScriptField)?.api() as IES6Class
        assertEquals("AnotherClass", aliasElement.name())
        assertEquals(setOf("AnotherClass"), aliasElement.moduleExportNames())
        assertEquals("@eclipse-scout/sdk-export-js", aliasElement.containingModule().name())
        assertSame(externalElement.containingModule(), aliasElement.containingModule())
    }

    fun testSuperClass() {
        val testClass = myIdeaNodeModule.export("LocalClass").orElseThrow() as IES6Class
        val superClass = testClass.superClass().orElseThrow()
        assertEquals("AnotherClass", superClass.name())
        assertEquals("@eclipse-scout/sdk-export-js", superClass.containingModule().name())
    }

    fun testJsonPointer() {
        val arrow = myIdeaNodeModule.export("SampleModel").orElseThrow() as IFunction
        val literal = arrow.resultingObjectLiteral().orElseThrow()

        // sub elements
        assertEquals("Third", literal.find("/fields/2/id").orElseThrow().asString().orElseThrow())
        assertEquals("WildcardClass", literal.find("/objectType").orElseThrow().asES6Class().orElseThrow().name())
        assertEquals(3, literal.find("/fields/1/subElements/0/arrItem/2").orElseThrow().convertTo(Int::class.java).orElseThrow())
        assertEquals(3, literal.find("/fields/1/subElements/0/arrItem/2/").orElseThrow().convertTo(Int::class.java).orElseThrow())
        assertEquals(4, literal.find("/fields/1/subElements/1/numberITem").orElseThrow().convertTo(Int::class.java).orElseThrow())

        // with escaped keys
        assertEquals("keyWithTilde", literal.find("/fields/1/m~0n").orElseThrow().asString().orElseThrow()) // ~0 = ~
        assertEquals("keyWithSlash", literal.find("/fields/1/a~1b").orElseThrow().asString().orElseThrow()) // ~1 = /

        // root element
        assertSame(literal, literal.find("").orElseThrow().asObjectLiteral().orElseThrow())
        assertSame(literal, literal.find(null as CharSequence?).orElseThrow().asObjectLiteral().orElseThrow())

        // not found elements
        assertTrue(literal.find("/fields/1/subElements33/0/arrItem/2").isEmpty)
        assertTrue(literal.find("/fields/1/subElements/100").isEmpty)
        assertTrue(literal.find("/fields/2/id/3/test").isEmpty)
    }

    fun testObjectLiteralReferences() {
        val withTypeRef = myIdeaNodeModule.export("WithTypeRef").orElseThrow() as IVariable
        val literal = withTypeRef.constantValue().asObjectLiteral().orElseThrow()

        val named = literal.property("named").orElseThrow()
        assertEquals(IConstantValue.ConstantValueType.ES6Class, named.type())
        assertEquals("NamedDefaultClass", named.asES6Class().orElseThrow().name())

        val wild = literal.property("wild").orElseThrow()
        assertEquals(IConstantValue.ConstantValueType.ES6Class, wild.type())
        assertEquals("WildcardClass", wild.asES6Class().orElseThrow().name())

        val alias = literal.property("alias").orElseThrow()
        assertEquals(IConstantValue.ConstantValueType.ES6Class, alias.type())
        assertEquals("AnotherClass", alias.asES6Class().orElseThrow().name())

        val local = literal.property("local").orElseThrow()
        assertEquals(IConstantValue.ConstantValueType.ES6Class, local.type())
        assertEquals("LocalClass", local.asES6Class().orElseThrow().name())
    }

    fun testObjectLiteralInArrow() {
        val arrow = myIdeaNodeModule.export("SampleModel").orElseThrow() as IFunction
        val literal = arrow.resultingObjectLiteral().orElseThrow()
        assertEquals("WildcardClass", literal.propertyAsES6Class("objectType").orElseThrow().name())
        val fields = literal.propertyAs("fields", Array<IObjectLiteral>::class.java).orElseThrow()
        assertEquals(4, fields.size)

        assertEquals("NamedDefaultClass", fields[0].propertyAsES6Class("objectType").orElseThrow().name())
        assertEquals("AnotherClass", fields[1].propertyAsES6Class("objectType").orElseThrow().name())
        assertEquals("LocalClass", fields[2].propertyAsES6Class("objectType").orElseThrow().name())
        assertEquals("DefaultExportedClass", fields[3].propertyAsES6Class("objectType").orElseThrow().name())
    }

    fun testReferencedConstantValues() {
        val referencedValue = myIdeaNodeModule.export("ReferencedValue").orElseThrow() as IVariable
        assertEquals("staticString", referencedValue.constantValue().asString().orElseThrow())

        val referencedValueProp = myIdeaNodeModule.export("ReferencedValueProp").orElseThrow() as IVariable
        assertEquals("HALF_UP", referencedValueProp.constantValue().asString().orElseThrow())

        val referencedEnum = myIdeaNodeModule.export("ReferencedEnum").orElseThrow() as IVariable
        val referencedEnumObjectLiteral = referencedEnum.constantValue().asObjectLiteral().orElseThrow()
        assertEquals(6, referencedEnumObjectLiteral.properties().size)
        assertEquals(2, referencedEnumObjectLiteral.property("c").orElseThrow().convertTo(Int::class.java).orElseThrow())

        val referencedEnumProp = myIdeaNodeModule.export("ReferencedEnumProp").orElseThrow() as IVariable
        val referencedEnumPropObjectLiteral = referencedEnumProp.constantValue().asObjectLiteral().orElseThrow()
        assertEquals(2, referencedEnumPropObjectLiteral.properties().size)
        assertFalse(referencedEnumPropObjectLiteral.property("b").orElseThrow().asBoolean().orElseThrow())

        val referencedType = myIdeaNodeModule.export("ReferencedType").orElseThrow() as IVariable
        assertEquals("WildcardClass", referencedType.constantValue().asES6Class().orElseThrow().name())

        val referencedTypeProp = myIdeaNodeModule.export("ReferencedTypeProp").orElseThrow() as IVariable
        assertEquals("WildcardClass", referencedTypeProp.constantValue().asES6Class().orElseThrow().name())
    }

    fun testRemoveModulesByDir() {
        val enumsModuleDir = myFixture.findFileInTempDir("node_modules/@eclipse-scout/sdk-enums-js")
        val exportModuleDir = myFixture.findFileInTempDir("node_modules/@eclipse-scout/sdk-export-js")
        val importModuleDir = myFixture.findFileInTempDir("")

        testRemoveModules(enumsModuleDir, exportModuleDir, importModuleDir)
    }

    fun testRemoveModulesByFile() {
        val enumsModuleFile = myFixture.findFileInTempDir("node_modules/@eclipse-scout/sdk-enums-js/src/enums.js")
        val exportModuleFile = myFixture.findFileInTempDir("node_modules/@eclipse-scout/sdk-export-js/src/Util.js")
        val importModuleFile = myFixture.findFileInTempDir("src/SomeClass.js")

        testRemoveModules(enumsModuleFile, exportModuleFile, importModuleFile)
    }

    fun testRemoveModulesByModule() {
        val enumsModuleName = "@eclipse-scout/sdk-enums-js"
        val exportModuleName = "@eclipse-scout/sdk-export-js"
        val importModuleName = "@eclipse-scout/sdk-external-imports-js"

        val enumsModuleDir = myFixture.findFileInTempDir("node_modules/$enumsModuleName")
        val exportModuleDir = myFixture.findFileInTempDir("node_modules/$exportModuleName")
        val importModuleDir = myFixture.findFileInTempDir("")

        createModules(setOf(enumsModuleDir))
        val notExistingEnumsModule = getModule(enumsModuleName)
        removeModule(notExistingEnumsModule)
        assertTrue(notExistingEnumsModule !in myNodeModules.getModules())

        testRemoveModules(
            enumsModuleDir, exportModuleDir, importModuleDir,
            enumsModuleName, exportModuleName, importModuleName,
            notExistingEnumsModule
        )
    }

    private fun testRemoveModules(
        enumsModuleFile: VirtualFile, exportModuleFile: VirtualFile, importModuleFile: VirtualFile,
        enumsModuleToRemove: Any = enumsModuleFile, exportModuleToRemove: Any = exportModuleFile, importModuleToRemove: Any = importModuleFile,
        notExistingModuleToRemove: Any = enumsModuleToRemove
    ) {
        val enumsModuleName = "@eclipse-scout/sdk-enums-js"
        val exportModuleName = "@eclipse-scout/sdk-export-js"
        val importModuleName = "@eclipse-scout/sdk-external-imports-js"

        // create all 3 modules
        // -> all 3 modules exist
        createAndAssertModules(setOf(enumsModuleFile, exportModuleFile, importModuleFile), setOf(enumsModuleName, exportModuleName, importModuleName))
        // remove importModule, it has no dependent modules
        // -> only importModule is removed, enumsModule and exportModule remain
        removeAndAssertModules(importModuleToRemove, setOf(importModuleName), setOf(enumsModuleName, exportModuleName))

        // create importModule again
        // -> all 3 modules exist
        createAndAssertModules(setOf(importModuleFile), setOf(enumsModuleName, exportModuleName, importModuleName))
        // remove exportModule, importModule dependents on it
        // -> exportModule and importModule are removed, only enumsModule remains
        removeAndAssertModules(exportModuleToRemove, setOf(exportModuleName, importModuleName), setOf(enumsModuleName))

        // create exportModule and importModule again
        // -> all 3 modules exist
        createAndAssertModules(setOf(exportModuleFile, importModuleFile), setOf(enumsModuleName, exportModuleName, importModuleName))
        // remove enumsModule, exportModule and importModule dependent on it
        // -> all 3 modules are removed, no module remains
        removeAndAssertModules(enumsModuleToRemove, setOf(enumsModuleName, exportModuleName, importModuleName), emptySet())

        // create only exportModule
        // -> only exportModule exists
        createAndAssertModules(setOf(exportModuleFile), setOf(exportModuleName))
        // remove a not existing module
        // -> no module is removed, exportModule remains
        removeAndAssertModules(notExistingModuleToRemove, emptySet(), setOf(exportModuleName))

        // create only enumsModule
        // -> enumsModule and exportModule exist
        createAndAssertModules(setOf(enumsModuleFile), setOf(enumsModuleName, exportModuleName))
        // remove enumsModule, exportModule and importModule dependent on it, but importModule was not created yet
        // -> enumsModule and exportModule are removed, no module remains
        removeAndAssertModules(enumsModuleToRemove, setOf(enumsModuleName, exportModuleName), emptySet())
    }

    private fun createModules(vfs: Collection<VirtualFile>) {
        vfs.forEach { myNodeModules.getOrCreateModule(it) }
    }

    private fun getModule(moduleName: String) = myNodeModules.getModules().first { it.packageJson().api().name() == moduleName }

    private fun removeModule(module: IdeaNodeModule): Collection<IdeaNodeModule> {
        return myNodeModules.remove(module)
    }

    private fun removeModule(moduleName: String): Collection<IdeaNodeModule> {
        return removeModule(getModule(moduleName))
    }

    private fun removeModule(vf: VirtualFile): Collection<IdeaNodeModule> {
        return myNodeModules.remove(vf)
    }

    private fun assertModules(expectedModuleNames: Set<String>, modules: Collection<IdeaNodeModule> = myNodeModules.getModules()) {
        assertEquals(expectedModuleNames.size, modules.size)
        assertEquals(expectedModuleNames, modules.map { it.packageJson().api().name() }.toSet())
    }

    private fun createAndAssertModules(modulesToCreate: Collection<VirtualFile>, expectedModuleNames: Set<String>) {
        createModules(modulesToCreate)
        assertModules(expectedModuleNames)
    }

    private fun removeAndAssertModules(moduleToRemove: Any, expectedRemovedModuleNames: Set<String>, expectedRemainingModuleNames: Set<String>) {
        assertModules(
            expectedRemovedModuleNames, when (moduleToRemove) {
                is IdeaNodeModule -> removeModule(moduleToRemove)
                is String -> removeModule(moduleToRemove)
                is VirtualFile -> removeModule(moduleToRemove)
                else -> emptySet()
            }
        )
        assertModules(expectedRemainingModuleNames)
    }
}