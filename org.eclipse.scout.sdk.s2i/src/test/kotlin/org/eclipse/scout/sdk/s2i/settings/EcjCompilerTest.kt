/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2i.settings

import junit.framework.TestCase
import org.eclipse.scout.sdk.core.s.java.apidef.ScoutApi
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.io.path.name

class EcjCompilerTest : TestCase() {
    /**
     * Ensure the available jars matches the list of supported ecj versions.
     */
    fun testCompilerJarPresent() {
        val workingDir = Paths.get("").toAbsolutePath()
        val ecjDir = workingDir.resolve("src/main/resources/ecj")
        val expectedFiles = ScoutApi.allKnown()
            .map { it.ecjVersion() }
            .distinct()
            .sorted()
            .map { "ecj-$it.jar" }
            .toList()
        val actualFiles = Files.list(ecjDir)
            .map { it.name }
            .filter { it.endsWith(".jar") }
            .sorted()
            .toList()
        assertEquals(expectedFiles, actualFiles)
    }
}