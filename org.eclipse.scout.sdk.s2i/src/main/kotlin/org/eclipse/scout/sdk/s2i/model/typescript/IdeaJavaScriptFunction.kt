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

import com.intellij.lang.javascript.psi.JSFunction
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.lang.javascript.psi.JSParenthesizedExpression
import org.eclipse.scout.sdk.core.typescript.model.api.IFunction
import org.eclipse.scout.sdk.core.typescript.model.api.internal.FunctionImplementor
import org.eclipse.scout.sdk.core.typescript.model.spi.AbstractNodeElementSpi
import org.eclipse.scout.sdk.core.typescript.model.spi.FunctionSpi
import org.eclipse.scout.sdk.core.typescript.model.spi.ObjectLiteralSpi
import java.util.*

open class IdeaJavaScriptFunction(protected val ideaModule: IdeaNodeModule, internal val javaScriptFunction: JSFunction) : AbstractNodeElementSpi<IFunction>(ideaModule), FunctionSpi {

    override fun createApi() = FunctionImplementor(this)

    override fun source() = ideaModule.sourceFor(javaScriptFunction)

    override fun name() = javaScriptFunction.name

    override fun resultingObjectLiteral(): Optional<ObjectLiteralSpi> {
        val literal = javaScriptFunction
            .takeIf { it.isArrowFunction }
            ?.children
            ?.firstNotNullOfOrNull { it as? JSParenthesizedExpression }
            ?.innerExpression as? JSObjectLiteralExpression
        return Optional.ofNullable(literal)
            .map { ideaModule.spiFactory.createObjectLiteralExpression(it) }
    }
}