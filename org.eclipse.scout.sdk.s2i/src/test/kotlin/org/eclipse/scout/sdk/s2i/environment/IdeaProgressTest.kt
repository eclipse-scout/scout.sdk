package org.eclipse.scout.sdk.s2i.environment

import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.util.Ref
import junit.framework.TestCase
import org.mockito.ArgumentMatchers
import org.mockito.Mockito

class IdeaProgressTest : TestCase() {

    fun testProgressWithChild() {
        val delta = 0.0000001
        val percent = Ref.create<Double>()
        val indicator = Mockito.mock(ProgressIndicator::class.java)
        Mockito.`when`(indicator.setFraction(ArgumentMatchers.anyDouble())).thenAnswer { inv ->
            percent.set(inv.getArgument(0))
        }
        val progress = IdeaProgress(indicator)

        progress.init("MyProgress", 10)
        assertEquals(0.0, percent.get(), delta)

        progress.worked(3)
        assertEquals(0.3, percent.get(), delta)

        val child = progress.newChild(2)
        assertEquals(0.3, percent.get(), delta)

        child.init("Child", 4)
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
}