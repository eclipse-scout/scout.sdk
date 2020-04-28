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

import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool
import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiTypeParameter
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes
import org.eclipse.scout.sdk.core.util.Strings
import org.eclipse.scout.sdk.s2i.EclipseScoutBundle
import org.eclipse.scout.sdk.s2i.isInstanceOf
import org.eclipse.scout.sdk.s2i.resolveSourceRoot


open class MissingClassIdInspection : AbstractBaseJavaLocalInspectionTool() {

    private val m_addMissingClassIdQuickFix = AddMissingClassIdQuickFix()
    private val m_template = EclipseScoutBundle.message("missing.classid.annotation")

    override fun checkClass(aClass: PsiClass, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor>? {

        if (!supportsClassId(aClass) || !isInSourceRoot(aClass)) {
            return ProblemDescriptor.EMPTY_ARRAY
        }

        val hasClassIdValue = ClassIdAnnotation.of(aClass)?.hasValue() ?: false
        if (hasClassIdValue) {
            return ProblemDescriptor.EMPTY_ARRAY
        }

        val nameElement = aClass.nameIdentifier ?: return ProblemDescriptor.EMPTY_ARRAY
        val problem = manager.createProblemDescriptor(nameElement, m_template, isOnTheFly, arrayOf(m_addMissingClassIdQuickFix), ProblemHighlightType.ERROR)
        return arrayOf(problem)
    }


    protected fun isInSourceRoot(aClass: PsiClass): Boolean =
            aClass.resolveSourceRoot() != null

    protected fun supportsClassId(candidate: PsiClass): Boolean {
        if (!candidate.isValid || !candidate.isPhysical || candidate.isAnnotationType || candidate.isEnum || candidate.isInterface
                || Strings.isBlank(candidate.name) || candidate is PsiTypeParameter) {
            return false
        }
        return candidate.isInstanceOf(IScoutRuntimeTypes.ITypeWithClassId)
    }
}
