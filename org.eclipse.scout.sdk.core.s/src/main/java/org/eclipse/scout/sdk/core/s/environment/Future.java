/*
 * Copyright (c) 2017 BSI Business Systems Integration AG.
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
import java.util.function.Supplier;

import org.eclipse.scout.sdk.core.log.SdkLog;

/**
 * <h3>{@link Future}</h3>
 *
 * @since 7.1.0
 */
public class Future<V> extends CompletableFuture<V> implements IFuture<V> {

  /**
   * Creates a completed {@link IFuture} with the specified result.
   *
   * @param result
   *          The result of the {@link IFuture}. May be {@code null}.
   * @return An already completed {@link IFuture} with the specified value.
   */
  public static <T> IFuture<T> completed(T result) {
    return completed(result, null);
  }

  /**
   * Creates a completed {@link IFuture} with the specified result or exception.
   *
   * @param result
   *          The result of the {@link IFuture}. May be {@code null}.
   * @param error
   *          The exception of the {@link IFuture}. May be {@code null}.
   * @return An already completed {@link IFuture} with the specified results.
   */
  public static <T> IFuture<T> completed(T result, Throwable error) {
    return completed(() -> result, error);
  }

  /**
   * Creates a completed {@link IFuture} that uses the result supplier specified to compute the value.
   *
   * @param resultExtractor
   *          The {@link Supplier} that provides the result of the resulting {@link IFuture}. The {@link Supplier} is
   *          only invoked if the provided error is {@code null}.
   * @param error
   *          The exception of the {@link IFuture}. May be {@code null}.
   * @return An already completed {@link IFuture} either holding the specified error (if not {@code null}) or the result
   *         as computed by the {@link Supplier} specified.
   */
  public static <T> IFuture<T> completed(Supplier<T> resultExtractor, Throwable error) {
    return new Future<T>().doCompletion(false, error, resultExtractor);
  }

  /**
   * Waits until all of the futures specified have completed. A future is completed if it ends successfully, threw an
   * exception or was cancelled.
   *
   * @param futures
   *          The futures to wait for
   * @throws RuntimeException
   *           if there was an exception during the execution of the future.
   */
  public static void awaitAll(Iterable<? extends IFuture<?>> futures) {
    if (futures == null) {
      return;
    }

    for (IFuture<?> future : futures) {
      if (future != null) {
        future.awaitDoneThrowingOnError();
      }
    }
  }

  @Override
  public Future<V> awaitDoneThrowingOnErrorOrCancel() {
    result();
    return this;
  }

  @Override
  public Future<V> awaitDoneThrowingOnError() {
    try {
      result();
    }
    catch (CancellationException e) {
      SdkLog.debug("Canellation silently ignored", e);
    }
    return this;
  }

  protected Future<V> doCompletion(boolean isCancelled, Throwable error, Supplier<V> resultExtractor) {
    if (isCancelled) {
      completeExceptionally(new CancellationException());
    }
    else {
      if (error == null) {
        if (resultExtractor == null) {
          complete(null);
        }
        else {
          complete(resultExtractor.get());
        }
      }
      else {
        completeExceptionally(error);
      }
    }
    return this;
  }

  @Override
  public V result() {
    try {
      return join();
    }
    catch (CompletionException e) {
      SdkLog.debug("Future completed with errors.", e);
      Throwable cause = e.getCause();
      if (cause instanceof RuntimeException) {
        throw (RuntimeException) cause;
      }
      throw e;
    }
  }
}
