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

import org.eclipse.scout.sdk.core.typescript.model.api.JsonPointer
import org.eclipse.scout.sdk.s2i.model.AbstractPackageJsonTest
import java.math.BigDecimal

class IdeaPackageJsonTestNpm : AbstractPackageJsonTest("npm") {

    fun testFindProperty() {
        val json = getOrCreateModule("types").packageJson()
        assertEquals("@sdk/testing", json.propertyAsString("name").orElseThrow())
        assertEquals("23.1.0-snapshot", json.propertyAsString("version").orElseThrow())
        assertFalse(json.propertyAsString("missing").isPresent)
        assertEquals(BigDecimal("1"), json.findPropertyAsNumber(JsonPointer.compile("/numeric1")).orElseThrow())
        assertEquals(BigDecimal("1.34"), json.findPropertyAsNumber(JsonPointer.compile("/numeric2")).orElseThrow())
        assertEquals(BigDecimal("1.34"), json.findPropertyAsNumber(JsonPointer.compile("/numeric2")).orElseThrow())
        assertEquals(listOf("s", BigDecimal("1"), BigDecimal("1234.6789"), false, mapOf<String, Any>("inArr" to true, "strInArr" to "strInArr")), json.findPropertyAsArray(JsonPointer.compile("/arr")).orElseThrow())
        assertTrue(json.findPropertyAsBoolean(JsonPointer.compile("/bool")).orElseThrow())
        assertFalse(json.propertyAsString("null").isPresent) // currently null properties are the same as if the property is not available. Maybe change this in the future
        assertEquals(mapOf("nestedObj" to mapOf<String, Any>("a" to BigDecimal("100.234"), "b" to "b"), "nestedBool" to true, "nestedString" to "str"), json.findPropertyAsObject(JsonPointer.compile("/obj")).orElseThrow())
    }
}