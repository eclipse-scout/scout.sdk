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
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessModuleDir
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiTreeChangeAdapter
import com.intellij.psi.PsiTreeChangeEvent
import com.intellij.util.concurrency.AppExecutorUtil
import org.eclipse.scout.sdk.core.log.SdkLog
import org.eclipse.scout.sdk.core.s.util.DelayedBuffer
import org.eclipse.scout.sdk.s2i.containingModule
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

class JsModuleCacheImplementor(val project: Project) : Disposable {

    private val m_moduleCache = ConcurrentHashMap<VirtualFile, JsModule>()
    private val m_delayedProcessor = DelayedBuffer(2, TimeUnit.SECONDS, AppExecutorUtil.getAppScheduledExecutorService(), true, this::processFileEvents)
    private var m_listenerRegistered = false

    fun getModule(file: VirtualFile) = file.canonicalFile?.let { m_moduleCache[it] }

    fun putModule(file: VirtualFile, module: JsModule) {
        startListeningIfNecessary() // only start listening for js file changes if there are cached elements.
        file.canonicalFile?.let { m_moduleCache[it] = module }
    }

    override fun dispose() {
        m_moduleCache.clear()
    }

    /**
     * Registers a Psi listener. The listener is only added if the cache is used at least one time.
     * If js files are never touched, the listener is not necessary and will not be added.
     */
    private fun startListeningIfNecessary() = synchronized(m_moduleCache) {
        if (m_listenerRegistered) return
        PsiManager.getInstance(project).addPsiTreeChangeListener(PsiListener(), this)
        m_listenerRegistered = true
    }

    private inner class PsiListener : PsiTreeChangeAdapter() {
        override fun childrenChanged(event: PsiTreeChangeEvent) {
            val file = event.file ?: return
            if (!file.isPhysical || !file.language.isKindOf(JavascriptLanguage.INSTANCE) || m_moduleCache.isEmpty()) {
                return
            }
            m_delayedProcessor.submit(file)
        }
    }

    private fun processFileEvents(events: List<PsiFile>) = events
            .asSequence()
            .distinct() // events for the same file: only process once for each file
            .mapNotNull { moduleRootOf(it) }
            .distinct() // events for the same module: only process once for each module
            .forEach { remove(it) }

    private fun moduleRootOf(file: PsiFile) = file.containingModule()?.guessModuleDir()

    private fun remove(file: VirtualFile) = file.canonicalFile?.let {
        val removed = m_moduleCache.remove(it)
        if (removed != null) {
            SdkLog.debug("Removed {} cache entry for '{}': '{}' (having root '{}').", JsModel::class.java.simpleName, it, removed, removed.moduleRoot)
        }
    }
}