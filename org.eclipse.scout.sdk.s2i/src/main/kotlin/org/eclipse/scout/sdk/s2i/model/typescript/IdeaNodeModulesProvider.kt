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

import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFileManager
import org.eclipse.scout.sdk.core.typescript.model.api.NodeModulesProvider
import org.eclipse.scout.sdk.core.typescript.model.spi.NodeModuleSpi
import org.eclipse.scout.sdk.core.typescript.model.spi.NodeModulesProviderSpi
import java.nio.file.Path
import java.util.*

class IdeaNodeModulesProvider(val project: Project) : NodeModulesProviderSpi, Disposable {

    private val m_nodeModuleInventory = IdeaNodeModules(project)

    init {
        NodeModulesProvider.register(project, this)
    }

    override fun create(nodeModuleDir: Path): Optional<NodeModuleSpi> {
        val path = VirtualFileManager.getInstance().findFileByNioPath(nodeModuleDir) ?: return Optional.empty()
        return Optional.ofNullable(m_nodeModuleInventory.create(path))
    }

    override fun dispose() {
        NodeModulesProvider.remove(project)
        m_nodeModuleInventory.clear()
    }
}