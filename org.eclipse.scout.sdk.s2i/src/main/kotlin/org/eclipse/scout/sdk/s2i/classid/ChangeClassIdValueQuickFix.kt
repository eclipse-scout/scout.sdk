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
import com.intellij.psi.LanguageAnnotationSupport
import com.intellij.psi.PsiLiteral
import org.eclipse.scout.sdk.core.s.classid.ClassIds
import org.eclipse.scout.sdk.s2i.EclipseScoutBundle
import org.eclipse.scout.sdk.s2i.environment.IdeaProgress
import org.eclipse.scout.sdk.s2i.environment.TransactionManager
import org.eclipse.scout.sdk.s2i.environment.TransactionMember
import org.eclipse.scout.sdk.s2i.toNioPath
import java.nio.file.Path

open class ChangeClassIdValueQuickFix(val annotation: ClassIdAnnotation) : LocalQuickFix {

    val quickFixName = EclipseScoutBundle.message("update.with.new.classid")

    override fun getFamilyName(): String = quickFixName

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) = TransactionManager.runInTransaction(project) {
        val newClassIdValue = ClassIds.next(annotation.psiClass.qualifiedName)
        val javaAnnotationSupport = LanguageAnnotationSupport.INSTANCE.forLanguage(annotation.psiClass.language)
        val value = javaAnnotationSupport.createLiteralValue(newClassIdValue, annotation.psiAnnotation)
        val targetFile = annotation.psiAnnotation.containingFile.virtualFile.toNioPath()

        TransactionManager.current().register(UpdateClassIdValue(annotation, value, targetFile))
    }

    companion object {
        private class UpdateClassIdValue(val annotation: ClassIdAnnotation, val newValue: PsiLiteral, val targetFile: Path) : TransactionMember {

            override fun file() = targetFile

            override fun commit(progress: IdeaProgress): Boolean {
                annotation.psiAnnotation.setDeclaredAttributeValue(ClassIdAnnotation.VALUE_ATTRIBUTE_NAME, newValue)
                return true
            }
        }
    }
}
