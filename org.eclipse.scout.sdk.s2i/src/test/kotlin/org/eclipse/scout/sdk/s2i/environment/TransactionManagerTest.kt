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
package org.eclipse.scout.sdk.s2i.environment

import com.intellij.openapi.project.Project
import junit.framework.TestCase
import org.mockito.Mockito.mock
import java.nio.file.Path
import java.nio.file.Paths

class TransactionManagerTest : TestCase() {
    fun testTransactionManager() {
        val mgr = TransactionManager(mock(Project::class.java))
        val member1 = TestingMember(Paths.get("test.java"))
        val member2 = TestingMember(Paths.get("test.java"))
        val member3 = TestingMember(Paths.get("test.js"))
        val member4 = TestingMember(Paths.get("a.xml"))
        mgr.register(member1)
        mgr.register(member2)
        mgr.register(member3)
        mgr.register(member4)
        assertEquals(4, mgr.size())
    }

    private class TestingMember(val file: Path) : TransactionMember {
        override fun file(): Path {
            return file
        }

        override fun commit(progress: IdeaProgress): Boolean = true
    }
}