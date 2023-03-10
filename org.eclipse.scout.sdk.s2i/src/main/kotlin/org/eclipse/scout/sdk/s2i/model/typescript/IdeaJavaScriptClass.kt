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

import com.intellij.lang.javascript.psi.ecma6.TypeScriptEnum
import com.intellij.lang.javascript.psi.ecma6.TypeScriptInterface
import com.intellij.lang.javascript.psi.ecmal4.JSClass
import org.eclipse.scout.sdk.core.typescript.model.api.IES6Class
import org.eclipse.scout.sdk.core.typescript.model.api.internal.ES6ClassImplementor
import org.eclipse.scout.sdk.core.typescript.model.spi.AbstractNodeElementSpi
import org.eclipse.scout.sdk.core.typescript.model.spi.ES6ClassSpi
import org.eclipse.scout.sdk.core.typescript.model.spi.FieldSpi
import org.eclipse.scout.sdk.core.typescript.model.spi.FunctionSpi
import org.eclipse.scout.sdk.core.util.FinalValue
import org.eclipse.scout.sdk.s2i.model.typescript.util.DataTypeSpiUtils
import org.eclipse.scout.sdk.s2i.model.typescript.util.FieldSpiUtils
import java.util.*
import java.util.stream.Stream

open class IdeaJavaScriptClass(protected val ideaModule: IdeaNodeModule, internal val javaScriptClass: JSClass) : AbstractNodeElementSpi<IES6Class>(ideaModule), ES6ClassSpi {

    private val m_fields = FinalValue<List<FieldSpi>>()
    private val m_superInterfaces = FinalValue<List<ES6ClassSpi>>()
    private val m_superClass = FinalValue<Optional<ES6ClassSpi>>()
    private val m_functions = FinalValue<List<FunctionSpi>>()
    private val m_name = FinalValue<String>()

    override fun createApi() = ES6ClassImplementor(this)

    override fun source() = ideaModule.sourceFor(javaScriptClass)

    override fun name(): String = m_name.computeIfAbsentAndGet { javaScriptClass.name }

    override fun isEnum() = javaScriptClass is TypeScriptEnum

    override fun isInterface() = javaScriptClass is TypeScriptInterface

    override fun createDataType(name: String) = DataTypeSpiUtils.createDataType(name, javaScriptClass, ideaModule)

    override fun functions(): List<FunctionSpi> = m_functions.computeIfAbsentAndGet {
        val functions = javaScriptClass.functions.map { ideaModule.nodeElementFactory().createJavaScriptFunction(it) }
        return@computeIfAbsentAndGet Collections.unmodifiableList(functions)
    }

    override fun superInterfaces(): Stream<ES6ClassSpi> = m_superInterfaces.computeIfAbsentAndGet {
        val superInterfaces = if (isInterface) javaScriptClass.superClasses else javaScriptClass.implementedInterfaces
        val spi = superInterfaces.mapNotNull {
            val module = ideaModule.moduleInventory.findContainingModule(it) ?: return@mapNotNull null
            module.createSpiForPsi(it) as? ES6ClassSpi
        }
        return@computeIfAbsentAndGet Collections.unmodifiableList(spi)
    }.stream()

    override fun superClass(): Optional<ES6ClassSpi> = m_superClass.computeIfAbsentAndGet {
        if (isInterface) return@computeIfAbsentAndGet Optional.empty() // interfaces have no super classes

        val superClass = javaScriptClass.superClasses.firstOrNull() ?: return@computeIfAbsentAndGet Optional.empty()
        val module = ideaModule.moduleInventory.findContainingModule(superClass) ?: return@computeIfAbsentAndGet Optional.empty()
        val superClassSpi = module.createSpiForPsi(superClass) as? ES6ClassSpi
        return@computeIfAbsentAndGet Optional.ofNullable(superClassSpi)
    }

    override fun fields(): List<FieldSpi> = m_fields.computeIfAbsentAndGet {
        FieldSpiUtils.collectFields(javaScriptClass, ideaModule)
    }
}