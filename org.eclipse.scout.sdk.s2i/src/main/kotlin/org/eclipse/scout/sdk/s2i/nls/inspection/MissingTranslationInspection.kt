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
package org.eclipse.scout.sdk.s2i.nls.inspection

import com.intellij.codeInspection.*
import com.intellij.openapi.module.Module
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import org.eclipse.scout.sdk.core.log.SdkLog
import org.eclipse.scout.sdk.core.s.environment.IProgress
import org.eclipse.scout.sdk.core.s.nls.query.MissingTranslationQuery
import org.eclipse.scout.sdk.core.s.util.search.FileQueryInput
import org.eclipse.scout.sdk.core.s.util.search.FileQueryMatch
import org.eclipse.scout.sdk.core.s.util.search.IFileQuery
import org.eclipse.scout.sdk.s2i.EclipseScoutBundle.Companion.message
import org.eclipse.scout.sdk.s2i.containingModule
import org.eclipse.scout.sdk.s2i.environment.IdeaEnvironment
import org.eclipse.scout.sdk.s2i.getNioPath
import org.eclipse.scout.sdk.s2i.moduleDirPath
import org.eclipse.scout.sdk.s2i.toScoutProgress
import java.util.concurrent.ConcurrentHashMap
import java.util.logging.Level

open class MissingTranslationInspection : LocalInspectionTool() {

    private val m_environmentByProject = ConcurrentHashMap<Project, Pair<IdeaEnvironment, MissingTranslationQuery>>()
    private val m_supportedFileTypes = MissingTranslationQuery.supportedFileTypes()

    override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor> {
        if (!m_supportedFileTypes.contains(file.virtualFile.extension)) {
            return ProblemDescriptor.EMPTY_ARRAY
        }

        val module = file.containingModule(false) ?: return ProblemDescriptor.EMPTY_ARRAY
        val progress = ProgressManager.getInstance().progressIndicator
        return if (isOnTheFly) {
            // single file: create short living environment for this file only
            val query = MissingTranslationQuery()
            IdeaEnvironment.callInIdeaEnvironmentSync(file.project, progress.toScoutProgress()) { e, p ->
                checkFile(file, module, query, manager, true, e, p)
            }
        } else {
            // batch inspection run: create environment and query in cache. Will be removed and closed in cleanup function
            val cache = m_environmentByProject.computeIfAbsent(file.project) { project ->
                Pair(IdeaEnvironment.createUnsafeFor(project) { }, MissingTranslationQuery())
            }
            checkFile(file, module, cache.second, manager, false, cache.first, progress.toScoutProgress())
        }
    }

    fun checkFile(file: PsiFile, module: Module, query: IFileQuery, manager: InspectionManager, isOnTheFly: Boolean, environment: IdeaEnvironment, progress: IProgress): Array<ProblemDescriptor> {
        val start = System.currentTimeMillis()
        val path = file.virtualFile.getNioPath()
        val queryInput = FileQueryInput(path, module.moduleDirPath()) { file.textToCharArray() }

        query.searchIn(queryInput, environment, progress)

        val result = query.result(path)
                .filter { it.severity() >= Level.WARNING.intValue() } // only report important findings
                .mapNotNull { toProblemDescription(it, file, manager, isOnTheFly) }
                .toTypedArray()
        SdkLog.debug("Missing translation inspection took {}ms", System.currentTimeMillis() - start)
        return result
    }

    protected fun toProblemDescription(range: FileQueryMatch, file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): ProblemDescriptor? {
        return IdeaEnvironment.computeInReadAction(file.project) {
            val element = file.findElementAt(range.start()) ?: return@computeInReadAction null
            val type = julLevelToProblemHighlightType(range.severity())
            val msg = when (range.severity()) {
                Level.INFO.intValue() -> message("possibly.missing.translation")
                else -> message("missing.translation.for.key.x", range.text())
            }
            val fixes = if (isOnTheFly) arrayOf(AddMissingTranslationQuickFix(range.text())) else LocalQuickFix.EMPTY_ARRAY
            return@computeInReadAction manager.createProblemDescriptor(element, msg, isOnTheFly, fixes, type)
        }
    }

    protected fun julLevelToProblemHighlightType(severity: Int): ProblemHighlightType = when (severity) {
        Level.INFO.intValue() -> ProblemHighlightType.WEAK_WARNING
        Level.SEVERE.intValue() -> ProblemHighlightType.ERROR
        else -> ProblemHighlightType.WARNING
    }

    override fun cleanup(project: Project) {
        m_environmentByProject.remove(project)?.first?.close()
        super.cleanup(project)
    }
}