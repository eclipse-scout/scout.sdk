/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2i.classid

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiAnnotation
import com.intellij.psi.PsiClass
import org.eclipse.scout.sdk.core.s.apidef.IScoutApi
import org.eclipse.scout.sdk.core.util.FinalValue
import org.eclipse.scout.sdk.core.util.Strings
import org.eclipse.scout.sdk.s2i.AbstractClassAnnotation
import org.eclipse.scout.sdk.s2i.environment.IdeaEnvironment.Factory.computeInReadAction

open class ClassIdAnnotation private constructor(psiClass: PsiClass, psiAnnotation: PsiAnnotation, project: Project, scoutApi: IScoutApi) : AbstractClassAnnotation(psiClass, psiAnnotation, project, scoutApi) {

    private val m_value = FinalValue<String>()

    companion object {
        fun of(annotation: PsiAnnotation?, project: Project, scoutApi: IScoutApi) = of(annotation, null, project, scoutApi)

        fun of(owner: PsiClass?, project: Project, scoutApi: IScoutApi) = of(null, owner, project, scoutApi)

        fun of(annotation: PsiAnnotation?, owner: PsiClass?, project: Project, scoutApi: IScoutApi) = classAnnotation(scoutApi.ClassId().fqn(), annotation, owner, project)
                ?.let { ClassIdAnnotation(it.first, it.second, project, scoutApi) }
    }

    fun value(): String? = m_value.computeIfAbsentAndGet {
        val valueElementName = scoutApi.ClassId().valueElementName()
        computeInReadAction(project) {
            psiAnnotation.findAttributeValue(valueElementName)?.valueAs(String::class.java)
        }
    }

    fun hasValue(): Boolean = Strings.hasText(value())
}