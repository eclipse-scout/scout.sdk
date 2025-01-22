/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
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
import com.intellij.openapi.startup.ProjectActivity
import org.jetbrains.idea.maven.project.MavenProjectsManager

/**
 * Ensures the Maven sources are automatically downloaded.
 * Scout requires sources to be present e.g. to parse text services (NLS)
 */
class MavenSourcesAutoDownloader : ProjectActivity, DumbAware {
    override suspend fun execute(project: Project) {
        MavenProjectsManager.getInstance(project).importingSettings.isDownloadSourcesAutomatically = true
    }
}
