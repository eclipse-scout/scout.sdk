/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2i.environment.model

import com.intellij.openapi.module.Module
import com.intellij.openapi.project.rootManager
import com.intellij.openapi.vfs.VirtualFile
import org.eclipse.scout.sdk.core.java.ecj.ClasspathEntry
import org.eclipse.scout.sdk.core.java.ecj.JavaEnvironmentWithEcj
import org.eclipse.scout.sdk.core.util.SdkException
import org.eclipse.scout.sdk.s2i.resolveLocalPath
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths


open class JavaEnvironmentWithIdea(val module: Module) : JavaEnvironmentWithEcj(javaHomeOf(module), classpathOf(module), null) {

    companion object Factory {
        protected fun javaHomeOf(module: Module): Path {
            val moduleRootManager = module.rootManager
            val sdkPath = moduleRootManager.sdk?.homePath
                ?: throw SdkException("Cannot find JRE in module '{}'.", module.name)
            val sdkRoot = Paths.get(sdkPath)
            return arrayOf("jre", "jre64", "jbr")
                .map { sdkRoot.resolve(it) }
                .filter { Files.isDirectory(it) }
                .firstOrNull { Files.isReadable(it) } ?: sdkRoot
        }

        protected fun classpathOf(module: Module): Collection<ClasspathEntry> {
            val manager = module.rootManager
            val result = ArrayList<ClasspathEntry>()
            manager.orderEntries().withoutSdk().allSourceRoots.mapNotNullTo(result) { toClasspathEntry(it, org.eclipse.scout.sdk.core.java.model.spi.ClasspathSpi.MODE_SOURCE) }
            manager.orderEntries().withoutSdk().allLibrariesAndSdkClassesRoots.mapNotNullTo(result) { toClasspathEntry(it, org.eclipse.scout.sdk.core.java.model.spi.ClasspathSpi.MODE_BINARY) }
            return result
        }

        protected fun toClasspathEntry(entry: VirtualFile, type: Int): ClasspathEntry? {
            val absolutePath = entry.resolveLocalPath() ?: return null
            if (absolutePath.nameCount < 1) return null
            return ClasspathEntry(absolutePath, type, entry.charset.name())
        }
    }
}
