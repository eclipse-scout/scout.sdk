/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.s2i.util

import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.roots.SourceFolder
import com.intellij.openapi.vfs.VirtualFile
import org.eclipse.scout.sdk.core.ISourceFolders
import org.eclipse.scout.sdk.core.model.api.IClasspathEntry
import org.eclipse.scout.sdk.core.s.IScoutSourceFolders
import org.eclipse.scout.sdk.core.s.util.ScoutTier
import org.eclipse.scout.sdk.core.util.FinalValue
import org.eclipse.scout.sdk.core.util.Strings
import org.jetbrains.jps.model.java.JavaSourceRootType
import java.util.*
import kotlin.streams.asSequence

class SourceFolderHelper(val project: Project, val sourceFolder: SourceFolder, val scoutTierOfModule: (Module) -> ScoutTier? = { S2iScoutTier.valueOf(it) }, val findClasspathEntry: (VirtualFile?) -> IClasspathEntry?) {

    private val m_classpathEntry = FinalValue<IClasspathEntry?>()

    private val m_tier = FinalValue<ScoutTier?>()

    private val m_clientSourceFolderPair = FinalValue<Pair<SourceFolder, IClasspathEntry>?>()
    private val m_sharedSourceFolderPair = FinalValue<Pair<SourceFolder, IClasspathEntry>?>()
    private val m_serverSourceFolderPair = FinalValue<Pair<SourceFolder, IClasspathEntry>?>()

    private val m_sharedGeneratedSourceFolder = FinalValue<IClasspathEntry?>()

    private val m_clientTestSourceFolder = FinalValue<IClasspathEntry?>()
    private val m_sharedTestSourceFolder = FinalValue<IClasspathEntry?>()
    private val m_serverTestSourceFolder = FinalValue<IClasspathEntry?>()

    private val m_clientMainTestSourceFolder = FinalValue<IClasspathEntry?>()
    private val m_sharedMainTestSourceFolder = FinalValue<IClasspathEntry?>()
    private val m_serverMainTestSourceFolder = FinalValue<IClasspathEntry?>()

    companion object {
        /**
         * Finds the closest dependency pair of [SourceFolder] and [IClasspathEntry] to the given [sourceFolder]. See [findClosestSourceFolder].
         */
        fun findClosestSourceFolderDependency(project: Project, scoutTier: ScoutTier, test: Boolean, sourceFolder: SourceFolder, scoutTierOfModule: ((Module) -> ScoutTier?)? = null, findClasspathEntry: (VirtualFile?) -> IClasspathEntry?): Pair<SourceFolder, IClasspathEntry>? {
            val module = sourceFolder.contentEntry.rootModel.module
            val dependencies = ModuleRootManager.getInstance(module).dependencies

            return findClosestSourceFolder(project, scoutTier, test, module, dependencies.asSequence(), scoutTierOfModule, findClasspathEntry)
        }

        /**
         * Finds the closest dependency pair of [SourceFolder] and [IClasspathEntry] to the given [sourceFolder]. See [findClosestSourceFolder].
         */
        fun findClosestDependentSourceFolder(project: Project, scoutTier: ScoutTier, test: Boolean, sourceFolder: SourceFolder, scoutTierOfModule: ((Module) -> ScoutTier?)? = null, findClasspathEntry: (VirtualFile?) -> IClasspathEntry?): Pair<SourceFolder, IClasspathEntry>? {
            val module = sourceFolder.contentEntry.rootModel.module
            val dependentModules = ModuleManager.getInstance(project).getModuleDependentModules(module)

            return findClosestSourceFolder(project, scoutTier, test, module, dependentModules.asSequence(), scoutTierOfModule, findClasspathEntry)
        }

        /**
         * Finds the closest pair of [SourceFolder] and [IClasspathEntry] to the given [reference] in the given [modules] that match the following restrictions:
         * @param scoutTier which [ScoutTier] to look for
         * @param test weather to look for test source folders or not
         */
        fun findClosestSourceFolder(project: Project, scoutTier: ScoutTier, test: Boolean, reference: Module, modules: Sequence<Module>, scoutTierOfModule: ((Module) -> ScoutTier?)? = null, findClasspathEntry: (VirtualFile?) -> IClasspathEntry?): Pair<SourceFolder, IClasspathEntry>? {
            val fileIndex = ProjectFileIndex.getInstance(project)
            val myScoutTierOfModule = scoutTierOfModule ?: { S2iScoutTier.valueOf(it) }

            val sourceFolder = modules.asSequence()
                    .flatMap {
                        ModuleRootManager.getInstance(it).sourceRoots.asSequence()
                    }
                    .mapNotNull { fileIndex.getSourceFolder(it) }
                    .filter { it.rootType is JavaSourceRootType && it.isTestSource == test }
                    .filter { !isGeneratedSourceFolder(it) }
                    .map { it to Strings.levenshteinDistance(it.contentEntry.rootModel.module.name, reference.name) }
                    .sortedBy { it.second }
                    .firstOrNull { (sf, _) -> myScoutTierOfModule(sf.contentEntry.rootModel.module) == scoutTier }?.first ?: return null

            return findClasspathEntry(sourceFolder.file)?.let { sourceFolder to it }
        }

        /**
         * Finds the closest pair of [SourceFolder] and [IClasspathEntry] in the given [project] that is a [ScoutTier.Client] for the given [sharedSourceFolder].
         */
        fun findClientSourceFolderPair(project: Project, sharedSourceFolder: SourceFolder?, scoutTierOfModule: ((Module) -> ScoutTier?)? = null, findClasspathEntry: (VirtualFile?) -> IClasspathEntry?): Pair<SourceFolder, IClasspathEntry>? {
            if (sharedSourceFolder == null) {
                return null
            }
            return findClosestDependentSourceFolder(project, ScoutTier.Client, false, sharedSourceFolder, scoutTierOfModule, findClasspathEntry)
        }

        /**
         * Finds the closest pair of [SourceFolder] and [IClasspathEntry] in the given [project] that is a [ScoutTier.Shared] for the given [clientOrServerSourceFolder].
         */
        fun findSharedSourceFolderPair(project: Project, clientOrServerSourceFolder: SourceFolder?, scoutTierOfModule: ((Module) -> ScoutTier?)? = null, findClasspathEntry: (VirtualFile?) -> IClasspathEntry?): Pair<SourceFolder, IClasspathEntry>? {
            if (clientOrServerSourceFolder == null) {
                return null
            }
            return findClosestSourceFolderDependency(project, ScoutTier.Shared, false, clientOrServerSourceFolder, scoutTierOfModule, findClasspathEntry)
        }

        /**
         * Finds the closest pair of [SourceFolder] and [IClasspathEntry] in the given [project] that is a [ScoutTier.Server] for the given [sharedSourceFolder].
         */
        fun findServerSourceFolderPair(project: Project, sharedSourceFolder: SourceFolder?, scoutTierOfModule: ((Module) -> ScoutTier?)? = null, findClasspathEntry: (VirtualFile?) -> IClasspathEntry?): Pair<SourceFolder, IClasspathEntry>? {
            if (sharedSourceFolder == null) {
                return null
            }
            return findClosestDependentSourceFolder(project, ScoutTier.Server, false, sharedSourceFolder, scoutTierOfModule, findClasspathEntry)
        }

        /**
         * Finds the closest test [IClasspathEntry] in the given [project] that is a [scoutTier] for the given [sourceFolder].
         */
        fun findTestSourceFolder(project: Project, scoutTier: ScoutTier, sourceFolder: SourceFolder?, scoutTierOfModule: ((Module) -> ScoutTier?)? = null, findClasspathEntry: (VirtualFile?) -> IClasspathEntry?): IClasspathEntry? {
            if (sourceFolder == null) {
                return null
            }
            val closestDependent = findClosestDependentSourceFolder(project, scoutTier, true, sourceFolder, scoutTierOfModule, findClasspathEntry)?.second
            if (closestDependent != null) {
                return closestDependent
            }
            val myScoutTierOfModule = scoutTierOfModule ?: { S2iScoutTier.valueOf(it) }
            if (myScoutTierOfModule(sourceFolder.contentEntry.rootModel.module) != scoutTier) {
                return null
            }
            return findTestSourceFolder(findClasspathEntry(sourceFolder.file))
        }

        /**
         * Finds the closest test [IClasspathEntry] in the given [project] that is a [ScoutTier.Client] for the given [clientSourceFolder].
         */
        fun findClientTestSourceFolder(project: Project, clientSourceFolder: SourceFolder?, scoutTierOfModule: ((Module) -> ScoutTier?)? = null, findClasspathEntry: (VirtualFile?) -> IClasspathEntry?): IClasspathEntry? {
            return findTestSourceFolder(project, ScoutTier.Client, clientSourceFolder, scoutTierOfModule, findClasspathEntry)
        }

        /**
         * Finds the closest test [IClasspathEntry] in the given [project] that is a [ScoutTier.Shared] for the given [sharedSourceFolder].
         */
        fun findSharedTestSourceFolder(project: Project, sharedSourceFolder: SourceFolder?, scoutTierOfModule: ((Module) -> ScoutTier?)? = null, findClasspathEntry: (VirtualFile?) -> IClasspathEntry?): IClasspathEntry? {
            return findTestSourceFolder(project, ScoutTier.Shared, sharedSourceFolder, scoutTierOfModule, findClasspathEntry)
        }

        /**
         * Finds the closest test [IClasspathEntry] in the given [project] that is a [ScoutTier.Server] for the given [serverSourceFolder].
         */
        fun findServerTestSourceFolder(project: Project, serverSourceFolder: SourceFolder?, scoutTierOfModule: ((Module) -> ScoutTier?)? = null, findClasspathEntry: (VirtualFile?) -> IClasspathEntry?): IClasspathEntry? {
            return findTestSourceFolder(project, ScoutTier.Server, serverSourceFolder, scoutTierOfModule, findClasspathEntry)
        }

        /**
         * Finds the src/main/java [IClasspathEntry] for the given [sourceFolder].
         */
        fun findMainSourceFolder(sourceFolder: IClasspathEntry?): IClasspathEntry? = findSourceFolder(sourceFolder) { isMainSourceFolder(it) }

        /**
         * Finds the src/main/java [SourceFolder] for the given [sourceFolder].
         */
        fun findMainSourceFolder(sourceFolder: SourceFolder?): SourceFolder? = findSourceFolder(sourceFolder) { isMainSourceFolder(it) }

        fun isMainSourceFolder(sourceFolder: IClasspathEntry?): Boolean = sourceFolderEndsWith(sourceFolder, ISourceFolders.MAIN_JAVA_SOURCE_FOLDER)

        fun isMainSourceFolder(sourceFolder: SourceFolder?): Boolean = sourceFolderEndsWith(sourceFolder, ISourceFolders.MAIN_JAVA_SOURCE_FOLDER)

        /**
         * Finds the src/generated/java [IClasspathEntry] for the given [sourceFolder].
         */
        fun findGeneratedSourceFolder(sourceFolder: IClasspathEntry?): IClasspathEntry? = findSourceFolder(sourceFolder) { isGeneratedSourceFolder(it) }

        fun isGeneratedSourceFolder(sourceFolder: IClasspathEntry?): Boolean = sourceFolderEndsWith(sourceFolder, IScoutSourceFolders.GENERATED_SOURCE_FOLDER)

        fun isGeneratedSourceFolder(sourceFolder: SourceFolder?): Boolean = sourceFolderEndsWith(sourceFolder, IScoutSourceFolders.GENERATED_SOURCE_FOLDER)

        /**
         * Finds the src/test/java [IClasspathEntry] for the given [sourceFolder].
         */
        fun findTestSourceFolder(sourceFolder: IClasspathEntry?): IClasspathEntry? = findSourceFolder(sourceFolder) { isTestSourceFolder(it) }

        fun isTestSourceFolder(sourceFolder: IClasspathEntry?): Boolean = sourceFolderEndsWith(sourceFolder, ISourceFolders.TEST_JAVA_SOURCE_FOLDER)

        /**
         * Finds the [IClasspathEntry] that matches the given [sourceFolderCondition] in the java environment of the given [sourceFolder].
         */
        private fun findSourceFolder(sourceFolder: IClasspathEntry?, sourceFolderCondition: (IClasspathEntry) -> Boolean): IClasspathEntry? {
            if (sourceFolder == null) {
                return null
            }
            if (sourceFolderCondition(sourceFolder)) {
                return sourceFolder
            }
            return sourceFolder.javaEnvironment().sourceFolders()
                    .asSequence()
                    .firstOrNull { sourceFolderCondition(it) }
        }

        /**
         * Finds the [SourceFolder] that matches the given [sourceFolderCondition] in the module of the given [sourceFolder].
         */
        private fun findSourceFolder(sourceFolder: SourceFolder?, sourceFolderCondition: (SourceFolder) -> Boolean): SourceFolder? {
            if (sourceFolder == null) {
                return null
            }
            if (sourceFolderCondition(sourceFolder)) {
                return sourceFolder
            }
            val module = sourceFolder.contentEntry.rootModel.module
            val project = module.project
            val fileIndex = ProjectFileIndex.getInstance(project)
            val sourceRoots = ModuleRootManager.getInstance(module)?.sourceRoots ?: return null

            return sourceRoots.asSequence()
                    .map { fileIndex.getSourceFolder(it) }
                    .filterNotNull()
                    .firstOrNull { sourceFolderCondition(it) }
        }

        private fun sourceFolderEndsWith(sourceFolder: IClasspathEntry?, suffix: String): Boolean {
            if (sourceFolder == null) {
                return false
            }
            return sourceFolder.path().endsWith(suffix)
        }

        private fun sourceFolderEndsWith(sourceFolder: SourceFolder?, suffix: String): Boolean {
            if (sourceFolder == null) {
                return false
            }
            return sourceFolder.file?.path?.endsWith(suffix) ?: false
        }
    }

    fun classpathEntry(): IClasspathEntry? = m_classpathEntry.computeIfAbsentAndGet {
        findClasspathEntry(sourceFolder.file)
    }

    fun tier(): ScoutTier? = m_tier.computeIfAbsentAndGet {
        scoutTierOfModule(sourceFolder.contentEntry.rootModel.module)
    }

    fun clientSourceFolder(): IClasspathEntry? = clientSourceFolderPair()?.second

    private fun clientSourceFolderPair(): Pair<SourceFolder, IClasspathEntry>? = m_clientSourceFolderPair.computeIfAbsentAndGet {
        if (tier() == ScoutTier.Client) {
            return@computeIfAbsentAndGet mainSourceFolderPair()
        }
        return@computeIfAbsentAndGet findClientSourceFolderPair(project, sharedSourceFolderPair()?.first, scoutTierOfModule, findClasspathEntry)
    }

    fun sharedSourceFolder(): IClasspathEntry? = sharedSourceFolderPair()?.second

    private fun sharedSourceFolderPair(): Pair<SourceFolder, IClasspathEntry>? = m_sharedSourceFolderPair.computeIfAbsentAndGet {
        if (tier() == ScoutTier.Shared) {
            return@computeIfAbsentAndGet mainSourceFolderPair()
        }
        return@computeIfAbsentAndGet findSharedSourceFolderPair(project, sourceFolder, scoutTierOfModule, findClasspathEntry)
    }

    fun serverSourceFolder(): IClasspathEntry? = serverSourceFolderPair()?.second

    private fun serverSourceFolderPair(): Pair<SourceFolder, IClasspathEntry>? = m_serverSourceFolderPair.computeIfAbsentAndGet {
        if (tier() == ScoutTier.Server) {
            return@computeIfAbsentAndGet mainSourceFolderPair()
        }
        return@computeIfAbsentAndGet findServerSourceFolderPair(project, sharedSourceFolderPair()?.first, scoutTierOfModule, findClasspathEntry)
    }

    private fun mainSourceFolderPair(): Pair<SourceFolder, IClasspathEntry>? {
        val mainSourceFolder = findMainSourceFolder(sourceFolder) ?: return null
        return findMainSourceFolder(classpathEntry())?.let { mainSourceFolder to it }
    }

    fun sharedGeneratedSourceFolder(): IClasspathEntry? = m_sharedGeneratedSourceFolder.computeIfAbsentAndGet {
        findGeneratedSourceFolder(sharedSourceFolder())
    }

    fun clientTestSourceFolder(): IClasspathEntry? = m_clientTestSourceFolder.computeIfAbsentAndGet {
        findClientTestSourceFolder(project, clientSourceFolderPair()?.first, scoutTierOfModule, findClasspathEntry)
    }

    fun sharedTestSourceFolder(): IClasspathEntry? = m_sharedTestSourceFolder.computeIfAbsentAndGet {
        findSharedTestSourceFolder(project, sharedSourceFolderPair()?.first, scoutTierOfModule, findClasspathEntry)
    }

    fun serverTestSourceFolder(): IClasspathEntry? = m_serverTestSourceFolder.computeIfAbsentAndGet {
        findServerTestSourceFolder(project, serverSourceFolderPair()?.first, scoutTierOfModule, findClasspathEntry)
    }

    fun clientMainTestSourceFolder(): IClasspathEntry? = m_clientMainTestSourceFolder.computeIfAbsentAndGet {
        findMainSourceFolder(clientTestSourceFolder())
    }

    fun sharedMainTestSourceFolder(): IClasspathEntry? = m_sharedMainTestSourceFolder.computeIfAbsentAndGet {
        findMainSourceFolder(sharedTestSourceFolder())
    }

    fun serverMainTestSourceFolder(): IClasspathEntry? = m_serverMainTestSourceFolder.computeIfAbsentAndGet {
        findMainSourceFolder(serverTestSourceFolder())
    }
}