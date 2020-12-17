/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.s2i.classid

import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import org.eclipse.scout.sdk.core.s.classid.ClassIds
import org.eclipse.scout.sdk.s2i.settings.ScoutSettings
import org.eclipse.scout.sdk.s2i.settings.ScoutSettings.Current.isAutoCreateClassIdAnnotations
import org.eclipse.scout.sdk.s2i.settings.SettingsChangedListener

class AutoCreateClassIdListener(val project: Project) : SettingsChangedListener, Disposable {

    init {
        ScoutSettings.addListener(this)
    }

    override fun changed(key: String, oldVal: String?, newVal: String?) {
        if (ScoutSettings.KEY_AUTO_CREATE_CLASS_ID == key) {
            updateClassIdAutoGeneration(project)
        }
    }

    override fun dispose() {
        ScoutSettings.removeListener(this)
    }

    fun updateClassIdAutoGeneration(project: Project) {
        // the settings are retrieved from the project given
        // but as the ClassIds class does not support multiple different values the value from the first project is used for the moment
        // this might be wrong in case multiple projects are open having different classid generation settings
        ClassIds.setAutomaticallyCreateClassIdAnnotation(isAutoCreateClassIdAnnotations(project))
    }
}
