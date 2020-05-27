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

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.util.PsiTreeUtil
import org.eclipse.scout.sdk.core.log.SdkLog
import org.eclipse.scout.sdk.s2i.EclipseScoutBundle
import org.eclipse.scout.sdk.s2i.environment.IdeaEnvironment.Factory.computeInReadAction

open class DuplicateClassIdInspection : LocalInspectionTool() {

    override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor> {
        return try {
            val javaFile = if (file is PsiJavaFile) file else null ?: return ProblemDescriptor.EMPTY_ARRAY
            val classIdCache = EclipseScoutBundle.classIdCache(javaFile.project)
            if (!classIdCache.isCacheReady()) {
                classIdCache.setup() // nop if already setup or currently calculating
            }
            classIdCache.duplicates(javaFile.virtualFile.path)
                    .mapNotNull { createProblemFor(it.value, javaFile, manager, isOnTheFly) }
                    .flatten()
                    .toTypedArray()
        } catch (e: ProcessCanceledException) {
            SdkLog.debug("Duplicate @ClassId inspection canceled.", e)
            ProblemDescriptor.EMPTY_ARRAY
        }
    }

    protected fun createProblemFor(duplicates: Collection<String>, file: PsiJavaFile, manager: InspectionManager, isOnTheFly: Boolean) =
            resolvePsi(duplicates, file)
                    .mapNotNull { createProblemFor(duplicates, it, manager, isOnTheFly) }

    protected fun createProblemFor(duplicates: Collection<String>, clazz: PsiClass, manager: InspectionManager, isOnTheFly: Boolean): ProblemDescriptor? {
        val myName = computeInReadAction(clazz.project) { clazz.qualifiedName }
        val othersWithSameValue = duplicates
                .filter { d -> d != myName }
                .joinToString()
        val annotation = ClassIdAnnotation.of(clazz) ?: return null
        val message = EclipseScoutBundle.message("duplicate.classid.value", othersWithSameValue)
        val quickFix = ChangeClassIdValueQuickFix(annotation)
        return manager.createProblemDescriptor(annotation.psiAnnotation, message, isOnTheFly, arrayOf(quickFix), ProblemHighlightType.ERROR)
    }

    protected fun resolvePsi(duplicates: Collection<String>, file: PsiJavaFile): List<PsiClass> = computeInReadAction(file.project) {
        return@computeInReadAction PsiTreeUtil.findChildrenOfType(file, PsiClass::class.java)
                .associateBy { it.qualifiedName }
                .filter { it.key != null }
                .filter { duplicates.contains(it.key) }
                .map { it.value }
    }
}