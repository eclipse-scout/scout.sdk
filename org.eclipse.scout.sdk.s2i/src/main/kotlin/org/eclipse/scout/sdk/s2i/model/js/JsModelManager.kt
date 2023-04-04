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
import com.intellij.openapi.Disposable
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessModuleDir
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiTreeChangeAdapter
import com.intellij.psi.PsiTreeChangeEvent
import com.intellij.util.concurrency.AppExecutorUtil
import org.eclipse.scout.sdk.core.log.SdkLog
import org.eclipse.scout.sdk.core.s.model.js.ScoutJsModel
import org.eclipse.scout.sdk.core.s.model.js.ScoutJsModels
import org.eclipse.scout.sdk.core.typescript.model.api.NodeModulesProvider
import org.eclipse.scout.sdk.core.typescript.model.spi.NodeModuleSpi
import org.eclipse.scout.sdk.core.typescript.model.spi.NodeModulesProviderSpi
import org.eclipse.scout.sdk.core.util.DelayedBuffer
import org.eclipse.scout.sdk.s2i.model.typescript.IdeaNodeModules
import org.eclipse.scout.sdk.s2i.resolveLocalPath
import java.nio.file.Path
import java.util.*
import java.util.concurrent.TimeUnit

class JsModelManager(val project: Project) : NodeModulesProviderSpi, Disposable {

    private val m_delayedProcessor = DelayedBuffer(2, TimeUnit.SECONDS, AppExecutorUtil.getAppScheduledExecutorService(), true, this::processFileEvents)
    private val m_nodeModuleInventory = IdeaNodeModules(project)

    companion object {
        fun getOrCreateScoutJsModel(module: Module) = moduleDir(module)?.let {
            ScoutJsModels.create(it, module.project).orElse(null)
        }

        fun getOrCreateNodeModule(module: Module) = moduleDir(module)?.let {
            NodeModulesProvider.createNodeModule(it, module.project).orElse(null)?.api()
        }

        private fun moduleDir(module: Module) = module.guessModuleDir()?.canonicalFile?.resolveLocalPath()
    }

    init {
        PsiManager.getInstance(project).addPsiTreeChangeListener(PsiListener(), this)
        NodeModulesProvider.registerProvider(project, this)
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
        NodeModulesProvider.clearNodeModules()
        NodeModulesProvider.removeProvider(project)
    }

    private inner class PsiListener : PsiTreeChangeAdapter() {
        override fun childrenChanged(event: PsiTreeChangeEvent) {
            val file = event.file ?: return
            if (!file.isPhysical || !file.language.isKindOf(JavascriptLanguage.INSTANCE)) {
                return
            }
            m_delayedProcessor.submit(file)
        }
    }

    private fun processFileEvents(events: List<PsiFile>) {
        if (!project.isInitialized || events.isEmpty()) return
        return events
            .asSequence()
            .map { it.virtualFile }
            .distinct() // events for the same file: only process once
            .forEach { remove(it) }
    }

    private fun remove(file: VirtualFile) = file.resolveLocalPath()
        ?.let { NodeModulesProvider.removeNodeModule(it) }
        ?.forEach { SdkLog.debug("Removed {} cache entry for '{}').", ScoutJsModel::class.java.simpleName, it) }
}