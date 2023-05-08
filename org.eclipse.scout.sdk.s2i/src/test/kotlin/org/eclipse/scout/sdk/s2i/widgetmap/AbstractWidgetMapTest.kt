/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2i.widgetmap

import com.intellij.openapi.vfs.VirtualFile
import org.eclipse.scout.sdk.core.testing.CoreTestingUtils
import org.eclipse.scout.sdk.core.typescript.model.api.NodeModulesProvider
import org.eclipse.scout.sdk.core.typescript.model.spi.NodeModuleSpi
import org.eclipse.scout.sdk.core.typescript.model.spi.NodeModulesProviderSpi
import org.eclipse.scout.sdk.s2i.model.AbstractModelTest
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*

abstract class AbstractWidgetMapTest(val modelPath: String, fixturePath: String = "project") : AbstractModelTest(fixturePath) {

    protected lateinit var myModelFile: VirtualFile

    override fun getTestDataPath() = "src/test/resources/widgetmap"

    open fun modelName() = "SomeFormModel"

    open fun modelFileName() = "${modelName()}.ts"

    open fun expectedModelName() = "${modelName()}Expected"

    open fun expectedModelFileName() = "${expectedModelName()}.ts"

    open fun modelTargetPath() = "src/someform"

    override fun setUp() {
        super.setUp()

        NodeModulesProvider.registerProvider(myFixture.project, object : NodeModulesProviderSpi {
            override fun create(nodeModuleDir: Path?): Optional<NodeModuleSpi> {
                val vf = nodeModuleDir?.toString()?.let { myFixture.findFileInTempDir(it) } ?: return Optional.empty()
                return Optional.ofNullable(myNodeModules.create(vf))
            }

            override fun remove(changedPath: Path?): Set<NodeModuleSpi> {
                val vf = changedPath?.toString()?.let { myFixture.findFileInTempDir(it) } ?: return emptySet()
                return myNodeModules.remove(vf)
            }

            override fun clear() {
                myNodeModules.clear()
            }
        })

        myModelFile = myFixture.copyFileToProject("$modelPath/${modelFileName()}", "${modelTargetPath()}/${modelFileName()}")
    }

    fun testWidgetMap() {
        WidgetMapUpdater.update(myModelFile, myFixture.project)
        assertEqualsIgnoreWhitespaces(Files.readString(Paths.get("$testDataPath/$modelPath/${expectedModelFileName()}")), psiManager.findFile(myModelFile)!!.text)
    }

    protected fun assertEqualsIgnoreWhitespaces(expected: CharSequence?, actual: CharSequence) {
        if (expected == null) {
            assertNull(actual)
            return
        }
        assertEquals(CoreTestingUtils.removeWhitespace(expected), CoreTestingUtils.removeWhitespace(actual))
    }
}