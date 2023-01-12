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

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.LanguageAnnotationSupport
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.codeStyle.JavaCodeStyleManager
import com.intellij.psi.util.PsiTreeUtil
import org.eclipse.scout.sdk.core.s.classid.ClassIds
import org.eclipse.scout.sdk.core.util.Ensure
import org.eclipse.scout.sdk.core.util.JavaTypes
import org.eclipse.scout.sdk.s2i.EclipseScoutBundle
import org.eclipse.scout.sdk.s2i.requireScoutApi

open class AddMissingClassIdQuickFix : LocalQuickFix {

    val quickFixName = EclipseScoutBundle.message("add.missing.classid.annotation")

    override fun getFamilyName(): String = quickFixName

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val psiClass = PsiTreeUtil.getParentOfType(descriptor.psiElement, PsiClass::class.java)
            ?: throw Ensure.newFail("No class found to add @ClassId. Element: '{}'.", descriptor.psiElement)
        val classId = psiClass.requireScoutApi().ClassId()
        val classIdFqn = classId.fqn()
        if (psiClass.hasAnnotation(classIdFqn)) {
            return // nothing to do
        }
        val psiElementFactory = JavaPsiFacade.getElementFactory(project)
        val javaAnnotationSupport = LanguageAnnotationSupport.INSTANCE.forLanguage(psiClass.language)
        val classIdValue = ClassIds.next(psiClass.qualifiedName)
        val psiLiteral = javaAnnotationSupport.createLiteralValue(classIdValue, psiClass)
        val anchor = findAnchor(psiClass, classIdValue, classIdFqn)
        val classIdAnnotation = psiElementFactory.createAnnotationFromText("@$classIdFqn", psiClass)
        classIdAnnotation.setDeclaredAttributeValue(classId.valueElementName(), psiLiteral)

        psiClass.modifierList?.addAfter(classIdAnnotation, anchor)
        JavaCodeStyleManager.getInstance(project).shortenClassReferences(psiClass)
    }

    protected fun findAnchor(psiClass: PsiClass, classIdValue: String, classIdFqn: String): PsiElement? {
        val newClassIdAnnotationLength = JavaTypes.simpleName(classIdFqn).length + classIdValue.length + 5 // 5 extra for @, (), ""
        for (i in psiClass.annotations.size - 1 downTo 0) {
            val annotation = psiClass.annotations[i]
            if (annotation.textLength < newClassIdAnnotationLength) {
                return annotation
            }
        }
        return null
    }
}
