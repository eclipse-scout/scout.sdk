/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2i.nls.inspection

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiRecursiveElementVisitor
import org.eclipse.scout.sdk.core.log.SdkLog
import org.eclipse.scout.sdk.core.s.nls.ITranslation
import org.eclipse.scout.sdk.core.s.nls.Translations
import org.eclipse.scout.sdk.core.s.nls.manager.TranslationManager
import org.eclipse.scout.sdk.s2i.EclipseScoutBundle.message
import org.eclipse.scout.sdk.s2i.containingModule
import org.eclipse.scout.sdk.s2i.moduleDirPath
import org.eclipse.scout.sdk.s2i.nls.TranslationLanguageSpec.Companion.translationDependencyScope
import org.eclipse.scout.sdk.s2i.nls.TranslationLanguageSpec.Companion.translationSpec
import org.eclipse.scout.sdk.s2i.nls.TranslationManagerCache.Companion.createCacheKey
import org.eclipse.scout.sdk.s2i.nls.TranslationManagerLoader.createManager
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap
import java.util.stream.Collectors.toSet

open class MissingTranslationInspection : LocalInspectionTool() {

    private val m_cachedKeysByProject = ConcurrentHashMap<Project, MutableMap<Pair<Path, Translations.DependencyScope?>, Set<String>>>()

    override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor> {
        val nlsDependencyScope = file.translationDependencyScope() ?: return ProblemDescriptor.EMPTY_ARRAY
        try {
            val module = file.containingModule(false) ?: return ProblemDescriptor.EMPTY_ARRAY
            return if (isOnTheFly) {
                val translationManager = createManager(module, nlsDependencyScope, true) ?: return ProblemDescriptor.EMPTY_ARRAY
                checkFile(file, keysOfManager(translationManager), manager, true)
            } else {
                // batch inspection run: create cache. Will be removed in cleanup function
                val cacheKey = createCacheKey(module.moduleDirPath(), nlsDependencyScope)
                val projectCache = m_cachedKeysByProject.computeIfAbsent(file.project) { ConcurrentHashMap() }
                val keys = projectCache.computeIfAbsent(cacheKey) {
                    createManager(module, nlsDependencyScope)
                            ?.let { keysOfManager(it) }
                            ?: emptySet() // do not use null because the ConcurrentHashMap does not store null values
                }
                if (keys.isEmpty()) {
                    // there are no translations at all. it is no scout module
                    return ProblemDescriptor.EMPTY_ARRAY
                }
                checkFile(file, keys, manager, false)
            }
        } catch (e: Exception) {
            SdkLog.error("Failed to check for missing translations in {}.", file, e)
            return ProblemDescriptor.EMPTY_ARRAY
        }
    }

    private fun keysOfManager(manager: TranslationManager): Set<String> = manager.allTranslations().map(ITranslation::key).collect(toSet())

    private fun checkFile(file: PsiFile, visibleKeys: Set<String>, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor> {
        val problems = ArrayList<ProblemDescriptor>()
        file.accept(object : PsiRecursiveElementVisitor() {
            override fun visitElement(element: PsiElement) {
                super.visitElement(element)
                val translationKey = element.translationSpec()?.resolveTranslationKey() ?: return
                if (!visibleKeys.contains(translationKey)) {
                    problems.add(toProblemDescription(element, translationKey, manager, isOnTheFly))
                }
            }
        })
        if (problems.isEmpty()) {
            return ProblemDescriptor.EMPTY_ARRAY
        }
        return problems.toTypedArray()
    }

    protected fun toProblemDescription(element: PsiElement, missingKey: String, manager: InspectionManager, isOnTheFly: Boolean): ProblemDescriptor {
        val quickFix = if (isOnTheFly) AddMissingTranslationQuickFix(missingKey) else null
        return manager.createProblemDescriptor(element, message("missing.translation.for.key.x", missingKey), quickFix, ProblemHighlightType.WARNING, isOnTheFly)
    }

    override fun cleanup(project: Project) {
        m_cachedKeysByProject.remove(project)?.clear()
        super.cleanup(project)
    }
}