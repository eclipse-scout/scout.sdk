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
import com.intellij.lang.javascript.psi.JSThisExpression
import org.eclipse.scout.sdk.core.typescript.model.api.IField
import org.eclipse.scout.sdk.core.typescript.model.api.internal.FieldImplementor
import org.eclipse.scout.sdk.core.typescript.model.spi.AbstractNodeElementSpi
import org.eclipse.scout.sdk.core.typescript.model.spi.FieldSpi

open class IdeaJavaScriptAssignmentExpressionAsField protected constructor(val ideaModule: IdeaNodeModule, val javaScriptAssignmentExpression: JSAssignmentExpression, val javaScriptReferenceExpression: JSReferenceExpression): AbstractNodeElementSpi<IField>(ideaModule), FieldSpi {

    override fun createApi() = FieldImplementor(this)

    override fun source() = ideaModule.sourceFor(javaScriptAssignmentExpression)

    override fun name() = javaScriptReferenceExpression.referenceName

    override fun isOptional(): Boolean = false

    companion object {
        fun parse(ideaModule: IdeaNodeModule, assignment: JSAssignmentExpression?): IdeaJavaScriptAssignmentExpressionAsField? {
            val reference: JSReferenceExpression = assignment?.definitionExpression?.expression as? JSReferenceExpression ?: return null
            if (reference.qualifier !is JSThisExpression) return null
            return IdeaJavaScriptAssignmentExpressionAsField(ideaModule, assignment, reference)
        }
    }
}