/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2i.model.typescript

import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import org.eclipse.scout.sdk.core.typescript.model.spi.NodeModulesProviderSpi

class IdeaNodeModulesProviderStartup : StartupActivity, DumbAware {
    /**
     * Executed on [Project] open
     */
    override fun runActivity(project: Project) {
        project.getService(NodeModulesProviderSpi::class.java) // automatically create service to register spi
    }
}