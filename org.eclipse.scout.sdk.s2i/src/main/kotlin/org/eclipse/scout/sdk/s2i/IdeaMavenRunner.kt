package org.eclipse.scout.sdk.s2i

import com.intellij.openapi.components.ProjectComponent
import com.intellij.openapi.project.Project
import org.eclipse.scout.sdk.core.log.SdkLog
import org.eclipse.scout.sdk.core.s.util.maven.IMavenRunnerSpi
import org.eclipse.scout.sdk.core.s.util.maven.MavenBuild
import org.eclipse.scout.sdk.core.s.util.maven.MavenRunner
import org.eclipse.scout.sdk.core.util.Ensure
import org.jetbrains.idea.maven.execution.MavenExecutionOptions
import org.jetbrains.idea.maven.execution.MavenRunConfigurationType
import org.jetbrains.idea.maven.execution.MavenRunnerParameters
import org.jetbrains.idea.maven.execution.MavenRunnerSettings
import org.jetbrains.idea.maven.project.MavenProjectsManager
import org.jetbrains.idea.maven.utils.MavenUtil
import java.util.*

open class IdeaMavenRunner(val project: Project) : IMavenRunnerSpi, ProjectComponent {

    private var m_origRunner: IMavenRunnerSpi? = null

    override fun initComponent() {
        // Scout requires sources to be present e.g. to parse text services (NLS)
        MavenProjectsManager.getInstance(project).importingSettings.isDownloadSourcesAutomatically = true

        m_origRunner = MavenRunner.get()
        MavenRunner.set(this)
    }

    override fun disposeComponent() {
        MavenRunner.set(m_origRunner)
        m_origRunner = null
    }

    override fun execute(build: MavenBuild) {
        SdkLog.debug("Executing embedded {}", build.toString())

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
        settings.mavenProperties = LinkedHashMap<String, String>(build.properties)
        settings.isSkipTests = build.properties.containsKey(MavenBuild.PROPERTY_SKIP_TESTS) || build.properties.containsKey(MavenBuild.PROPERTY_SKIP_TEST_CREATION)

        val parameters = MavenRunnerParameters(true, build.workingDirectory.toString(), null,
                build.goals.toList(), null, null)

        MavenRunConfigurationType.runConfiguration(project, parameters, generalSettings, settings, null)
    }
}