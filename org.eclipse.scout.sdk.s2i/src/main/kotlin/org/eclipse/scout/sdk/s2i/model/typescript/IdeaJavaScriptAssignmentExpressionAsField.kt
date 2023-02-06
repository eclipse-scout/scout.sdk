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
import org.eclipse.scout.sdk.core.util.FinalValue

open class IdeaJavaScriptAssignmentExpressionAsField internal constructor(val ideaModule: IdeaNodeModule, val javaScriptAssignmentExpression: JSAssignmentExpression, val javaScriptReferenceExpression: JSReferenceExpression) :
    AbstractFieldSpi(ideaModule) {

    private val m_dataType = FinalValue<DataTypeSpi?>()
    private val m_constantValue = FinalValue<IConstantValue>()

    override fun source() = ideaModule.sourceFor(javaScriptAssignmentExpression)

    override fun name() = javaScriptReferenceExpression.referenceName

    override fun isOptional() = false

    override fun hasModifier(modifier: Modifier) = false

    override fun constantValue(): IConstantValue = m_constantValue.computeIfAbsentAndGet {
        ideaModule.spiFactory.createConstantValue(javaScriptAssignmentExpression.rOperand, ideaModule)
    }

    override fun dataTypeImpl(): DataTypeSpi? = m_dataType.computeIfAbsentAndGet {
        ideaModule.dataTypeFactory.createDataType(javaScriptAssignmentExpression, this::constantValue)
    }
}