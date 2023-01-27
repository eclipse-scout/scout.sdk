/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2i.model

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.eclipse.scout.sdk.core.typescript.TypeScriptTypes
import org.eclipse.scout.sdk.core.typescript.model.api.IES6Class
import org.eclipse.scout.sdk.core.typescript.model.spi.ES6ClassSpi
import org.eclipse.scout.sdk.s2i.model.typescript.IdeaNodeModule
import org.eclipse.scout.sdk.s2i.model.typescript.IdeaSpiFactory
import org.mockito.Mockito

abstract class AbstractES6ClassTest(val fixturePath: String) : BasePlatformTestCase() {

    protected val ideaModule: IdeaNodeModule = Mockito.mock(IdeaNodeModule::class.java, "nodeModule")
    protected lateinit var psiFile: PsiFile

    override fun getTestDataPath() = "src/test/resources"

    override fun setUp() {
        super.setUp()
        Mockito.`when`(ideaModule.spiFactory).thenReturn(IdeaSpiFactory(ideaModule))
        psiFile = myFixture.configureByFile(fixturePath)
    }

    protected fun <T : PsiElement> findChildOfType(type: Class<T>): T = PsiTreeUtil.getRequiredChildOfType(psiFile, type)

    fun testES6Class() {
        val spi = createES6ClassSpi()
        assertNotNull(spi)
        assertES6Class(spi.api())
    }

    protected abstract fun createES6ClassSpi(): ES6ClassSpi

    protected open fun isOptionalPossible(): Boolean = true

    protected open fun isDataTypePresentForAllFields(): Boolean = true

    protected open fun assertES6Class(es6Class: IES6Class, optionalPossible: Boolean = isOptionalPossible(), dataTypePresentForAllFields: Boolean = isDataTypePresentForAllFields()) {
        assertEquals(7, es6Class.fields().stream().count())
        assertField(es6Class, "myStringOpt", optionalPossible, TypeScriptTypes._string)
        assertField(es6Class, "myNumber", false, TypeScriptTypes._number)
        assertField(es6Class, "myBoolean", false, TypeScriptTypes._boolean)
        assertField(es6Class, "myUndefined", false, if (dataTypePresentForAllFields) TypeScriptTypes._undefined else null)
        assertField(es6Class, "myNull", false, if (dataTypePresentForAllFields) TypeScriptTypes._null else null)
        assertField(es6Class, "myObject", false, TypeScriptTypes._object)
        assertField(es6Class, "myAnyOpt", optionalPossible, if (dataTypePresentForAllFields) TypeScriptTypes._any else null)
    }

    protected open fun assertField(es6Class: IES6Class, name: String, optional: Boolean, dataType: String?) {
        val field = es6Class.field(name).orElse(null)
        assertNotNull(field)
        assertEquals(optional, field.isOptional)
        if (dataType != null) {
            assertEquals(dataType, field.dataType().orElseThrow().name())
            assertEquals(TypeScriptTypes.isPrimitive(dataType), field.dataType().orElseThrow().isPrimitive)
        } else {
            assertTrue(field.dataType().isEmpty)
        }
    }
}