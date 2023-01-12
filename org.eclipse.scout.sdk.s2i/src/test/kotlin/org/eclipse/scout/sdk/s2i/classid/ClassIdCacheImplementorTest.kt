/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2i.classid

import com.intellij.openapi.project.Project
import junit.framework.TestCase
import org.mockito.Mockito.mock

class ClassIdCacheImplementorTest : TestCase() {

    fun testUsageByClassId() {
        val cache = createCache(mutableMapOf(
                "A.java" to mutableMapOf("a" to "1", "b" to "2", "c" to "3"),
                "B.java" to mutableMapOf("d" to "4", "e" to "2", "f" to "5")
        ))
        val usage = cache.usageByClassId()
        assertEquals(5, usage.size)
        assertEquals(listOf("a"), usage["1"])
        assertEquals(2, usage["2"]?.size)
        assertEquals(listOf("c"), usage["3"])
        assertEquals(listOf("d"), usage["4"])

        assertTrue(usage["1"]?.contains("a") ?: false)
        assertTrue(usage["2"]?.contains("e") ?: false)
        assertTrue(usage["2"]?.contains("b") ?: false)

        assertEquals(1, cache.duplicates().size)
        assertEquals(1, cache.duplicates("A.java").size)
    }

    private fun createCache(initialMap: MutableMap<String /* file name */, MutableMap<String /* fqn */, String /* classid */>>): ClassIdCacheImplementor {
        val cache = ClassIdCacheImplementor(mock(Project::class.java))
        initialMap.forEach { cache.fileCache()[it.key] = it.value }
        return cache
    }
}