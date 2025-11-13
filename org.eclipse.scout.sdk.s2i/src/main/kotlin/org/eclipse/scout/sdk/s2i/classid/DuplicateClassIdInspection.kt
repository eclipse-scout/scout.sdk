/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
import org.eclipse.scout.sdk.core.s.java.apidef.IScoutApi
import org.eclipse.scout.sdk.s2i.EclipseScoutBundle
import org.eclipse.scout.sdk.s2i.classid.ClassIdCache.ClassIdOccurrence
import org.eclipse.scout.sdk.s2i.environment.IdeaEnvironment.Factory.computeInReadAction
import org.eclipse.scout.sdk.s2i.util.ApiHelper

open class DuplicateClassIdInspection : LocalInspectionTool() {

    override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor> {
        return try {
            val javaFile = (file as? PsiJavaFile) ?: return ProblemDescriptor.EMPTY_ARRAY
            val classIdCache = EclipseScoutBundle.classIdCache(javaFile.project)
            if (!classIdCache.isCacheReady()) {
                classIdCache.scheduleSetup() // does nothing if already scheduled or already set up
                // do not wait for the cache to not delay other inspections
                // as soon as the cache is ready, this inspection will start its work
                return ProblemDescriptor.EMPTY_ARRAY
            }
            val currentFile = javaFile.virtualFile.path
            val duplicates = classIdCache.duplicates(currentFile)
            if (duplicates.isEmpty()) return ProblemDescriptor.EMPTY_ARRAY

            val scoutApi = ApiHelper.scoutApiFor(javaFile) ?: return ProblemDescriptor.EMPTY_ARRAY
            duplicates
                .mapNotNull { createProblemFor(it.value, javaFile, currentFile, manager, isOnTheFly, scoutApi) }
                .flatten()
                .toTypedArray()
        } catch (e: Exception) {
            SdkLog.error("Duplicate @ClassId inspection failed for file '{}'.", file, e)
            ProblemDescriptor.EMPTY_ARRAY
        }
    }

    protected fun createProblemFor(duplicatesWithSameClassId: Collection<ClassIdOccurrence>, file: PsiJavaFile, currentFile: String, manager: InspectionManager, isOnTheFly: Boolean, scoutApi: IScoutApi) =
        resolveAnnotations(duplicatesWithSameClassId, file, currentFile, scoutApi)
            .mapNotNull { createProblemFor(duplicatesWithSameClassId, it.key, currentFile, it.value, manager, isOnTheFly) }

    protected fun createProblemFor(duplicatesWithSameClassId: Collection<ClassIdOccurrence>, myFqn: String, myFilePath: String, myAnnotation: ClassIdAnnotation, manager: InspectionManager, isOnTheFly: Boolean): ProblemDescriptor? {
        val othersWithSameValue = duplicatesWithSameClassId.filter { d -> d.fqn != myFqn || d.path != myFilePath }
        if (othersWithSameValue.isEmpty()) return null

        val others = othersWithSameValue.joinToString { "${it.fqn} in ${it.path}" }
        val message = EclipseScoutBundle.message("duplicate.classid.value", others)
        val quickFix = ChangeClassIdValueQuickFix()
        return manager.createProblemDescriptor(myAnnotation.psiAnnotation, message, isOnTheFly, arrayOf(quickFix), ProblemHighlightType.ERROR)
    }

    protected fun resolveAnnotations(duplicatesWithSameClassId: Collection<ClassIdOccurrence>, file: PsiJavaFile, filePath: String, scoutApi: IScoutApi): Map<String /* fqn */, ClassIdAnnotation> = computeInReadAction(file.project) {
        val localClassFqnWithSameClassId = duplicatesWithSameClassId
            .filter { it.path == filePath }
            .map { it.fqn }
            .toSet()
        val project = file.project
        PsiTreeUtil.findChildrenOfType(file, PsiClass::class.java).asSequence()
            .mapNotNull { it.qualifiedName?.let { fqn -> fqn to it } }
            .filter { localClassFqnWithSameClassId.contains(it.first) }
            .mapNotNull { ClassIdAnnotation.of(it.second, project, scoutApi)?.let { a -> it.first to a } }
            .toMap()
    }
}