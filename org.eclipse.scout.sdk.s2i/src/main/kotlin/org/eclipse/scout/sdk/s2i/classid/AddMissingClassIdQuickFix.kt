package org.eclipse.scout.sdk.s2i.classid

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.LanguageAnnotationSupport
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.codeStyle.JavaCodeStyleManager
import com.intellij.psi.util.PsiTreeUtil
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes
import org.eclipse.scout.sdk.core.s.classid.ClassIds
import org.eclipse.scout.sdk.core.util.Ensure
import org.eclipse.scout.sdk.core.util.JavaTypes


open class AddMissingClassIdQuickFix : LocalQuickFix {

    val quickFixName = "Add missing @ClassId Annotation"

    override fun getFamilyName(): String = quickFixName

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val psiClass = PsiTreeUtil.getParentOfType(descriptor.psiElement, PsiClass::class.java)
                ?: throw Ensure.newFail("No class found to add @ClassId annotation. Element: '{}'.", descriptor.psiElement)

        val psiElementFactory = JavaPsiFacade.getElementFactory(project)
        val javaAnnotationSupport = LanguageAnnotationSupport.INSTANCE.forLanguage(psiClass.language)
        val javaCodeStyleManager = JavaCodeStyleManager.getInstance(project)

        val classIdAnnotation = psiElementFactory.createAnnotationFromText("@" + IScoutRuntimeTypes.ClassId, psiClass)
        val classIdValue = ClassIds.next(psiClass.qualifiedName)
        val value = javaAnnotationSupport.createLiteralValue(classIdValue, psiClass)
        classIdAnnotation.setDeclaredAttributeValue("value", value)
        javaCodeStyleManager.shortenClassReferences(classIdAnnotation)

        psiClass.modifierList?.addAfter(classIdAnnotation, findAnchor(psiClass, classIdValue))
    }

    protected fun findAnchor(psiClass: PsiClass, classIdValue: String): PsiElement? {
        val newClassIdAnnotationLength = JavaTypes.simpleName(IScoutRuntimeTypes.ClassId).length + classIdValue.length + 5 // 5 extra for @, (), ""
        for (i in psiClass.annotations.size - 1 downTo 0) {
            val annotation = psiClass.annotations[i]
            if (annotation.textLength < newClassIdAnnotationLength) {
                return annotation
            }
        }
        return null
    }
}
