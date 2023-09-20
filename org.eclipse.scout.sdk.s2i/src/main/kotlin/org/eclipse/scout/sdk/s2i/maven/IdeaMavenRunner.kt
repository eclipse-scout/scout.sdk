/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2i.maven

import com.intellij.execution.process.ProcessAdapter
import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.ui.RunContentDescriptor
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import com.intellij.util.ui.EDT
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
import org.jetbrains.idea.maven.project.MavenGeneralSettings
import org.jetbrains.idea.maven.project.MavenProjectsManager
import org.jetbrains.idea.maven.utils.MavenUtil
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

open class IdeaMavenRunner : IMavenRunnerSpi, StartupActivity, DumbAware {

    /**
     * Executed on [Project] open
     */
    override fun runActivity(project: Project) {
        MavenRunner.set(this)
    }

    override fun execute(build: MavenBuild, env: IEnvironment, progress: IProgress) {
        SdkLog.debug("Executing embedded {}", build.toString())

        val project = (env as IdeaEnvironment).project

        val generalSettings = getGeneralSettings(project, build)
        val settings = getMavenRunnerSettings(project, build)
        val parameters = MavenRunnerParameters(false, build.workingDirectory.toString(), null, build.goals.toList(), null, null)

        runConfigurationAndWait(project, parameters, generalSettings, settings)
    }

    protected fun getMavenRunnerSettings(project: Project, build: MavenBuild): MavenRunnerSettings {
        val runner = org.jetbrains.idea.maven.execution.MavenRunner.getInstance(project)
        val settings = runner.state.clone()
        settings.mavenProperties.putAll(build.properties)
        settings.isSkipTests = build.properties.containsKey(MavenBuild.PROPERTY_SKIP_TESTS) || build.properties.containsKey(MavenBuild.PROPERTY_SKIP_TEST_CREATION)
        return settings
    }

    protected fun getGeneralSettings(project: Project, build: MavenBuild): MavenGeneralSettings {
        val projectsManager = MavenProjectsManager.getInstance(project)
        val mavenHome = MavenUtil.resolveMavenHomeDirectory(projectsManager.generalSettings.mavenHome)
                ?: throw Ensure.newFail("No valid Maven installation found. " +
                        "Either set the home directory in the configuration dialog or set the M2_HOME environment variable on your system.")
        val debug = SdkLog.isDebugEnabled() || build.hasOption(MavenBuild.OPTION_DEBUG)
        val generalSettings = projectsManager.generalSettings.clone()
        generalSettings.beginUpdate()
        try {
            generalSettings.mavenHome = mavenHome.path
            generalSettings.isAlwaysUpdateSnapshots = build.hasOption(MavenBuild.OPTION_UPDATE_SNAPSHOTS)
            generalSettings.isNonRecursive = build.hasOption(MavenBuild.OPTION_NON_RECURSIVE)
            generalSettings.isWorkOffline = build.hasOption(MavenBuild.OPTION_OFFLINE)
            generalSettings.threads = "1"
            generalSettings.outputLevel = if (debug) MavenExecutionOptions.LoggingLevel.DEBUG else MavenExecutionOptions.LoggingLevel.INFO
            generalSettings.isPrintErrorStackTraces = debug
        } finally {
            generalSettings.endUpdate()
        }
        return generalSettings
    }

    protected open fun runConfigurationAndWait(project: Project, parameters: MavenRunnerParameters, generalSettings: MavenGeneralSettings, settings: MavenRunnerSettings) {
        val processStarted = CountDownLatch(1)
        val processTerminated = CountDownLatch(1)

        Ensure.isFalse(EDT.isCurrentThreadEdt(), "Not allowed to execute Maven runner in UI thread.") // as this thread will be blocked (freeze) until the maven call completed.
        ApplicationManager.getApplication().invokeLater {
            MavenRunConfigurationType.runConfiguration(project, parameters, generalSettings, settings) { onMavenProcessStarted(it, processStarted, processTerminated) }
        }

        val processCreated = processStarted.await(10, TimeUnit.SECONDS)
        if (processCreated) {
            processTerminated.await(30, TimeUnit.MINUTES)
        }
    }

    protected open fun onMavenProcessStarted(descriptor: RunContentDescriptor, processStarted: CountDownLatch, processTerminated: CountDownLatch) {
        val handler = descriptor.processHandler
        if (handler == null) {
            SdkLog.warning("No handler for embedded Maven call.")
            processStarted.countDown()
            processTerminated.countDown()
            return
        }

        handler.addProcessListener(ProcessTerminatedListener(processTerminated), descriptor)
        processStarted.countDown()

        if (handler.isProcessTerminated) {
            // in case the maven call ended already before the ProcessTerminatedListener has been attached
            onMavenProcessTerminated(processTerminated, handler.exitCode, null)
        }
    }

    protected open fun onMavenProcessTerminated(processTerminated: CountDownLatch, exitCode: Int?, text: String?) {
        processTerminated.countDown()
        when (exitCode) {
            null -> SdkLog.warning("Embedded Maven call completed without exit code.")
            0 -> SdkLog.debug("Embedded Maven call completed successfully.")
            else -> SdkLog.error("Embedded Maven call completed with error code {}:\n{}.", exitCode, text)
        }
    }

    private inner class ProcessTerminatedListener(val processTerminatedLatch: CountDownLatch) : ProcessAdapter() {
        override fun processTerminated(event: ProcessEvent) = onMavenProcessTerminated(processTerminatedLatch, event.exitCode, event.text)
    }
}