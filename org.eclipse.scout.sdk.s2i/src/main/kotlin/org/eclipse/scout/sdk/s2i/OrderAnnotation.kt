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
import org.eclipse.scout.sdk.core.s.ISdkConstants
import org.eclipse.scout.sdk.core.s.apidef.IScoutApi
import org.eclipse.scout.sdk.core.util.FinalValue
import org.eclipse.scout.sdk.s2i.environment.IdeaEnvironment

class OrderAnnotation private constructor(psiClass: PsiClass, psiAnnotation: PsiAnnotation, scoutApi: IScoutApi) : AbstractClassAnnotation(psiClass, psiAnnotation, scoutApi) {

    private val m_value = FinalValue<Double>()

    companion object {
        fun of(annotation: PsiAnnotation?, scoutApi: IScoutApi) = of(annotation, null, scoutApi)

        fun of(owner: PsiClass?, scoutApi: IScoutApi) = of(null, owner, scoutApi)

        fun of(annotation: PsiAnnotation?, owner: PsiClass?, scoutApi: IScoutApi) = classAnnotation(scoutApi.Order().fqn(), annotation, owner)
                ?.let { OrderAnnotation(it.first, it.second, scoutApi) }

        fun valueOf(owner: PsiClass?, scoutApi: IScoutApi) = of(null, owner, scoutApi)?.value()
    }

    fun value(): Double = m_value.computeIfAbsentAndGet {
        val valueElementName = scoutApi.ClassId().valueElementName()
        IdeaEnvironment.computeInReadAction(psiClass.project) {
            psiAnnotation.findAttributeValue(valueElementName)
                    ?.valueAs(Number::class.java)
                    ?.toDouble() ?: defaultOrder()
        }
    }

    fun defaultOrder() = if (psiClass.isInstanceOf(scoutApi.IOrdered())) ISdkConstants.DEFAULT_VIEW_ORDER else ISdkConstants.DEFAULT_BEAN_ORDER
}