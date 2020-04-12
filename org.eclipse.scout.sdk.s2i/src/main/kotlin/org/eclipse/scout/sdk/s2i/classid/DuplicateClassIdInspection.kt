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
import com.intellij.lang.java.JavaLanguage
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiAnnotation
import com.intellij.psi.PsiFile
import com.intellij.psi.search.GlobalSearchScope.*
import com.intellij.structuralsearch.MatchOptions
import com.intellij.util.containers.ContainerUtil
import org.eclipse.scout.sdk.core.log.SdkLog
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes
import org.eclipse.scout.sdk.s2i.EclipseScoutBundle
import org.eclipse.scout.sdk.s2i.environment.IdeaEnvironment
import org.eclipse.scout.sdk.s2i.findAllTypesAnnotatedWith
import org.eclipse.scout.sdk.s2i.structuralSearch
import java.util.concurrent.ConcurrentHashMap

open class DuplicateClassIdInspection : LocalInspectionTool() {

    private val m_duplicateClassIdsByFile = ConcurrentHashMap<Project, Map<PsiFile, List<ClassIdAnnotation>>>()
    private val m_duplicateClassIdsByValue = ConcurrentHashMap<Project, Map<String?, List<ClassIdAnnotation>>>()

    override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor> {
        try {
            if (isOnTheFly) {
                // use optimized implementation for single file
                return getProblemsForFile(file, manager).toTypedArray()
            }

            // use optimized implementation (with caches) for large scopes
            val duplicates = getDuplicatesByFileCached(file.project)[file] ?: return ProblemDescriptor.EMPTY_ARRAY
            return duplicates
                    .mapNotNull { createProblemFor(it, manager, false) }
                    .toTypedArray()
        } catch (e: ProcessCanceledException) {
            SdkLog.debug("Duplicate @ClassId inspection canceled.", e)
            return ProblemDescriptor.EMPTY_ARRAY
        }
    }

    protected fun getProblemsForFile(file: PsiFile, manager: InspectionManager): Set<ProblemDescriptor> {
        val project = file.project
        val progress = ProgressManager.getInstance().progressIndicator
        progress.isIndeterminate = false
        val classIdAnnotationsInFile = project.findAllTypesAnnotatedWith(IScoutRuntimeTypes.ClassId, fileScope(file), progress)
                .mapNotNull { ClassIdAnnotation.of(it) }
                .filter { it.hasValue() }
                .toList()

        val problems = ContainerUtil.newConcurrentSet<ProblemDescriptor>()
        classIdAnnotationsInFile.parallelStream().forEach {
            val findings = searchForAnnotationInProject(it, progress).toList()
            if (findings.size > 1) {
                problems.add(createProblemFor(it, manager, true, findings))
            }
        }
        return problems
    }

    protected fun searchForAnnotationInProject(classIdAnnotationToFind: ClassIdAnnotation, progress: ProgressIndicator): Sequence<ClassIdAnnotation> {
        val project = classIdAnnotationToFind.psiClass.project
        val options = MatchOptions()
        options.dialect = JavaLanguage.INSTANCE
        options.isCaseSensitiveMatch = true
        options.isRecursiveSearch = true
        options.scope = projectScope(project)
        options.searchPattern = "@${IScoutRuntimeTypes.ClassId}(\"${classIdAnnotationToFind.value()}\")"
        return project.structuralSearch(options, progress)
                .map { it.match }
                .filter { it.isValid }
                .filter { it.isPhysical }
                .filter { it is PsiAnnotation }
                .map { it as PsiAnnotation }
                .mapNotNull { ClassIdAnnotation.of(it) }
                .filter { it.hasValue() }
    }

    protected fun getDuplicatesByFileCached(project: Project) = m_duplicateClassIdsByFile.computeIfAbsent(project) { p ->
        getDuplicatesByValueCached(p)
                .map { it.value }
                .flatten()
                .groupBy { it.psiClass.containingFile }
    }

    protected fun getDuplicatesByValueCached(project: Project) = m_duplicateClassIdsByValue.computeIfAbsent(project) { p ->
        project.findAllTypesAnnotatedWith(IScoutRuntimeTypes.ClassId, allScope(p))
                .filter { it.isValid }
                .mapNotNull { ClassIdAnnotation.of(it) }
                .filter { it.hasValue() }
                .groupBy { it.value() }
                .filter { it.value.size > 1 }
    }

    protected fun createProblemFor(duplicate: ClassIdAnnotation, manager: InspectionManager, isOnTheFly: Boolean): ProblemDescriptor? {
        val withSameValue = getDuplicatesByValueCached(duplicate.psiClass.project)[duplicate.value()] ?: return null
        return createProblemFor(duplicate, manager, isOnTheFly, withSameValue)
    }

    protected fun createProblemFor(duplicate: ClassIdAnnotation, manager: InspectionManager, isOnTheFly: Boolean, withSameValue: List<ClassIdAnnotation>): ProblemDescriptor {
        val othersWithSameValue = IdeaEnvironment.computeInReadAction(duplicate.psiClass.project) {
            withSameValue
                    .filter { d -> d != duplicate }
                    .map { d -> d.psiClass }
                    .map { psi -> psi.qualifiedName }
                    .joinToString()
        }
        val message = EclipseScoutBundle.message("duplicate.classid.value", othersWithSameValue)
        val quickFix = ChangeClassIdValueQuickFix(duplicate)
        return manager.createProblemDescriptor(duplicate.psiAnnotation, message, isOnTheFly, arrayOf(quickFix), ProblemHighlightType.ERROR)
    }

    /**
     * Executed on not-onTheFly executions of the inspection when the result tab is closed
     */
    override fun cleanup(project: Project) {
        m_duplicateClassIdsByValue.remove(project)
        m_duplicateClassIdsByFile.remove(project)
        super.cleanup(project)
    }
}