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

import com.intellij.analysis.AnalysisScope
import com.intellij.codeInspection.*
import com.intellij.codeInspection.ex.GlobalInspectionContextUtil
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import com.intellij.psi.search.SearchScope
import org.eclipse.scout.sdk.core.log.SdkLog
import org.eclipse.scout.sdk.core.s.ISdkConstants
import org.eclipse.scout.sdk.core.s.environment.IProgress
import org.eclipse.scout.sdk.core.s.java.apidef.ScoutApi
import org.eclipse.scout.sdk.core.s.nls.ITranslationStore
import org.eclipse.scout.sdk.core.s.nls.Language
import org.eclipse.scout.sdk.core.s.nls.Translations
import org.eclipse.scout.sdk.core.s.nls.query.TranslationKeysQuery
import org.eclipse.scout.sdk.core.s.util.search.FileQueryInput
import org.eclipse.scout.sdk.s2i.*
import org.eclipse.scout.sdk.s2i.EclipseScoutBundle.message
import org.eclipse.scout.sdk.s2i.environment.IdeaEnvironment
import org.eclipse.scout.sdk.s2i.environment.IdeaEnvironment.Factory.callInIdeaEnvironmentSync
import org.eclipse.scout.sdk.s2i.environment.IdeaProgress
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.streams.asSequence

open class UnusedTranslationInspection : GlobalInspectionTool() {

    override fun runInspection(scope: AnalysisScope, manager: InspectionManager, globalContext: GlobalInspectionContext, problemDescriptionsProcessor: ProblemDescriptionsProcessor) {
        try {
            val project = manager.project
            val unusedKeys = getAllTranslationKeysIn(scope.toSearchScope(), project)
            val modulePath = Paths.get("") // the module path is not used
            AnalysisScope(project).accept { removeKeysFoundIn(it, modulePath, unusedKeys) } // always search the whole project for keys of selected scope
            if (unusedKeys.isNotEmpty()) {
                val psiManager = PsiManager.getInstance(project)
                unusedKeys.entries.groupBy({ it.value }, { it.key }).forEach { createProblems(it.key, it.value, psiManager, manager, globalContext, problemDescriptionsProcessor) }
            }
        } catch (e: Exception) {
            SdkLog.error("Unused translation inspection failed.", e)
        }
    }

    private fun removeKeysFoundIn(file: VirtualFile, modulePaths: Path, unusedKeys: MutableMap<String, ITranslationStore>): Boolean {
        if (file.isDirectory) return true
        if (!file.isValid || !file.isInLocalFileSystem) return true
        if (!TranslationKeysQuery.acceptFileExtension(file.extension)) return true
        val filePath = file.resolveLocalPath() ?: return true

        ProgressManager.getInstance().progressIndicator?.text = message("searching.in.x", filePath)
        val queryInput = FileQueryInput(filePath, modulePaths) { file.contentAsText() }
        val query = TranslationKeysQuery()
        query.searchIn(queryInput)
        query.keysFound().forEach {
            unusedKeys.remove(it)
        }
        return unusedKeys.isNotEmpty() // stop if list is empty
    }

    protected fun createProblems(store: ITranslationStore, unusedKeys: List<String>, psiManager: PsiManager, inspectionManager: InspectionManager, context: GlobalInspectionContext, processor: ProblemDescriptionsProcessor) =
        unusedKeys.forEach { createProblems(store, it, psiManager, inspectionManager, context, processor) }

    protected fun createProblems(store: ITranslationStore, unusedKey: String, psiManager: PsiManager, inspectionManager: InspectionManager, context: GlobalInspectionContext, processor: ProblemDescriptionsProcessor) {
        val lang = store.get(unusedKey).get().languages().sorted().findFirst().orElse(Language.LANGUAGE_DEFAULT)
        val propertiesFile = store.resolvePropertiesFile(lang, psiManager) ?: return
        val psiFile = propertiesFile.containingFile // don't pass the exact property but only the file and the property range instead. the property might become invalid when applying multiple fixes on the same file!
        val propertyElementRange = propertiesFile.findPropertyByKey(unusedKey)?.psiElement?.textRange
        val quickFix = RemoveUnusedTranslationQuickFix(unusedKey, store)
        val problem = inspectionManager.createProblemDescriptor(psiFile, propertyElementRange, message("translation.x.not.used", unusedKey), ProblemHighlightType.WARNING, false, quickFix)
        processor.addProblemElement(GlobalInspectionContextUtil.retrieveRefElement(psiFile, context), problem)
    }

    protected fun getAllTranslationKeysIn(scope: SearchScope, project: Project): MutableMap<String, ITranslationStore> {
        return callInIdeaEnvironmentSync(project, IdeaProgress.currentOrEmpty()) { e, p ->
            getAllTranslationKeysIn(scope, project, e, p)
        }
    }

    protected fun getAllTranslationKeysIn(scope: SearchScope, project: Project, env: IdeaEnvironment, progress: IProgress) = ScoutApi.allKnown().asSequence()
        .map { it.AbstractDynamicNlsTextProviderService().fqn() }
            .distinct()
            .flatMap { project.findTypesByName(it) }
            .flatMap { it.newSubTypeHierarchy(scope, true, includeAnonymous = false, includeRoot = false).asSequence() }
            .filter { it.name?.endsWith(ISdkConstants.SUFFIX_PERMISSION_TEXT_PROVIDER_SERVICE) == false }
            .mapNotNull { it.toScoutType(env, false) }
            .mapNotNull { Translations.createStore(it, progress).orElse(null) }
            .filter { it.isEditable }
            .flatMap { store -> store.keys().asSequence().map { it to store } }
            .toMap(HashMap<String, ITranslationStore>())

    override fun isGraphNeeded() = false
}