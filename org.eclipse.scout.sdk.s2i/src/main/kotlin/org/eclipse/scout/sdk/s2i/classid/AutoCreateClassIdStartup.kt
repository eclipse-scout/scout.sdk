/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.s2i.classid

import com.intellij.openapi.Disposable
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.Key
import org.eclipse.scout.sdk.core.s.classid.ClassIds
import org.eclipse.scout.sdk.s2i.settings.ScoutSettings
import org.eclipse.scout.sdk.s2i.settings.ScoutSettings.Current.isAutoCreateClassIdAnnotations
import org.eclipse.scout.sdk.s2i.settings.SettingsChangedListener

open class AutoCreateClassIdStartup : StartupActivity, DumbAware {

    private val m_classIdSettingsListenerKey = Key.create<SettingsChangedListener>("scout.CLASS_ID_SETTINGS_LISTENER_KEY")

    /**
     * Executed on [Project] open
     */
    override fun runActivity(project: Project) {
        Disposer.register(project, Disposable { dispose(project) })

        val settingsChangedListener = object : SettingsChangedListener {
            override fun changed(key: String, oldVal: String?, newVal: String?) {
                if (ScoutSettings.KEY_AUTO_CREATE_CLASS_ID == key) {
                    updateClassIdAutoGeneration(project)
                }
            }
        }
        ScoutSettings.addListener(settingsChangedListener)
        project.putUserData(m_classIdSettingsListenerKey, settingsChangedListener)
        updateClassIdAutoGeneration(project)
    }

    /**
     * Executed on [Project] close
     */
    fun dispose(project: Project) {
        val listener = project.getUserData(m_classIdSettingsListenerKey) ?: return
        ScoutSettings.removeListener(listener)
        project.putUserData(m_classIdSettingsListenerKey, null)
    }

    protected fun updateClassIdAutoGeneration(project: Project) {
        // the settings are retrieved from the project given
        // but as the ClassIds class does not support multiple different values the value from the first project is used for the moment
        // this might be wrong in case multiple projects are open having different classid generation settings
        ClassIds.setAutomaticallyCreateClassIdAnnotation(isAutoCreateClassIdAnnotations(project))
    }
}
