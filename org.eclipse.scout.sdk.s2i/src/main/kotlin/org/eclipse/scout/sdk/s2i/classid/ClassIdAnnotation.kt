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
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes
import org.eclipse.scout.sdk.core.util.FinalValue
import org.eclipse.scout.sdk.core.util.Strings
import org.eclipse.scout.sdk.s2i.AbstractClassAnnotation
import org.eclipse.scout.sdk.s2i.environment.IdeaEnvironment.Factory.computeInReadAction

open class ClassIdAnnotation private constructor(psiClass: PsiClass, psiAnnotation: PsiAnnotation) : AbstractClassAnnotation(psiClass, psiAnnotation) {

    private val m_value = FinalValue<String>()

    companion object {

        const val VALUE_ATTRIBUTE_NAME = org.eclipse.scout.sdk.core.s.annotation.ClassIdAnnotation.VALUE_ELEMENT_NAME

        fun of(annotation: PsiAnnotation?) = of(annotation, null)

        fun of(owner: PsiClass?) = of(null, owner)

        fun of(annotation: PsiAnnotation?, owner: PsiClass?) = classAnnotation(IScoutRuntimeTypes.ClassId, annotation, owner)
                ?.let { ClassIdAnnotation(it.first, it.second) }
    }

    fun value(): String? = m_value.computeIfAbsentAndGet {
        computeInReadAction(psiClass.project) {
            psiAnnotation.findAttributeValue(VALUE_ATTRIBUTE_NAME)?.valueAs(String::class.java)
        }
    }

    fun hasValue(): Boolean = Strings.hasText(value())
}
