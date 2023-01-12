/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2i.util

import junit.framework.TestCase
import java.util.regex.Pattern

class VelocityRunnerTest : TestCase() {

    /**
     * tests that properties are replaced and that if variables can be used
     */
    fun testReplaceAndIf() {
        val propertyValue = "propVal"
        val result = VelocityRunner()
                .withProperty("TEXTS", propertyValue)
                .withProperty("dd", false)
                .eval("start\$nls\$end\n\${TEXTS}.get()\n\$dd\n#if( \$dd )static #end class\$dollar")
        assertEquals("start\$nls\$end\n${propertyValue}.get()\nfalse\n class$", result.toString())
    }

    /**
     * Tests that the post processors are executed after the velocity evaluation
     */
    fun testWithPostProcessor() {
        val result = VelocityRunner()
                .withProperty("xx", "zz")
                .withPostProcessor(Pattern.compile("zz(\\d+)zz")) { '>' + it.group(1) + '<' }
                .eval("\${xx}123\${xx}\${xx}456\${xx}")
        assertEquals(">123<>456<", result.toString())
    }
}