/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2i.settings.ide

import com.intellij.compiler.impl.javaCompiler.eclipse.EclipseCompilerConfiguration
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PathMacroManager
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.vfs.encoding.EncodingProjectManager
import org.eclipse.scout.sdk.core.log.SdkLog
import org.eclipse.scout.sdk.core.s.java.apidef.IScoutApi
import org.eclipse.scout.sdk.core.util.Strings
import org.eclipse.scout.sdk.s2i.environment.IdeaEnvironment
import org.eclipse.scout.sdk.s2i.environment.IdeaProgress
import org.eclipse.scout.sdk.s2i.isJavaModule
import org.eclipse.scout.sdk.s2i.util.ApiHelper
import org.jetbrains.idea.maven.project.MavenProjectsManager
import org.jetbrains.jps.model.serialization.PathMacroUtil
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

/**
 * Applies IDE settings required for Scout. Only active if Scout is found in the project.
 */
class ScoutIdeSettingsUpdater(val mavenManager: MavenProjectsManager) : MavenProjectsManager.Listener, Disposable {
    init {
        // in case the manager is already active when the startup activity is executed (then the 'activated' event will not be fired anymore)
        updateScoutDependentSettings()
    }

    override fun activated() {
        updateScoutDependentSettings()
    }

    override fun projectImportCompleted() {
        updateScoutDependentSettings()
    }

    private fun updateScoutDependentSettings() {
        if (mavenManager.hasProjects()) {
            update(mavenManager.project)
        }
    }

    fun update(project: Project) {
        try {
            val scoutApi = IdeaEnvironment.callInIdeaEnvironmentSync(project, IdeaProgress.empty()) { env, _ ->
                ModuleManager.getInstance(project).modules
                    .filter { it.isJavaModule() }
                    .firstNotNullOfOrNull { ApiHelper.scoutApiFor(it, env) }
            } ?: return // no Scout module in project

            applyScoutDefaultEncodings(project, scoutApi)
            applyScoutEcjSettings(project, scoutApi)
        } catch (e: Throwable) {
            SdkLog.warning("Error detecting Scout version of project '{}'.", project.name, e)
        }
    }

    /**
     * Sets Project encoding to UTF-8.
     * Automatically sets encoding for .properties files according to Scout version found in project.
     * Can be removed as soon as UTF-8 is the default for .properties files in IntelliJ and only Scout >= 23.2 is supported.
     */
    private fun applyScoutDefaultEncodings(project: Project, scoutApi: IScoutApi) {
        try {
            val encodingManager = EncodingProjectManager.getInstance(project)
            val propertiesEncoding = scoutApi.propertiesEncoding()
            ApplicationManager.getApplication().invokeLater {
                try {
                    encodingManager.defaultCharsetName = StandardCharsets.UTF_8.name()
                    encodingManager.setDefaultCharsetForPropertiesFiles(null, propertiesEncoding)
                    encodingManager.setNative2AsciiForPropertiesFiles(null, StandardCharsets.ISO_8859_1.equals(propertiesEncoding))
                } catch (e: Throwable) {
                    SdkLog.warning("Error applying Scout encoding settings.", e)
                }
            }
        } catch (e: Throwable) {
            SdkLog.warning("Error applying Scout encoding settings.", e)
        }
    }

    /**
     * Automatically configures the correct ecj path for Scout.
     * Workaround for https://youtrack.jetbrains.com/issue/IDEA-355457/Eclipse-ECJ-compiler-does-not-support-Java-21-and-throws-an-error
     *
     * Also required to have the same ECJ in IntelliJ as in the Maven build for the corresponding Scout version.
     */
    private fun applyScoutEcjSettings(project: Project, scoutApi: IScoutApi) {
        try {
            val eclipseCompilerOptions = EclipseCompilerConfiguration.getOptions(project, EclipseCompilerConfiguration::class.java)
            val ecjPath = getEcjPathFor(scoutApi, project)
            ApplicationManager.getApplication().invokeLater {
                try {
                    eclipseCompilerOptions.ECJ_TOOL_PATH = ecjPath
                } catch (e: Throwable) {
                    SdkLog.warning("Error writing ECJ compiler path.", e)
                }
            }
        } catch (e: Throwable) {
            SdkLog.warning("Error writing ECJ compiler path.", e)
        }
    }

    private fun getEcjPathFor(scoutApi: IScoutApi, project: Project): String {
        val fileName = "ecj-${scoutApi.ecjVersion()}.jar"
        val dest = getEcjDir(project).resolve(fileName)
        if (!Files.isRegularFile(dest)) {
            Files.createDirectories(dest.parent)
            this::class.java.classLoader.getResourceAsStream("ecj/$fileName").use { input ->
                Files.newOutputStream(dest).buffered().use {
                    input?.transferTo(it)
                }
            }
        }
        return dest.toString().replace('\\', '/') // replace windows path separator as IJ path substitution only works on unix file separators.
    }

    private fun getEcjDir(project: Project): Path {
        val explicitConfig = getExplicitPath("scout.ecj.path", project)
        if (Strings.hasText(explicitConfig)) {
            return Paths.get(explicitConfig!!)
        }

        // by default use a path in the user home so that for different developers on different platforms the path is identical in case the compiler.xml is shared using scm.
        return Paths.get(PathMacroUtil.getUserHomePath()).resolve(".scout").resolve("ecj")
    }

    private fun getExplicitPath(property: String, project: Project): String? {
        val prop = System.getProperty(property) ?: return null
        val isQuoted = prop.length > 1 && '"' == prop[0] && '"' == prop[prop.length - 1]
        return PathMacroManager.getInstance(project).expandPath(if (isQuoted) prop.substring(1, prop.length - 1) else prop)
    }

    override fun dispose() {
        Disposer.dispose(this) // removes me as listener if present
    }
}