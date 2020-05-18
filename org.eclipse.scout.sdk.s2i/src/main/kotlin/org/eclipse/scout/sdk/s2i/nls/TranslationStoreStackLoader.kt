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
import org.eclipse.scout.sdk.s2i.EclipseScoutBundle.Companion.message
import org.eclipse.scout.sdk.s2i.containingModule
import org.eclipse.scout.sdk.s2i.environment.IdeaEnvironment.Factory.callInIdeaEnvironmentSync
import org.eclipse.scout.sdk.s2i.environment.IdeaEnvironment.Factory.computeInLongReadAction
import org.eclipse.scout.sdk.s2i.moduleDirPath
import org.eclipse.scout.sdk.s2i.toNioPath
import org.eclipse.scout.sdk.s2i.toScoutProgress

class TranslationStoreStackLoader private constructor() {

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

    companion object {

        fun createModalLoader(module: Module, nlsFile: VirtualFile? = null, title: String? = null) = ModalLoader(module, nlsFile, title)

        fun createModalLoader(nlsFile: VirtualFile, project: Project, title: String? = null): ModalLoader {
            val module = nlsFile.containingModule(project) ?: throw newFail("Module of file '{}' not found.", nlsFile)
            return createModalLoader(module, nlsFile, title)
        }

        fun createStack(module: Module) = createStack(module, null)?.stack

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
            val nlsFilePath = nlsFile.toNioPath()
            return NlsFile(nlsFilePath)
                    .findMatchingStoreIn(stack)
                    .orElseGet {
                        stack.allStores()
                                .findFirst()
                                .orElseThrow { newFail("No translation stores found for nls file '{}'.", nlsFilePath) }
                    }
        }
    }
}