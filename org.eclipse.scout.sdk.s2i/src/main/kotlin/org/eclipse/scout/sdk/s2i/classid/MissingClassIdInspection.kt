package org.eclipse.scout.sdk.s2i.classid

import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool
import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.psi.PsiClass
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes
import org.eclipse.scout.sdk.core.util.Strings
import org.eclipse.scout.sdk.s2i.findTypesByName
import org.eclipse.scout.sdk.s2i.resolveSourceRoot


open class MissingClassIdInspection : AbstractBaseJavaLocalInspectionTool() {

    private val m_addMissingClassIdQuickFix = AddMissingClassIdQuickFix()
    private val m_template = "Missing @ClassId Annotation"

    override fun checkClass(aClass: PsiClass, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor>? {
        if (aClass.hasAnnotation(IScoutRuntimeTypes.ClassId)) {
            return ProblemDescriptor.EMPTY_ARRAY
        }

        if (!supportsClassId(aClass) || !isInSourceRoot(aClass)) {
            return ProblemDescriptor.EMPTY_ARRAY
        }

        val nameElement = aClass.nameIdentifier ?: return ProblemDescriptor.EMPTY_ARRAY
        val problem = manager.createProblemDescriptor(nameElement, m_template, isOnTheFly, arrayOf(m_addMissingClassIdQuickFix), ProblemHighlightType.ERROR)
        return arrayOf(problem)
    }


    protected fun isInSourceRoot(aClass: PsiClass): Boolean =
            aClass.resolveSourceRoot() != null

    protected fun supportsClassId(aClass: PsiClass): Boolean {
        if (!aClass.isValid || Strings.isBlank(aClass.name)) {
            return false
        }

        val iTypeWithClassId = aClass.project
                .findTypesByName(IScoutRuntimeTypes.ITypeWithClassId)
                .firstOrNull()
                ?: return false
        return aClass.isInheritor(iTypeWithClassId, true)
    }
}
