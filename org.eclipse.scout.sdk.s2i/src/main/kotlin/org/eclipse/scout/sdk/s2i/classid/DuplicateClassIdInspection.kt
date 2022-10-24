/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.s2i.classid

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.util.PsiTreeUtil
import org.eclipse.scout.sdk.core.log.SdkLog
import org.eclipse.scout.sdk.s2i.EclipseScoutBundle
import org.eclipse.scout.sdk.s2i.environment.IdeaEnvironment.Factory.computeInReadAction
import org.eclipse.scout.sdk.s2i.util.ApiHelper

open class DuplicateClassIdInspection : LocalInspectionTool() {

    override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor> {
        return try {
            val javaFile = if (file is PsiJavaFile) file else null ?: return ProblemDescriptor.EMPTY_ARRAY
            val classIdCache = EclipseScoutBundle.classIdCache(javaFile.project)
            if (!classIdCache.isCacheReady()) {
                classIdCache.scheduleSetup() // does nothing if already scheduled or already set up
                // do not wait for the cache to not delay other inspections
                // as soon as the cache is ready, this inspection will start its work
                return ProblemDescriptor.EMPTY_ARRAY
            }

            classIdCache.duplicates(javaFile.virtualFile.path)
                .mapNotNull { createProblemFor(it.value, javaFile, manager, isOnTheFly) }
                .flatten()
                .toTypedArray()
        } catch (e: Exception) {
            SdkLog.error("Duplicate @ClassId inspection failed for file '{}'.", file, e)
            ProblemDescriptor.EMPTY_ARRAY
        }
    }

    protected fun createProblemFor(duplicates: Collection<String>, file: PsiJavaFile, manager: InspectionManager, isOnTheFly: Boolean) =
        resolvePsi(duplicates, file)
            .mapNotNull { createProblemFor(duplicates, it, manager, isOnTheFly) }

    protected fun createProblemFor(duplicates: Collection<String>, clazz: PsiClass, manager: InspectionManager, isOnTheFly: Boolean): ProblemDescriptor? {
        val scoutApi = ApiHelper.scoutApiFor(clazz) ?: return null
        val project = clazz.project
        val annotation = ClassIdAnnotation.of(clazz, project, scoutApi) ?: return null
        val myName = computeInReadAction(project) { clazz.qualifiedName }
        val othersWithSameValue = duplicates
            .filter { d -> d != myName }
            .joinToString()
        val message = EclipseScoutBundle.message("duplicate.classid.value", othersWithSameValue)
        val quickFix = ChangeClassIdValueQuickFix()
        return manager.createProblemDescriptor(annotation.psiAnnotation, message, isOnTheFly, arrayOf(quickFix), ProblemHighlightType.ERROR)
    }

    protected fun resolvePsi(duplicates: Collection<String>, file: PsiJavaFile): List<PsiClass> = computeInReadAction(file.project) {
        PsiTreeUtil.findChildrenOfType(file, PsiClass::class.java)
            .associateBy { it.qualifiedName }
            .filter { it.key != null }
            .filter { duplicates.contains(it.key) }
            .map { it.value }
    }
}