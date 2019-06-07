package org.eclipse.scout.sdk.s2i.environment

import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase.assertThrows
import org.eclipse.scout.sdk.core.util.FinalValue
import org.junit.Assert
import java.util.concurrent.CancellationException
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class OperationTaskTest : LightPlatformCodeInsightFixtureTestCase() {

    fun testOperationWithException() {
        val ex = NullPointerException()
        val t = OperationTask("Test with Exception", project) { throw ex }
        val f = t.schedule<Any>()

        assertThrows<NullPointerException>(ex.javaClass) { f.result() }
        assertFalse(f.isCancelled)
        assertTrue(f.isDone)
        assertTrue(f.isCompletedExceptionally)

        try {
            f.awaitDoneThrowingOnErrorOrCancel()
            Assert.fail()
        } catch (e: NullPointerException) {
            assertSame(ex, e)
        }
    }

    fun testOperationCancelled() {
        val first = FinalValue<Boolean>()
        val second = FinalValue<Boolean>()
        val taskStarted = CountDownLatch(1)
        val cancelPerformed = CountDownLatch(1)

        val t = OperationTask("Cancel Test", project) {
            it.init("test", 2)
            first.set(true)
            taskStarted.countDown()
            cancelPerformed.await(1, TimeUnit.MINUTES)

            it.worked(1)
            second.set(true)
        }

        val canceler = CancelThread(taskStarted, cancelPerformed, t)
        canceler.start()

        val f = t.schedule<Any>()

        assertTrue(first.get())
        assertFalse(second.isSet)
        assertTrue(f.isCancelled)
        assertTrue(f.isDone)

        try {
            f.awaitDoneThrowingOnErrorOrCancel()
            Assert.fail()
        } catch (e: CancellationException) {
            assertNotNull(e)
        }
    }

    fun testOperationCancelledAfterFinish() {
        val t = OperationTask("Test", project) {}
        t.schedule<Any>().awaitDoneThrowingOnErrorOrCancel()
        val result = t.cancel()
        assertTrue(result)
    }

    private inner class CancelThread(private val waitUntilTaskReady: CountDownLatch, private val canceled: CountDownLatch, private val task: OperationTask) : Thread("cancel thread") {
        override fun run() {
            waitUntilTaskReady.await(1, TimeUnit.MINUTES)
            assertTrue(task.cancel())
            canceled.countDown()
        }
    }
}
