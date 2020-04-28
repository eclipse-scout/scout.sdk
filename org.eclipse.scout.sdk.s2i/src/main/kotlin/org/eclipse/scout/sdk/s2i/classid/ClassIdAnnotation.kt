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

import com.intellij.psi.*
import com.intellij.psi.util.PsiTreeUtil
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes
import org.eclipse.scout.sdk.core.util.FinalValue
import org.eclipse.scout.sdk.core.util.JavaTypes
import org.eclipse.scout.sdk.core.util.Strings
import org.eclipse.scout.sdk.s2i.environment.IdeaEnvironment

open class ClassIdAnnotation private constructor(val psiClass: PsiClass, val psiAnnotation: PsiAnnotation) {

    private val m_value = FinalValue<String>()
    private val m_ownerFqn = FinalValue<String>()

    companion object {

        const val VALUE_ATTRIBUTE_NAME = org.eclipse.scout.sdk.core.s.annotation.ClassIdAnnotation.VALUE_ELEMENT_NAME
        val CLASS_ID_SIMPLE_NAME: String = JavaTypes.simpleName(IScoutRuntimeTypes.ClassId)

        fun of(annotation: PsiAnnotation?): ClassIdAnnotation? {
            return of(annotation, null)
        }

        fun of(owner: PsiClass?): ClassIdAnnotation? {
            return of(null, owner)
        }

        fun of(annotation: PsiAnnotation?, owner: PsiClass?): ClassIdAnnotation? {
            if (owner == null && annotation == null) {
                return null
            }

            val declaringClass = owner ?: IdeaEnvironment.computeInReadAction(annotation!!.project) {
                PsiTreeUtil.getParentOfType(annotation, PsiClass::class.java)
            } ?: return null
            val annotationElement = resolveAnnotation(annotation, declaringClass) ?: return null

            return ClassIdAnnotation(declaringClass, annotationElement)
        }

        private fun resolveAnnotation(annotation: PsiAnnotation?, owner: PsiClass): PsiAnnotation? {
            if (annotation == null) {
                return IdeaEnvironment.computeInReadAction(owner.project) { owner.getAnnotation(IScoutRuntimeTypes.ClassId) }
            }

            val annotationName = IdeaEnvironment.computeInReadAction(annotation.project) {
                // do not use annotation.qualifiedName here because it requires a resolve (slow and might not be allowed in contexts like psi change listeners)
                annotation.nameReferenceElement?.referenceName
            } ?: return null
            if (CLASS_ID_SIMPLE_NAME == annotationName || IScoutRuntimeTypes.ClassId == annotationName) {
                return annotation
            }
            return null
        }
    }

    fun ownerFqn(): String? = m_ownerFqn.computeIfAbsentAndGet {
        IdeaEnvironment.computeInReadAction(psiClass.project) {
            psiClass.qualifiedName
        }
    }

    fun value(): String? = m_value.computeIfAbsentAndGet {
        IdeaEnvironment.computeInReadAction(psiClass.project) {
            psiAnnotation.findAttributeValue(VALUE_ATTRIBUTE_NAME)?.stringValue()
        }
    }

    fun hasValue(): Boolean = Strings.hasText(value())

    protected fun PsiAnnotationMemberValue.stringValue(): String? {
        var computedValue: Any? = null

        if (this is PsiLiteral) {
            computedValue = value
        } else if (this is PsiReference) {
            val target = resolve()
            if (target is PsiVariable) {
                computedValue = target.computeConstantValue()
            }
        }

        return if (computedValue is String) computedValue else null
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (javaClass != other?.javaClass) {
            return false
        }

        other as ClassIdAnnotation
        return psiAnnotation == other.psiAnnotation
    }

    override fun hashCode(): Int {
        return psiAnnotation.hashCode()
    }

    override fun toString() = "${ClassIdAnnotation::class.java.simpleName} of ${psiClass.name}"
}
