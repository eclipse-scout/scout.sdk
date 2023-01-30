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
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import org.eclipse.scout.sdk.core.typescript.model.api.IConstantValue
import org.eclipse.scout.sdk.core.typescript.model.api.IObjectLiteral
import org.eclipse.scout.sdk.core.typescript.model.api.internal.ObjectLiteralImplementor
import org.eclipse.scout.sdk.core.typescript.model.spi.AbstractNodeElementSpi
import org.eclipse.scout.sdk.core.typescript.model.spi.ObjectLiteralSpi
import org.eclipse.scout.sdk.core.util.FinalValue
import java.util.*

open class IdeaJavaScriptObjectLiteral(protected val ideaModule: IdeaNodeModule, internal val jsObjectLiteral: JSObjectLiteralExpression) : AbstractNodeElementSpi<IObjectLiteral>(ideaModule), ObjectLiteralSpi {

    private val m_properties = FinalValue<Map<String, IConstantValue>>()

    override fun createApi() = ObjectLiteralImplementor(this)

    override fun source() = ideaModule.sourceFor(jsObjectLiteral)

    override fun name() = "" // anonymous

    override fun properties(): Map<String, IConstantValue> = m_properties.computeIfAbsentAndGet {
        return@computeIfAbsentAndGet Collections.unmodifiableMap(collectProperties())
    }

    fun collectProperties(): Map<String, IConstantValue> {
        return jsObjectLiteral.properties
            .mapNotNull { createMapping(it.name, it.value) }
            .toMap()
    }

    private fun createMapping(name: String?, expression: JSExpression?): Pair<String, IdeaConstantValue>? {
        if (name == null || expression == null) return null
        return name to ideaModule.spiFactory.createConstantValue(expression, ideaModule)
    }
}
