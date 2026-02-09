/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2i.settings.ide

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import org.eclipse.scout.sdk.s2i.settings.ScoutSettings
import org.eclipse.scout.sdk.s2i.settings.ScoutSettingsHelper
import org.eclipse.scout.sdk.s2i.settings.SettingsChangedListener
import org.jetbrains.idea.maven.project.MavenProjectsManager

@Service(Service.Level.PROJECT)
class AutoUpdateIdeSettingsListener(val project: Project) : SettingsChangedListener, Disposable {

    private var m_listener: ScoutIdeSettingsUpdater? = null

    init {
        ScoutSettingsHelper.addListener(this)
    }

    override fun changed(key: String, oldVal: String?, newVal: String?) {
        if (ScoutSettings.KEY_AUTO_UPDATE_IDE_SETTINGS == key) {
            updateIdeSettingsListener()
        }
    }

    override fun dispose() {
        Disposer.dispose(this)
        ScoutSettingsHelper.removeListener(this)
        disable()
    }

    /**
     * Called on Project open and when the Scout settings change
     */
    fun updateIdeSettingsListener() {
        if (ScoutSettingsHelper.isAutoUpdateIdeSettings(project)) {
            enable()
        } else {
            disable()
        }
    }

    fun enable() {
        synchronized(this) {
            val currentListener = this.m_listener
            if (currentListener != null) return // already enabled

            val mavenManager = MavenProjectsManager.getInstance(project)
            val newListener = ScoutIdeSettingsUpdater(mavenManager)
            mavenManager.addManagerListener(newListener, newListener)
            m_listener = newListener
        }
    }

    fun disable() = synchronized(this) {
        m_listener?.dispose() // will remove it from the Maven manager
        m_listener = null
    }
}