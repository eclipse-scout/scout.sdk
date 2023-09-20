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
internal class WrappingCancellablePromise<T>(private val m_inner: CancellablePromise<T>) : AsyncPromise<T>() {

    override fun blockingGet(timeout: Int) = try {
        m_inner.blockingGet(timeout)
    } catch (e: Throwable) {
        throw unwrap(e)
    }

    override fun blockingGet(timeout: Int, timeUnit: TimeUnit) = try {
        m_inner.blockingGet(timeout, timeUnit)
    } catch (e: Throwable) {
        throw unwrap(e)
    }

    override fun get(): T = try {
        m_inner.get()
    } catch (e: Throwable) {
        throw unwrap(e)
    }

    override fun get(timeout: Long, unit: TimeUnit): T = try {
        m_inner.get(timeout, unit)
    } catch (e: Throwable) {
        throw unwrap(e)
    }

    private fun unwrap(e: Throwable): Throwable {
        val executionException = e as? ExecutionException
        return executionException?.cause ?: e
    }

    override fun <SUB_RESULT : Any?> then(done: Function<in T, out SUB_RESULT>): CancellablePromise<SUB_RESULT> =
        WrappingCancellablePromise(m_inner.then(done) as CancellablePromise<SUB_RESULT>)

    override fun <SUB_RESULT : Any?> thenAsync(doneF: Function<in T, out Promise<SUB_RESULT>>): CancellablePromise<SUB_RESULT> =
        WrappingCancellablePromise(m_inner.thenAsync(doneF) as CancellablePromise<SUB_RESULT>)

    override fun onSuccess(handler: Consumer<in T>): AsyncPromise<T> =
        WrappingCancellablePromise(m_inner.onSuccess(handler))

    override fun onError(rejected: Consumer<in Throwable>): AsyncPromise<T> =
        WrappingCancellablePromise(m_inner.onError(rejected))

    override fun processed(child: Promise<in T>): CancellablePromise<T> =
        WrappingCancellablePromise(m_inner.processed(child) as CancellablePromise<T>)

    override fun onProcessed(processed: Consumer<in T?>) =
        WrappingCancellablePromise(m_inner.onProcessed(processed))

    override fun getState() = m_inner.state

    override fun cancel() = m_inner.cancel()

    override fun cancel(mayInterruptIfRunning: Boolean) = m_inner.cancel(mayInterruptIfRunning)

    override fun isCancelled() = m_inner.isCancelled

    override fun isDone() = m_inner.isDone
}