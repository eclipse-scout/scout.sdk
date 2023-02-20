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

import org.eclipse.scout.sdk.core.typescript.model.api.INodeModule
import org.eclipse.scout.sdk.s2i.model.AbstractModelTest


class IdeaPackageJsonTest : AbstractModelTest("javascript/moduleWithExternalImports") {


    fun testVersion() {
        assertVersion("23.1.0-snapshot", myIdeaNodeModule)
    }

    fun testDependencies() {
        assertVersion("23.1.0-snapshot", myIdeaNodeModule)

        val packageJson = myIdeaNodeModule.packageJson()

        val sdkExportJs = packageJson.dependency("@eclipse-scout/sdk-export-js").orElseThrow()
        assertVersion("23.1.0-snapshot", sdkExportJs)

        val sdkEnumJs = packageJson.dependency("@eclipse-scout/sdk-enums-js").orElseThrow()
        assertVersion("23.1.0-snapshot", sdkEnumJs)
    }

    fun assertVersion(expectedVersion: String, module: INodeModule) {
        val packageJson = module.packageJson()
        assertNotNull(packageJson)
        assertEquals(expectedVersion, packageJson.version())
    }
}