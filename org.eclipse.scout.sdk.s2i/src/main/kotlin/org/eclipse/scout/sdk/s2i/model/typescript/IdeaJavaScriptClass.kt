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

import com.intellij.lang.javascript.psi.JSAssignmentExpression
import com.intellij.lang.javascript.psi.JSExpressionStatement
import com.intellij.lang.javascript.psi.ecmal4.JSClass
import com.intellij.lang.javascript.psi.util.JSClassUtils
import org.eclipse.scout.sdk.core.typescript.model.api.IES6Class
import org.eclipse.scout.sdk.core.typescript.model.api.internal.ES6ClassImplementor
import org.eclipse.scout.sdk.core.typescript.model.spi.AbstractNodeElementSpi
import org.eclipse.scout.sdk.core.typescript.model.spi.ES6ClassSpi
import org.eclipse.scout.sdk.core.typescript.model.spi.FieldSpi
import org.eclipse.scout.sdk.core.util.FinalValue
import java.util.*

open class IdeaJavaScriptClass(protected val ideaModule: IdeaNodeModule, internal val javaScriptClass: JSClass) : AbstractNodeElementSpi<IES6Class>(ideaModule), ES6ClassSpi {

    private val m_fields = FinalValue<List<FieldSpi>>()

    override fun createApi() = ES6ClassImplementor(this)

    override fun source() = ideaModule.sourceFor(javaScriptClass)

    override fun name() = javaScriptClass.name

    override fun fields(): List<FieldSpi> = m_fields.computeIfAbsentAndGet {
        val collector: MutableList<FieldSpi> = ArrayList()
        collectFields(collector)
        return@computeIfAbsentAndGet Collections.unmodifiableList(collector)
    }

    protected open fun collectFields(collector: MutableCollection<FieldSpi>) {
        javaScriptClass.fields.asSequence()
            .filterNotNull()
            .filter { !JSClassUtils.isStaticMethodOrField(it) }
            .map { IdeaJavaScriptField(ideaModule, it) }
            .forEach { collector.add(it) }

        val block = javaScriptClass.constructor?.block ?: return
        block.statementListItems.asSequence()
            .filter { it is JSExpressionStatement }
            .map { it.children.asSequence().firstNotNullOfOrNull { child -> child as? JSAssignmentExpression } }
            .map { IdeaJavaScriptAssignmentExpressionAsField.parse(ideaModule, it) }
            .filterNotNull()
            .forEach { collector.add(it) }
    }
}