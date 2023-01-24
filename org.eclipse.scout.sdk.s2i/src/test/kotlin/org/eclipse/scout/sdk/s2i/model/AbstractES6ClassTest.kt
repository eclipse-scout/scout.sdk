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
import org.eclipse.scout.sdk.core.typescript.model.api.IES6Class
import org.eclipse.scout.sdk.core.typescript.model.spi.ES6ClassSpi
import org.eclipse.scout.sdk.s2i.model.typescript.IdeaNodeModule
import org.mockito.Mockito

abstract class AbstractES6ClassTest(val fixturePath: String) : BasePlatformTestCase() {

    protected val ideaModule: IdeaNodeModule = Mockito.mock(IdeaNodeModule::class.java, "nodeModule")
    protected lateinit var psiFile: PsiFile

    override fun getTestDataPath() = "src/test/resources"

    override fun setUp() {
        super.setUp()
        psiFile = myFixture.configureByFile(fixturePath)
    }

    protected fun <T : PsiElement> findChildOfType(type: Class<T>): T = PsiTreeUtil.getRequiredChildOfType(psiFile, type)

    fun testES6Class() {
        val spi = createES6ClassSpi()
        assertNotNull(spi)
        assertES6Class(spi.api())
    }

    protected abstract fun createES6ClassSpi(): ES6ClassSpi

    protected open fun isAssertOptional(): Boolean = true

    protected fun assertES6Class(es6Class: IES6Class, assertOptional: Boolean = isAssertOptional()) {
        assertEquals(5, es6Class.fields().stream().count())
        assertField(es6Class, "myNumber", false)
        assertField(es6Class, "myBoolean", false)
        assertField(es6Class, "myStringOpt", assertOptional)
        assertField(es6Class, "myAnyOpt", assertOptional)
        assertField(es6Class, "myObject", false)
    }

    protected fun assertField(es6Class: IES6Class, name: String, optional: Boolean) {
        val field = es6Class.field(name).orElse(null)
        assertNotNull(field)
        assertEquals(optional, field.isOptional)
    }
}