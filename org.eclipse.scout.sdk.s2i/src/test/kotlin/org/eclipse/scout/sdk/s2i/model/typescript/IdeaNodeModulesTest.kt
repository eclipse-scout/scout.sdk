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

import org.eclipse.scout.sdk.core.typescript.model.api.*
import org.eclipse.scout.sdk.s2i.model.AbstractModelTest


class IdeaNodeModulesTest : AbstractModelTest("javascript/moduleWithExternalImports") {

    fun testResolveReferencedElement() {
        val testClass = myIdeaNodeModule.export("TestClass").orElseThrow().referencedElement() as IES6Class
        assertEquals("@eclipse-scout/sdk-external-imports-js", testClass.containingModule().name())

        val external = testClass.field("external").orElseThrow().spi() as IdeaJavaScriptField
        val local = testClass.field("local").orElseThrow().spi() as IdeaJavaScriptField
        val wild = testClass.field("wild").orElseThrow().spi() as IdeaJavaScriptField
        val alias = testClass.field("alias").orElseThrow().spi() as IdeaJavaScriptField

        val externalElement = myNodeModules.resolveReferencedElement(external.javaScriptField)?.api() as IES6Class
        assertEquals("NamedDefaultClass", externalElement.name())
        assertEquals("NamedClazz", externalElement.exportAlias().orElseThrow())
        assertEquals("@eclipse-scout/sdk-export-js", externalElement.containingModule().name())

        val localElement = myNodeModules.resolveReferencedElement(local.javaScriptField)?.api() as IES6Class
        assertEquals("LocalClass", localElement.name())
        assertEquals("LocalClass", localElement.exportAlias().orElseThrow())
        assertEquals("@eclipse-scout/sdk-external-imports-js", localElement.containingModule().name())
        assertSame(testClass.containingModule(), localElement.containingModule())

        val wildElement = myNodeModules.resolveReferencedElement(wild.javaScriptField)?.api() as IES6Class
        assertEquals("WildcardClass", wildElement.name())
        assertEquals("WildcardClass", wildElement.exportAlias().orElseThrow())
        assertEquals("@eclipse-scout/sdk-export-js", wildElement.containingModule().name())
        assertSame(externalElement.containingModule(), wildElement.containingModule())

        val aliasElement = myNodeModules.resolveReferencedElement(alias.javaScriptField)?.api() as IES6Class
        assertEquals("AnotherClass", aliasElement.name())
        assertEquals("AnotherClass", aliasElement.exportAlias().orElseThrow())
        assertEquals("@eclipse-scout/sdk-export-js", aliasElement.containingModule().name())
        assertSame(externalElement.containingModule(), aliasElement.containingModule())
    }

    fun testSuperClass() {
        val testClass = myIdeaNodeModule.export("LocalClass").orElseThrow().referencedElement() as IES6Class
        val superClass = testClass.superClass().orElseThrow()
        assertEquals("AnotherClass", superClass.name())
        assertEquals("@eclipse-scout/sdk-export-js", superClass.containingModule().name())
    }

    fun testJsonPointer() {
        val arrow = myIdeaNodeModule.export("SampleModel").orElseThrow().referencedElement() as IFunction
        val literal = arrow.resultingObjectLiteral().orElseThrow()

        // sub elements
        assertEquals("Third", literal.find("/fields/2/id").orElseThrow().asString().orElseThrow())
        assertEquals("WildcardClass", literal.find("/objectType").orElseThrow().asES6Class().orElseThrow().name())
        assertEquals(3, literal.find("/fields/1/subElements/0/arrItem/2").orElseThrow().convertTo(Int::class.java).orElseThrow())
        assertEquals(3, literal.find("/fields/1/subElements/0/arrItem/2/").orElseThrow().convertTo(Int::class.java).orElseThrow())
        assertEquals(4, literal.find("/fields/1/subElements/1/numberITem").orElseThrow().convertTo(Int::class.java).orElseThrow())

        // with escaped keys
        assertEquals("keyWithTilde", literal.find("/fields/1/m~0n").orElseThrow().asString().orElseThrow()) // ~0 = /
        assertEquals("keyWithSlash", literal.find("/fields/1/a~1b").orElseThrow().asString().orElseThrow()) // ~1 = ~

        // root element
        assertSame(literal, literal.find("").orElseThrow().convertTo(IObjectLiteral::class.java).orElseThrow())
        assertSame(literal, literal.find(null as CharSequence?).orElseThrow().convertTo(IObjectLiteral::class.java).orElseThrow())

        // not found elements
        assertTrue(literal.find("/fields/1/subElements33/0/arrItem/2").isEmpty)
        assertTrue(literal.find("/fields/1/subElements/100").isEmpty)
        assertTrue(literal.find("/fields/2/id/3/test").isEmpty)
    }

    fun testObjectLiteralReferences() {
        val withTypeRef = myIdeaNodeModule.export("WithTypeRef").orElseThrow().referencedElement() as IVariable
        val literal = withTypeRef.objectLiteralValue().orElseThrow()

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
        val arrow = myIdeaNodeModule.export("SampleModel").orElseThrow().referencedElement() as IFunction
        val literal = arrow.resultingObjectLiteral().orElseThrow()
        assertEquals("WildcardClass", literal.propertyAsES6Class("objectType").orElseThrow().name())
        val fields = literal.propertyAs("fields", Array<IObjectLiteral>::class.java).orElseThrow()
        assertEquals(3, fields.size)

        assertEquals("NamedDefaultClass", fields[0].propertyAsES6Class("objectType").orElseThrow().name())
        assertEquals("AnotherClass", fields[1].propertyAsES6Class("objectType").orElseThrow().name())
        assertEquals("LocalClass", fields[2].propertyAsES6Class("objectType").orElseThrow().name())
    }

    fun testReferencedConstantValues() {
        val referencedValue = myIdeaNodeModule.export("ReferencedValue").orElseThrow().referencedElement() as IVariable
        assertEquals("staticString", referencedValue.stringValue().orElseThrow())

        val referencedValueProp = myIdeaNodeModule.export("ReferencedValueProp").orElseThrow().referencedElement() as IVariable
        assertEquals("HALF_UP", referencedValueProp.stringValue().orElseThrow())

        val referencedEnum = myIdeaNodeModule.export("ReferencedEnum").orElseThrow().referencedElement() as IVariable
        val referencedEnumObjectLiteral = referencedEnum.objectLiteralValue().orElseThrow()
        assertEquals(6, referencedEnumObjectLiteral.properties().size)
        assertEquals(2, referencedEnumObjectLiteral.property("c").orElseThrow().convertTo(Int::class.java).orElseThrow())

        val referencedEnumProp = myIdeaNodeModule.export("ReferencedEnumProp").orElseThrow().referencedElement() as IVariable
        val referencedEnumPropObjectLiteral = referencedEnumProp.objectLiteralValue().orElseThrow()
        assertEquals(2, referencedEnumPropObjectLiteral.properties().size)
        assertFalse(referencedEnumPropObjectLiteral.property("b").orElseThrow().convertTo(Boolean::class.java).orElseThrow())

        val referencedType = myIdeaNodeModule.export("ReferencedType").orElseThrow().referencedElement() as IVariable
        assertEquals("WildcardClass", referencedType.constantValue().asES6Class().orElseThrow().name())

        val referencedTypeProp = myIdeaNodeModule.export("ReferencedTypeProp").orElseThrow().referencedElement() as IVariable
        assertEquals("WildcardClass", referencedTypeProp.constantValue().asES6Class().orElseThrow().name())
    }
}