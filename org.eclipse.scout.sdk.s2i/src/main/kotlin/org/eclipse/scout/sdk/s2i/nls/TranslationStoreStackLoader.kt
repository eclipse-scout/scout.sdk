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
package org.eclipse.scout.sdk.s2i.nls

import com.intellij.openapi.module.Module
import com.intellij.openapi.progress.*
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import org.eclipse.scout.sdk.core.log.SdkLog
import org.eclipse.scout.sdk.core.s.nls.ITranslationStore
import org.eclipse.scout.sdk.core.s.nls.NlsFile
import org.eclipse.scout.sdk.core.s.nls.TranslationStoreStack
import org.eclipse.scout.sdk.core.s.nls.TranslationStores
import org.eclipse.scout.sdk.core.s.nls.TranslationStores.DependencyScope
import org.eclipse.scout.sdk.core.util.Ensure.newFail
import org.eclipse.scout.sdk.s2i.EclipseScoutBundle.message
import org.eclipse.scout.sdk.s2i.EclipseScoutBundle.translationStoreStackCache
import org.eclipse.scout.sdk.s2i.containingModule
import org.eclipse.scout.sdk.s2i.environment.IdeaEnvironment.Factory.callInIdeaEnvironmentSync
import org.eclipse.scout.sdk.s2i.environment.IdeaEnvironment.Factory.computeInLongReadAction
import org.eclipse.scout.sdk.s2i.moduleDirPath
import org.eclipse.scout.sdk.s2i.toScoutProgress
import org.eclipse.scout.sdk.s2i.util.getNioPath
import java.nio.file.Path

object TranslationStoreStackLoader {

    data class TranslationStoreStackLoaderResult(val stack: TranslationStoreStack, val primaryStore: ITranslationStore?, val nlsFile: VirtualFile?, val module: Module)

    class ModalLoader(val module: Module, val nlsFile: VirtualFile?, val scope: DependencyScope?, title: String?) : Task.Modal(module.project, title ?: message("loading.translations"), true) {

        private var m_errorHandler: ((Throwable) -> Unit)? = null
        private var m_stackCreatedHandler: ((TranslationStoreStackLoaderResult?) -> Unit)? = null

        fun withErrorHandler(handler: ((Throwable) -> Unit)?): ModalLoader {
            m_errorHandler = handler
            return this
        }

        fun withStackCreatedHandler(handler: ((TranslationStoreStackLoaderResult?) -> Unit)?): ModalLoader {
            m_stackCreatedHandler = handler
            return this
        }

        override fun run(indicator: ProgressIndicator) {
            m_stackCreatedHandler?.invoke(createStack(module, scope, false, nlsFile))
        }

        override fun onThrowable(error: Throwable) {
            val handler = m_errorHandler ?: { SdkLog.error("Error computing texts for module '{}'.", module.name, it) }
            handler.invoke(error)
        }
    }

    /**
     * Creates a [ModalLoader] to asynchronously create a [TranslationStoreStackLoaderResult].
     * Use [ModalLoader.withStackCreatedHandler] and [ModalLoader.withErrorHandler] to register callbacks to be executed when the load finishes.
     * [ModalLoader.queue] starts the computation. During the computation a modal progress indicator is displayed to the user.
     *
     * @param nlsFile The *.nls file that should be loaded
     * @param project The [Project] in which it should loaded
     * @param scope The [DependencyScope] to consider when resolving the visible [ITranslationStore] instances. The resulting [TranslationStoreStack] contains the stores as found according to the scope given. If null, all scopes are searched.
     * @param title An optional title to display in the modal progress bar
     */
    fun createModalLoader(nlsFile: VirtualFile, project: Project, scope: DependencyScope?, title: String? = null): ModalLoader {
        val module = nlsFile.containingModule(project) ?: throw newFail("Module of file '{}' not found.", nlsFile)
        return ModalLoader(module, nlsFile, scope, title)
    }

    /**
     * Loads the [TranslationStoreStack] for the given [Module].
     *
     * The load is executed synchronously and may take some time. Therefore it is recommended to use [createModalLoader] instead which executes the load asynchronously showing a progress bar.
     * @param module The module for which the stack should be created.
     * @param scope The [DependencyScope] to consider when resolving the visible [ITranslationStore] instances. The resulting [TranslationStoreStack] contains the stores as found according to the scope given. If null, all scopes are searched.
     * @param useCache Specifies if the application wide short living TTL cache should be asked before computing a new stack. Default is false.
     * @return A [TranslationStoreStack] or null if not stack can be found for the given [Module] (e.g. if it is no Scout module).
     */
    fun createStack(module: Module, scope: DependencyScope?, useCache: Boolean = false) = createStack(module, scope, useCache, null)?.stack

    /**
     * Loads the [TranslationStoreStackLoaderResult] for the given [Module].
     *
     * The load is executed synchronously and may take some time. Therefore it is recommended to use [createModalLoader] instead which executes the load asynchronously showing a progress bar.
     * @param module The module for which the stack should be created.
     * @param scope The [DependencyScope] to consider when resolving the visible [ITranslationStore] instances. The resulting [TranslationStoreStack] contains the stores as found according to the scope given. If null, all scopes are searched.
     * @param useCache Specifies if the application wide short living TTL cache should be asked before computing a new stack. Default is false.
     * @param nlsFile An optional [VirtualFile] that points to a *.nls file. This file will then be used to calculate the primary store of the stack.

     * @return A [TranslationStoreStackLoaderResult] which holds the [TranslationStoreStack] (amongst others). The result may be null if no stack can be found for the given [Module] (e.g. if it is no Scout module).
     */
    fun createStack(module: Module, scope: DependencyScope?, useCache: Boolean = false, nlsFile: VirtualFile? = null): TranslationStoreStackLoaderResult? {
        val project = module.project
        val modulePath = module.moduleDirPath()
        val stack = createStack(project, modulePath, scope, useCache)
        if (!useCache) {
            // it is a newly created stack (or null). remember it in cache for future cached requests
            translationStoreStackCache(project).putStack(modulePath, scope, stack)
        }
        val createdStack = stack ?: return null
        val primaryStore = findPrimaryStore(nlsFile, createdStack)
        return TranslationStoreStackLoaderResult(createdStack, primaryStore, nlsFile, module)
    }

    internal fun createStack(project: Project, modulePath: Path, scope: DependencyScope?, useCache: Boolean = false): TranslationStoreStack? {
        if (useCache) {
            return translationStoreStackCache(project).getOrCreateStack(modulePath, scope)
        }

        val start = System.currentTimeMillis()
        return try {
            val indicator = ProgressManager.getInstance().progressIndicator ?: EmptyProgressIndicator()
            callInIdeaEnvironmentSync(project, indicator.toScoutProgress()) { e, p ->
                computeInLongReadAction(project, p.indicator) {
                    TranslationStores.createStack(modulePath, e, p, scope).orElse(null)
                }
            }
        } catch (e: ProcessCanceledException) {
            throw e
        } catch (e: RuntimeException) {
            SdkLog.error("Error computing texts for module '{}'.", modulePath, e)
            null
        } finally {
            SdkLog.debug("Translation stack creation took {}ms", System.currentTimeMillis() - start)
        }
    }

    private fun findPrimaryStore(nlsFile: VirtualFile?, stack: TranslationStoreStack): ITranslationStore {
        val parsedNlsFile = nlsFile?.getNioPath()?.let { NlsFile(it) }
        return parsedNlsFile?.findMatchingStoreIn(stack)?.orElse(null)
                ?: firstStoreIn(stack, parsedNlsFile)
    }

    private fun firstStoreIn(stack: TranslationStoreStack, nlsFile: NlsFile?): ITranslationStore {
        val fallback = stack.allStores()
                .findFirst()
                .orElseThrow { newFail("No translation stores found for nls file '{}'.", nlsFile?.path()) }
        nlsFile?.nlsClassFqn()?.ifPresent {
            SdkLog.warning("Translation store '{}' specified in file '{}' could not be found. Fallback to '{}'.", it, nlsFile.path(), fallback.service().type().name())
        }
        return fallback
    }
}