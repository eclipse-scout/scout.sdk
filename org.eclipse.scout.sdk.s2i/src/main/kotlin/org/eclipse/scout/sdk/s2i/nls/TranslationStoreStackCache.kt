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

import com.intellij.AppTopics
import com.intellij.openapi.Disposable
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileDocumentManagerListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.concurrency.AppExecutorUtil
import com.intellij.util.messages.MessageBusConnection
import org.eclipse.scout.sdk.core.s.ISdkConstants
import org.eclipse.scout.sdk.core.s.nls.TranslationStoreStack
import org.eclipse.scout.sdk.core.s.nls.TranslationStores.DependencyScope
import org.eclipse.scout.sdk.core.s.nls.properties.AbstractTranslationPropertiesFile
import org.eclipse.scout.sdk.core.util.JavaTypes
import org.eclipse.scout.sdk.core.util.TtlCache
import org.eclipse.scout.sdk.s2i.nls.TranslationStoreStackLoader.createStack
import java.nio.file.Path
import java.util.concurrent.TimeUnit

class TranslationStoreStackCache(val project: Project) : Disposable {

    companion object {
        private const val TEXT_SERVICE_FILE_SUFFIX = ISdkConstants.SUFFIX_TEXT_PROVIDER_SERVICE + JavaTypes.JAVA_FILE_SUFFIX

        /**
         * @return a cache key to be used for a [TranslationStoreStack].
         */
        fun createCacheKey(modulePath: Path, scope: DependencyScope?) = modulePath to scope

    }

    private val m_cache = TtlCache<Pair<Path, DependencyScope?>, TranslationStoreStack>(1, TimeUnit.HOURS, AppExecutorUtil.getAppScheduledExecutorService())
    private var m_busConnection: MessageBusConnection? = null

    init {
        m_busConnection = project.messageBus.connect()
        m_busConnection?.subscribe(AppTopics.FILE_DOCUMENT_SYNC, DocumentSyncListener())
    }

    /**
     * @param modulePath The [Path] to the root of the module (the directory where the pom.xml of the module can be found).
     * @param scope An optional [DependencyScope] for which the cache should be created. If it is null, [DependencyScope.ALL] is used.
     * @return The [TranslationStoreStack] for the module and scope given or null if no stack can be created for the module given (e.g. if it is no Scout module).
     */
    fun getOrCreateStack(modulePath: Path, scope: DependencyScope?): TranslationStoreStack? =
            m_cache.computeIfAbsent(createCacheKey(modulePath, scope)) {
                createStack(project, modulePath, scope, false)
            }

    /**
     * Puts the stack given into the cache. An existing entry may be replaced.
     * @param modulePath The [Path] to the root of the module (the directory where the pom.xml of the module can be found).
     * @param scope An optional [DependencyScope] for which the cache should be created. If it is null, [DependencyScope.ALL] is used.
     * @param stack The [TranslationStoreStack] to store.
     * @return the previous [TranslationStoreStack] associated with the path and scope, or null if there was no mapping before.
     */
    fun putStack(modulePath: Path, scope: DependencyScope?, stack: TranslationStoreStack?): TranslationStoreStack? =
            m_cache.put(createCacheKey(modulePath, scope), stack)

    /**
     * Removes all stacks from the cache.
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
            if (path.endsWith(AbstractTranslationPropertiesFile.FILE_SUFFIX) || path.endsWith(TEXT_SERVICE_FILE_SUFFIX)) {
                clear()
            }
        }

        override fun beforeDocumentSaving(document: Document) {
            val file = FileDocumentManager.getInstance().getFile(document) ?: return
            fileContentReloaded(file, document)
        }
    }
}