/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.s2i

import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.intellij.testFramework.fixtures.impl.CodeInsightTestFixtureImpl
import org.eclipse.scout.sdk.core.log.SdkLog

object IdeaTestingHelper {
    /**
     * Tries to remove the [com.intellij.testFramework.ThreadTracker] from the given fixture.
     * This is a workaround for wrong trackers that let the test fail if thread are parked by the thread-pool
     * using 'parkUntil' instead of 'park' (see com.intellij.testFramework.ThreadTracker.isIdleCommonPoolThread).
     */
    fun removeThreadTracker(fixture: CodeInsightTestFixture) {
        try {
            val testFixture = fixture as? CodeInsightTestFixtureImpl
            if (testFixture == null) {
                SdkLog.warning("Unable to remove ThreadTracker. Fixture is no CodeInsightTestFixtureImpl but '{}' instead.", fixture.javaClass.name)
                return
            }
            val myProjectFixtureField = CodeInsightTestFixtureImpl::class.java.getDeclaredField("myProjectFixture")
            myProjectFixtureField.isAccessible = true
            val ideaProjectTestFixture = myProjectFixtureField.get(testFixture)

            val projectTestFixtureClassName = "com.intellij.testFramework.fixtures.impl.HeavyIdeaTestFixtureImpl"
            if (ideaProjectTestFixture == null || ideaProjectTestFixture.javaClass.name != projectTestFixtureClassName) {
                SdkLog.warning("Unable to remove ThreadTracker. ProjectFixture is not '{}' but '{}' instead.", projectTestFixtureClassName, ideaProjectTestFixture.javaClass.name)
                return
            }
            val myThreadTrackerField = Class.forName(projectTestFixtureClassName).getDeclaredField("myThreadTracker")
            myThreadTrackerField.isAccessible = true
            myThreadTrackerField.set(ideaProjectTestFixture, null) // clear tracker so that it will not complain
        } catch (e: Throwable) {
            SdkLog.warning("Unable to remove ThreadTracker.", e)
        }
    }
}