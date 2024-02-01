/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2i.maven

import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import com.intellij.util.concurrency.AppExecutorUtil
import kotlinx.coroutines.runBlocking
import org.apache.commons.lang3.ClassUtils
import org.eclipse.scout.sdk.core.log.SdkLog
import org.eclipse.scout.sdk.s2i.EclipseScoutBundle
import org.jetbrains.idea.maven.project.MavenProjectsManager
import java.lang.reflect.InvocationTargetException
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import kotlin.reflect.KFunction
import kotlin.reflect.full.callSuspend
import kotlin.reflect.full.functions

/**
 * Ensures the Maven sources are automatically downloaded (on importing, project startup and after Maven reload)
 * Scout requires sources to be present e.g. to parse text services (NLS)
 */
class MavenSourcesAutoDownloader : StartupActivity, DumbAware {

    override fun runActivity(project: Project) {
        val manager = MavenProjectsManager.getInstance(project)

        manager.importingSettings.isDownloadSourcesAutomatically = true

        // add listener to automatically schedule a sources download when necessary (is not done automatically on IJ >= 2023.2)
        val downloadArtifactsFun = getDownloadArtifactsFun()
        if (downloadArtifactsFun != null) {
            val disposable = EclipseScoutBundle.derivedResourceManager(project) // project level service: remove listener on service (=project) disposal
            manager.addManagerListener(MavenManagerListener(manager, downloadArtifactsFun), disposable)
        }
    }

    // can be removed as soon as IJ 2023.2 is the latest supported version. Use code from DownloadAllSourcesAndDocsAction as replacement
    private fun getDownloadArtifactsFun(): KFunction<*>? {
        return try {
            ClassUtils.getClass(this::class.java.classLoader, "org.jetbrains.idea.maven.project.MavenProjectsManagerEx", true)
                .kotlin
                .functions
                .find { it.name == "downloadArtifacts" }
        } catch (e: Throwable) {
            SdkLog.debug("Could not find MavenProjectsManagerEx. Skipping Maven sources auto download.", e)
            null
        }
    }

    private class MavenManagerListener(private val m_manager: MavenProjectsManager, private val m_downloadArtifactsFun: KFunction<*>) : MavenProjectsManager.Listener {

        private var m_future: Future<*>? = null

        init {
            // in case the manager is already active when the startup activity is executed (then the 'activated' event will not be fired anymore)
            scheduleMavenSourcesDownload()
        }

        override fun activated() {
            scheduleMavenSourcesDownload()
        }

        override fun projectImportCompleted() {
            scheduleMavenSourcesDownload()
        }

        private fun scheduleMavenSourcesDownload() {
            if (!m_manager.hasProjects()) return
            runAsync {
                try {
                    m_downloadArtifactsFun.callSuspend(m_manager, *arrayOf(m_manager.projects, null, true, false))
                } catch (e: InvocationTargetException) {
                    throw e.targetException
                }
            }
        }

        @Synchronized
        private fun runAsync(action: suspend () -> Unit) {
            val future = m_future
            if (future != null) {
                future.cancel(false)
                m_future = null
            }
            m_future = AppExecutorUtil.getAppScheduledExecutorService().schedule({
                runBlocking {
                    try {
                        action()
                    } catch (e: Throwable) {
                        SdkLog.info("Error downloading Maven sources.", e)
                    }
                }
            }, 5, TimeUnit.SECONDS)
        }
    }
}