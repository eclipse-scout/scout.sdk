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

import com.intellij.util.Function
import org.jetbrains.concurrency.AsyncPromise
import org.jetbrains.concurrency.CancellablePromise
import org.jetbrains.concurrency.Promise
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit
import java.util.function.Consumer

/**
 * Unwraps [ExecutionException] when getting the result from the future
 */
internal class WrappingCancellablePromise<T>(private val inner: CancellablePromise<T>) : AsyncPromise<T>() {

    override fun blockingGet(timeout: Int) = try {
        inner.blockingGet(timeout)
    } catch (e: Throwable) {
        throw unwrap(e)
    }

    override fun blockingGet(timeout: Int, timeUnit: TimeUnit) = try {
        inner.blockingGet(timeout, timeUnit)
    } catch (e: Throwable) {
        throw unwrap(e)
    }

    override fun get(): T = try {
        inner.get()
    } catch (e: Throwable) {
        throw unwrap(e)
    }

    override fun get(timeout: Long, unit: TimeUnit): T = try {
        inner.get(timeout, unit)
    } catch (e: Throwable) {
        throw unwrap(e)
    }

    private fun unwrap(e: Throwable): Throwable {
        val executionException = e as? ExecutionException
        return executionException?.cause ?: e
    }

    override fun <SUB_RESULT : Any?> then(done: Function<in T, out SUB_RESULT>): CancellablePromise<SUB_RESULT> =
            WrappingCancellablePromise(inner.then(done) as CancellablePromise<SUB_RESULT>)

    override fun <SUB_RESULT : Any?> thenAsync(doneF: Function<in T, out Promise<SUB_RESULT>>): CancellablePromise<SUB_RESULT> =
            WrappingCancellablePromise(inner.thenAsync(doneF) as CancellablePromise<SUB_RESULT>)

    override fun onSuccess(handler: Consumer<in T>): AsyncPromise<T> =
            WrappingCancellablePromise(inner.onSuccess(handler))

    override fun onError(rejected: Consumer<in Throwable>): AsyncPromise<T> =
            WrappingCancellablePromise(inner.onError(rejected))

    override fun processed(child: Promise<in T>): CancellablePromise<T> =
            WrappingCancellablePromise(inner.processed(child) as CancellablePromise<T>)

    override fun onProcessed(processed: Consumer<in T?>) =
            WrappingCancellablePromise(inner.onProcessed(processed))

    override fun getState() = inner.state

    override fun cancel() = inner.cancel()

    override fun cancel(mayInterruptIfRunning: Boolean) = inner.cancel(mayInterruptIfRunning)

    override fun isCancelled() = inner.isCancelled

    override fun isDone() = inner.isDone
}