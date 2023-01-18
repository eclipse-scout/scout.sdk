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

import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool
import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiTypeParameter
import org.eclipse.scout.sdk.core.log.SdkLog
import org.eclipse.scout.sdk.core.s.java.apidef.IScoutApi
import org.eclipse.scout.sdk.core.s.java.apidef.ScoutApi
import org.eclipse.scout.sdk.core.util.Strings
import org.eclipse.scout.sdk.s2i.EclipseScoutBundle
import org.eclipse.scout.sdk.s2i.isInstanceOf
import org.eclipse.scout.sdk.s2i.resolveSourceRoot
import kotlin.streams.asSequence


open class MissingClassIdInspection : AbstractBaseJavaLocalInspectionTool() {

    private val m_addMissingClassIdQuickFix = AddMissingClassIdQuickFix()
    private val m_template = EclipseScoutBundle.message("missing.classid.annotation")

    private val m_scoutApiByiTypeWithClassId = ScoutApi.allKnown().asSequence()
            .map { Pair<String, String>(it.ITypeWithClassId().fqn(), it.ClassId().fqn()) to it }
            .toMap()

    override fun checkClass(aClass: PsiClass, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor>? {
        if (aClass.resolveSourceRoot() == null) {
            return ProblemDescriptor.EMPTY_ARRAY
        }
        val matchingApis = getClassIdApiCandidatesFor(aClass) ?: return ProblemDescriptor.EMPTY_ARRAY
        if (matchingApis.isEmpty()) {
            // not instanceof ITypeWithClassId
            return ProblemDescriptor.EMPTY_ARRAY
        }

        try {
            val hasClassIdValue = matchingApis.mapNotNull { ClassIdAnnotation.of(aClass, aClass.project, it) }.any { it.hasValue() }
            if (hasClassIdValue) {
                return ProblemDescriptor.EMPTY_ARRAY
            }

            val nameElement = aClass.nameIdentifier ?: return ProblemDescriptor.EMPTY_ARRAY
            val problem = manager.createProblemDescriptor(nameElement, m_template, isOnTheFly, arrayOf(m_addMissingClassIdQuickFix), ProblemHighlightType.ERROR)
            return arrayOf(problem)
        } catch (e: Exception) {
            SdkLog.error("Failed to check for missing @ClassId in {}.", aClass, e)
            return ProblemDescriptor.EMPTY_ARRAY
        }
    }

    protected fun getClassIdApiCandidatesFor(aClass: PsiClass): Set<IScoutApi>? {
        if (!aClass.isValid || !aClass.isPhysical || aClass.isAnnotationType || aClass.isEnum || aClass.isInterface
                || Strings.isBlank(aClass.name) || aClass is PsiTypeParameter) {
            return null
        }
        return m_scoutApiByiTypeWithClassId
                .filter { aClass.isInstanceOf(it.key.first) }
                .map { it.value }
                .toSet()
    }
}
