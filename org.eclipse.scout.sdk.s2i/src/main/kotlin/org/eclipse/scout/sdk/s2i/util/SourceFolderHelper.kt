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

class SourceFolderHelper(val project: Project, val sourceFolder: SourceFolder, val scoutTierOfModule: (Module) -> ScoutTier? = { S2iScoutTier.valueOf(it) }, val findClasspathEntry: (VirtualFile?) -> IClasspathEntry?) {

    private val m_classpathEntry = FinalValue<IClasspathEntry?>()

    private val m_tier = FinalValue<ScoutTier?>()

    private val m_clientSourceFolderPair = FinalValue<Pair<SourceFolder, IClasspathEntry>?>()
    private val m_sharedSourceFolderPair = FinalValue<Pair<SourceFolder, IClasspathEntry>?>()
    private val m_serverSourceFolderPair = FinalValue<Pair<SourceFolder, IClasspathEntry>?>()

    private val m_sharedGeneratedSourceFolder = FinalValue<IClasspathEntry?>()

    private val m_clientTestSourceFolderPair = FinalValue<Pair<SourceFolder, IClasspathEntry>?>()
    private val m_sharedTestSourceFolderPair = FinalValue<Pair<SourceFolder, IClasspathEntry>?>()
    private val m_serverTestSourceFolderPair = FinalValue<Pair<SourceFolder, IClasspathEntry>?>()

    private val m_clientMainTestSourceFolder = FinalValue<IClasspathEntry?>()
    private val m_sharedMainTestSourceFolder = FinalValue<IClasspathEntry?>()
    private val m_serverMainTestSourceFolder = FinalValue<IClasspathEntry?>()

    companion object {
        /**
         * Finds the closest dependency pair of [SourceFolder] and [IClasspathEntry] to the given [sourceFolder]. See [findClosestSourceFolder].
         */
        fun findClosestSourceFolderDependency(
            project: Project,
            scoutTier: ScoutTier,
            test: Boolean,
            sourceFolder: SourceFolder,
            scoutTierOfModule: ((Module) -> ScoutTier?)? = null,
            findClasspathEntry: (VirtualFile?) -> IClasspathEntry?
        ): Pair<SourceFolder, IClasspathEntry>? {
            val module = sourceFolder.contentEntry.rootModel.module
            val dependencies = ModuleRootManager.getInstance(module).dependencies

            return findClosestSourceFolder(project, scoutTier, test, module, dependencies.asSequence(), scoutTierOfModule, findClasspathEntry)
        }

        /**
         * Finds the closest dependency pair of [SourceFolder] and [IClasspathEntry] to the given [sourceFolder]. See [findClosestSourceFolder].
         */
        fun findClosestDependentSourceFolder(
            project: Project,
            scoutTier: ScoutTier,
            test: Boolean,
            sourceFolder: SourceFolder,
            scoutTierOfModule: ((Module) -> ScoutTier?)? = null,
            findClasspathEntry: (VirtualFile?) -> IClasspathEntry?
        ): Pair<SourceFolder, IClasspathEntry>? {
            val module = sourceFolder.contentEntry.rootModel.module
            val dependentModules = ModuleManager.getInstance(project).getModuleDependentModules(module)

            return findClosestSourceFolder(project, scoutTier, test, module, dependentModules.asSequence(), scoutTierOfModule, findClasspathEntry)
        }

        /**
         * Finds the closest pair of [SourceFolder] and [IClasspathEntry] to the given [reference] in the given [modules] that match the following restrictions:
         * @param scoutTier which [ScoutTier] to look for
         * @param test weather to look for test source folders or not
         */
        fun findClosestSourceFolder(
            project: Project,
            scoutTier: ScoutTier,
            test: Boolean,
            reference: Module,
            modules: Sequence<Module>,
            scoutTierOfModule: ((Module) -> ScoutTier?)? = null,
            findClasspathEntry: (VirtualFile?) -> IClasspathEntry?
        ): Pair<SourceFolder, IClasspathEntry>? {
            val fileIndex = ProjectFileIndex.getInstance(project)
            val myScoutTierOfModule = scoutTierOfModule ?: { S2iScoutTier.valueOf(it) }

            val sourceFolder = modules
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
        fun findSharedSourceFolderPair(
            project: Project,
            clientOrServerSourceFolder: SourceFolder?,
            scoutTierOfModule: ((Module) -> ScoutTier?)? = null,
            findClasspathEntry: (VirtualFile?) -> IClasspathEntry?
        ): Pair<SourceFolder, IClasspathEntry>? {
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
         * Finds the closest test pair of [SourceFolder] and [IClasspathEntry] in the given [project] that is a [scoutTier] for the given [sourceFolder].
         */
        fun findTestSourceFolderPair(
            project: Project,
            scoutTier: ScoutTier,
            sourceFolder: SourceFolder?,
            scoutTierOfModule: ((Module) -> ScoutTier?)? = null,
            findClasspathEntry: (VirtualFile?) -> IClasspathEntry?
        ): Pair<SourceFolder, IClasspathEntry>? {
            if (sourceFolder == null) {
                return null
            }
            val myScoutTierOfModule = scoutTierOfModule ?: { S2iScoutTier.valueOf(it) }
            if (myScoutTierOfModule(sourceFolder.contentEntry.rootModel.module) == scoutTier) {
                findTestSourceFolderPair(sourceFolder)?.let { findClasspathEntry(it.file) }?.let { return sourceFolder to it }
            }
            return findClosestDependentSourceFolder(project, scoutTier, true, sourceFolder, scoutTierOfModule, findClasspathEntry)
        }

        /**
         * Finds the closest test pair of [SourceFolder] and [IClasspathEntry] in the given [project] that is a [ScoutTier.Client] for the given [clientSourceFolder].
         */
        fun findClientTestSourceFolderPair(
            project: Project,
            clientSourceFolder: SourceFolder?,
            scoutTierOfModule: ((Module) -> ScoutTier?)? = null,
            findClasspathEntry: (VirtualFile?) -> IClasspathEntry?
        ): Pair<SourceFolder, IClasspathEntry>? {
            return findTestSourceFolderPair(project, ScoutTier.Client, clientSourceFolder, scoutTierOfModule, findClasspathEntry)
        }

        /**
         * Finds the closest test pair of [SourceFolder] and [IClasspathEntry] in the given [project] that is a [ScoutTier.Shared] for the given [sharedSourceFolder].
         */
        fun findSharedTestSourceFolderPair(
            project: Project,
            sharedSourceFolder: SourceFolder?,
            scoutTierOfModule: ((Module) -> ScoutTier?)? = null,
            findClasspathEntry: (VirtualFile?) -> IClasspathEntry?
        ): Pair<SourceFolder, IClasspathEntry>? {
            return findTestSourceFolderPair(project, ScoutTier.Shared, sharedSourceFolder, scoutTierOfModule, findClasspathEntry)
        }

        /**
         * Finds the closest test pair of [SourceFolder] and [IClasspathEntry] in the given [project] that is a [ScoutTier.Server] for the given [serverSourceFolder].
         */
        fun findServerTestSourceFolderPair(
            project: Project,
            serverSourceFolder: SourceFolder?,
            scoutTierOfModule: ((Module) -> ScoutTier?)? = null,
            findClasspathEntry: (VirtualFile?) -> IClasspathEntry?
        ): Pair<SourceFolder, IClasspathEntry>? {
            return findTestSourceFolderPair(project, ScoutTier.Server, serverSourceFolder, scoutTierOfModule, findClasspathEntry)
        }

        /**
         * Finds the src/main/java [SourceFolder] for the given [sourceFolder].
         */
        fun findMainSourceFolder(sourceFolder: SourceFolder?): SourceFolder? = findSourceFolder(sourceFolder) { isMainSourceFolder(it) }

        fun isMainSourceFolder(sourceFolder: SourceFolder?): Boolean = sourceFolderEndsWith(sourceFolder, ISourceFolders.MAIN_JAVA_SOURCE_FOLDER) && sourceFolder?.rootType is JavaSourceRootType && !sourceFolder.isTestSource

        /**
         * Finds the src/generated/java [SourceFolder] for the given [sourceFolder].
         */
        fun findGeneratedSourceFolder(sourceFolder: SourceFolder?): SourceFolder? = findSourceFolder(sourceFolder) { isGeneratedSourceFolder(it) }

        fun isGeneratedSourceFolder(sourceFolder: SourceFolder?): Boolean = sourceFolderEndsWith(sourceFolder, IScoutSourceFolders.GENERATED_SOURCE_FOLDER) && sourceFolder?.rootType is JavaSourceRootType && !sourceFolder.isTestSource

        /**
         * Finds the src/test/java [SourceFolder] for the given [sourceFolder].
         */
        fun findTestSourceFolderPair(sourceFolder: SourceFolder?): SourceFolder? = findSourceFolder(sourceFolder) { isTestSourceFolder(it) }

        fun isTestSourceFolder(sourceFolder: SourceFolder?): Boolean = sourceFolderEndsWith(sourceFolder, ISourceFolders.TEST_JAVA_SOURCE_FOLDER) && sourceFolder?.rootType is JavaSourceRootType && sourceFolder.isTestSource

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
        return findClasspathEntry(mainSourceFolder.file)?.let { mainSourceFolder to it }
    }

    fun sharedGeneratedSourceFolder(): IClasspathEntry? = m_sharedGeneratedSourceFolder.computeIfAbsentAndGet {
        findGeneratedSourceFolder(sharedSourceFolderPair()?.first)?.file?.let { return@computeIfAbsentAndGet findClasspathEntry(it) }
    }

    fun clientTestSourceFolder(): IClasspathEntry? = clientTestSourceFolderPair()?.second

    private fun clientTestSourceFolderPair(): Pair<SourceFolder, IClasspathEntry>? = m_clientTestSourceFolderPair.computeIfAbsentAndGet {
        findClientTestSourceFolderPair(project, clientSourceFolderPair()?.first, scoutTierOfModule, findClasspathEntry)
    }

    fun sharedTestSourceFolder(): IClasspathEntry? = sharedTestSourceFolderPair()?.second

    private fun sharedTestSourceFolderPair(): Pair<SourceFolder, IClasspathEntry>? = m_sharedTestSourceFolderPair.computeIfAbsentAndGet {
        findSharedTestSourceFolderPair(project, sharedSourceFolderPair()?.first, scoutTierOfModule, findClasspathEntry)
    }

    fun serverTestSourceFolder(): IClasspathEntry? = serverTestSourceFolderPair()?.second

    private fun serverTestSourceFolderPair(): Pair<SourceFolder, IClasspathEntry>? = m_serverTestSourceFolderPair.computeIfAbsentAndGet {
        findServerTestSourceFolderPair(project, serverSourceFolderPair()?.first, scoutTierOfModule, findClasspathEntry)
    }

    fun clientMainTestSourceFolder(): IClasspathEntry? = m_clientMainTestSourceFolder.computeIfAbsentAndGet {
        findMainSourceFolder(clientTestSourceFolderPair()?.first)?.file?.let { return@computeIfAbsentAndGet findClasspathEntry(it) }
    }

    fun sharedMainTestSourceFolder(): IClasspathEntry? = m_sharedMainTestSourceFolder.computeIfAbsentAndGet {
        findMainSourceFolder(sharedTestSourceFolderPair()?.first)?.file?.let { return@computeIfAbsentAndGet findClasspathEntry(it) }
    }

    fun serverMainTestSourceFolder(): IClasspathEntry? = m_serverMainTestSourceFolder.computeIfAbsentAndGet {
        findMainSourceFolder(serverTestSourceFolderPair()?.first)?.file?.let { return@computeIfAbsentAndGet findClasspathEntry(it) }
    }
}