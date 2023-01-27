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
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.lang.javascript.psi.JSThisExpression
import com.intellij.lang.javascript.psi.jsdoc.JSDocComment
import org.eclipse.scout.sdk.core.typescript.model.api.IConstantValue
import org.eclipse.scout.sdk.core.typescript.model.api.IField
import org.eclipse.scout.sdk.core.typescript.model.api.Modifier
import org.eclipse.scout.sdk.core.typescript.model.api.internal.FieldImplementor
import org.eclipse.scout.sdk.core.typescript.model.spi.AbstractNodeElementSpi
import org.eclipse.scout.sdk.core.typescript.model.spi.DataTypeSpi
import org.eclipse.scout.sdk.core.typescript.model.spi.FieldSpi
import org.eclipse.scout.sdk.core.util.FinalValue

open class IdeaJavaScriptAssignmentExpressionAsField internal constructor(val ideaModule: IdeaNodeModule, val javaScriptAssignmentExpression: JSAssignmentExpression, val javaScriptReferenceExpression: JSReferenceExpression) :
    AbstractNodeElementSpi<IField>(ideaModule), FieldSpi {

    private val m_javaScriptExpressionStatement = FinalValue<JSExpressionStatement?>()
    private val m_dataType = FinalValue<DataTypeSpi?>()
    private val m_constantValue = FinalValue<IConstantValue>()

    override fun createApi() = FieldImplementor(this)

    override fun source() = ideaModule.sourceFor(javaScriptAssignmentExpression)

    override fun name() = javaScriptReferenceExpression.referenceName

    override fun isOptional() = false

    override fun hasModifier(modifier: Modifier) = false

    override fun constantValue(): IConstantValue = m_constantValue.computeIfAbsentAndGet {
        ideaModule.spiFactory.createConstantValue(javaScriptAssignmentExpression.rOperand, ideaModule)
    }

    protected fun javaScriptExpressionStatement() = m_javaScriptExpressionStatement.computeIfAbsentAndGet { javaScriptAssignmentExpression.parent as? JSExpressionStatement }

    override fun dataType(): DataTypeSpi? = m_dataType.computeIfAbsentAndGet {
        val comment = javaScriptExpressionStatement()?.children?.firstNotNullOfOrNull { it as? JSDocComment }
        comment?.let { return@computeIfAbsentAndGet IdeaJavaScriptDocCommentAsDataType.parseType(ideaModule, it) }
        return@computeIfAbsentAndGet constantValue().dataType().orElse(null)?.spi()
    }

    companion object {
        fun parse(ideaModule: IdeaNodeModule, assignment: JSAssignmentExpression?): IdeaJavaScriptAssignmentExpressionAsField? {
            val reference: JSReferenceExpression = assignment?.definitionExpression?.expression as? JSReferenceExpression ?: return null
            if (reference.qualifier !is JSThisExpression) return null
            return ideaModule.spiFactory.createJavaScriptAssignmentExpressionAsField(assignment, reference)
        }
    }
}