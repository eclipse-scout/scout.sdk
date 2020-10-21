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

import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import org.eclipse.scout.sdk.s2i.EclipseScoutBundle

class AutoCreateClassIdStartup : StartupActivity, DumbAware {
    /**
     * Executed on [Project] open
     */
    override fun runActivity(project: Project) {
        EclipseScoutBundle.autoCreateClassIdListener(project).updateClassIdAutoGeneration(project) // it will dispose itself
    }
}