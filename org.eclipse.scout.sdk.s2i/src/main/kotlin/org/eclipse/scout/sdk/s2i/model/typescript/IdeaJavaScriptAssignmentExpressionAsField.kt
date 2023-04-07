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
import com.intellij.lang.javascript.psi.JSReferenceExpression
import org.eclipse.scout.sdk.core.typescript.model.api.IConstantValue
import org.eclipse.scout.sdk.core.typescript.model.api.Modifier
import org.eclipse.scout.sdk.core.typescript.model.spi.AbstractFieldSpi
import org.eclipse.scout.sdk.core.typescript.model.spi.DataTypeSpi
import org.eclipse.scout.sdk.core.typescript.model.spi.ES6ClassSpi
import org.eclipse.scout.sdk.core.util.FinalValue
import org.eclipse.scout.sdk.core.util.SourceRange
import org.eclipse.scout.sdk.s2i.model.typescript.util.DataTypeSpiUtils
import java.util.*

open class IdeaJavaScriptAssignmentExpressionAsField internal constructor(
    protected val ideaModule: IdeaNodeModule,
    internal val javaScriptAssignmentExpression: JSAssignmentExpression,
    internal val javaScriptReferenceExpression: JSReferenceExpression,
    declaringClass: ES6ClassSpi
) : AbstractFieldSpi(ideaModule, declaringClass) {

    private val m_dataType = FinalValue<DataTypeSpi?>()
    private val m_constantValue = FinalValue<IConstantValue>()
    private val m_source = FinalValue<Optional<SourceRange>>()

    override fun source(): Optional<SourceRange> = m_source.computeIfAbsentAndGet { ideaModule.sourceFor(javaScriptAssignmentExpression) }

    override fun name() = javaScriptReferenceExpression.referenceName

    override fun isOptional() = false

    override fun hasModifier(modifier: Modifier) = false

    override fun constantValue(): IConstantValue = m_constantValue.computeIfAbsentAndGet {
        ideaModule.nodeElementFactory().createConstantValue(javaScriptAssignmentExpression.rOperand)
    }

    override fun dataType(): DataTypeSpi? = m_dataType.computeIfAbsentAndGet {
        DataTypeSpiUtils.createDataType(javaScriptAssignmentExpression, ideaModule, this::constantValue)
    }
}