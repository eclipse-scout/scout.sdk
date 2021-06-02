/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.s2i.environment

import com.intellij.openapi.project.Project
import junit.framework.TestCase
import org.mockito.Mockito.mock
import java.nio.file.Path
import java.nio.file.Paths

class TransactionManagerTest : TestCase() {

    fun testReplacement() {
        val mgr = TransactionManager(mock(Project::class.java))

        val member0 = TestingMember(Paths.get("test.java"))
        mgr.register(member0)
        assertEquals(1, mgr.size())

        mgr.register(TestingMember(Paths.get("test.java"), false))
        assertEquals(2, mgr.size())

        mgr.register(TestingMember(Paths.get("test2.java")))
        assertEquals(3, mgr.size())

        mgr.register(TestingMember(Paths.get("test3.java")))
        assertEquals(4, mgr.size())

        val member1 = TestingMember(Paths.get("test.java"))
        mgr.register(member1) // replaces the first and the second item
        assertEquals(3, mgr.size())
        val members = mgr.members()
        assertEquals(0, members.count { it === member0 })
        assertEquals(1, members.count { it === member1 })

        // assert the member1 is stored at the original location (order is not affected if overwriting)
        assertSame(member1, members.first())
    }

    fun testTransactionManager() {
        val mgr = TransactionManager(mock(Project::class.java))
        val member1 = TestingMember(Paths.get("test.java"))
        val member2 = TestingMember(Paths.get("test.java"), false)
        val member3 = TestingMember(Paths.get("test.js"))
        val member4 = TestingMember(Paths.get("a.xml"))
        mgr.register(member1)
        mgr.register(member2)
        mgr.register(member3)
        mgr.register(member4)
        assertEquals(4, mgr.size())
    }

    fun testUnwrap() {
        val exWithoutCause = RuntimeException()
        val exWithCause = RuntimeException(exWithoutCause)
        assertSame(exWithoutCause, TransactionManager.unwrap(exWithoutCause))
        assertSame(exWithoutCause, TransactionManager.unwrap(exWithCause))
    }

    private class TestingMember(val file: Path, val replaces: Boolean = true) : TransactionMember {
        override fun file(): Path {
            return file
        }

        override fun commit(progress: IdeaProgress): Boolean = true

        override fun replaces(member: TransactionMember) = replaces
    }
}