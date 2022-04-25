/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.s2i.nls

import com.intellij.AppTopics
import com.intellij.openapi.Disposable
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileDocumentManagerListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.concurrency.AppExecutorUtil
import com.intellij.util.messages.MessageBusConnection
import org.eclipse.scout.sdk.core.s.nls.TextProviderService
import org.eclipse.scout.sdk.core.s.nls.Translations.DependencyScope
import org.eclipse.scout.sdk.core.s.nls.manager.TranslationManager
import org.eclipse.scout.sdk.core.s.nls.properties.AbstractTranslationPropertiesFile
import org.eclipse.scout.sdk.core.util.TtlCache
import org.eclipse.scout.sdk.s2i.nls.TranslationManagerLoader.createManager
import java.nio.file.Path
import java.util.concurrent.TimeUnit

class TranslationManagerCache(val project: Project) : Disposable {

    companion object {
        /**
         * @return a cache key to be used for a [TranslationManager].
         */
        fun createCacheKey(modulePath: Path, scope: DependencyScope?) = modulePath to scope

    }

    private val m_cache = TtlCache<Pair<Path, DependencyScope?>, TranslationManager>(1, TimeUnit.HOURS, AppExecutorUtil.getAppScheduledExecutorService())
    private var m_busConnection: MessageBusConnection? = null

    init {
        m_busConnection = project.messageBus.connect()
        m_busConnection?.subscribe(AppTopics.FILE_DOCUMENT_SYNC, DocumentSyncListener())
    }

    /**
     * @param modulePath The [Path] to the root of the module (the directory where the pom.xml of the module can be found).
     * @param scope An optional [DependencyScope] for which the cache should be created. If it is null, [DependencyScope.ALL] is used.
     * @return The [TranslationManager] for the module and scope given or null if no manager can be created for the module given (e.g. if it is no Scout module).
     */
    fun getOrCreateManager(modulePath: Path, scope: DependencyScope?): TranslationManager? =
        m_cache.computeIfAbsent(createCacheKey(modulePath, scope)) {
            createManager(project, modulePath, scope, false)
        }

    /**
     * Puts the manager given into the cache. An existing entry may be replaced.
     * @param modulePath The [Path] to the root of the module (the directory where the pom.xml of the module can be found).
     * @param scope An optional [DependencyScope] for which the cache should be created. If it is null, [DependencyScope.ALL] is used.
     * @param manager The [TranslationManager] to store.
     * @return the previous [TranslationManager] associated with the path and scope, or null if there was no mapping before.
     */
    fun putManager(modulePath: Path, scope: DependencyScope?, manager: TranslationManager?): TranslationManager? =
        m_cache.put(createCacheKey(modulePath, scope), manager)

    /**
     * Removes all managers from the cache.
     */
    fun clear() = m_cache.clear()

    override fun dispose() {
        m_busConnection?.disconnect()
        m_busConnection = null
        clear()
    }

    private inner class DocumentSyncListener : FileDocumentManagerListener {
        override fun fileContentReloaded(file: VirtualFile, document: Document) {
            val path = file.path
            if (path.endsWith(AbstractTranslationPropertiesFile.FILE_SUFFIX) || path.endsWith(TextProviderService.TEXT_SERVICE_FILE_SUFFIX)) {
                clear()
            }
        }

        override fun beforeDocumentSaving(document: Document) {
            val file = FileDocumentManager.getInstance().getFile(document) ?: return
            fileContentReloaded(file, document)
        }
    }
}