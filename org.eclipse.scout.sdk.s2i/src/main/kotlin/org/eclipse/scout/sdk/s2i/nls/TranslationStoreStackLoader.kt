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
import org.eclipse.scout.sdk.core.s.nls.TranslationStores.createStack
import org.eclipse.scout.sdk.core.util.Ensure.newFail
import org.eclipse.scout.sdk.s2i.EclipseScoutBundle.message
import org.eclipse.scout.sdk.s2i.containingModule
import org.eclipse.scout.sdk.s2i.environment.IdeaEnvironment.Factory.callInIdeaEnvironmentSync
import org.eclipse.scout.sdk.s2i.environment.IdeaEnvironment.Factory.computeInLongReadAction
import org.eclipse.scout.sdk.s2i.moduleDirPath
import org.eclipse.scout.sdk.s2i.toScoutProgress
import org.eclipse.scout.sdk.s2i.util.getNioPath

object TranslationStoreStackLoader {

    data class TranslationStoreStackLoaderResult(val stack: TranslationStoreStack, val primaryStore: ITranslationStore?, val nlsFile: VirtualFile?, val module: Module)

    class ModalLoader(val module: Module, val nlsFile: VirtualFile?, title: String?) : Task.Modal(module.project, title ?: message("loading.translations"), true) {

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
            m_stackCreatedHandler?.invoke(createStack(module, nlsFile, indicator))
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
     * @param module The module for which the stack should be created.
     * @param nlsFile An optional *.nls file within that module
     * @param title An optional title to display in the modal progress bar
     */
    fun createModalLoader(module: Module, nlsFile: VirtualFile? = null, title: String? = null) = ModalLoader(module, nlsFile, title)

    /**
     * Creates a [ModalLoader] to asynchronously create a [TranslationStoreStackLoaderResult].
     * Use [ModalLoader.withStackCreatedHandler] and [ModalLoader.withErrorHandler] to register callbacks to be executed when the load finishes.
     * [ModalLoader.queue] starts the computation. During the computation a modal progress indicator is displayed to the user.
     *
     * @param nlsFile The *.nls file that should be loaded
     * @param project The [Project] in which it should loaded
     * @param title An optional title to display in the modal progress bar
     */
    fun createModalLoader(nlsFile: VirtualFile, project: Project, title: String? = null): ModalLoader {
        val module = nlsFile.containingModule(project) ?: throw newFail("Module of file '{}' not found.", nlsFile)
        return createModalLoader(module, nlsFile, title)
    }

    /**
     * Loads the [TranslationStoreStack] for the given [Module].
     *
     * The load is executed synchronously and may take some time. Therefore it is recommended to use [createModalLoader] instead which executes the load asynchronously showing a progress bar.
     * @param module The module for which the stack should be created.
     * @return A [TranslationStoreStackLoaderResult] which holds the [TranslationStoreStack] (amongst others). The result may be null if not stack can be found for the given [Module] (e.g. if it is no Scout module).
     */
    fun createStack(module: Module) = createStack(module, null)?.stack

    /**
     * Loads the [TranslationStoreStack] for the given [Module].
     *
     * The load is executed synchronously and may take some time. Therefore it is recommended to use [createModalLoader] instead which executes the load asynchronously showing a progress bar.
     * @param module The module for which the stack should be created.
     * @param nlsFile An optional [VirtualFile] that points to a *.nls file. This file will then be used to calculate the primary store of the stack.
     * @return A [TranslationStoreStackLoaderResult] which holds the [TranslationStoreStack] (amongst others). The result may be null if not stack can be found for the given [Module] (e.g. if it is no Scout module).
     */
    fun createStack(module: Module, nlsFile: VirtualFile? = null): TranslationStoreStackLoaderResult? {
        val start = System.currentTimeMillis()
        return try {
            val indicator = ProgressManager.getInstance().progressIndicator ?: EmptyProgressIndicator()
            return createStack(module, nlsFile, indicator)
        } catch (e: ProcessCanceledException) {
            throw e
        } catch (e: RuntimeException) {
            SdkLog.error("Error computing texts for module '{}'.", module.name, e)
            null
        } finally {
            SdkLog.debug("Translation stack creation took {}ms", System.currentTimeMillis() - start)
        }
    }

    private fun createStack(module: Module, nlsFile: VirtualFile?, indicator: ProgressIndicator) = callInIdeaEnvironmentSync(module.project, indicator.toScoutProgress()) { e, p ->
        return@callInIdeaEnvironmentSync computeInLongReadAction(module.project, p.indicator) {
            return@computeInLongReadAction createStack(module.moduleDirPath(), e, p)
                    .map { stack -> TranslationStoreStackLoaderResult(stack, nlsFile?.let { findPrimaryStore(it, stack) }, nlsFile, module) }
                    .orElse(null)
        }
    }

    private fun findPrimaryStore(nlsFile: VirtualFile, stack: TranslationStoreStack): ITranslationStore {
        val nlsFilePath = nlsFile.getNioPath()
        val file = NlsFile(nlsFilePath)
        return file.findMatchingStoreIn(stack)
                .orElseGet { fallbackStore(file, stack) }
    }

    private fun fallbackStore(nlsFile: NlsFile, stack: TranslationStoreStack): ITranslationStore? {
        val fallback = stack.allStores()
                .findFirst()
                .orElseThrow { newFail("No translation stores found for nls file '{}'.", nlsFile.path()) }
        nlsFile.nlsClassFqn().ifPresent {
            SdkLog.warning("Translation store '{}' specified in file '{}' could not be found. Fallback to '{}'.", it, nlsFile.path(), fallback.service().type().name())
        }
        return fallback
    }
}