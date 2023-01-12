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
import com.intellij.psi.LanguageAnnotationSupport
import com.intellij.psi.PsiAnnotation
import org.eclipse.scout.sdk.core.s.classid.ClassIds
import org.eclipse.scout.sdk.s2i.EclipseScoutBundle
import org.eclipse.scout.sdk.s2i.environment.TransactionManager.Companion.runInNewTransaction
import org.eclipse.scout.sdk.s2i.util.ApiHelper

open class ChangeClassIdValueQuickFix : LocalQuickFix {

    val quickFixName = EclipseScoutBundle.message("update.with.new.classid")

    override fun getFamilyName(): String = quickFixName

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) = runInNewTransaction(project, quickFixName) {
        val psiElement = descriptor.psiElement as PsiAnnotation
        val scoutApi = ApiHelper.scoutApiFor(psiElement) ?: return@runInNewTransaction
        val annotation = ClassIdAnnotation.of(psiElement, project, scoutApi) ?: return@runInNewTransaction

        val newClassIdValue = ClassIds.next(annotation.psiClass.qualifiedName)
        val javaAnnotationSupport = LanguageAnnotationSupport.INSTANCE.forLanguage(annotation.psiClass.language)
        val value = javaAnnotationSupport.createLiteralValue(newClassIdValue, annotation.psiAnnotation)
        annotation.psiAnnotation.setDeclaredAttributeValue(annotation.scoutApi.ClassId().valueElementName(), value)
    }
}
