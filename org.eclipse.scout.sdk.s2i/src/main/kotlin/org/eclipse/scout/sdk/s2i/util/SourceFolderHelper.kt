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
import com.jetbrains.rd.util.first
import org.eclipse.scout.sdk.core.ISourceFolders
import org.eclipse.scout.sdk.core.model.api.IClasspathEntry
import org.eclipse.scout.sdk.core.s.IScoutSourceFolders
import org.eclipse.scout.sdk.core.s.util.ITier
import org.eclipse.scout.sdk.core.s.util.TierTree
import org.eclipse.scout.sdk.core.util.FinalValue
import org.eclipse.scout.sdk.core.util.Strings
import org.jetbrains.jps.model.java.JavaSourceRootType
import java.util.*

class SourceFolderHelper(val project: Project, val sourceFolder: SourceFolder, tierOfModule: ((Module) -> ITier<*>?)? = null, val findClasspathEntry: (VirtualFile?) -> IClasspathEntry?) {

    private val m_sourceFolderHelperInfo = SourceFolderHelperInfo(project, sourceFolder, tierOfModule(tierOfModule), findClasspathEntry)

    private val m_tier = FinalValue<ITier<*>?>()
    private val m_classpathEntry = FinalValue<IClasspathEntry?>()

    private val m_tierSourceFolderBundleMap = HashMap<ITier<*>?, SourceFolderBundle?>()
    private val m_moduleTierMap = HashMap<Module, ITier<*>?>()

    companion object {
        /**
         * Finds the closest dependency pair of [SourceFolder] and [IClasspathEntry] to the given [sourceFolder].
         */
        fun findClosestSourceFolderDependency(
            sourceFolder: SourceFolder,
            tier: ITier<*>,
            test: Boolean,
            project: Project,
            tierOfModule: (Module) -> ITier<*>?,
            findClasspathEntry: (VirtualFile?) -> IClasspathEntry?
        ): Pair<SourceFolder, IClasspathEntry>? {
            val module = sourceFolder.contentEntry.rootModel.module
            val tierSourceFolderBundle = tierOfModule(module)?.let { t ->
                val sourceFolderHelperInfo = SourceFolderHelperInfo(project, sourceFolder, tierOfModule, findClasspathEntry)
                DependencySourceFolderBundle(tier, StartSourceFolderBundle(t, sourceFolderHelperInfo), sourceFolderHelperInfo)
            } ?: return null
            return if (test) tierSourceFolderBundle.testSourceFolderPair() else tierSourceFolderBundle.sourceFolderPair()
        }

        /**
         * Finds the closest dependency pair of [SourceFolder] and [IClasspathEntry] to the given [sourceFolder].
         */
        fun findClosestDependentSourceFolder(
            sourceFolder: SourceFolder,
            tier: ITier<*>,
            test: Boolean,
            project: Project,
            tierOfModule: (Module) -> ITier<*>?,
            findClasspathEntry: (VirtualFile?) -> IClasspathEntry?
        ): Pair<SourceFolder, IClasspathEntry>? {
            val module = sourceFolder.contentEntry.rootModel.module
            val tierSourceFolderBundle = tierOfModule(module)?.let { t ->
                val sourceFolderHelperInfo = SourceFolderHelperInfo(project, sourceFolder, tierOfModule, findClasspathEntry)
                DependentSourceFolderBundle(tier, StartSourceFolderBundle(t, sourceFolderHelperInfo), sourceFolderHelperInfo)
            } ?: return null
            return if (test) tierSourceFolderBundle.testSourceFolderPair() else tierSourceFolderBundle.sourceFolderPair()
        }

        private fun Module.getTier(tierOfModule: (Module) -> ITier<*>?): ITier<*>? {
            return tierOfModule(this)
        }
    }

    private fun tierOfModule(tierOfModule: ((Module) -> ITier<*>?)?): (Module) -> ITier<*>? = { module -> m_moduleTierMap.computeIfAbsent(module, tierOfModule ?: { S2iTier.of(it) }) }

    fun tier(): ITier<*>? = m_tier.computeIfAbsentAndGet {
        sourceFolder.contentEntry.rootModel.module.getTier(m_sourceFolderHelperInfo.tierOfModule)
    }

    fun classpathEntry(): IClasspathEntry? = m_classpathEntry.computeIfAbsentAndGet {
        findClasspathEntry(sourceFolder.file)
    }

    fun sourceFolder(tier: ITier<*>): IClasspathEntry? = sourceFolderBundle(tier)?.sourceFolderPair()?.second

    fun testSourceFolder(tier: ITier<*>): IClasspathEntry? = sourceFolderBundle(tier)?.testSourceFolderPair()?.second

    fun mainTestSourceFolder(tier: ITier<*>): IClasspathEntry? = sourceFolderBundle(tier)?.mainTestSourceFolder()

    fun generatedSourceFolder(tier: ITier<*>): IClasspathEntry? = sourceFolderBundle(tier)?.generatedSourceFolder()

    private fun sourceFolderBundle(tier: ITier<*>): SourceFolderBundle? {
        // add start sourceFolderBundle
        m_tierSourceFolderBundleMap.computeIfAbsent(tier()) {
            it?.let { t -> StartSourceFolderBundle(t, m_sourceFolderHelperInfo) }
        }
        if (m_tierSourceFolderBundleMap.isEmpty()) {
            return null
        }

        m_tierSourceFolderBundleMap[tier]?.let { return it }

        // consider a tree like this, uppercase letters represent nodes for whom we already computed the sourceFolderBundle
        //
        //    a <- root
        //   b
        //  c d
        // E   f <- example 1
        //  G
        //   h
        //    i <- example 2

        // example 1
        // search for node f, which is not a descendant of any node for whom we already computed the sourceFolderBundle
        // -> we need to compute some ancestors first before we can compute the descendants down to f (after all necessary descendants are computed this example is equivalent to example 2)

        // example 2
        // search for node i, which is a descendant of at least one node for whom we already computed the sourceFolderBundle
        // -> we just need to compute some more descendants

        // get a path (start inclusive, end exclusive) from the root node to the first node for whom we already computed the sourceFolderBundle

        // example 1 & 2: E
        var existingTier: ITier<*>? = null
        // example 1 & 2: a > b > c
        val pathToExistingTier = TierTree.topDownPath(null, m_tierSourceFolderBundleMap.first().key)
            .takeWhile { t ->
                existingTier = t
                return@takeWhile !m_tierSourceFolderBundleMap.containsKey(t)
            }

        // get a path (start exclusive, end inclusive) from the last node for whom we already computed the sourceFolderBundle or that is contained in the path we just created to the node we search for

        // example 1: b
        // example 2: G
        var startTier: ITier<*>? = null
        // example 1: d > f
        // example 2: h > i
        val pathToTier = TierTree.topDownPath(null, tier).reversed()
            .takeWhile { t ->
                startTier = t
                return@takeWhile !m_tierSourceFolderBundleMap.containsKey(t) && !pathToExistingTier.contains(t)
            }
            .reversed()

        if (pathToExistingTier.contains(startTier)) {
            // we need to compute sourceFolderBundles for all elements from the end of pathToExistingTier until startTier
            (pathToExistingTier.size - 1 downTo pathToExistingTier.lastIndexOf(startTier))
                .map { pathToExistingTier[it] } // example 1: c > b
                .forEach {
                    // compute a sourceFolderBundle as dependency of existingTier and update existingTier
                    m_tierSourceFolderBundleMap[it] = DependencySourceFolderBundle(it, m_tierSourceFolderBundleMap[existingTier], m_sourceFolderHelperInfo)
                    existingTier = it

                    // example 1: first compute C as ancestor of E, then compute B as ancestor of C
                    // after this the tree looks like this
                    //
                    //    a
                    //   B
                    //  C d
                    // E   f
                    //  G
                    //   h
                    //    i
                }
        }

        if (!m_tierSourceFolderBundleMap.containsKey(startTier)) {
            return null
        }

        // compute sourceFolderBundles for all elements from startTier to the tier we search for
        pathToTier.forEach {
            // compute a sourceFolderBundle as dependency of startTier and update startTier
            m_tierSourceFolderBundleMap[it] = DependentSourceFolderBundle(it, m_tierSourceFolderBundleMap[startTier], m_sourceFolderHelperInfo)
            startTier = it

            // example 1: first compute D as descendant of B, then compute F as descendant of D
            // after this the tree looks like this
            //
            //    a
            //   B
            //  C D
            // E   F
            //  G
            //   h
            //    i

            // example 2: first compute H as descendant of G, then compute I as descendant of I
            // after this the tree looks like this
            //
            //    a
            //   b
            //  c d
            // E   f
            //  G
            //   H
            //    I
        }

        return m_tierSourceFolderBundleMap[tier]
    }

    private data class SourceFolderHelperInfo(val project: Project, val sourceFolder: SourceFolder, val tierOfModule: (Module) -> ITier<*>?, val findClasspathEntry: (VirtualFile?) -> IClasspathEntry?)

    private abstract class SourceFolderBundle(val tier: ITier<*>, val referenceSourceFolderBundle: SourceFolderBundle?, val sourceFolderHelperInfo: SourceFolderHelperInfo) {
        private val m_sourceFolderPair = FinalValue<Pair<SourceFolder, IClasspathEntry>?>()
        private val m_testSourceFolderPair = FinalValue<Pair<SourceFolder, IClasspathEntry>?>()
        private val m_mainTestSourceFolder = FinalValue<IClasspathEntry?>()
        private val m_generatedSourceFolder = FinalValue<IClasspathEntry?>()

        protected abstract fun computeSourceFolderPair(): Pair<SourceFolder, IClasspathEntry>?

        fun sourceFolderPair() = m_sourceFolderPair.computeIfAbsentAndGet { computeSourceFolderPair() }

        fun testSourceFolderPair() = m_testSourceFolderPair.computeIfAbsentAndGet {
            findTestSourceFolderPair()
        }

        fun mainTestSourceFolder() = m_mainTestSourceFolder.computeIfAbsentAndGet {
            findMainSourceFolder(testSourceFolderPair()?.first)?.file?.let { return@computeIfAbsentAndGet sourceFolderHelperInfo.findClasspathEntry(it) }
        }

        fun generatedSourceFolder() = m_generatedSourceFolder.computeIfAbsentAndGet {
            findGeneratedSourceFolder(sourceFolderPair()?.first)?.file?.let { return@computeIfAbsentAndGet sourceFolderHelperInfo.findClasspathEntry(it) }
        }

        /**
         * Finds the closest dependency pair of [SourceFolder] and [IClasspathEntry] to the given [sourceFolder]. See [findClosestSourceFolder].
         */
        protected fun findClosestSourceFolderDependency(
            sourceFolder: SourceFolder,
            test: Boolean
        ): Pair<SourceFolder, IClasspathEntry>? {
            val module = sourceFolder.contentEntry.rootModel.module
            val dependencies = ModuleRootManager.getInstance(module).dependencies

            return findClosestSourceFolder(module, dependencies.asSequence(), test)
        }

        /**
         * Finds the closest dependency pair of [SourceFolder] and [IClasspathEntry] to the given [sourceFolder]. See [findClosestSourceFolder].
         */
        protected fun findClosestDependentSourceFolder(
            sourceFolder: SourceFolder,
            test: Boolean
        ): Pair<SourceFolder, IClasspathEntry>? {
            val module = sourceFolder.contentEntry.rootModel.module
            val dependentModules = ModuleManager.getInstance(sourceFolderHelperInfo.project).getModuleDependentModules(module)

            return findClosestSourceFolder(module, dependentModules.asSequence(), test)
        }


        /**
         * Finds the closest pair of [SourceFolder] and [IClasspathEntry] to the given [reference] in the given [modules] that match the following restrictions:
         * @param test weather to look for test source folders or not
         */
        private fun findClosestSourceFolder(
            reference: Module,
            modules: Sequence<Module>,
            test: Boolean
        ): Pair<SourceFolder, IClasspathEntry>? {
            val fileIndex = ProjectFileIndex.getInstance(sourceFolderHelperInfo.project)

            val lookupName = reference.getTier(sourceFolderHelperInfo.tierOfModule)?.convert(tier, reference.name) ?: reference.name

            val sourceFoldersSorted = modules
                .flatMap {
                    ModuleRootManager.getInstance(it).sourceRoots.asSequence()
                }
                .mapNotNull { fileIndex.getSourceFolder(it) }
                .filter { it.rootType is JavaSourceRootType && it.isTestSource == test }
                .filter { !isGeneratedSourceFolder(it) }
                .map { it to Strings.levenshteinDistance(it.contentEntry.rootModel.module.name, lookupName) }
                .sortedBy { it.second }
                .map { it.first }
                .toList()
            val sourceFolder = sourceFoldersSorted.firstOrNull { isTier(it) } ?: sourceFoldersSorted.firstOrNull { isTierIncludedIn(it) } ?: return null

            return sourceFolderHelperInfo.findClasspathEntry(sourceFolder.file)?.let { sourceFolder to it }
        }

        private fun isTier(sourceFolder: SourceFolder): Boolean = tier == sourceFolder.contentEntry.rootModel.module.getTier(sourceFolderHelperInfo.tierOfModule)

        private fun isTierIncludedIn(sourceFolder: SourceFolder): Boolean = tier.isIncludedIn(sourceFolder.contentEntry.rootModel.module.getTier(sourceFolderHelperInfo.tierOfModule))

        /**
         * Finds the closest test pair of [SourceFolder] and [IClasspathEntry] in the given [project] that is a [ITier] for the given [sourceFolder].
         */
        private fun findTestSourceFolderPair(): Pair<SourceFolder, IClasspathEntry>? {
            val sourceFolder = sourceFolderPair()?.first ?: return null
            if (isTierIncludedIn(sourceFolder)) {
                findTestSourceFolder(sourceFolder)?.let { sf -> sourceFolderHelperInfo.findClasspathEntry(sf.file)?.let { sf to it } }?.let { return it }
            }
            return findClosestDependentSourceFolder(sourceFolder, true)
        }

        /**
         * Finds the src/test/java [SourceFolder] for the given [sourceFolder].
         */
        private fun findTestSourceFolder(sourceFolder: SourceFolder?): SourceFolder? = findSourceFolder(sourceFolder) { isTestSourceFolder(it) }

        /**
         * Finds the src/main/java [SourceFolder] for the given [sourceFolder].
         */
        protected fun findMainSourceFolder(sourceFolder: SourceFolder?): SourceFolder? = findSourceFolder(sourceFolder) { isMainSourceFolder(it) }

        /**
         * Finds the src/generated/java [SourceFolder] for the given [sourceFolder].
         */
        private fun findGeneratedSourceFolder(sourceFolder: SourceFolder?): SourceFolder? = findSourceFolder(sourceFolder) { isGeneratedSourceFolder(it) }

        private fun isTestSourceFolder(sourceFolder: SourceFolder?): Boolean = sourceFolderEndsWith(sourceFolder, ISourceFolders.TEST_JAVA_SOURCE_FOLDER) && sourceFolder?.rootType is JavaSourceRootType && sourceFolder.isTestSource

        private fun isMainSourceFolder(sourceFolder: SourceFolder?): Boolean = sourceFolderEndsWith(sourceFolder, ISourceFolders.MAIN_JAVA_SOURCE_FOLDER) && sourceFolder?.rootType is JavaSourceRootType && !sourceFolder.isTestSource

        private fun isGeneratedSourceFolder(sourceFolder: SourceFolder?): Boolean =
            sourceFolderEndsWith(sourceFolder, IScoutSourceFolders.GENERATED_SOURCE_FOLDER) && sourceFolder?.rootType is JavaSourceRootType && !sourceFolder.isTestSource

        private fun sourceFolderEndsWith(sourceFolder: SourceFolder?, suffix: String): Boolean {
            if (sourceFolder == null) {
                return false
            }
            return sourceFolder.file?.path?.endsWith(suffix) ?: false
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
                .mapNotNull { fileIndex.getSourceFolder(it) }
                .firstOrNull { sourceFolderCondition(it) }
        }
    }

    private class DependentSourceFolderBundle(tier: ITier<*>, referenceSourceFolderBundle: SourceFolderBundle?, sourceFolderHelperInfo: SourceFolderHelperInfo) :
        SourceFolderBundle(tier, referenceSourceFolderBundle, sourceFolderHelperInfo) {
        override fun computeSourceFolderPair(): Pair<SourceFolder, IClasspathEntry>? = referenceSourceFolderBundle?.sourceFolderPair()?.first?.let { sf -> findClosestDependentSourceFolder(sf, false) }
    }

    private class DependencySourceFolderBundle(tier: ITier<*>, referenceSourceFolderBundle: SourceFolderBundle?, sourceFolderHelperInfo: SourceFolderHelperInfo) :
        SourceFolderBundle(tier, referenceSourceFolderBundle, sourceFolderHelperInfo) {
        override fun computeSourceFolderPair(): Pair<SourceFolder, IClasspathEntry>? = referenceSourceFolderBundle?.sourceFolderPair()?.first?.let { sf -> findClosestSourceFolderDependency(sf, false) }
    }

    private class StartSourceFolderBundle(tier: ITier<*>, sourceFolderHelperInfo: SourceFolderHelperInfo) : SourceFolderBundle(tier, null, sourceFolderHelperInfo) {
        override fun computeSourceFolderPair(): Pair<SourceFolder, IClasspathEntry>? {
            val mainSourceFolder = findMainSourceFolder(sourceFolderHelperInfo.sourceFolder) ?: return null
            return sourceFolderHelperInfo.findClasspathEntry(mainSourceFolder.file)?.let { mainSourceFolder to it }
        }
    }
}