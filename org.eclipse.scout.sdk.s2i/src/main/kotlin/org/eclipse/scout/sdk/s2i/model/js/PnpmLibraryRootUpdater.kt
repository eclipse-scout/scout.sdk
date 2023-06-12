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

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.RootsChangeRescanningInfo
import com.intellij.openapi.roots.ex.ProjectRootManagerEx
import com.intellij.openapi.util.EmptyRunnable
import com.intellij.openapi.vfs.AsyncFileListener
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import com.intellij.openapi.vfs.newvfs.events.VFilePropertyChangeEvent
import org.eclipse.scout.sdk.core.log.SdkLog
import org.eclipse.scout.sdk.core.util.Strings
import org.eclipse.scout.sdk.s2i.util.compat.CompatibilityMethodCaller

/**
 * Workaround for IntelliJ issue https://youtrack.jetbrains.com/issue/WEB-60948
 * Can be removed as soon as the issue is fixed upstream and all supported IJ versions contain the fix.
 */
class PnpmLibraryRootUpdater(val project: Project) : AsyncFileListener, Disposable {

    init {
        VirtualFileManager.getInstance().addAsyncFileListener(this, this)
    }

    override fun prepareChange(events: List<VFileEvent>): AsyncFileListener.ChangeApplier? {
        if (!hasPnpmLinkChange(events)) return null
        return object : AsyncFileListener.ChangeApplier {
            override fun afterVfsChange() {
                val application = ApplicationManager.getApplication()
                application.invokeLater { application.runWriteAction { reScanRoots() } }
            }
        }
    }

    private fun hasPnpmLinkChange(events: List<VFileEvent>) = events.any {
        if (it !is VFilePropertyChangeEvent || it.propertyName != VirtualFile.PROP_SYMLINK_TARGET) return@any false
        val newSymlinkLocation = it.newValue as? CharSequence ?: return@any false
        return@any Strings.indexOf("/node_modules/.pnpm/", newSymlinkLocation) > 0
    }

    private fun reScanRoots() {
        try {
            val start = System.currentTimeMillis()
            val nodeModulesDirectories = getNodeModulesDirectories()
            nodeModulesDirectories.forEach { directory ->
                CompatibilityMethodCaller<Any>()
                    .withCandidate("com.intellij.javascript.nodejs.library.node_modules.NodeModulesLibraryDirectory", "invalidateRoots") {
                        // for IJ >= 2023.2 use ...library.node_modules.NodeModulesLibraryDirectory
                        it.invoke(directory)
                    }
                    .withCandidate("com.intellij.javascript.nodejs.library.NodeModulesLibraryDirectory", "invalidateRoots") {
                        // for IJ < 2023.2 use ...library.NodeModulesLibraryDirectory
                        it.invoke(directory)
                    }.invoke()
            }
            ProjectRootManagerEx.getInstanceEx(project).makeRootsChange(EmptyRunnable.INSTANCE, RootsChangeRescanningInfo.TOTAL_RESCAN)
            SdkLog.info("Rescan of {} Node library roots took {}ms.", nodeModulesDirectories.size, System.currentTimeMillis() - start)
        } catch (t: Throwable) {
            SdkLog.warning("Unable to reload Node library roots.", t)
        }
    }

    private fun getNodeModulesDirectories(): List<Any> {
        val nodeModulesDirectoryManager = CompatibilityMethodCaller<Any>()
            .withCandidate("com.intellij.javascript.nodejs.library.node_modules.NodeModulesDirectoryManager", "getInstance", Project::class.java.name) {
                // for IJ >= 2023.2 use ...library.node_modules.NodeModulesDirectoryManager
                it.invokeStatic(project)
            }
            .withCandidate("com.intellij.javascript.nodejs.library.NodeModulesDirectoryManager", "getInstance", Project::class.java.name) {
                // for IJ < 2023.2 use ...library.NodeModulesDirectoryManager
                it.invokeStatic(project)
            }.invoke()
        return CompatibilityMethodCaller<List<Any>>()
            .withCandidate(nodeModulesDirectoryManager.javaClass, "buildNodeModulesDirectories") {
                // for IJ >= 2023.1 use method name "buildNodeModulesDirectories"
                it.invoke(nodeModulesDirectoryManager)
            }
            .withCandidate(nodeModulesDirectoryManager.javaClass, "getNodeModulesDirectories") {
                // for IJ < 2023.1: use method name "getNodeModulesDirectories"
                it.invoke(nodeModulesDirectoryManager)
            }.invoke()
    }

    override fun dispose() {}
}