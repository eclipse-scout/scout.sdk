/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2i.environment

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.eclipse.scout.sdk.core.util.FinalValue
import org.junit.Assert
import org.junit.jupiter.api.Assertions
import java.util.concurrent.CancellationException
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class OperationTaskTest : BasePlatformTestCase() {

    fun testOperationWithException() {
        val ex = NullPointerException()
        val t = OperationTask("Test with Exception", project) { throw ex }
        val f = t.schedule<Any>()

        Assertions.assertThrows(ex.javaClass) { f.result() } // do not use assertThrows of the super class as the API has changed in IJ 2021.1
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

    fun testOperationCanceled() {
        val first = FinalValue<Boolean>()
        val second = FinalValue<Boolean>()
        val taskStarted = CountDownLatch(1)
        val cancelPerformed = CountDownLatch(1)

        val t = OperationTask("Cancel Test", project) {
            it.init(2, "test")
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

    fun testOperationCanceledAfterFinish() {
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
