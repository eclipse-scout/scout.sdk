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
import com.intellij.openapi.roots.*
import com.intellij.openapi.vfs.VirtualFile
import junit.framework.TestCase
import org.eclipse.scout.sdk.core.ISourceFolders
import org.eclipse.scout.sdk.core.model.api.IClasspathEntry
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment
import org.eclipse.scout.sdk.core.s.IScoutSourceFolders
import org.eclipse.scout.sdk.core.s.util.ScoutTier
import org.jetbrains.jps.model.java.JavaSourceRootType
import org.mockito.ArgumentMatchers.same
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import java.nio.file.Path
import java.util.*
import java.util.stream.Stream
import kotlin.collections.HashMap

class SourceFolderHelperTest : TestCase() {

    private val m_project = mock(Project::class.java, "project")
    private val m_moduleManager = mock(ModuleManager::class.java, "moduleManager")
    private val m_projectFileIndex = mock(ProjectFileIndex::class.java, "projectFileIndex")

    private val m_findClasspathEntryMap: MutableMap<VirtualFile, IClasspathEntry> = HashMap()
    private val m_findClasspathEntry: (VirtualFile?) -> IClasspathEntry? = { m_findClasspathEntryMap[it] }

    private val m_scoutTierOfModuleMap: MutableMap<Module, ScoutTier> = HashMap()
    private val m_scoutTierOfModule: (Module) -> ScoutTier? = { m_scoutTierOfModuleMap[it] }

    // uiHtml
    private val m_uiHtmlModule = mock(Module::class.java, "uiHtmlModule")
    private val m_uiHtmlJavaEnvironment = mock(IJavaEnvironment::class.java, "uiHtmlJavaEnvironment")
    private val m_uiHtmlMainSourceFolder = mock(SourceFolder::class.java, "uiHtmlMainSourceFolder")
    private val m_uiHtmlMainClasspathEntry = mock(IClasspathEntry::class.java, "uiHtmlMainClasspathEntry")

    // client
    private val m_clientModule = mock(Module::class.java, "clientModule")
    private val m_clientJavaEnvironment = mock(IJavaEnvironment::class.java, "clientJavaEnvironment")
    private val m_clientMainSourceFolder = mock(SourceFolder::class.java, "clientMainSourceFolder")
    private val m_clientMainClasspathEntry = mock(IClasspathEntry::class.java, "clientMainClasspathEntry")

    // shared
    private val m_sharedModule = mock(Module::class.java, "sharedModule")
    private val m_sharedJavaEnvironment = mock(IJavaEnvironment::class.java, "sharedJavaEnvironment")
    private val m_sharedMainSourceFolder = mock(SourceFolder::class.java, "sharedMainSourceFolder")
    private val m_sharedMainClasspathEntry = mock(IClasspathEntry::class.java, "sharedMainClasspathEntry")
    private val m_sharedGeneratedSourceFolder = mock(SourceFolder::class.java, "sharedGeneratedSourceFolder")
    private val m_sharedGeneratedClasspathEntry = mock(IClasspathEntry::class.java, "sharedGeneratedClasspathEntry")

    // server
    private val m_serverModule = mock(Module::class.java, "serverModule")
    private val m_serverJavaEnvironment = mock(IJavaEnvironment::class.java, "serverJavaEnvironment")
    private val m_serverMainSourceFolder = mock(SourceFolder::class.java, "serverMainSourceFolder")
    private val m_serverMainClasspathEntry = mock(IClasspathEntry::class.java, "serverMainClasspathEntry")

    // client test
    private val m_clientTestModule = mock(Module::class.java, "clientTestModule")
    private val m_clientTestJavaEnvironment = mock(IJavaEnvironment::class.java, "clientTestJavaEnvironment")
    private val m_clientTestMainSourceFolder = mock(SourceFolder::class.java, "clientTestMainSourceFolder")
    private val m_clientTestMainClasspathEntry = mock(IClasspathEntry::class.java, "clientTestMainClasspathEntry")
    private val m_clientTestTestSourceFolder = mock(SourceFolder::class.java, "clientTestTestSourceFolder")
    private val m_clientTestTestClasspathEntry = mock(IClasspathEntry::class.java, "clientTestTestClasspathEntry")

    // shared test
    private val m_sharedTestModule = mock(Module::class.java, "sharedTestModule")
    private val m_sharedTestJavaEnvironment = mock(IJavaEnvironment::class.java, "sharedTestJavaEnvironment")
    private val m_sharedTestMainSourceFolder = mock(SourceFolder::class.java, "sharedTestMainSourceFolder")
    private val m_sharedTestMainClasspathEntry = mock(IClasspathEntry::class.java, "sharedTestMainClasspathEntry")
    private val m_sharedTestTestSourceFolder = mock(SourceFolder::class.java, "sharedTestTestSourceFolder")
    private val m_sharedTestTestClasspathEntry = mock(IClasspathEntry::class.java, "sharedTestTestClasspathEntry")

    // server test
    private val m_serverTestModule = mock(Module::class.java, "serverTestModule")
    private val m_serverTestJavaEnvironment = mock(IJavaEnvironment::class.java, "serverTestJavaEnvironment")
    private val m_serverTestMainSourceFolder = mock(SourceFolder::class.java, "serverTestMainSourceFolder")
    private val m_serverTestMainClasspathEntry = mock(IClasspathEntry::class.java, "serverTestMainClasspathEntry")
    private val m_serverTestTestSourceFolder = mock(SourceFolder::class.java, "serverTestTestSourceFolder")
    private val m_serverTestTestClasspathEntry = mock(IClasspathEntry::class.java, "serverTestTestClasspathEntry")

    private val m_moduleSourceRootsMap: MutableMap<Module, MutableList<VirtualFile>> = HashMap()

    init {
        initProject()
        initModules()
        initJavaEnvironments()
        initSourceFolders()
        initDependencies()
    }

    private fun initProject() {
        `when`(m_project.getService(same(ProjectFileIndex::class.java))).thenAnswer { m_projectFileIndex }
        `when`(m_project.getComponent(same(ModuleManager::class.java))).thenAnswer { m_moduleManager }
    }

    private fun initModules() {
        initModule(m_uiHtmlModule, ScoutTier.HtmlUi)
        initModule(m_clientModule, ScoutTier.Client)
        initModule(m_clientTestModule, ScoutTier.Client)
        initModule(m_sharedModule, ScoutTier.Shared)
        initModule(m_sharedTestModule, ScoutTier.Shared)
        initModule(m_serverModule, ScoutTier.Server)
        initModule(m_serverTestModule, ScoutTier.Server)
    }

    private fun initModule(m: Module, scoutTier: ScoutTier) {
        m_scoutTierOfModuleMap[m] = scoutTier
        `when`(m.project).thenAnswer { m_project }
    }

    private fun initJavaEnvironments() {
        // primarySourceFolder
        `when`(m_uiHtmlJavaEnvironment.primarySourceFolder()).thenAnswer { Optional.of(m_uiHtmlMainClasspathEntry) }
        `when`(m_clientJavaEnvironment.primarySourceFolder()).thenAnswer { Optional.of(m_clientMainClasspathEntry) }
        `when`(m_clientTestJavaEnvironment.primarySourceFolder()).thenAnswer { Optional.of(m_clientTestMainClasspathEntry) }
        `when`(m_sharedJavaEnvironment.primarySourceFolder()).thenAnswer { Optional.of(m_sharedMainClasspathEntry) }
        `when`(m_sharedTestJavaEnvironment.primarySourceFolder()).thenAnswer { Optional.of(m_sharedTestMainClasspathEntry) }
        `when`(m_serverJavaEnvironment.primarySourceFolder()).thenAnswer { Optional.of(m_serverMainClasspathEntry) }
        `when`(m_serverTestJavaEnvironment.primarySourceFolder()).thenAnswer { Optional.of(m_serverTestMainClasspathEntry) }

        // sourceFolders
        `when`(m_uiHtmlJavaEnvironment.sourceFolders()).thenAnswer { Stream.of(m_uiHtmlMainClasspathEntry) }
        `when`(m_clientJavaEnvironment.sourceFolders()).thenAnswer { Stream.of(m_clientMainClasspathEntry) }
        `when`(m_clientTestJavaEnvironment.sourceFolders()).thenAnswer { Stream.of(m_clientTestMainClasspathEntry, m_clientTestTestClasspathEntry) }
        `when`(m_sharedJavaEnvironment.sourceFolders()).thenAnswer { Stream.of(m_sharedMainClasspathEntry, m_sharedGeneratedClasspathEntry) }
        `when`(m_sharedTestJavaEnvironment.sourceFolders()).thenAnswer { Stream.of(m_sharedTestMainClasspathEntry, m_sharedTestTestClasspathEntry) }
        `when`(m_serverJavaEnvironment.sourceFolders()).thenAnswer { Stream.of(m_serverMainClasspathEntry) }
        `when`(m_serverTestJavaEnvironment.sourceFolders()).thenAnswer { Stream.of(m_serverTestMainClasspathEntry, m_serverTestTestClasspathEntry) }
    }

    private fun initSourceFolders() {
        // uiHtml
        initSourceFolder(m_uiHtmlMainSourceFolder, m_uiHtmlModule, m_uiHtmlMainClasspathEntry, m_uiHtmlJavaEnvironment, false, ISourceFolders.MAIN_JAVA_SOURCE_FOLDER)

        // client
        initSourceFolder(m_clientMainSourceFolder, m_clientModule, m_clientMainClasspathEntry, m_clientJavaEnvironment, false, ISourceFolders.MAIN_JAVA_SOURCE_FOLDER)

        // shared
        initSourceFolder(m_sharedGeneratedSourceFolder, m_sharedModule, m_sharedGeneratedClasspathEntry, m_sharedJavaEnvironment, false, IScoutSourceFolders.GENERATED_SOURCE_FOLDER)
        initSourceFolder(m_sharedMainSourceFolder, m_sharedModule, m_sharedMainClasspathEntry, m_sharedJavaEnvironment, false, ISourceFolders.MAIN_JAVA_SOURCE_FOLDER)

        // server
        initSourceFolder(m_serverMainSourceFolder, m_serverModule, m_serverMainClasspathEntry, m_serverJavaEnvironment, false, ISourceFolders.MAIN_JAVA_SOURCE_FOLDER)

        // client test
        initSourceFolder(m_clientTestMainSourceFolder, m_clientTestModule, m_clientTestMainClasspathEntry, m_clientTestJavaEnvironment, false, ISourceFolders.MAIN_JAVA_SOURCE_FOLDER)
        initSourceFolder(m_clientTestTestSourceFolder, m_clientTestModule, m_clientTestTestClasspathEntry, m_clientTestJavaEnvironment, true, ISourceFolders.TEST_JAVA_SOURCE_FOLDER)

        // shared test
        initSourceFolder(m_sharedTestMainSourceFolder, m_sharedTestModule, m_sharedTestMainClasspathEntry, m_sharedTestJavaEnvironment, false, ISourceFolders.MAIN_JAVA_SOURCE_FOLDER)
        initSourceFolder(m_sharedTestTestSourceFolder, m_sharedTestModule, m_sharedTestTestClasspathEntry, m_sharedTestJavaEnvironment, true, ISourceFolders.TEST_JAVA_SOURCE_FOLDER)

        // server test
        initSourceFolder(m_serverTestMainSourceFolder, m_serverTestModule, m_serverTestMainClasspathEntry, m_serverTestJavaEnvironment, false, ISourceFolders.MAIN_JAVA_SOURCE_FOLDER)
        initSourceFolder(m_serverTestTestSourceFolder, m_serverTestModule, m_serverTestTestClasspathEntry, m_serverTestJavaEnvironment, true, ISourceFolders.TEST_JAVA_SOURCE_FOLDER)
    }

    private fun initSourceFolder(sf: SourceFolder, m: Module, cpe: IClasspathEntry, je: IJavaEnvironment, test: Boolean, pathSuffix: String) {
        `when`(sf.rootType).thenAnswer { if (test) JavaSourceRootType.TEST_SOURCE else JavaSourceRootType.SOURCE }
        `when`(sf.isTestSource).thenAnswer { test }

        val ce: ContentEntry = mock(ContentEntry::class.java, "$sf - ContentEntry")
        `when`(sf.contentEntry).thenAnswer { ce }

        val rm: ModuleRootModel = mock(ModuleRootModel::class.java, "$sf - ModuleRootModel")
        `when`(ce.rootModel).thenAnswer { rm }
        `when`(rm.module).thenAnswer { m }

        val mrm: ModuleRootManager = mock(ModuleRootManager::class.java, "$m - ModuleRootManager")
        `when`(m.getComponent(same(ModuleRootManager::class.java))).thenAnswer { mrm }
        `when`(mrm.sourceRoots).thenAnswer { m_moduleSourceRootsMap[m]?.toTypedArray() }

        val vf = mock(VirtualFile::class.java, "$sf - VirtualFile")
        `when`(vf.path).thenAnswer { "$m/$pathSuffix" }
        m_moduleSourceRootsMap.computeIfAbsent(m) { ArrayList() }.add(vf)
        `when`(m_projectFileIndex.getSourceFolder(same(vf))).thenAnswer { sf }
        `when`(sf.file).thenAnswer { vf }

        m_findClasspathEntryMap[vf] = cpe

        `when`(cpe.javaEnvironment()).thenAnswer { je }
        `when`(cpe.path()).thenAnswer { Path.of(cpe.toString(), pathSuffix) }
    }

    private fun initDependencies() {
        // uiHtml
        `when`(ModuleRootManager.getInstance(m_uiHtmlModule).dependencies).thenAnswer {
            arrayOf(m_clientModule)
        }

        // client
        `when`(ModuleRootManager.getInstance(m_clientModule).dependencies).thenAnswer {
            arrayOf(m_sharedModule)
        }
        `when`(m_moduleManager.getModuleDependentModules(same(m_clientModule))).thenAnswer {
            listOf(m_uiHtmlModule, m_clientTestModule)
        }

        // shared
        `when`(m_moduleManager.getModuleDependentModules(same(m_sharedModule))).thenAnswer {
            listOf(m_clientModule, m_serverModule, m_sharedTestModule)
        }

        // server
        `when`(ModuleRootManager.getInstance(m_serverModule).dependencies).thenAnswer {
            arrayOf(m_sharedModule)
        }
        `when`(m_moduleManager.getModuleDependentModules(same(m_serverModule))).thenAnswer {
            listOf(m_serverTestModule)
        }

        // client test
        `when`(ModuleRootManager.getInstance(m_clientTestModule).dependencies).thenAnswer {
            arrayOf(m_clientModule, m_sharedTestModule)
        }

        // shared test
        `when`(ModuleRootManager.getInstance(m_sharedTestModule).dependencies).thenAnswer {
            arrayOf(m_sharedModule)
        }
        `when`(m_moduleManager.getModuleDependentModules(same(m_sharedTestModule))).thenAnswer {
            listOf(m_clientTestModule, m_serverTestModule)
        }

        // server test
        `when`(ModuleRootManager.getInstance(m_serverTestModule).dependencies).thenAnswer {
            arrayOf(m_serverModule, m_sharedTestModule)
        }
    }

    fun testFindClosestSourceFolderDependency() {
        assertPairMatches(m_clientMainSourceFolder to m_clientMainClasspathEntry,
                SourceFolderHelper.findClosestSourceFolderDependency(m_project, ScoutTier.Client, false, m_uiHtmlMainSourceFolder, m_scoutTierOfModule, m_findClasspathEntry))
        assertPairMatches(m_sharedMainSourceFolder to m_sharedMainClasspathEntry,
                SourceFolderHelper.findClosestSourceFolderDependency(m_project, ScoutTier.Shared, false, m_clientMainSourceFolder, m_scoutTierOfModule, m_findClasspathEntry))
        assertPairMatches(m_serverMainSourceFolder to m_serverMainClasspathEntry,
                SourceFolderHelper.findClosestSourceFolderDependency(m_project, ScoutTier.Server, false, m_serverTestTestSourceFolder, m_scoutTierOfModule, m_findClasspathEntry))
        assertPairMatches(m_sharedTestTestSourceFolder to m_sharedTestTestClasspathEntry,
                SourceFolderHelper.findClosestSourceFolderDependency(m_project, ScoutTier.Shared, true, m_serverTestTestSourceFolder, m_scoutTierOfModule, m_findClasspathEntry))
    }

    fun testFindClosestDependentSourceFolder() {
        assertPairMatches(m_uiHtmlMainSourceFolder to m_uiHtmlMainClasspathEntry,
                SourceFolderHelper.findClosestDependentSourceFolder(m_project, ScoutTier.HtmlUi, false, m_clientMainSourceFolder, m_scoutTierOfModule, m_findClasspathEntry))
        assertPairMatches(m_clientMainSourceFolder to m_clientMainClasspathEntry,
                SourceFolderHelper.findClosestDependentSourceFolder(m_project, ScoutTier.Client, false, m_sharedMainSourceFolder, m_scoutTierOfModule, m_findClasspathEntry))
        assertPairMatches(m_serverTestMainSourceFolder to m_serverTestMainClasspathEntry,
                SourceFolderHelper.findClosestDependentSourceFolder(m_project, ScoutTier.Server, false, m_serverMainSourceFolder, m_scoutTierOfModule, m_findClasspathEntry))
        assertPairMatches(m_serverTestTestSourceFolder to m_serverTestTestClasspathEntry,
                SourceFolderHelper.findClosestDependentSourceFolder(m_project, ScoutTier.Server, true, m_serverMainSourceFolder, m_scoutTierOfModule, m_findClasspathEntry))
    }

    fun testStartFromSourceFolder() {
        assertSourceFolderHelper(SourceFolderHelper(m_project, m_clientMainSourceFolder, m_scoutTierOfModule, m_findClasspathEntry), m_clientMainClasspathEntry, ScoutTier.Client)
        assertSourceFolderHelper(SourceFolderHelper(m_project, m_clientTestMainSourceFolder, m_scoutTierOfModule, m_findClasspathEntry), m_clientTestMainClasspathEntry, ScoutTier.Client, true)

        assertSourceFolderHelper(SourceFolderHelper(m_project, m_sharedMainSourceFolder, m_scoutTierOfModule, m_findClasspathEntry), m_sharedMainClasspathEntry, ScoutTier.Shared)
        assertSourceFolderHelper(SourceFolderHelper(m_project, m_sharedGeneratedSourceFolder, m_scoutTierOfModule, m_findClasspathEntry), m_sharedGeneratedClasspathEntry, ScoutTier.Shared)
        assertSourceFolderHelper(SourceFolderHelper(m_project, m_sharedTestMainSourceFolder, m_scoutTierOfModule, m_findClasspathEntry), m_sharedTestMainClasspathEntry, ScoutTier.Shared, true)

        assertSourceFolderHelper(SourceFolderHelper(m_project, m_serverMainSourceFolder, m_scoutTierOfModule, m_findClasspathEntry), m_serverMainClasspathEntry, ScoutTier.Server)
        assertSourceFolderHelper(SourceFolderHelper(m_project, m_serverTestMainSourceFolder, m_scoutTierOfModule, m_findClasspathEntry), m_serverTestMainClasspathEntry, ScoutTier.Server, true)
    }

    private fun <K, V> assertPairMatches(expectedPair: Pair<K, V>, actualPair: Pair<K, V>?) {
        assertNotNull(actualPair)
        assertEquals(expectedPair.first, actualPair?.first)
        assertEquals(expectedPair.second, actualPair?.second)
    }

    private fun assertSourceFolderHelper(sourceFolderHelper: SourceFolderHelper, classpathEntry: IClasspathEntry, scoutTier: ScoutTier, startInTestModule: Boolean = false) {
        assertEquals(classpathEntry, sourceFolderHelper.classpathEntry())
        assertEquals(scoutTier, sourceFolderHelper.tier())

        assertEquals(if (startInTestModule) m_clientTestMainClasspathEntry else m_clientMainClasspathEntry, sourceFolderHelper.clientSourceFolder())
        assertEquals(m_clientTestTestClasspathEntry, sourceFolderHelper.clientTestSourceFolder())
        assertEquals(m_clientTestMainClasspathEntry, sourceFolderHelper.clientMainTestSourceFolder())

        assertEquals(if (startInTestModule) m_sharedTestMainClasspathEntry else m_sharedMainClasspathEntry, sourceFolderHelper.sharedSourceFolder())
        assertEquals(if (startInTestModule) null else m_sharedGeneratedClasspathEntry, sourceFolderHelper.sharedGeneratedSourceFolder())
        assertEquals(m_sharedTestTestClasspathEntry, sourceFolderHelper.sharedTestSourceFolder())
        assertEquals(m_sharedTestMainClasspathEntry, sourceFolderHelper.sharedMainTestSourceFolder())

        assertEquals(if (startInTestModule) m_serverTestMainClasspathEntry else m_serverMainClasspathEntry, sourceFolderHelper.serverSourceFolder())
        assertEquals(m_serverTestTestClasspathEntry, sourceFolderHelper.serverTestSourceFolder())
        assertEquals(m_serverTestMainClasspathEntry, sourceFolderHelper.serverMainTestSourceFolder())
    }
}