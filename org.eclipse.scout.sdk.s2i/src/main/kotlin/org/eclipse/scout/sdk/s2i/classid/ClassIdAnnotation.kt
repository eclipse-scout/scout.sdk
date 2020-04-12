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
import org.eclipse.scout.sdk.core.util.Strings
import org.eclipse.scout.sdk.s2i.environment.IdeaEnvironment

open class ClassIdAnnotation private constructor(val psiClass: PsiClass, val psiAnnotation: PsiAnnotation) {

    private val m_value = FinalValue<String>()

    companion object {

        const val VALUE_ATTRIBUTE_NAME = "value"

        fun of(annotation: PsiAnnotation?): ClassIdAnnotation? {
            if (annotation == null) {
                return null
            }
            val owner = IdeaEnvironment.computeInReadAction(annotation.project) {
                PsiTreeUtil.getParentOfType(annotation, PsiClass::class.java)
            } ?: return null
            return ClassIdAnnotation(owner, annotation)
        }

        fun of(owner: PsiClass?): ClassIdAnnotation? {
            if (owner == null) {
                return null
            }
            val classIdAnnotation = IdeaEnvironment.computeInReadAction(owner.project) {
                owner.getAnnotation(IScoutRuntimeTypes.ClassId)
            } ?: return null
            return ClassIdAnnotation(owner, classIdAnnotation)
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
