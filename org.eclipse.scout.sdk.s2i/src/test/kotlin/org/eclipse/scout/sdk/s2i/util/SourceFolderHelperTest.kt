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

    // sub client
    private val m_subClientModule = mock(Module::class.java, "subClientModule")
    private val m_subClientJavaEnvironment = mock(IJavaEnvironment::class.java, "subClientJavaEnvironment")
    private val m_subClientMainSourceFolder = mock(SourceFolder::class.java, "subClientMainSourceFolder")
    private val m_subClientMainClasspathEntry = mock(IClasspathEntry::class.java, "subClientMainClasspathEntry")
    private val m_subClientTestSourceFolder = mock(SourceFolder::class.java, "subClientTestSourceFolder")
    private val m_subClientTestClasspathEntry = mock(IClasspathEntry::class.java, "subClientTestClasspathEntry")

    // sub shared
    private val m_subSharedModule = mock(Module::class.java, "subSharedModule")
    private val m_subSharedJavaEnvironment = mock(IJavaEnvironment::class.java, "subSharedJavaEnvironment")
    private val m_subSharedMainSourceFolder = mock(SourceFolder::class.java, "subSharedMainSourceFolder")
    private val m_subSharedMainClasspathEntry = mock(IClasspathEntry::class.java, "subSharedMainClasspathEntry")
    private val m_subSharedTestSourceFolder = mock(SourceFolder::class.java, "subSharedTestSourceFolder")
    private val m_subSharedTestClasspathEntry = mock(IClasspathEntry::class.java, "subSharedTestClasspathEntry")
    private val m_subSharedGeneratedSourceFolder = mock(SourceFolder::class.java, "subSharedGeneratedSourceFolder")
    private val m_subSharedGeneratedClasspathEntry = mock(IClasspathEntry::class.java, "subSharedGeneratedClasspathEntry")

    // sub server
    private val m_subServerModule = mock(Module::class.java, "subServerModule")
    private val m_subServerJavaEnvironment = mock(IJavaEnvironment::class.java, "subServerJavaEnvironment")
    private val m_subServerMainSourceFolder = mock(SourceFolder::class.java, "subServerMainSourceFolder")
    private val m_subServerMainClasspathEntry = mock(IClasspathEntry::class.java, "subServerMainClasspathEntry")
    private val m_subServerTestSourceFolder = mock(SourceFolder::class.java, "subServerTestSourceFolder")
    private val m_subServerTestClasspathEntry = mock(IClasspathEntry::class.java, "subServerTestClasspathEntry")

    // sub sub client
    private val m_subSubClientModule = mock(Module::class.java, "subSubClientModule")
    private val m_subSubClientJavaEnvironment = mock(IJavaEnvironment::class.java, "subSubClientJavaEnvironment")
    private val m_subSubClientMainSourceFolder = mock(SourceFolder::class.java, "subSubClientMainSourceFolder")
    private val m_subSubClientMainClasspathEntry = mock(IClasspathEntry::class.java, "subSubClientMainClasspathEntry")
    private val m_subSubClientTestSourceFolder = mock(SourceFolder::class.java, "subSubClientTestSourceFolder")
    private val m_subSubClientTestClasspathEntry = mock(IClasspathEntry::class.java, "subSubClientTestClasspathEntry")

    // sub sub shared
    private val m_subSubSharedModule = mock(Module::class.java, "subSubSharedModule")
    private val m_subSubSharedJavaEnvironment = mock(IJavaEnvironment::class.java, "subSubSharedJavaEnvironment")
    private val m_subSubSharedMainSourceFolder = mock(SourceFolder::class.java, "subSubSharedMainSourceFolder")
    private val m_subSubSharedMainClasspathEntry = mock(IClasspathEntry::class.java, "subSubSharedMainClasspathEntry")
    private val m_subSubSharedTestSourceFolder = mock(SourceFolder::class.java, "subSubSharedTestSourceFolder")
    private val m_subSubSharedTestClasspathEntry = mock(IClasspathEntry::class.java, "subSubSharedTestClasspathEntry")

    // sub sub server
    private val m_subSubServerModule = mock(Module::class.java, "subSubServerModule")
    private val m_subSubServerJavaEnvironment = mock(IJavaEnvironment::class.java, "subSubServerJavaEnvironment")
    private val m_subSubServerMainSourceFolder = mock(SourceFolder::class.java, "subSubServerMainSourceFolder")
    private val m_subSubServerMainClasspathEntry = mock(IClasspathEntry::class.java, "subSubServerMainClasspathEntry")
    private val m_subSubServerTestSourceFolder = mock(SourceFolder::class.java, "subSubServerTestSourceFolder")
    private val m_subSubServerTestClasspathEntry = mock(IClasspathEntry::class.java, "subSubServerTestClasspathEntry")

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
        initModule(m_uiHtmlModule, ScoutTier.HtmlUi, "source.folder.helper.ui.html")
        initModule(m_clientModule, ScoutTier.Client, "source.folder.helper.client")
        initModule(m_sharedModule, ScoutTier.Shared, "source.folder.helper.shared")
        initModule(m_serverModule, ScoutTier.Server, "source.folder.helper.server")

        initModule(m_clientTestModule, ScoutTier.Client, "source.folder.helper.client.test")
        initModule(m_sharedTestModule, ScoutTier.Shared, "source.folder.helper.shared.test")
        initModule(m_serverTestModule, ScoutTier.Server, "source.folder.helper.server.test")

        initModule(m_subClientModule, ScoutTier.Client, "source.folder.helper.submodule.client")
        initModule(m_subSharedModule, ScoutTier.Shared, "source.folder.helper.submodule.shared")
        initModule(m_subServerModule, ScoutTier.Server, "source.folder.helper.submodule.server")

        initModule(m_subSubClientModule, ScoutTier.Client, "source.folder.helper.submodule.specific.client")
        initModule(m_subSubSharedModule, ScoutTier.Shared, "source.folder.helper.submodule.specific.shared")
        initModule(m_subSubServerModule, ScoutTier.Server, "source.folder.helper.submodule.specific.server")
    }

    private fun initModule(m: Module, scoutTier: ScoutTier, name: String) {
        m_scoutTierOfModuleMap[m] = scoutTier
        `when`(m.project).thenAnswer { m_project }
        `when`(m.name).thenAnswer { name }
    }

    private fun initJavaEnvironments() {
        // primarySourceFolder
        `when`(m_uiHtmlJavaEnvironment.primarySourceFolder()).thenAnswer { Optional.of(m_uiHtmlMainClasspathEntry) }
        `when`(m_clientJavaEnvironment.primarySourceFolder()).thenAnswer { Optional.of(m_clientMainClasspathEntry) }
        `when`(m_sharedJavaEnvironment.primarySourceFolder()).thenAnswer { Optional.of(m_sharedMainClasspathEntry) }
        `when`(m_serverJavaEnvironment.primarySourceFolder()).thenAnswer { Optional.of(m_serverMainClasspathEntry) }

        `when`(m_clientTestJavaEnvironment.primarySourceFolder()).thenAnswer { Optional.of(m_clientTestMainClasspathEntry) }
        `when`(m_sharedTestJavaEnvironment.primarySourceFolder()).thenAnswer { Optional.of(m_sharedTestMainClasspathEntry) }
        `when`(m_serverTestJavaEnvironment.primarySourceFolder()).thenAnswer { Optional.of(m_serverTestMainClasspathEntry) }

        `when`(m_subClientJavaEnvironment.primarySourceFolder()).thenAnswer { Optional.of(m_subClientMainClasspathEntry) }
        `when`(m_subSharedJavaEnvironment.primarySourceFolder()).thenAnswer { Optional.of(m_subSharedMainClasspathEntry) }
        `when`(m_subServerJavaEnvironment.primarySourceFolder()).thenAnswer { Optional.of(m_subServerMainClasspathEntry) }

        `when`(m_subSubClientJavaEnvironment.primarySourceFolder()).thenAnswer { Optional.of(m_subSubClientMainClasspathEntry) }
        `when`(m_subSubSharedJavaEnvironment.primarySourceFolder()).thenAnswer { Optional.of(m_subSubSharedMainClasspathEntry) }
        `when`(m_subSubServerJavaEnvironment.primarySourceFolder()).thenAnswer { Optional.of(m_subSubServerMainClasspathEntry) }

        // sourceFolders
        `when`(m_uiHtmlJavaEnvironment.sourceFolders()).thenAnswer {
            Stream.of(m_uiHtmlMainClasspathEntry,
                    m_clientMainClasspathEntry,
                    m_sharedMainClasspathEntry, m_sharedGeneratedClasspathEntry)
        }
        `when`(m_clientJavaEnvironment.sourceFolders()).thenAnswer {
            Stream.of(m_clientMainClasspathEntry,
                    m_sharedMainClasspathEntry, m_sharedGeneratedClasspathEntry)
        }
        `when`(m_sharedJavaEnvironment.sourceFolders()).thenAnswer {
            Stream.of(m_sharedMainClasspathEntry, m_sharedGeneratedClasspathEntry)
        }
        `when`(m_serverJavaEnvironment.sourceFolders()).thenAnswer {
            Stream.of(m_serverMainClasspathEntry,
                    m_sharedMainClasspathEntry, m_sharedGeneratedClasspathEntry)
        }

        `when`(m_clientTestJavaEnvironment.sourceFolders()).thenAnswer {
            Stream.of(m_clientTestMainClasspathEntry, m_clientTestTestClasspathEntry,
                    m_clientMainClasspathEntry,
                    m_sharedMainClasspathEntry, m_sharedGeneratedClasspathEntry)
        }
        `when`(m_sharedTestJavaEnvironment.sourceFolders()).thenAnswer {
            Stream.of(m_sharedTestMainClasspathEntry, m_sharedTestTestClasspathEntry,
                    m_sharedMainClasspathEntry, m_sharedGeneratedClasspathEntry)
        }
        `when`(m_serverTestJavaEnvironment.sourceFolders()).thenAnswer {
            Stream.of(m_serverTestMainClasspathEntry, m_serverTestTestClasspathEntry,
                    m_serverMainClasspathEntry,
                    m_sharedMainClasspathEntry, m_sharedGeneratedClasspathEntry)
        }

        `when`(m_subClientJavaEnvironment.sourceFolders()).thenAnswer {
            Stream.of(m_subClientMainClasspathEntry, m_subClientTestClasspathEntry,
                    m_subSharedMainClasspathEntry, m_subSharedGeneratedClasspathEntry, m_subSharedTestClasspathEntry,
                    m_clientMainClasspathEntry,
                    m_sharedMainClasspathEntry, m_sharedGeneratedClasspathEntry)
        }
        `when`(m_subSharedJavaEnvironment.sourceFolders()).thenAnswer {
            Stream.of(m_subSharedMainClasspathEntry, m_subSharedGeneratedClasspathEntry, m_subSharedTestClasspathEntry,
                    m_sharedMainClasspathEntry, m_sharedGeneratedClasspathEntry)
        }
        `when`(m_subServerJavaEnvironment.sourceFolders()).thenAnswer {
            Stream.of(m_subServerMainClasspathEntry, m_subServerTestClasspathEntry,
                    m_subSharedMainClasspathEntry, m_subSharedGeneratedClasspathEntry, m_subSharedTestClasspathEntry,
                    m_serverMainClasspathEntry,
                    m_sharedMainClasspathEntry, m_sharedGeneratedClasspathEntry)
        }

        `when`(m_subSubClientJavaEnvironment.sourceFolders()).thenAnswer {
            Stream.of(m_subSubClientMainClasspathEntry, m_subSubClientTestClasspathEntry,
                    m_subSubSharedMainClasspathEntry, m_subSubSharedTestClasspathEntry,
                    m_subClientMainClasspathEntry, m_subClientTestClasspathEntry,
                    m_subSharedMainClasspathEntry, m_subSharedGeneratedClasspathEntry, m_subSharedTestClasspathEntry,
                    m_clientMainClasspathEntry,
                    m_sharedMainClasspathEntry, m_sharedGeneratedClasspathEntry)
        }
        `when`(m_subSubSharedJavaEnvironment.sourceFolders()).thenAnswer {
            Stream.of(m_subSubSharedMainClasspathEntry, m_subSubSharedTestClasspathEntry,
                    m_subSharedMainClasspathEntry, m_subSharedGeneratedClasspathEntry, m_subSharedTestClasspathEntry,
                    m_sharedMainClasspathEntry, m_sharedGeneratedClasspathEntry)
        }
        `when`(m_subSubServerJavaEnvironment.sourceFolders()).thenAnswer {
            Stream.of(m_subSubServerMainClasspathEntry, m_subSubServerTestClasspathEntry,
                    m_subSubSharedMainClasspathEntry, m_subSubSharedTestClasspathEntry,
                    m_subServerMainClasspathEntry, m_subServerTestClasspathEntry,
                    m_subSharedMainClasspathEntry, m_subSharedGeneratedClasspathEntry, m_subSharedTestClasspathEntry,
                    m_serverMainClasspathEntry,
                    m_sharedMainClasspathEntry, m_sharedGeneratedClasspathEntry)
        }
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

        // sub client
        initSourceFolder(m_subClientMainSourceFolder, m_subClientModule, m_subClientMainClasspathEntry, m_subClientJavaEnvironment, false, ISourceFolders.MAIN_JAVA_SOURCE_FOLDER)
        initSourceFolder(m_subClientTestSourceFolder, m_subClientModule, m_subClientTestClasspathEntry, m_subClientJavaEnvironment, true, ISourceFolders.TEST_JAVA_SOURCE_FOLDER)

        // sub shared
        initSourceFolder(m_subSharedGeneratedSourceFolder, m_subSharedModule, m_subSharedGeneratedClasspathEntry, m_subSharedJavaEnvironment, false, IScoutSourceFolders.GENERATED_SOURCE_FOLDER)
        initSourceFolder(m_subSharedMainSourceFolder, m_subSharedModule, m_subSharedMainClasspathEntry, m_subSharedJavaEnvironment, false, ISourceFolders.MAIN_JAVA_SOURCE_FOLDER)
        initSourceFolder(m_subSharedTestSourceFolder, m_subSharedModule, m_subSharedTestClasspathEntry, m_subSharedJavaEnvironment, true, ISourceFolders.TEST_JAVA_SOURCE_FOLDER)

        // sub server
        initSourceFolder(m_subServerMainSourceFolder, m_subServerModule, m_subServerMainClasspathEntry, m_subServerJavaEnvironment, false, ISourceFolders.MAIN_JAVA_SOURCE_FOLDER)
        initSourceFolder(m_subServerTestSourceFolder, m_subServerModule, m_subServerTestClasspathEntry, m_subServerJavaEnvironment, true, ISourceFolders.TEST_JAVA_SOURCE_FOLDER)

        // sub sub client
        initSourceFolder(m_subSubClientMainSourceFolder, m_subSubClientModule, m_subSubClientMainClasspathEntry, m_subSubClientJavaEnvironment, false, ISourceFolders.MAIN_JAVA_SOURCE_FOLDER)
        initSourceFolder(m_subSubClientTestSourceFolder, m_subSubClientModule, m_subSubClientTestClasspathEntry, m_subSubClientJavaEnvironment, true, ISourceFolders.TEST_JAVA_SOURCE_FOLDER)

        // sub sub shared
        initSourceFolder(m_subSubSharedMainSourceFolder, m_subSubSharedModule, m_subSubSharedMainClasspathEntry, m_subSubSharedJavaEnvironment, false, ISourceFolders.MAIN_JAVA_SOURCE_FOLDER)
        initSourceFolder(m_subSubSharedTestSourceFolder, m_subSubSharedModule, m_subSubSharedTestClasspathEntry, m_subSubSharedJavaEnvironment, true, ISourceFolders.TEST_JAVA_SOURCE_FOLDER)

        // sub sub server
        initSourceFolder(m_subSubServerMainSourceFolder, m_subSubServerModule, m_subSubServerMainClasspathEntry, m_subSubServerJavaEnvironment, false, ISourceFolders.MAIN_JAVA_SOURCE_FOLDER)
        initSourceFolder(m_subSubServerTestSourceFolder, m_subSubServerModule, m_subSubServerTestClasspathEntry, m_subSubServerJavaEnvironment, true, ISourceFolders.TEST_JAVA_SOURCE_FOLDER)
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
            arrayOf(m_clientModule, m_sharedModule)
        }

        // client
        `when`(ModuleRootManager.getInstance(m_clientModule).dependencies).thenAnswer {
            arrayOf(m_sharedModule)
        }
        `when`(m_moduleManager.getModuleDependentModules(same(m_clientModule))).thenAnswer {
            listOf(m_uiHtmlModule,
                    m_clientTestModule,
                    m_subClientModule,
                    m_subSubClientModule)
        }

        // shared
        `when`(m_moduleManager.getModuleDependentModules(same(m_sharedModule))).thenAnswer {
            listOf(m_uiHtmlModule, m_clientModule, m_serverModule,
                    m_clientTestModule, m_sharedTestModule, m_serverTestModule,
                    m_subClientModule, m_subSharedModule, m_subServerModule,
                    m_subSubClientModule, m_subSubSharedModule, m_subSubServerModule)
        }

        // server
        `when`(ModuleRootManager.getInstance(m_serverModule).dependencies).thenAnswer {
            arrayOf(m_sharedModule)
        }
        `when`(m_moduleManager.getModuleDependentModules(same(m_serverModule))).thenAnswer {
            listOf(m_serverTestModule,
                    m_subServerModule,
                    m_subSubServerModule)
        }

        // client test
        `when`(ModuleRootManager.getInstance(m_clientTestModule).dependencies).thenAnswer {
            arrayOf(m_clientModule, m_sharedModule,
                    m_sharedTestModule)
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
            arrayOf(m_sharedModule, m_serverModule,
                    m_sharedTestModule)
        }

        // sub client
        `when`(ModuleRootManager.getInstance(m_subClientModule).dependencies).thenAnswer {
            arrayOf(m_clientModule, m_sharedModule,
                    m_subSharedModule)
        }
        `when`(m_moduleManager.getModuleDependentModules(same(m_subClientModule))).thenAnswer {
            listOf(m_subSubClientModule)
        }

        // sub shared
        `when`(ModuleRootManager.getInstance(m_subSharedModule).dependencies).thenAnswer {
            arrayOf(m_sharedModule)
        }
        `when`(m_moduleManager.getModuleDependentModules(same(m_subSharedModule))).thenAnswer {
            listOf(m_subClientModule, m_subServerModule,
                    m_subSubClientModule, m_subSubSharedModule, m_subSubServerModule)
        }

        // sub server
        `when`(ModuleRootManager.getInstance(m_subServerModule).dependencies).thenAnswer {
            arrayOf(m_sharedModule,
                    m_subSharedModule)
        }
        `when`(m_moduleManager.getModuleDependentModules(same(m_subServerModule))).thenAnswer {
            listOf(m_subSubServerModule)
        }

        // sub sub client
        `when`(ModuleRootManager.getInstance(m_subSubClientModule).dependencies).thenAnswer {
            arrayOf(m_clientModule, m_sharedModule,
                    m_subClientModule, m_subSharedModule,
                    m_subSubSharedModule)
        }

        // sub sub shared
        `when`(ModuleRootManager.getInstance(m_subSubSharedModule).dependencies).thenAnswer {
            arrayOf(m_sharedModule,
                    m_subSharedModule,
                    m_subSubSharedModule)
        }
        `when`(m_moduleManager.getModuleDependentModules(same(m_subSubSharedModule))).thenAnswer {
            listOf(m_subSubClientModule, m_subSubServerModule)
        }

        // sub sub server
        `when`(ModuleRootManager.getInstance(m_subSubServerModule).dependencies).thenAnswer {
            arrayOf(m_sharedModule, m_serverModule,
                    m_subSharedModule, m_subServerModule,
                    m_subSubSharedModule)
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
        assertPairMatches(m_clientMainSourceFolder to m_clientMainClasspathEntry,
                SourceFolderHelper.findClosestSourceFolderDependency(m_project, ScoutTier.Client, false, m_subClientMainSourceFolder, m_scoutTierOfModule, m_findClasspathEntry))
        assertPairMatches(m_subSubSharedMainSourceFolder to m_subSubSharedMainClasspathEntry,
                SourceFolderHelper.findClosestSourceFolderDependency(m_project, ScoutTier.Shared, false, m_subSubServerMainSourceFolder, m_scoutTierOfModule, m_findClasspathEntry))
        assertPairMatches(m_subServerTestSourceFolder to m_subServerTestClasspathEntry,
                SourceFolderHelper.findClosestSourceFolderDependency(m_project, ScoutTier.Server, true, m_subSubServerMainSourceFolder, m_scoutTierOfModule, m_findClasspathEntry))
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
        assertPairMatches(m_subSubClientMainSourceFolder to m_subSubClientMainClasspathEntry,
                SourceFolderHelper.findClosestDependentSourceFolder(m_project, ScoutTier.Client, false, m_subClientMainSourceFolder, m_scoutTierOfModule, m_findClasspathEntry))
        assertPairMatches(m_subSubServerMainSourceFolder to m_subSubServerMainClasspathEntry,
                SourceFolderHelper.findClosestDependentSourceFolder(m_project, ScoutTier.Server, false, m_subSubSharedMainSourceFolder, m_scoutTierOfModule, m_findClasspathEntry))
        assertPairMatches(m_subSubServerTestSourceFolder to m_subSubServerTestClasspathEntry,
                SourceFolderHelper.findClosestDependentSourceFolder(m_project, ScoutTier.Server, true, m_subServerMainSourceFolder, m_scoutTierOfModule, m_findClasspathEntry))
    }

    fun testStartFromSourceFolder() {
        assertSourceFolderHelper(SourceFolderHelper(m_project, m_clientMainSourceFolder, m_scoutTierOfModule, m_findClasspathEntry), m_clientMainClasspathEntry, ScoutTier.Client)
        assertSourceFolderHelper(SourceFolderHelper(m_project, m_sharedMainSourceFolder, m_scoutTierOfModule, m_findClasspathEntry), m_sharedMainClasspathEntry, ScoutTier.Shared)
        assertSourceFolderHelper(SourceFolderHelper(m_project, m_sharedGeneratedSourceFolder, m_scoutTierOfModule, m_findClasspathEntry), m_sharedGeneratedClasspathEntry, ScoutTier.Shared)
        assertSourceFolderHelper(SourceFolderHelper(m_project, m_serverMainSourceFolder, m_scoutTierOfModule, m_findClasspathEntry), m_serverMainClasspathEntry, ScoutTier.Server)

        assertSourceFolderHelper(SourceFolderHelper(m_project, m_clientTestMainSourceFolder, m_scoutTierOfModule, m_findClasspathEntry), m_clientTestMainClasspathEntry, ScoutTier.Client, true)
        assertSourceFolderHelper(SourceFolderHelper(m_project, m_sharedTestMainSourceFolder, m_scoutTierOfModule, m_findClasspathEntry), m_sharedTestMainClasspathEntry, ScoutTier.Shared, true)
        assertSourceFolderHelper(SourceFolderHelper(m_project, m_serverTestMainSourceFolder, m_scoutTierOfModule, m_findClasspathEntry), m_serverTestMainClasspathEntry, ScoutTier.Server, true)

        assertSourceFolderHelperSub(SourceFolderHelper(m_project, m_subClientMainSourceFolder, m_scoutTierOfModule, m_findClasspathEntry), m_subClientMainClasspathEntry, ScoutTier.Client)
        assertSourceFolderHelperSub(SourceFolderHelper(m_project, m_subSharedMainSourceFolder, m_scoutTierOfModule, m_findClasspathEntry), m_subSharedMainClasspathEntry, ScoutTier.Shared)
        assertSourceFolderHelperSub(SourceFolderHelper(m_project, m_subSharedGeneratedSourceFolder, m_scoutTierOfModule, m_findClasspathEntry), m_subSharedGeneratedClasspathEntry, ScoutTier.Shared)
        assertSourceFolderHelperSub(SourceFolderHelper(m_project, m_subServerMainSourceFolder, m_scoutTierOfModule, m_findClasspathEntry), m_subServerMainClasspathEntry, ScoutTier.Server)

        assertSourceFolderHelperSubSub(SourceFolderHelper(m_project, m_subSubClientMainSourceFolder, m_scoutTierOfModule, m_findClasspathEntry), m_subSubClientMainClasspathEntry, ScoutTier.Client)
        assertSourceFolderHelperSubSub(SourceFolderHelper(m_project, m_subSubSharedMainSourceFolder, m_scoutTierOfModule, m_findClasspathEntry), m_subSubSharedMainClasspathEntry, ScoutTier.Shared)
        assertSourceFolderHelperSubSub(SourceFolderHelper(m_project, m_subSubServerMainSourceFolder, m_scoutTierOfModule, m_findClasspathEntry), m_subSubServerMainClasspathEntry, ScoutTier.Server)
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

    private fun assertSourceFolderHelperSub(sourceFolderHelper: SourceFolderHelper, classpathEntry: IClasspathEntry, scoutTier: ScoutTier) {
        assertEquals(classpathEntry, sourceFolderHelper.classpathEntry())
        assertEquals(scoutTier, sourceFolderHelper.tier())

        assertEquals(m_subClientMainClasspathEntry, sourceFolderHelper.clientSourceFolder())
        assertEquals(m_subClientTestClasspathEntry, sourceFolderHelper.clientTestSourceFolder())
        assertEquals(m_subClientMainClasspathEntry, sourceFolderHelper.clientMainTestSourceFolder())

        assertEquals(m_subSharedMainClasspathEntry, sourceFolderHelper.sharedSourceFolder())
        assertEquals(m_subSharedGeneratedClasspathEntry, sourceFolderHelper.sharedGeneratedSourceFolder())
        assertEquals(m_subSharedTestClasspathEntry, sourceFolderHelper.sharedTestSourceFolder())
        assertEquals(m_subSharedMainClasspathEntry, sourceFolderHelper.sharedMainTestSourceFolder())

        assertEquals(m_subServerMainClasspathEntry, sourceFolderHelper.serverSourceFolder())
        assertEquals(m_subServerTestClasspathEntry, sourceFolderHelper.serverTestSourceFolder())
        assertEquals(m_subServerMainClasspathEntry, sourceFolderHelper.serverMainTestSourceFolder())
    }

    private fun assertSourceFolderHelperSubSub(sourceFolderHelper: SourceFolderHelper, classpathEntry: IClasspathEntry, scoutTier: ScoutTier) {
        assertEquals(classpathEntry, sourceFolderHelper.classpathEntry())
        assertEquals(scoutTier, sourceFolderHelper.tier())

        assertEquals(m_subSubClientMainClasspathEntry, sourceFolderHelper.clientSourceFolder())
        assertEquals(m_subSubClientTestClasspathEntry, sourceFolderHelper.clientTestSourceFolder())
        assertEquals(m_subSubClientMainClasspathEntry, sourceFolderHelper.clientMainTestSourceFolder())

        assertEquals(m_subSubSharedMainClasspathEntry, sourceFolderHelper.sharedSourceFolder())
        assertNull(sourceFolderHelper.sharedGeneratedSourceFolder())
        assertEquals(m_subSubSharedTestClasspathEntry, sourceFolderHelper.sharedTestSourceFolder())
        assertEquals(m_subSubSharedMainClasspathEntry, sourceFolderHelper.sharedMainTestSourceFolder())

        assertEquals(m_subSubServerMainClasspathEntry, sourceFolderHelper.serverSourceFolder())
        assertEquals(m_subSubServerTestClasspathEntry, sourceFolderHelper.serverTestSourceFolder())
        assertEquals(m_subSubServerMainClasspathEntry, sourceFolderHelper.serverMainTestSourceFolder())
    }
}