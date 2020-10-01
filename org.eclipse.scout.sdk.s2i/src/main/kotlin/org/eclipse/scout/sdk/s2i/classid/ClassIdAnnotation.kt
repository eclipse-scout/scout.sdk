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
package org.eclipse.scout.sdk.s2i.classid

import com.intellij.psi.PsiAnnotation
import com.intellij.psi.PsiClass
import org.eclipse.scout.sdk.core.s.apidef.IScoutApi
import org.eclipse.scout.sdk.core.util.FinalValue
import org.eclipse.scout.sdk.core.util.Strings
import org.eclipse.scout.sdk.s2i.AbstractClassAnnotation
import org.eclipse.scout.sdk.s2i.environment.IdeaEnvironment.Factory.computeInReadAction

open class ClassIdAnnotation private constructor(psiClass: PsiClass, psiAnnotation: PsiAnnotation, scoutApi: IScoutApi) : AbstractClassAnnotation(psiClass, psiAnnotation, scoutApi) {

    private val m_value = FinalValue<String>()

    companion object {
        fun of(annotation: PsiAnnotation?, scoutApi: IScoutApi) = of(annotation, null, scoutApi)

        fun of(owner: PsiClass?, scoutApi: IScoutApi) = of(null, owner, scoutApi)

        fun of(annotation: PsiAnnotation?, owner: PsiClass?, scoutApi: IScoutApi) = classAnnotation(scoutApi.ClassId().fqn(), annotation, owner)
                ?.let { ClassIdAnnotation(it.first, it.second, scoutApi) }
    }

    fun value(): String? = m_value.computeIfAbsentAndGet {
        val valueElementName = scoutApi.ClassId().valueElementName()
        computeInReadAction(psiClass.project) {
            psiAnnotation.findAttributeValue(valueElementName)?.valueAs(String::class.java)
        }
    }

    fun hasValue(): Boolean = Strings.hasText(value())
}
