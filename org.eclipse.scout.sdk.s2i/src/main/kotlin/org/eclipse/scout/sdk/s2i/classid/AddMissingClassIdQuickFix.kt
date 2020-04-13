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

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import com.intellij.psi.*
import com.intellij.psi.codeStyle.JavaCodeStyleManager
import com.intellij.psi.util.PsiTreeUtil
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes
import org.eclipse.scout.sdk.core.s.classid.ClassIds
import org.eclipse.scout.sdk.core.util.Ensure
import org.eclipse.scout.sdk.core.util.JavaTypes
import org.eclipse.scout.sdk.s2i.EclipseScoutBundle
import org.eclipse.scout.sdk.s2i.environment.IdeaProgress
import org.eclipse.scout.sdk.s2i.environment.TransactionManager
import org.eclipse.scout.sdk.s2i.environment.TransactionMember
import org.eclipse.scout.sdk.s2i.toNioPath
import java.nio.file.Path


open class AddMissingClassIdQuickFix : LocalQuickFix {

    val quickFixName = EclipseScoutBundle.message("add.missing.classid.annotation")

    override fun getFamilyName(): String = quickFixName

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) = TransactionManager.runInNewTransaction(project) {
        val psiClass = PsiTreeUtil.getParentOfType(descriptor.psiElement, PsiClass::class.java)
                ?: throw Ensure.newFail("No class found to add @ClassId. Element: '{}'.", descriptor.psiElement)
        val psiElementFactory = JavaPsiFacade.getElementFactory(project)
        val javaAnnotationSupport = LanguageAnnotationSupport.INSTANCE.forLanguage(psiClass.language)
        val classIdValue = ClassIds.next(psiClass.qualifiedName)
        val psiLiteral = javaAnnotationSupport.createLiteralValue(classIdValue, psiClass)
        val anchor = findAnchor(psiClass, classIdValue)
        val targetFile = psiClass.containingFile.virtualFile.toNioPath()
        val classIdAnnotation = psiElementFactory.createAnnotationFromText("@" + IScoutRuntimeTypes.ClassId, psiClass)
        classIdAnnotation.setDeclaredAttributeValue(ClassIdAnnotation.VALUE_ATTRIBUTE_NAME, psiLiteral)

        TransactionManager.current().register(CreateClassIdAnnotation(psiClass, classIdAnnotation, anchor, targetFile))
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

    companion object {
        private class CreateClassIdAnnotation(val targetClass: PsiClass, val annotationToAdd: PsiAnnotation, val anchor: PsiElement?, val targetFile: Path) : TransactionMember {

            override fun file() = targetFile

            override fun commit(progress: IdeaProgress): Boolean {
                targetClass.modifierList?.addAfter(annotationToAdd, anchor)
                JavaCodeStyleManager.getInstance(targetClass.project).shortenClassReferences(targetClass)
                return true
            }
        }
    }
}
