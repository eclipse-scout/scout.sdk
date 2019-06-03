package org.eclipse.scout.sdk.s2i.environment.model

import com.intellij.openapi.module.Module
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.vfs.VirtualFile
import org.eclipse.scout.sdk.core.model.ecj.ClasspathEntry
import org.eclipse.scout.sdk.core.model.ecj.JavaEnvironmentWithEcj
import org.eclipse.scout.sdk.core.model.spi.ClasspathSpi
import org.eclipse.scout.sdk.core.util.SdkException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*


open class JavaEnvironmentWithIdea(val module: Module) : JavaEnvironmentWithEcj(javaHomeOf(module), classpathOf(module), null) {

    companion object Factory {
        protected fun javaHomeOf(module: Module): Path {
            val moduleRootManager = ModuleRootManager.getInstance(module)
            val sdkPath = moduleRootManager.sdk?.homePath ?: throw SdkException("Cannot find JRE in module '{}'.", module.name)
            val sdkRoot = Paths.get(sdkPath)
            val jreSubFolder = sdkRoot.resolve("jre")
            if(Files.isDirectory(jreSubFolder) && Files.isReadable(jreSubFolder)) {
                return jreSubFolder
            }
            val jre64SubFolder = sdkRoot.resolve("jre64")
            if(Files.isDirectory(jre64SubFolder) && Files.isReadable(jre64SubFolder)) {
                return jre64SubFolder
            }
            return sdkRoot
        }

        protected fun classpathOf(module: Module): Collection<ClasspathEntry> {
            val manager = ModuleRootManager.getInstance(module)
            val result = ArrayList<ClasspathEntry>()
            manager.orderEntries().withoutSdk().allSourceRoots.mapNotNullTo(result) { toClasspathEntry(it, ClasspathSpi.MODE_SOURCE) }
            manager.orderEntries().withoutSdk().allLibrariesAndSdkClassesRoots.mapNotNullTo(result) { toClasspathEntry(it, ClasspathSpi.MODE_BINARY) }
            return result
        }

        protected fun toClasspathEntry(entry: VirtualFile, type: Int): ClasspathEntry? {
            var absolutePath = Paths.get(entry.path)
            if (absolutePath.nameCount < 1) {
                return null
            }

            val lastSegmentName = absolutePath.fileName?.toString() ?: return null
            if (lastSegmentName.endsWith("!")) {
                absolutePath = absolutePath.parent.resolve(lastSegmentName.substring(0, lastSegmentName.length - 1))
            }
            return ClasspathEntry(absolutePath, type, entry.charset.name())
        }
    }
}
