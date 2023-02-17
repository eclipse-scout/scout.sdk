/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2i.model.js

import com.intellij.openapi.module.Module
import com.intellij.openapi.project.guessModuleDir
import com.intellij.openapi.vfs.VirtualFile
import org.eclipse.scout.sdk.core.s.model.js.ScoutJsModel
import org.eclipse.scout.sdk.s2i.EclipseScoutBundle.jsModuleCache

/**
 * Represents the full Scout JavaScript model of a Node module.
 * This includes the JavaScript code of the module itself and all direct dependencies of the module.
 */
class JsModel {

    companion object {
        fun getOrCreateModule(module: Module): ScoutJsModel? {
            val moduleRoot = module.guessModuleDir() ?: return null
            val project = module.project
            val jsModuleCache = jsModuleCache(project)
            return getOrCreateModule(jsModuleCache, moduleRoot)
        }

        private fun getOrCreateModule(jsModuleCache: JsModuleCacheImplementor, moduleRoot: VirtualFile): ScoutJsModel? {
            jsModuleCache.getModule(moduleRoot)?.let { return it }
            ScoutJsModel.create(moduleRoot.toNioPath(), jsModuleCache.project).orElse(null)?.let {
                jsModuleCache.putModule(moduleRoot, it)
                return it
            }
            return null
        }
    }
}