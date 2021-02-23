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
package org.eclipse.scout.sdk.s2i.nls.inspection

import com.intellij.analysis.AnalysisScope
import com.intellij.codeInspection.*
import com.intellij.codeInspection.ex.GlobalInspectionContextUtil
import com.intellij.codeInspection.reference.RefEntity
import com.intellij.codeInspection.reference.RefModule
import com.intellij.lang.properties.psi.PropertiesFile
import com.intellij.openapi.module.Module
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.rootManager
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VfsUtilCore.iterateChildrenRecursively
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import com.intellij.psi.search.SearchScope
import org.eclipse.scout.sdk.core.log.SdkLog
import org.eclipse.scout.sdk.core.s.ISdkConstants
import org.eclipse.scout.sdk.core.s.apidef.ScoutApi
import org.eclipse.scout.sdk.core.s.environment.IProgress
import org.eclipse.scout.sdk.core.s.nls.ITranslationStore
import org.eclipse.scout.sdk.core.s.nls.Language
import org.eclipse.scout.sdk.core.s.nls.TranslationStores
import org.eclipse.scout.sdk.core.s.nls.query.TranslationKeysQuery
import org.eclipse.scout.sdk.core.s.util.search.FileQueryInput
import org.eclipse.scout.sdk.s2i.*
import org.eclipse.scout.sdk.s2i.EclipseScoutBundle.message
import org.eclipse.scout.sdk.s2i.environment.IdeaEnvironment
import org.eclipse.scout.sdk.s2i.environment.IdeaEnvironment.Factory.callInIdeaEnvironmentSync
import org.eclipse.scout.sdk.s2i.environment.IdeaProgress
import java.nio.file.Path
import kotlin.streams.asSequence

open class UnusedTranslationInspection : GlobalInspectionTool() {

    private val m_keys = Key.create<MutableMap<String, ITranslationStore>>("scout.UnusedTranslationInspection.unusedKeys")

    override fun runInspection(scope: AnalysisScope, manager: InspectionManager, globalContext: GlobalInspectionContext, problemDescriptionsProcessor: ProblemDescriptionsProcessor) {
        try {
            val unusedKeys = getAllTranslationKeysIn(scope.toSearchScope(), manager.project)
            globalContext.putUserData(m_keys, unusedKeys)
            super.runInspection(scope, manager, globalContext, problemDescriptionsProcessor)
            if (unusedKeys.isNotEmpty()) {
                val psiManager = PsiManager.getInstance(manager.project)
                unusedKeys.entries
                        .groupBy({ it.value }, { it.key })
                        .forEach { createProblemsForUnusedTranslations(it.key, it.value, psiManager, manager, globalContext, problemDescriptionsProcessor) }
            }
        } catch (e: Exception) {
            SdkLog.error("Unused translation inspection failed.", e)
        }
    }

    override fun checkElement(refEntity: RefEntity, scope: AnalysisScope, manager: InspectionManager, globalContext: GlobalInspectionContext): Array<CommonProblemDescriptor>? {
        val module = (refEntity as? RefModule)?.module ?: return null
        val unusedKeys = globalContext.getUserData(m_keys) ?: return null
        if (unusedKeys.isEmpty()) return null
        val modulePath = module.moduleDirPath()

        ProgressManager.getInstance().progressIndicator?.text = message("searching.in.x", module.name)
        interestingFoldersOf(module).forEach { root ->
            iterateChildrenRecursively(root, this::acceptFile) { child ->
                child.takeUnless { it.isDirectory }
                        ?.let { removeKeysFoundIn(it, modulePath, unusedKeys) }
                        ?: true // continue visiting on directories
            }
        }
        return null
    }

    private fun interestingFoldersOf(module: Module): List<VirtualFile> = module.rootManager
            .contentRoots
            .flatMap { listOf(it.findChild("src"), it.findChild("test")) }
            .filterNotNull()

    private fun acceptFile(fileOrDirectory: VirtualFile) = fileOrDirectory.isValid && fileOrDirectory.isInLocalFileSystem

    private fun removeKeysFoundIn(file: VirtualFile, modulePath: Path, unusedKeys: MutableMap<String, ITranslationStore>): Boolean {
        val filePath = file.resolveLocalPath() ?: return true
        val queryInput = FileQueryInput(filePath, modulePath) { file.contentAsText() }
        val query = TranslationKeysQuery()
        query.searchIn(queryInput)
        query.keysFound().forEach {
            unusedKeys.remove(it)
        }
        return unusedKeys.isNotEmpty() // stop if list is empty
    }

    protected fun createProblemsForUnusedTranslations(store: ITranslationStore, unusedKeys: List<String>, psiManager: PsiManager, inspectionManager: InspectionManager,
                                                      globalContext: GlobalInspectionContext, problemDescriptionsProcessor: ProblemDescriptionsProcessor) =
            store.resolvePropertiesFile(Language.LANGUAGE_DEFAULT, psiManager)
                    ?.let { createProblemsForUnusedTranslations(store, it, unusedKeys, inspectionManager, globalContext, problemDescriptionsProcessor) }

    protected fun createProblemsForUnusedTranslations(store: ITranslationStore, file: PropertiesFile, unusedKeys: List<String>, inspectionManager: InspectionManager,
                                                      globalContext: GlobalInspectionContext, problemDescriptionsProcessor: ProblemDescriptionsProcessor) = unusedKeys.forEach {
        val psiFile = file.containingFile // don't pass the exact property but only the file and the property range instead. the property might become invalid when applying multiple fixes on the same file!
        val propertyElementRange = file.findPropertyByKey(it)?.psiElement?.textRange
        val quickFix = RemoveUnusedTranslationQuickFix(it, store)
        val problem = inspectionManager.createProblemDescriptor(psiFile, propertyElementRange, message("translation.x.not.used", it), ProblemHighlightType.WARNING, false, quickFix)
        problemDescriptionsProcessor.addProblemElement(GlobalInspectionContextUtil.retrieveRefElement(psiFile, globalContext), problem)
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
            .mapNotNull { TranslationStores.create(it, progress).orElse(null) }
            .filter { it.isEditable }
            .flatMap { store -> store.keys().asSequence().map { it to store } }
            .toMap(HashMap<String, ITranslationStore>())

    override fun isGraphNeeded() = false
}