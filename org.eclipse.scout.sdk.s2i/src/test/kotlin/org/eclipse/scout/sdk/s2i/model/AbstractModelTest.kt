/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2i.model

import com.intellij.openapi.project.rootManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.eclipse.scout.sdk.core.typescript.model.api.INodeModule
import org.eclipse.scout.sdk.core.typescript.model.api.IPackageJson
import org.eclipse.scout.sdk.core.util.SdkException
import org.eclipse.scout.sdk.s2i.model.typescript.IdeaNodeModules
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.function.Function
import java.util.stream.Collectors.toMap

abstract class AbstractModelTest(val fixturePath: String) : BasePlatformTestCase() {

    protected val allFixturesRoot: Path = Paths.get("").toAbsolutePath().resolve(testDataPath)
    protected val myFixtureRoot: Path = allFixturesRoot.resolve(fixturePath)
    protected val myNodeModulesRoot: Path = myFixtureRoot.resolve("node_modules")
    protected lateinit var myNodeModules: IdeaNodeModules
    protected lateinit var myIdeaNodeModule: INodeModule

    override fun getTestDataPath() = "src/test/resources/model"

    override fun setUp() {
        super.setUp()
        myFixture.copyDirectoryToProject(fixturePath, "")

        val module = myFixture.module
        val moduleRoot = module.rootManager.contentRoots.first()
        myNodeModules = IdeaNodeModules(module.project)
        myIdeaNodeModule = myNodeModules.create(moduleRoot)?.api() ?: throw IllegalArgumentException("'$moduleRoot' is no valid node module root.")
        myIdeaNodeModule.packageJson().jsonObject("dependencies").ifPresent {
            npmInstall(it.keys)
        }
    }

    protected fun npmInstall(dependencies: Set<String>) {
        if (dependencies.isEmpty()) return

        val availableModules = Files.walk(allFixturesRoot, 3)
            .filter { it.fileName.toString() == IPackageJson.FILE_NAME }
            .map { IPackageJson.parse(it.parent) }
            .collect(toMap(IPackageJson::name, Function.identity()))
        val requiredModules = dependencies
            .map { availableModules[it] ?: throw SdkException("Dependency '{}' of '{}' could not be found.", it, myFixtureRoot.resolve(IPackageJson.FILE_NAME)) }

        requiredModules.forEach { npmInstall(it) }
    }

    protected fun npmInstall(dependency: IPackageJson) {
        val sourcePathRelative = allFixturesRoot
            .relativize(dependency.directory())
            .toString()
            .replace('\\', '/')
        val destinationPathRelative = myFixtureRoot
            .relativize(myNodeModulesRoot)
            .resolve(dependency.name())
            .toString()
            .replace('\\', '/')
        myFixture.copyDirectoryToProject(sourcePathRelative, destinationPathRelative)
    }
}