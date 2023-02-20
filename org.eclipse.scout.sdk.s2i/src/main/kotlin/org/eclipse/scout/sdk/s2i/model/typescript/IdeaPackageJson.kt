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

import com.intellij.javascript.nodejs.NodeModuleSearchUtil.collectVisibleNodeModules
import com.intellij.openapi.vfs.VirtualFile
import org.eclipse.scout.sdk.core.typescript.model.api.IPackageJson
import org.eclipse.scout.sdk.core.typescript.model.api.internal.PackageJsonImplementor
import org.eclipse.scout.sdk.core.typescript.model.spi.AbstractNodeElementSpi
import org.eclipse.scout.sdk.core.typescript.model.spi.NodeModuleSpi
import org.eclipse.scout.sdk.core.typescript.model.spi.PackageJsonSpi
import org.eclipse.scout.sdk.core.util.FinalValue
import org.eclipse.scout.sdk.s2i.resolveLocalPath

class IdeaPackageJson(private val ideaModule: IdeaNodeModule, private val moduleDir: VirtualFile) : AbstractNodeElementSpi<IPackageJson>(ideaModule), PackageJsonSpi {

    private val m_dependencies = FinalValue<Collection<NodeModuleSpi>>()

    override fun createApi() = PackageJsonImplementor(this)

    override fun content() = moduleDir.findChild(IPackageJson.FILE_NAME)?.inputStream

    override fun containingDir() = moduleDir.resolveLocalPath()

    override fun existsFile(relPath: String) = moduleDir.findFileByRelativePath(relPath) != null

    override fun dependencies(): Collection<NodeModuleSpi> = m_dependencies.computeIfAbsentAndGet {
        collectVisibleNodeModules(HashMap(), ideaModule.moduleInventory.project, moduleDir).asSequence()
            .mapNotNull { it.virtualFile }
            .mapNotNull { ideaModule.moduleInventory.getOrCreateModule(it) }
            .toSet()
    }
}