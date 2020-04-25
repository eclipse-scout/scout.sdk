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

import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.util.Ref
import junit.framework.TestCase
import org.mockito.ArgumentMatchers
import org.mockito.Mockito

class IdeaProgressTest : TestCase() {

    private fun runWithIndicatorMock(test: (percent: Ref<Double>, progress: IdeaProgress) -> Unit) {
        val percent = Ref.create<Double>()
        val indicator = Mockito.mock(ProgressIndicator::class.java)
        Mockito.`when`(indicator.setFraction(ArgumentMatchers.anyDouble())).thenAnswer { inv ->
            percent.set(inv.getArgument(0))
        }
        val progress = IdeaProgress(indicator)
        test.invoke(percent, progress)
    }

    fun testProgressWithChild() = runWithIndicatorMock { percent, progress ->
        val delta = 0.0000001

        progress.init(10, "MyProgress")
        assertEquals(0.0, percent.get(), delta)

        progress.worked(3)
        assertEquals(0.3, percent.get(), delta)

        val child = progress.newChild(2)
        assertEquals(0.3, percent.get(), delta)

        child.init(4, "Child")
        assertEquals(0.3, percent.get(), delta)

        child.worked(3)
        assertEquals(0.45, percent.get(), delta)

        child.setWorkRemaining(2)
        assertEquals(0.4, percent.get(), delta)

        child.worked(2)
        assertEquals(0.5, percent.get(), delta)

        progress.worked(2)
        assertEquals(0.7, percent.get(), delta)

        progress.setWorkRemaining(1)
        assertEquals(0.9, percent.get(), delta)
    }

    fun testNewChildEnhancesProgress() = runWithIndicatorMock { percent, progress ->
        val delta = 0.0000001
        progress.init(10, "test")
        progress.newChild(2)
        assertEquals(0.0, percent.get(), delta)
        progress.newChild(3)
        assertEquals(0.2, percent.get(), delta)
        progress.newChild(1)
        assertEquals(0.5, percent.get(), delta)
    }
}
