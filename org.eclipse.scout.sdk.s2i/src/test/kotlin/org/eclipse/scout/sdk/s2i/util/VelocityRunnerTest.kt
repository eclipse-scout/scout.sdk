/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.s2i.util

import junit.framework.TestCase
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes
import java.util.regex.Pattern

class VelocityRunnerTest : TestCase() {

    /**
     * tests that properties are replaced and that if variables can be used
     */
    fun testReplaceAndIf() {
        val result = VelocityRunner()
                .withProperty("TEXTS", IScoutRuntimeTypes.TEXTS)
                .withProperty("dd", false)
                .eval("start\$nls\$end\n\${TEXTS}.get()\n\$dd\n#if( \$dd )static #end class\$dollar")
        assertEquals("start\$nls\$end\n${IScoutRuntimeTypes.TEXTS}.get()\nfalse\n class$", result.toString())
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