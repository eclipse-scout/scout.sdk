/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2i.model.typescript.util

import com.intellij.javascript.nodejs.PackageJsonData
import com.intellij.javascript.nodejs.PackageJsonDependency
import com.intellij.lang.javascript.buildTools.npm.PackageJsonUtil
import com.intellij.lang.javascript.modules.NodeModuleUtil
import com.intellij.openapi.vfs.VirtualFile
import org.eclipse.scout.sdk.core.util.SdkException

object NodeModuleUtils {

    /**
     * Find all dependencies for a module. The [moduleDir] must contain a "package.json" file.
     */
    fun findDependenciesInNodeModulesDirs(moduleDir: VirtualFile) = findDependenciesInNodeModulesDirs(moduleDir, PackageJsonDependency.dependencies)

    /**
     * Find all dependencies of the given [dependencyType] for a module. The [moduleDir] must contain a "package.json" file.
     */
    private fun findDependenciesInNodeModulesDirs(moduleDir: VirtualFile, dependencyType: PackageJsonDependency): Collection<VirtualFile> {
        val modules = HashMap<String, VirtualFile>()
        val packageJson = PackageJsonUtil.findChildPackageJsonFile(moduleDir) ?: throw SdkException("Cannot find 'package.json' for module '{}'.", moduleDir)
        val packageJsonData = PackageJsonData.getOrCreate(packageJson)
        val dependencies = packageJsonData.allDependencyEntries
            .filter { it.value.dependencyType == dependencyType }
            .mapNotNull { it.key }
            .toMutableSet()

        processUpNodeModulesDirs(moduleDir) {
            collectDependenciesInNodeModulesDir(it, modules, dependencies)
            dependencies.removeAll(modules.keys)
            dependencies.isNotEmpty()
        }

        return modules.values
    }

    /**
     * Collect [dependencies] in a "node_modules" directory and add them to [collector] if absent.
     */
    private fun collectDependenciesInNodeModulesDir(nodeModulesDir: VirtualFile, collector: MutableMap<String, VirtualFile>, dependencies: Set<String>) {
        dependencies.forEach { dependency ->
            val child = nodeModulesDir.findFileByRelativePath(dependency)
            if (child != null && child.isDirectory && child.isValid) {
                collector.computeIfAbsent(dependency) { child }
            }
        }
    }

    /**
     * Processes "node_modules" directories inside the [startDir] and all of its parents.
     *
     * @param processor Process a "node_modules" directory. Return true to continue.
     */
    private fun processUpNodeModulesDirs(startDir: VirtualFile, processor: (VirtualFile) -> Boolean) {
        var dir: VirtualFile? = startDir
        while (dir != null) {
            NodeModuleUtil.findChildNodeModulesDir(dir)
                ?.let { if (!processor(it)) return }
            dir = dir.parent
        }
    }
}