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

import com.intellij.lang.javascript.psi.JSExpression
import com.intellij.lang.javascript.psi.ecma6.TypeScriptEnum
import com.intellij.lang.javascript.psi.ecma6.TypeScriptInterface
import com.intellij.lang.javascript.psi.ecma6.TypeScriptTypeAlias
import com.intellij.lang.javascript.psi.ecmal4.JSClass
import org.eclipse.scout.sdk.core.typescript.model.api.IES6Class
import org.eclipse.scout.sdk.core.typescript.model.api.Modifier
import org.eclipse.scout.sdk.core.typescript.model.api.internal.ES6ClassImplementor
import org.eclipse.scout.sdk.core.typescript.model.spi.*
import org.eclipse.scout.sdk.core.util.FinalValue
import org.eclipse.scout.sdk.s2i.model.typescript.util.DataTypeSpiUtils
import org.eclipse.scout.sdk.s2i.model.typescript.util.FieldSpiUtils
import org.eclipse.scout.sdk.s2i.model.typescript.util.exportType
import org.eclipse.scout.sdk.s2i.model.typescript.util.toModifierType
import org.eclipse.scout.sdk.s2i.resolveLocalPath
import java.util.*
import java.util.stream.Stream

open class IdeaJavaScriptClass(protected val ideaModule: IdeaNodeModule, internal val javaScriptClass: JSClass) : AbstractNodeElementSpi<IES6Class>(ideaModule), ES6ClassSpi {

    private val m_fields = FinalValue<List<FieldSpi>>()
    private val m_superInterfaces = FinalValue<List<ES6ClassSpi>>()
    private val m_superClass = FinalValue<Optional<ES6ClassSpi>>()
    private val m_functions = FinalValue<List<FunctionSpi>>()
    private val m_name = FinalValue<String>()
    private val m_aliasedDataType = FinalValue<Optional<DataTypeSpi>>()

    override fun createApi() = ES6ClassImplementor(this)

    override fun source() = ideaModule.sourceFor(javaScriptClass)

    override fun exportType() = javaScriptClass.exportType()

    override fun resolveContainingFile() = javaScriptClass.containingFile.virtualFile.resolveLocalPath()

    override fun name(): String = m_name.computeIfAbsentAndGet { javaScriptClass.name }

    override fun hasModifier(modifier: Modifier) = javaScriptClass.hasModifier(modifier.toModifierType())

    override fun childTypes(): Collection<DataTypeSpi> = aliasedDataType().map { listOf(it) }.orElseGet { emptyList<DataTypeSpi>() }

    override fun isEnum() = javaScriptClass is TypeScriptEnum

    override fun isInterface() = javaScriptClass is TypeScriptInterface

    override fun isTypeAlias() = javaScriptClass is TypeScriptTypeAlias

    override fun aliasedDataType(): Optional<DataTypeSpi> = m_aliasedDataType.computeIfAbsentAndGet {
        val typeAlias = javaScriptClass as? TypeScriptTypeAlias ?: return@computeIfAbsentAndGet Optional.empty()
        val jsType = typeAlias.parsedTypeDeclaration ?: return@computeIfAbsentAndGet Optional.empty()
        Optional.ofNullable(DataTypeSpiUtils.createDataType(jsType, ideaModule))
    }

    override fun createDataType(name: String) = DataTypeSpiUtils.createDataType(name, javaScriptClass, ideaModule)

    override fun functions(): List<FunctionSpi> = m_functions.computeIfAbsentAndGet {
        val functions = javaScriptClass.functions.map { ideaModule.nodeElementFactory().createJavaScriptFunction(it) }
        return@computeIfAbsentAndGet Collections.unmodifiableList(functions)
    }

    override fun typeArguments() = emptyList<DataTypeSpi>()

    override fun superInterfaces(): Stream<ES6ClassSpi> = m_superInterfaces.computeIfAbsentAndGet {
        val superInterfaces = if (isInterface) javaScriptClass.extendsList else javaScriptClass.implementsList
        val interfaces = superInterfaces?.expressions?.mapNotNull { resolveReferencedClass(it) } ?: emptyList()
        return@computeIfAbsentAndGet Collections.unmodifiableList(interfaces)
    }.stream()

    override fun superClass(): Optional<ES6ClassSpi> = m_superClass.computeIfAbsentAndGet {
        if (isInterface) return@computeIfAbsentAndGet Optional.empty() // interfaces have no super classes
        val superClassRef = javaScriptClass.extendsList?.expressions?.firstOrNull() ?: return@computeIfAbsentAndGet Optional.empty()
        return@computeIfAbsentAndGet Optional.ofNullable(resolveReferencedClass(superClassRef))
    }

    protected fun resolveReferencedClass(referencedClass: JSExpression) = ideaModule.resolveReferencedElement(referencedClass) as? ES6ClassSpi

    override fun fields(): List<FieldSpi> = m_fields.computeIfAbsentAndGet {
        FieldSpiUtils.collectFields(javaScriptClass, this, ideaModule)
    }
}