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

import org.eclipse.scout.sdk.core.generator.type.PrimaryTypeGenerator
import org.eclipse.scout.sdk.core.log.SdkLog
import org.eclipse.scout.sdk.s2i.AbstractTestCaseWithRunningClasspathModule
import org.eclipse.scout.sdk.s2i.environment.IdeaEnvironment.Factory.callInIdeaEnvironment


class IdeaEnvironmentTest : AbstractTestCaseWithRunningClasspathModule() {

    fun testAsyncWriteOperationInheritsTransaction() {
        callInIdeaEnvironment(project, "Test") { env, progress ->
            val sourceFolder = env.toScoutJavaEnvironment(module)?.primarySourceFolder()?.orElse(null) ?: throw IllegalArgumentException("Could not find primary testing source folder")
            val generator = PrimaryTypeGenerator.create()
                .withElementName("Test")
                .withPackageName("a.b.c")
            env.writeCompilationUnitAsync(generator, sourceFolder, progress).awaitDoneThrowingOnError()

            assertEquals(1, TransactionManager.current().size()) // written compilation unit is registered in transaction of main thread
        }.awaitDoneThrowingOnError()
        waitForWorkerThreadsFinished()
    }

    private fun waitForWorkerThreadsFinished() {
        // IJ test runner uses ThreadTracker.checkLeak() in tearDown() to let the test fail if there are leaking threads detected.
        // A thread is considered leaking if it is new (since the test start) and not parked in the pool at the end of the test.
        // Sometimes this check is faster than our async worker threads are parked in the pool (even the future is already completed).
        // Therefore, give the workers some time (after the future completion) to be parked in the pool.
        try {
            Thread.sleep(1000)
        } catch (ie: InterruptedException) {
            SdkLog.debug("Interrupted while waiting for workers to be parked.", ie)
        }
    }
}