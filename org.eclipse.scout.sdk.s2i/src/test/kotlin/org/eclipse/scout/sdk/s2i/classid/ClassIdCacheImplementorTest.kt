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
package org.eclipse.scout.sdk.s2i.classid

import com.intellij.openapi.project.Project
import junit.framework.TestCase
import org.mockito.Mockito.mock
import java.util.Collections.singleton

class ClassIdCacheImplementorTest : TestCase() {

    private val m_fileName = "file"

    fun testAnnotationAdded() {
        createCache(mutableMapOf("a" to "1", "b" to "2", "c" to "3"))
                .updateCacheWith(mutableMapOf("a" to "1", "b" to "2", "c" to "3", "d" to "4"))
    }

    fun testAnnotationRemoved() {
        createCache(mutableMapOf("a" to "1", "b" to "2", "c" to "3"))
                .updateCacheWith(mutableMapOf("a" to "1", "b" to "2"))
    }

    fun testAnnotationChanged() {
        createCache(mutableMapOf("a" to "1", "b" to "2", "c" to "3"))
                .updateCacheWith(mutableMapOf("a" to "1", "b" to "4", "c" to "3"))
    }

    fun testNewFile() {
        val cache = createCache(mutableMapOf("a" to "1", "b" to "2", "c" to "3"))
        val newFileName = "newFile"
        cache.updateCacheWith(mutableMapOf("x" to "9"), newFileName)
        assertEquals(4, cache.classIdCache().size)
        assertEquals(singleton("x"), cache.typesWithClassId("9"))
        assertEquals(setOf(m_fileName, newFileName), cache.fileCache().keys)
    }

    fun testFileRemoved() {
        createCache(mutableMapOf("a" to "1", "b" to "2", "c" to "3"))
                .updateCacheWith(mutableMapOf())
    }

    private fun ClassIdCacheImplementor.updateCacheWith(newMappings: MutableMap<String /* fqn */, String /* classid value */>, file: String = m_fileName) {
        updateCache(file, newMappings)
        if (m_fileName == file) {
            assertCacheState(newMappings)
        }
    }

    private fun ClassIdCacheImplementor.assertCacheState(state: MutableMap<String /* fqn */, String /* classid value */>) {
        assertEquals(if (state.isEmpty()) null else state, fileCache()[m_fileName])
        val typesByClassId = state.entries.groupBy { it.value }
                .mapValues { it.value.map { e -> e.key }.toSet() }
        assertEquals(typesByClassId, classIdCache())
    }

    private fun createCache(initialMap: MutableMap<String /* fqn */, String /* classid */>): ClassIdCacheImplementor {
        val cache = ClassIdCacheImplementor(mock(Project::class.java))
        initialMap.forEach { cache.updateOrAddType(it.key, it.value, null) }
        cache.fileCache()[m_fileName] = initialMap
        return cache
    }
}