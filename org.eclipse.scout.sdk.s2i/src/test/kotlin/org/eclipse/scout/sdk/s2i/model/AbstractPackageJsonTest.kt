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

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtil
import junit.framework.TestCase
import org.eclipse.scout.sdk.core.typescript.model.api.INodeModule
import org.eclipse.scout.sdk.core.util.Ensure
import org.eclipse.scout.sdk.s2i.model.typescript.IdeaNodeModules
import org.mockito.Mockito
import java.nio.file.Path
import java.nio.file.Paths

abstract class AbstractPackageJsonTest(fixturePath: String) : TestCase() {

    protected val myAllFixturesPath = "src/test/resources/model/dependencies"
    protected val myFixtureRoot: Path = Paths.get("").toAbsolutePath().resolve(myAllFixturesPath).resolve(fixturePath)
    protected val myProject: Project = Mockito.mock(Project::class.java, "project")
    protected val myNodeModules = IdeaNodeModules(myProject)

    fun testCore() {
        assertCore(getOrCreateModule("core"))
    }

    fun testTesting() {
        assertTesting(getOrCreateModule("testing"))
    }

    fun testLayer() {
        assertLayer(getOrCreateModule("layer"))
    }

    fun testProject() {
        assertProject(getOrCreateModule("project"))
    }

    protected fun assertJquery(jquery: INodeModule) {
        assertEquals("jquery", jquery.name())
        assertVersion("3.6.3", jquery)
        assertEquals(0, jquery.packageJson().dependencies().count())
    }

    protected fun assertCore(core: INodeModule) {
        assertEquals("@sdk/core", core.name())
        assertVersion("23.1.0-snapshot", core)
        assertEquals(1, core.packageJson().dependencies().count())
        assertJquery(core.packageJson().dependency("jquery").orElseThrow())
    }

    protected fun assertTesting(testing: INodeModule) {
        assertEquals("@sdk/testing", testing.name())
        assertVersion("23.1.0-snapshot", testing)
        assertEquals(1, testing.packageJson().dependencies().count())
        assertCore(testing.packageJson().dependency("@sdk/core").orElseThrow())
    }

    protected fun assertLayer(layer: INodeModule) {
        assertEquals("@sdk/layer", layer.name())
        assertVersion("23.1.0-snapshot", layer)
        assertEquals(1, layer.packageJson().dependencies().count())
        assertCore(layer.packageJson().dependency("@sdk/core").orElseThrow())
    }

    protected fun assertProject(project: INodeModule) {
        assertEquals("@sdk/project", project.name())
        assertVersion("23.1.0-snapshot", project)
        assertEquals(1, project.packageJson().dependencies().count())
        assertLayer(project.packageJson().dependency("@sdk/layer").orElseThrow())
    }

    protected fun assertVersion(expectedVersion: String, module: INodeModule) {
        val packageJson = module.packageJson()
        assertNotNull(packageJson)
        assertEquals(expectedVersion, packageJson.version())
    }

    protected fun getOrCreateModule(relPath: String) = VfsUtil.findFile(myFixtureRoot.resolve(relPath), true)
        ?.let { myNodeModules.getOrCreateModule(it) }
        ?.api()
        ?: throw Ensure.newFail("INodeModule for path {} not found.", relPath)
}