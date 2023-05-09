/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2i.model.js

import com.intellij.lang.javascript.JavascriptLanguage
import com.intellij.lang.javascript.library.JSLibraryManager
import com.intellij.lang.javascript.library.JSLibraryManager.JSLibraryManagerChangeListener
import com.intellij.openapi.Disposable
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessModuleDir
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiTreeChangeAdapter
import com.intellij.psi.PsiTreeChangeEvent
import com.intellij.util.concurrency.AppExecutorUtil
import org.eclipse.scout.sdk.core.log.SdkLog
import org.eclipse.scout.sdk.core.s.model.js.ScoutJsModels
import org.eclipse.scout.sdk.core.typescript.model.api.NodeModulesProvider
import org.eclipse.scout.sdk.core.typescript.model.spi.NodeModuleSpi
import org.eclipse.scout.sdk.core.typescript.model.spi.NodeModulesProviderSpi
import org.eclipse.scout.sdk.core.util.DelayedBuffer
import org.eclipse.scout.sdk.s2i.environment.IdeaEnvironment.Factory.computeInReadAction
import org.eclipse.scout.sdk.s2i.model.typescript.IdeaNodeModules
import org.eclipse.scout.sdk.s2i.resolveLocalPath
import java.nio.file.Path
import java.util.*
import java.util.concurrent.TimeUnit

class JsModelManager(val project: Project) : NodeModulesProviderSpi, Disposable {

    private val m_nodeModuleInventory = IdeaNodeModules(project)
    private val m_busConnection = project.messageBus.connect()

    companion object {
        fun getOrCreateScoutJsModel(module: Module) = moduleDir(module)?.let {
            try {
                ScoutJsModels.create(it, module.project).orElse(null)
            } catch (e: Exception) {
                SdkLog.warning("Error creating Scout JS model for module '{}'.", module.name, e)
                null
            }
        }

        fun getOrCreateNodeModule(module: Module) = moduleDir(module)?.let {
            try {
                NodeModulesProvider.createNodeModule(it, module.project).orElse(null)?.api()
            } catch (e: Exception) {
                SdkLog.warning("Error creating NodeModule for module '{}'.", module.name, e)
                null
            }
        }

        private fun moduleDir(module: Module) = module.guessModuleDir()?.canonicalFile?.resolveLocalPath()
    }

    init {
        PsiManager.getInstance(project).addPsiTreeChangeListener(PsiListener(project), this)
        m_busConnection.subscribe(JSLibraryManager.TOPIC, JSLibraryChangeListener())
        NodeModulesProvider.registerProvider(project, this)
    }

    private class JSLibraryChangeListener : JSLibraryManagerChangeListener {
        override fun onChange() {
            NodeModulesProvider.clearNodeModules()
            SdkLog.debug("NodeModule cache cleared because of JS library change.")
        }
    }

    override fun create(nodeModuleDir: Path): Optional<NodeModuleSpi> {
        val path = VirtualFileManager.getInstance().findFileByNioPath(nodeModuleDir) ?: return Optional.empty()
        return Optional.ofNullable(m_nodeModuleInventory.create(path))
    }

    override fun remove(changedPath: Path): Set<NodeModuleSpi> {
        val path = VirtualFileManager.getInstance().findFileByNioPath(changedPath) ?: return emptySet()
        return m_nodeModuleInventory.remove(path)
    }

    override fun clear() {
        m_nodeModuleInventory.clear()
    }

    override fun dispose() {
        m_busConnection.disconnect()
        NodeModulesProvider.clearNodeModules()
        NodeModulesProvider.removeProvider(project)
    }

    private class PsiListener(private val project: Project) : PsiTreeChangeAdapter() {

        private val m_delayedProcessor = DelayedBuffer(2, TimeUnit.SECONDS, AppExecutorUtil.getAppScheduledExecutorService(), true, this::processFileEvents)

        override fun childrenChanged(event: PsiTreeChangeEvent) {
            val file = event.file ?: return
            if (!file.language.isKindOf(JavascriptLanguage.INSTANCE)) return
            m_delayedProcessor.submit(file)
        }

        private fun processFileEvents(events: List<PsiFile>) {
            if (!project.isInitialized || events.isEmpty()) return
            val changedPaths = computeInReadAction(project) { // PsiElement.isValid may require read-action
                events
                    .asSequence()
                    .filter { it.isPhysical && !it.isDirectory && it.isValid }
                    .map { it.virtualFile }
                    .distinct() // events for the same file: only process once
                    .mapNotNull { it.resolveLocalPath() }
                    .toList()
            }
            return changedPaths.forEach {
                NodeModulesProvider.removeNodeModule(it)
                    .forEach { removedModule -> SdkLog.debug("NodeModule cache entry for '{}' removed.", removedModule.api()) }
            }
        }
    }
}