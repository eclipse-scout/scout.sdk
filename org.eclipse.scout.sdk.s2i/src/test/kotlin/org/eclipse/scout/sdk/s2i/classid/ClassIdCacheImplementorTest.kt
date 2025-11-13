/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
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
        assertEquals(listOf(ClassIdCache.ClassIdOccurrence("A.java", "a", "1")), usage["1"])
        assertEquals(2, usage["2"]?.size)
        assertEquals(listOf(ClassIdCache.ClassIdOccurrence("A.java", "c", "3")), usage["3"])
        assertEquals(listOf(ClassIdCache.ClassIdOccurrence("B.java", "d", "4")), usage["4"])

        assertTrue(usage["1"]?.any { it.fqn == "a" } ?: false)
        assertTrue(usage["2"]?.any { it.fqn == "e" } ?: false)
        assertTrue(usage["2"]?.any { it.fqn == "b" } ?: false)

        assertEquals(1, cache.duplicates().size)
        assertEquals(1, cache.duplicates("A.java").size)
    }

    fun testWithSameFqn() {
        val cache = createCache(
            mutableMapOf(
                "A.java" to mutableMapOf("a" to "1"),
                "B.java" to mutableMapOf("a" to "1", "b" to "2", "c" to "3")
            )
        )
        val usage = cache.usageByClassId()
        assertEquals(3, usage.size)
        assertEquals(2, usage["1"]?.size)

        val duplicates = cache.duplicates("A.java")
        assertEquals(1, duplicates.size)
        val occurrence = duplicates["1"]
        assertEquals(2, occurrence?.size)
        assertEquals("a", occurrence?.get(0)?.fqn)
        assertEquals("a", occurrence?.get(1)?.fqn)
        val files = occurrence?.map { it.path }?.toList()
        assertTrue(files?.contains("A.java") ?: false)
        assertTrue(files?.contains("B.java") ?: false)
    }

    private fun createCache(initialMap: MutableMap<String /* file name */, MutableMap<String /* fqn */, String /* classid */>>): ClassIdCacheImplementor {
        val cache = ClassIdCacheImplementor(mock(Project::class.java))
        initialMap.forEach { cache.fileCache()[it.key] = it.value }
        return cache
    }
}