package org.eclipse.scout.sdk.s2i

import com.intellij.openapi.Disposable
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import com.intellij.openapi.util.Disposer
import org.eclipse.scout.sdk.core.log.SdkLog
import org.eclipse.scout.sdk.core.s.environment.IEnvironment
import org.eclipse.scout.sdk.core.s.environment.IProgress
import org.eclipse.scout.sdk.core.s.util.maven.IMavenRunnerSpi
import org.eclipse.scout.sdk.core.s.util.maven.MavenBuild
import org.eclipse.scout.sdk.core.s.util.maven.MavenRunner
import org.eclipse.scout.sdk.core.util.Ensure
import org.eclipse.scout.sdk.s2i.environment.IdeaEnvironment
import org.jetbrains.idea.maven.execution.MavenExecutionOptions
import org.jetbrains.idea.maven.execution.MavenRunConfigurationType
import org.jetbrains.idea.maven.execution.MavenRunnerParameters
import org.jetbrains.idea.maven.execution.MavenRunnerSettings
import org.jetbrains.idea.maven.project.MavenProjectsManager
import org.jetbrains.idea.maven.utils.MavenUtil
import java.util.*

open class IdeaMavenRunner : IMavenRunnerSpi, StartupActivity, DumbAware, Disposable {

    private var m_previousRunner: IMavenRunnerSpi? = null

    /**
     * Executed on [Project] open
     */
    override fun runActivity(project: Project) {
        val existingRunner = MavenRunner.get()
        if (existingRunner == this) {
            return
        }

        Disposer.register(project, this)

        // Scout requires sources to be present e.g. to parse text services (NLS)
        MavenProjectsManager.getInstance(project).importingSettings.isDownloadSourcesAutomatically = true

        m_previousRunner = existingRunner
        MavenRunner.set(this)
    }

    /**
     * Executed on [Project] close
     */
    override fun dispose() {
        MavenRunner.set(m_previousRunner)
        m_previousRunner = null
    }

    override fun execute(build: MavenBuild, env: IEnvironment, progress: IProgress) {
        SdkLog.debug("Executing embedded {}", build.toString())

        val environment = env as IdeaEnvironment
        val project = environment.project
        val runner = org.jetbrains.idea.maven.execution.MavenRunner.getInstance(project)
        val projectsManager = MavenProjectsManager.getInstance(project)
        val mavenHome = MavenUtil.resolveMavenHomeDirectory(projectsManager.generalSettings.mavenHome)
                ?: throw Ensure.newFail("No valid Maven installation found. " +
                        "Either set the home directory in the configuration dialog or set the M2_HOME environment variable on your system.")
        val debug = SdkLog.isDebugEnabled() || build.hasOption(MavenBuild.OPTION_DEBUG)
        val generalSettings = projectsManager.generalSettings.clone()
        generalSettings.mavenHome = mavenHome.path
        generalSettings.isAlwaysUpdateSnapshots = build.hasOption(MavenBuild.OPTION_UPDATE_SNAPSHOTS)
        generalSettings.isNonRecursive = build.hasOption(MavenBuild.OPTION_NON_RECURSIVE)
        generalSettings.isWorkOffline = build.hasOption(MavenBuild.OPTION_OFFLINE)
        generalSettings.threads = "1"
        generalSettings.outputLevel = if (debug) MavenExecutionOptions.LoggingLevel.DEBUG else MavenExecutionOptions.LoggingLevel.INFO
        generalSettings.isPrintErrorStackTraces = debug

        val settings: MavenRunnerSettings = runner.settings.clone()
        settings.mavenProperties = LinkedHashMap(build.properties)
        settings.isSkipTests = build.properties.containsKey(MavenBuild.PROPERTY_SKIP_TESTS) || build.properties.containsKey(MavenBuild.PROPERTY_SKIP_TEST_CREATION)

        val parameters = MavenRunnerParameters(true, build.workingDirectory.toString(), null,
                build.goals.toList(), null, null)

        MavenRunConfigurationType.runConfiguration(project, parameters, generalSettings, settings, null)
    }
}