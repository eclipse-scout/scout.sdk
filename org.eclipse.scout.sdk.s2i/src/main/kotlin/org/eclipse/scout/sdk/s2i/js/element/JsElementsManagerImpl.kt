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
package org.eclipse.scout.sdk.s2i.js.element

import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.roots.SourceFolder
import com.intellij.psi.search.SearchScope
import org.eclipse.scout.sdk.core.log.SdkLog
import org.eclipse.scout.sdk.core.model.api.IType
import org.eclipse.scout.sdk.core.s.apidef.ScoutApi
import org.eclipse.scout.sdk.core.s.environment.IEnvironment
import org.eclipse.scout.sdk.core.s.environment.IFuture
import org.eclipse.scout.sdk.core.s.environment.IProgress
import org.eclipse.scout.sdk.core.s.js.element.JsElementModel
import org.eclipse.scout.sdk.core.s.js.element.JsElementsCreateOperation
import org.eclipse.scout.sdk.core.s.js.element.gen.IJsSourceBuilder
import org.eclipse.scout.sdk.core.s.js.element.gen.IJsSourceGenerator
import org.eclipse.scout.sdk.core.s.util.ScoutTier
import org.eclipse.scout.sdk.core.util.FinalValue
import org.eclipse.scout.sdk.core.util.SdkException
import org.eclipse.scout.sdk.s2i.*
import org.eclipse.scout.sdk.s2i.EclipseScoutBundle.message
import org.eclipse.scout.sdk.s2i.environment.IdeaEnvironment
import org.eclipse.scout.sdk.s2i.environment.IdeaEnvironment.Factory.callInIdeaEnvironment
import org.eclipse.scout.sdk.s2i.environment.IdeaEnvironment.Factory.computeInReadAction
import org.eclipse.scout.sdk.s2i.environment.IdeaProgress
import org.eclipse.scout.sdk.s2i.environment.TransactionManager
import org.eclipse.scout.sdk.s2i.util.SourceFolderHelper
import java.nio.file.Path
import kotlin.streams.asSequence

class JsElementsManagerImpl(val project: Project) : JsElementsManager {

    private val m_fileIndex = FinalValue<ProjectFileIndex>()
    private val m_sourceFolderSourceFolderHelperMap = HashMap<SourceFolder, SourceFolderHelper>()
    private val m_typeSourceFolderMap = HashMap<IType, SourceFolder>()
    private val m_typeTargetJsFolderPathMap = HashMap<IType, Path>()
    private val m_typeModuleNameMap = HashMap<IType, String>()

    override fun scheduleJsElementsCreation(scope: SearchScope) = callInIdeaEnvironment(project, message("create.js.elements.for.scope")) { e, p ->
        try {
            createJsElements(scope, e, p)
        } catch (t: Throwable) {
            SdkLog.warning("JS elements creation failed.", t)
        }
    }

    private fun createJsElements(scope: SearchScope, env: IdeaEnvironment, progress: IdeaProgress) {
        val totalWork = 200
        val workForDiscovery = 10
        progress.init(totalWork, message("create.js.elements.for.scope"))
        val forms = computeInReadAction(project, true, progress.newChild(workForDiscovery).indicator) {
            ScoutApi.allKnown().asSequence()
                .flatMap { JsElementModel.validSuperTypes(it).asSequence() }
                .map { it.fqn() }
                .distinct()
                .flatMap { project.findTypesByName(it) }
                .flatMap { it.newSubTypeHierarchy(scope, checkDeep = true, includeAnonymous = false, includeRoot = false).asSequence() }
                .filter { it.isPhysical }
                .mapNotNull { it.toScoutType(env) }
                .toList()

        }
        if (forms.isEmpty()) {
            return
        }
        val operation = object : JsElementsCreateOperation() {
            private var m_counter = 0

            override fun write(generator: IJsSourceGenerator<IJsSourceBuilder<*>>, fileName: String, element: IType, env: IEnvironment, progress: IProgress): IFuture<*> {
                val indicator = progress.toIdea().indicator
                indicator.text2 = element.name()
                indicator.checkCanceled()

                val future = super.write(generator, fileName, element, env, progress)
                commitIfNecessary()
                return future
            }

            private fun commitIfNecessary() {
                m_counter++
                if (m_counter >= TransactionManager.BULK_UPDATE_LIMIT) {
                    TransactionManager.current().checkpoint(null)
                    m_counter = 0
                }
            }
        }
        operation
            .withElements(forms)
            .withGetTargetJsFolderPathStrategy { targetJsFolderPath(it, env) }
            .withGetModuleNameStrategy { moduleName(it, env) }
            .accept(env, progress.newChild(totalWork - workForDiscovery))
    }

    private fun fileIndex(): ProjectFileIndex = m_fileIndex.computeIfAbsentAndGet { ProjectFileIndex.getInstance(project) }

    private fun sourceFolderHelper(sourceFolder: SourceFolder, env: IdeaEnvironment): SourceFolderHelper = m_sourceFolderSourceFolderHelperMap.computeIfAbsent(sourceFolder) {
        return@computeIfAbsent SourceFolderHelper(project, it) { vf -> env.findClasspathEntry(vf) }
    }

    private fun sourceFolder(element: IType): SourceFolder = m_typeSourceFolderMap.computeIfAbsent(element) {
        return@computeIfAbsent it.resolvePsi()?.resolveSourceRoot()?.let { vf -> fileIndex().getSourceFolder(vf) } ?: throw  SdkException("No source folder could be determined for IType '$it'")
    }

    private fun targetJsFolderPath(element: IType, env: IdeaEnvironment): Path = m_typeTargetJsFolderPathMap.computeIfAbsent(element) {
        val sourceFolder = sourceFolder(it)
        val sourceFolderHelper = sourceFolderHelper(sourceFolder, env)
        return@computeIfAbsent sourceFolderHelper.sourceFolder(ScoutTier.HtmlUi)?.javaEnvironment()?.toIdea()?.module?.moduleDirPath()?.resolve("src/main/js") ?: throw SdkException("No target path could be determined for IType '$it")
    }

    private fun moduleName(element: IType, env: IdeaEnvironment): String = m_typeModuleNameMap.computeIfAbsent(element) {
        val sourceFolder = sourceFolder(it)
        val sourceFolderHelper = sourceFolderHelper(sourceFolder, env)
        return@computeIfAbsent sourceFolderHelper.classpathEntry()?.javaEnvironment()?.toIdea()?.module?.name ?: throw SdkException("No module name could be determined for IType '$it")
    }
}