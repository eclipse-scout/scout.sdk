/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2i

import com.intellij.openapi.project.Project
import com.intellij.psi.*
import com.intellij.psi.util.PsiTreeUtil
import org.eclipse.scout.sdk.core.java.JavaTypes
import org.eclipse.scout.sdk.core.s.java.apidef.IScoutApi
import org.eclipse.scout.sdk.core.util.FinalValue
import org.eclipse.scout.sdk.s2i.environment.IdeaEnvironment.Factory.computeInReadAction

abstract class AbstractClassAnnotation protected constructor(val psiClass: PsiClass, val psiAnnotation: PsiAnnotation, val project: Project, val scoutApi: IScoutApi) {

    private val m_ownerFqn = FinalValue<String>()

    companion object {
        fun classAnnotation(fqn: String, annotation: PsiAnnotation?, owner: PsiClass?, project: Project): Pair<PsiClass, PsiAnnotation>? {
            if (owner == null && annotation == null) {
                return null
            }

            val declaringClass = owner ?: computeInReadAction(project) { PsiTreeUtil.getParentOfType(annotation, PsiClass::class.java) } ?: return null
            val annotationElement = resolveAnnotation(fqn, annotation, declaringClass, project) ?: return null
            return declaringClass to annotationElement
        }

        private fun resolveAnnotation(fqn: String, annotation: PsiAnnotation?, owner: PsiClass, project: Project): PsiAnnotation? {
            if (annotation == null) {
                return computeInReadAction(project) { owner.getAnnotation(fqn) }
            }
            val annotationName = computeInReadAction(project) {
                // do not use annotation.qualifiedName here because it requires a resolve (slow and might not be allowed in contexts like psi change listeners)
                annotation.nameReferenceElement?.referenceName
            } ?: return null
            if (JavaTypes.simpleName(fqn) == annotationName || fqn == annotationName) {
                return annotation
            }
            return null
        }
    }

    fun ownerFqn(): String? = m_ownerFqn.computeIfAbsentAndGet {
        computeInReadAction(project) {
            psiClass.qualifiedName
        }
    }

    protected fun <T> PsiAnnotationMemberValue.valueAs(type: Class<T>): T? {
        var computedValue: Any? = null

        if (this is PsiLiteral) {
            computedValue = value
        } else if (this is PsiReference) {
            val target = resolve()
            if (target is PsiVariable) {
                computedValue = target.computeConstantValue()
            }
        }
        @Suppress("UNCHECKED_CAST")
        return if (type.isInstance(computedValue)) computedValue as T else null
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (javaClass != other?.javaClass) {
            return false
        }

        other as AbstractClassAnnotation
        return psiAnnotation == other.psiAnnotation
    }

    override fun hashCode() = psiAnnotation.hashCode()

    override fun toString() = "${javaClass.simpleName} of ${psiClass.name}"
}