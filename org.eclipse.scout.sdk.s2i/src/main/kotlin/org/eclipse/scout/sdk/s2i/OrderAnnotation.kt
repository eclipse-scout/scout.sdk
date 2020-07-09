/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.s2i

import com.intellij.psi.PsiAnnotation
import com.intellij.psi.PsiClass
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes
import org.eclipse.scout.sdk.core.s.ISdkProperties
import org.eclipse.scout.sdk.core.util.FinalValue
import org.eclipse.scout.sdk.s2i.environment.IdeaEnvironment

class OrderAnnotation private constructor(psiClass: PsiClass, psiAnnotation: PsiAnnotation) : AbstractClassAnnotation(psiClass, psiAnnotation) {

    private val m_value = FinalValue<Double>()

    companion object {

        const val VALUE_ATTRIBUTE_NAME = org.eclipse.scout.sdk.core.s.annotation.OrderAnnotation.VALUE_ELEMENT_NAME

        fun of(annotation: PsiAnnotation?) = of(annotation, null)

        fun of(owner: PsiClass?) = of(null, owner)

        fun of(annotation: PsiAnnotation?, owner: PsiClass?) =
                classAnnotation(IScoutRuntimeTypes.Order, annotation, owner)
                        ?.let { OrderAnnotation(it.first, it.second) }

        fun valueOf(owner: PsiClass?) = of(owner)?.value()
    }

    fun value(): Double = m_value.computeIfAbsentAndGet {
        IdeaEnvironment.computeInReadAction(psiClass.project) {
            psiAnnotation.findAttributeValue(VALUE_ATTRIBUTE_NAME)
                    ?.valueAs(Number::class.java)
                    ?.toDouble() ?: defaultOrder()
        }
    }

    private fun defaultOrder() = if (psiClass.isInstanceOf(IScoutRuntimeTypes.IOrdered)) ISdkProperties.DEFAULT_VIEW_ORDER else ISdkProperties.DEFAULT_BEAN_ORDER
}