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
package org.eclipse.scout.sdk.core.s.environment;

import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Future;
import java.util.function.Supplier;

/**
 * <h3>{@link IFuture}</h3>
 * <p>
 * Represents a {@link Future} and {@link CompletionStage} (also known as promise) with some convenience functions.
 *
 * @since 7.0.0
 * @see Future
 * @see CompletionStage
 */
public interface IFuture<V> extends Future<Supplier<V>>, CompletionStage<Supplier<V>> {

  /**
   * Returns the result value when complete, or throws an (unchecked) exception if completed exceptionally. To better
   * conform with the use of common functional forms, if a computation involved in the completion of this
   * {@link IFuture} threw an exception, this method throws an (unchecked) {@link CompletionException} with the
   * underlying exception as its cause.
   * <p>
   * If the original (unchecked) exception should be thrown instead of a wrapping {@link CompletionException}, use
   * {@link #result()} instead.
   *
   * @return A {@link Supplier} that returns the result value
   * @throws CancellationException
   *           if the computation was canceled. See {@link #cancel(boolean)} for more details.
   * @throws CompletionException
   *           if this future completed exceptionally or a completion computation threw an exception.
   */
  Supplier<V> join();

  /**
   * Returns the result value (or throws any encountered exception) if completed, else returns the given valueIfAbsent
   * {@link Supplier}.
   *
   * @param valueIfAbsent
   *          the value to return if not completed
   * @return the result value, if completed, else the given valueIfAbsent
   * @throws CancellationException
   *           if the computation was canceled. See {@link #cancel(boolean)} for more details.
   * @throws CompletionException
   *           if this future completed exceptionally or a completion computation threw an exception
   * @see #join()
   */
  Supplier<V> getNow(Supplier<V> valueIfAbsent);

  /**
   * Returns {@code true} if this {@link IFuture} completed exceptionally, in any way. Possible causes include
   * cancellation, an exception was thrown in the executed task and abrupt termination of a {@link CompletionStage}
   * action.
   *
   * @return {@code true} if this {@link IFuture} completed exceptionally
   */
  boolean isCompletedExceptionally();

  /**
   * Waits if necessary for the computation to complete, and then retrieves its result. This method call blocks until
   * the asynchronous task of this {@link IFuture} is completed. A future is completed if it ends successfully, threw an
   * exception or was canceled (see {@link #cancel(boolean)} for more details).
   * <p>
   * If completion computations have been registered (e.g. by using {@link #thenRun(Runnable)}), the method call to this
   * {@link #result()} method returns before (!) the completion stage is finished. It returns as soon as the result is
   * available. To wait for completion tasks use the {@link CompletableFuture#join() join} method on the result of the
   * completion task registration.
   * <p>
   * If an {@link Exception} occurred during the execution of the task, this {@link Exception} is re thrown here. If the
   * original {@link Exception} was a checked {@link Exception} it is wrapped into a {@link CompletionException}. In
   * that case the original {@link Exception} can be obtained using {@link Exception#getCause()}.
   * <p>
   * If the future was canceled, this method throws a {@link CancellationException}.
   * <p>
   * The difference to {@link #get()} is that this method does not make use of checked {@link Exception}s and
   * automatically unwraps unchecked exceptions throwing the original exception if possible.<br>
   * The difference to {@link #join()} is that this method automatically unwraps unchecked exceptions throwing the
   * original exception if possible (instead of always throwing a {@link CompletionException}).
   * <p>
   * Use {@link #get(long, java.util.concurrent.TimeUnit)} to specify a timeout.
   *
   * @return the computed result
   * @throws RuntimeException
   *           if a {@link RuntimeException} was thrown while executing this {@link IFuture}.
   * @throws CompletionException
   *           if this future completed with a checked exception or a completion computation threw a checked exception.
   *           The original checked exception can be obtained using {@link Exception#getCause()}.
   * @throws CancellationException
   *           if the future was canceled.
   * @see #join()
   * @see #get()
   * @see #cancel(boolean)
   * @see #isCompletedExceptionally()
   * @see #isCancelled()
   */
  V result();

  /**
   * Same as {@link #join()} but returns this {@link IFuture} instead of the result value.
   *
   * @return this
   */
  IFuture<V> awaitDoneThrowingOnErrorOrCancel();

  /**
   * Same as {@link #awaitDoneThrowingOnErrorOrCancel()} but does not throw an {@link CancellationException} but
   * silently ignores it.
   *
   * @return this
   */
  IFuture<V> awaitDoneThrowingOnError();

  /**
   * Attempts to cancel execution of this task. This attempt will fail if the task has already completed, has already
   * been canceled, or could not be canceled for some other reason.
   * <p>
   * If successful, and this task has not started when {@code cancel} is called, this task should never run. If the task
   * has already started, then the {@code mayInterruptIfRunning} parameter determines whether the thread executing this
   * task should be interrupted in an attempt to stop the task.
   * <p>
   * After this method returns, subsequent calls to {@link #isDone} will always return {@code true}. Subsequent calls to
   * {@link #isCancelled} will always return {@code true} if this method returned {@code true}.
   * <p>
   * After this method returns (and if the result was {@code true}) any waiting threads are no longer blocked and return
   * a {@code null} result (for {@link #result()}) or throw a {@link CancellationException} (for {@link #get()},
   * {@link #join()}, etc.). {@link #awaitDoneThrowingOnErrorOrCancel()} just returns without any result.<br>
   * Please note that even though the threads are no longer blocked the thread executing this task might continue to run
   * (depending on the implementation). But the result of this {@link IFuture} will remain as described above even if an
   * exception occurs in the task after cancellation. This exception will then be swallowed!
   *
   * @param mayInterruptIfRunning
   *          {@code true} if the thread executing this task should be interrupted; otherwise, in-progress tasks are
   *          allowed to complete
   * @return {@code false} if the task could not be canceled, typically because it has already completed normally;
   *         {@code true} otherwise
   */
  @Override
  boolean cancel(boolean mayInterruptIfRunning);

}
