/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2i.project

import com.intellij.ide.util.EditorHelper
import com.intellij.ide.util.projectWizard.*
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.externalSystem.service.project.manage.ExternalProjectsManagerImpl
import com.intellij.openapi.module.JavaModuleType
import com.intellij.openapi.module.ModuleType
import com.intellij.openapi.module.StdModuleTypes
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.JavaSdk
import com.intellij.openapi.projectRoots.SdkTypeId
import com.intellij.openapi.projectRoots.impl.SdkVersionUtil
import com.intellij.openapi.roots.ModifiableRootModel
import com.intellij.openapi.roots.ui.configuration.ModulesProvider
import com.intellij.openapi.ui.Messages.*
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.psi.PsiManager
import org.eclipse.scout.sdk.core.s.project.ScoutProjectNewHelper
import org.eclipse.scout.sdk.core.s.project.ScoutProjectNewHelper.createProject
import org.eclipse.scout.sdk.core.s.util.maven.IMavenConstants
import org.eclipse.scout.sdk.s2i.EclipseScoutBundle.message
import org.eclipse.scout.sdk.s2i.EclipseScoutBundle.scoutIcon
import org.eclipse.scout.sdk.s2i.environment.IdeaEnvironment.Factory.callInIdeaEnvironment
import org.eclipse.scout.sdk.s2i.environment.IdeaEnvironment.Factory.computeInReadAction
import org.jetbrains.idea.maven.project.MavenProjectsManager
import org.jetbrains.idea.maven.utils.MavenUtil
import java.nio.file.Path
import java.nio.file.Paths

class ScoutModuleBuilder : ModuleBuilder() {

    lateinit var groupId: String
    lateinit var artifactId: String
    lateinit var displayName: String
    var useJavaUiLang = true
    var javaVersion: Int? = null
    var scoutVersion: String? = null /* use latest */

    override fun getModuleType(): ModuleType<*> = StdModuleTypes.JAVA

    override fun createWizardSteps(wizardContext: WizardContext, modulesProvider: ModulesProvider): Array<ModuleWizardStep> = ModuleWizardStep.EMPTY_ARRAY

    override fun getCustomOptionsStep(context: WizardContext, parentDisposable: Disposable) = ScoutModuleWizardStep(context, this)

    override fun setupRootModel(modifiableRootModel: ModifiableRootModel) {
        val path = contentEntryPath?.let { Paths.get(it) } ?: return
        val project = modifiableRootModel.project
        MavenUtil.runWhenInitialized(project) {
            createFromArchetype(project, path)
        }
    }

    private fun createFromArchetype(project: Project, path: Path) {
        callInIdeaEnvironment(project, message("create.new.scout.modules")) { env, p ->
            createProject(path, groupId, artifactId, displayName, !useJavaUiLang, javaVersion?.toString(), scoutVersion, env, p)
        }.thenAccept {
            importModules(project, path)
        }
    }

    private fun importModules(project: Project, path: Path) {
        val rootPom = path.resolve(artifactId).resolve(IMavenConstants.POM)
        LocalFileSystem.getInstance().refreshIoFiles(listOf(rootPom.parent.toFile()), true, true) {
            val vFileRootPom = LocalFileSystem.getInstance().findFileByPath(rootPom.toString()) ?: return@refreshIoFiles
            val mavenProjectManager = MavenProjectsManager.getInstance(project)
            mavenProjectManager.addManagedFilesOrUnignore(listOf(vFileRootPom)) // triggers the maven update

            openParentPom(project, path)
        }
    }

    private fun openParentPom(project: Project, path: Path) {
        val parentPom = path.resolve(artifactId).resolve(artifactId).resolve(IMavenConstants.POM)
        val vFileParentPom = LocalFileSystem.getInstance().refreshAndFindFileByPath(parentPom.toString()) ?: return
        val psiFile = computeInReadAction(project, true, null) {
            PsiManager.getInstance(project).findFile(vFileParentPom)
        } ?: return
        ApplicationManager.getApplication().invokeLater {
            EditorHelper.openInEditor(psiFile, false, true)
        }
    }

    override fun modifySettingsStep(settingsStep: SettingsStep): ModuleWizardStep? {
        val nameLocationSettings = settingsStep.moduleNameLocationSettings
        if (nameLocationSettings != null) {
            nameLocationSettings.moduleName = artifactId
        }

        javaVersion = settingsStep.context.projectJdk.homePath
            ?.let { SdkVersionUtil.getJdkVersionInfo(it)?.version?.feature }

        return super.modifySettingsStep(settingsStep)
    }

    override fun validate(currentProject: Project?, project: Project): Boolean {
        val selectedJavaMajorVersion = javaVersion
        if (selectedJavaMajorVersion != null) {
            val supportedJavaVersions = ScoutProjectNewHelper.getSupportedJavaVersions(scoutVersion)
            if (!supportedJavaVersions.contains(selectedJavaMajorVersion)) {
                val msg = message("jdk.not.supported.msg", selectedJavaMajorVersion, scoutVersion ?: IMavenConstants.LATEST, supportedJavaVersions.joinToString(", "))
                val result = showOkCancelDialog(msg, message("jdk.not.supported.title"), getYesButton(), getNoButton(), getWarningIcon())
                if (result != OK) return false
            }
        }
        return super.validate(currentProject, project)
    }

    override fun getBuilderId(): String? = javaClass.name

    override fun getPresentableName() = "Scout"

    override fun getGroupName() = presentableName

    override fun getParentGroup() = JavaModuleType.JAVA_GROUP

    override fun getWeight() = JavaModuleBuilder.JAVA_WEIGHT

    override fun getDescription() = message("module.builder.desc")

    override fun getNodeIcon() = scoutIcon(15)

    override fun isSuitableSdkType(sdkType: SdkTypeId?) = sdkType === JavaSdk.getInstance()

    override fun createProject(name: String?, path: String?) = ExternalProjectsManagerImpl.setupCreatedProject(super.createProject(name, path))
}