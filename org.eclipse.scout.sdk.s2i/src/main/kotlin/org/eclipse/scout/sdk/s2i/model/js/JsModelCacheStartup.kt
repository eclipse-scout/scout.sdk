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
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessModuleDir
import com.intellij.openapi.startup.StartupActivity
import org.eclipse.scout.sdk.core.log.SdkLog
import org.eclipse.scout.sdk.core.s.model.js.ScoutJsModel
import org.eclipse.scout.sdk.core.typescript.model.api.IPackageJson
import org.eclipse.scout.sdk.s2i.environment.IdeaEnvironment

class JsModelCacheStartup : StartupActivity, DumbAware {
    override fun runActivity(project: Project) {
        // enforce service creation to ensure the psi listener is active
        project.getService(JsModelManager::class.java)
        IdeaEnvironment.computeInReadActionAsync(project) {
            preloadCache(project)
        }
    }

    private fun preloadCache(project: Project) {
        val start = System.currentTimeMillis()
        ModuleManager.getInstance(project).modules
            .filter { it.name.contains(".ui") }
            .filter { containsPackageJson(it) }
            .sortedBy { it.name }
            .firstNotNullOfOrNull { JsModelManager.getOrCreateScoutJsModel(it) }
            ?.let {
                preloadModel(it)
                SdkLog.info("Scout JS model cache preloaded for module '{}' took {}ms.", it.nodeModule().name(), System.currentTimeMillis() - start)
            }
    }

    private fun preloadModel(model: ScoutJsModel) {
        model
            .findScoutObjects()
            .withIncludeDependencies(true)
            .stream()
            .forEach { it.declaringClass().supers().stream().toList() }
    }

    private fun containsPackageJson(module: Module): Boolean {
        val dir = module.guessModuleDir() ?: return false
        val packageJson = dir.findChild(IPackageJson.FILE_NAME) ?: return false
        return packageJson.isValid && packageJson.isInLocalFileSystem
    }

}