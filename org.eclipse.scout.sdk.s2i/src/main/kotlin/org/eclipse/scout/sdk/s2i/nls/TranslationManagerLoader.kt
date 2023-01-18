/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2i.nls

import com.intellij.openapi.module.Module
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.IndexNotReadyException
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import org.eclipse.scout.sdk.core.log.SdkLog
import org.eclipse.scout.sdk.core.s.nls.ITranslationStore
import org.eclipse.scout.sdk.core.s.nls.NlsFile
import org.eclipse.scout.sdk.core.s.nls.Translations
import org.eclipse.scout.sdk.core.s.nls.Translations.DependencyScope
import org.eclipse.scout.sdk.core.s.nls.manager.TranslationManager
import org.eclipse.scout.sdk.core.util.Ensure.newFail
import org.eclipse.scout.sdk.s2i.EclipseScoutBundle.message
import org.eclipse.scout.sdk.s2i.EclipseScoutBundle.translationStoreManagerCache
import org.eclipse.scout.sdk.s2i.containingModule
import org.eclipse.scout.sdk.s2i.environment.IdeaEnvironment.Factory.callInIdeaEnvironmentSync
import org.eclipse.scout.sdk.s2i.environment.IdeaEnvironment.Factory.computeInReadAction
import org.eclipse.scout.sdk.s2i.environment.IdeaProgress
import org.eclipse.scout.sdk.s2i.moduleDirPath
import org.eclipse.scout.sdk.s2i.resolveLocalPath
import org.eclipse.scout.sdk.s2i.toScoutProgress
import java.nio.file.Path

object TranslationManagerLoader {

    data class TranslationManagerLoaderResult(val manager: TranslationManager, val primaryStore: ITranslationStore?, val nlsFile: VirtualFile?, val module: Module)

    class ModalLoader(val module: Module, val nlsFile: VirtualFile?, val scope: DependencyScope?, title: String?) : Task.Modal(module.project, title ?: message("loading.translations"), true) {

        private var m_errorHandler: ((Throwable) -> Unit)? = null
        private var m_managerCreatedHandler: ((TranslationManagerLoaderResult?) -> Unit)? = null

        fun withErrorHandler(handler: ((Throwable) -> Unit)?): ModalLoader {
            m_errorHandler = handler
            return this
        }

        fun withManagerCreatedHandler(handler: ((TranslationManagerLoaderResult?) -> Unit)?): ModalLoader {
            m_managerCreatedHandler = handler
            return this
        }

        override fun run(indicator: ProgressIndicator) {
            m_managerCreatedHandler?.invoke(createManager(module, scope, false, nlsFile))
        }

        override fun onThrowable(error: Throwable) {
            val handler = m_errorHandler ?: { SdkLog.error("Error computing texts for module '{}'.", module.name, it) }
            handler(error)
        }
    }

    /**
     * Creates a [ModalLoader] to asynchronously create a [TranslationManagerLoaderResult].
     * Use [ModalLoader.withManagerCreatedHandler] and [ModalLoader.withErrorHandler] to register callbacks to be executed when the load finishes.
     * [ModalLoader.queue] starts the computation. During the computation a modal progress indicator is displayed to the user.
     *
     * @param nlsFile The *.nls file that should be loaded
     * @param project The [Project] in which it should be loaded
     * @param scope The [DependencyScope] to consider when resolving the visible [ITranslationStore] instances. The resulting [TranslationManager] contains the stores as found according to the scope given. If null, all scopes are searched.
     * @param title An optional title to display in the modal progress bar
     */
    fun createModalLoader(nlsFile: VirtualFile, project: Project, scope: DependencyScope?, title: String? = null): ModalLoader {
        val module = nlsFile.containingModule(project) ?: throw newFail("Module of file '{}' not found.", nlsFile)
        return ModalLoader(module, nlsFile, scope, title)
    }

    /**
     * Loads the [TranslationManager] for the given [Module].
     *
     * The load is executed synchronously and may take some time. Therefore, it is recommended to use [createModalLoader] instead which executes the load asynchronously showing a progress bar.
     * @param module The module for which the manager should be created.
     * @param scope The [DependencyScope] to consider when resolving the visible [ITranslationStore] instances. The resulting [TranslationManager] contains the stores as found according to the scope given. If null, all scopes are searched.
     * @param useCache Specifies if the application wide short living TTL cache should be asked before computing a new manager. Default is false.
     * @return A [TranslationManager] or null if not manager can be found for the given [Module] (e.g. if it is no Scout module).
     */
    fun createManager(module: Module, scope: DependencyScope?, useCache: Boolean = false) = createManager(module, scope, useCache, null)?.manager

    /**
     * Loads the [TranslationManagerLoaderResult] for the given [Module].
     *
     * The load is executed synchronously and may take some time. Therefore, it is recommended to use [createModalLoader] instead which executes the load asynchronously showing a progress bar.
     * @param module The module for which the manager should be created.
     * @param scope The [DependencyScope] to consider when resolving the visible [ITranslationStore] instances. The resulting [TranslationManager] contains the stores as found according to the scope given. If null, all scopes are searched.
     * @param useCache Specifies if the application wide short living TTL cache should be asked before computing a new manager. Default is false.
     * @param nlsFile An optional [VirtualFile] that points to a *.nls file. This file will then be used to calculate the primary store of the manager.

     * @return A [TranslationManagerLoaderResult] which holds the [TranslationManager] (amongst others). The result may be null if no manager can be found for the given [Module] (e.g. if it is no Scout module).
     */
    fun createManager(module: Module, scope: DependencyScope?, useCache: Boolean = false, nlsFile: VirtualFile? = null): TranslationManagerLoaderResult? {
        val project = module.project
        val modulePath = module.moduleDirPath()
        val manager = createManager(project, modulePath, scope, useCache)
        if (!useCache) {
            // it is a newly created manager (or null). remember it in cache for future cached requests
            translationStoreManagerCache(project).putManager(modulePath, scope, manager)
        }
        val createdManager = manager ?: return null
        val primaryStore = findPrimaryStore(nlsFile, createdManager)
        return TranslationManagerLoaderResult(createdManager, primaryStore, nlsFile, module)
    }

    /**
     * Creates a modal loader and asynchronously reloads the [TranslationManager] given under the progress of the modal loader.
     * @param project The [Project] in which the modal loader should be shown
     * @param translationManager The [TranslationManager] to reload.
     */
    fun scheduleManagerReload(project: Project, translationManager: TranslationManager) {
        object : Task.Modal(project, message("loading.translations"), true) {
            override fun run(indicator: ProgressIndicator) = callInIdeaEnvironmentSync(project, indicator.toScoutProgress()) { e, p ->
                computeInReadAction(project, progress = p.indicator) {
                    translationManager.reload(e, p)
                }
            }
        }.queue()
    }

    internal fun createManager(project: Project, modulePath: Path, scope: DependencyScope?, useCache: Boolean = false): TranslationManager? {
        if (useCache) {
            return translationStoreManagerCache(project).getOrCreateManager(modulePath, scope)
        }

        val start = System.currentTimeMillis()
        return try {
            callInIdeaEnvironmentSync(project, IdeaProgress.currentOrEmpty()) { e, p ->
                computeInReadAction(project, progress = p.indicator) {
                    Translations.createManager(modulePath, e, p, scope).orElse(null)
                }
            }
        } catch (e: IndexNotReadyException) {
            // in case the current call is already in a read-action.
            // then the creator of the read-action must know that the index is not ready and may retry it afterwards
            throw e
        } catch (e: RuntimeException) {
            SdkLog.error("Error computing texts for module '{}'.", modulePath, e)
            null
        } finally {
            SdkLog.debug("Translation manager creation took {}ms", System.currentTimeMillis() - start)
        }
    }

    private fun findPrimaryStore(nlsFile: VirtualFile?, manager: TranslationManager): ITranslationStore {
        val parsedNlsFile = nlsFile?.resolveLocalPath()?.let { NlsFile(it) }
        return parsedNlsFile?.findMatchingStoreIn(manager)?.orElse(null)
            ?: firstStoreIn(manager, parsedNlsFile)
    }

    private fun firstStoreIn(manager: TranslationManager, nlsFile: NlsFile?): ITranslationStore {
        val fallback = manager.allStores()
            .findFirst()
            .orElseThrow { newFail("No translation stores found for nls file '{}'.", nlsFile?.path()) }
        nlsFile?.nlsClassFqn()?.ifPresent {
            SdkLog.warning("Translation store '{}' specified in file '{}' could not be found. Fallback to '{}'.", it, nlsFile.path(), fallback.service().type().name())
        }
        return fallback
    }
}