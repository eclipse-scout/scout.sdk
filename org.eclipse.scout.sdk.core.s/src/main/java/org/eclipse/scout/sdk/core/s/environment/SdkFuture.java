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

import static java.lang.System.lineSeparator;
import static java.util.Collections.unmodifiableCollection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Supplier;

import org.eclipse.scout.sdk.core.log.SdkLog;
import org.eclipse.scout.sdk.core.util.FinalValue;
import org.eclipse.scout.sdk.core.util.SdkException;
import org.eclipse.scout.sdk.core.util.Strings;

/**
 * <h3>{@link SdkFuture}</h3>
 *
 * @since 7.1.0
 */
public class SdkFuture<V> extends CompletableFuture<Supplier<V>> implements IFuture<V> {

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
    return new SdkFuture<T>().doCompletion(false, error, resultExtractor);
  }

  /**
   * Waits until all of the futures specified have completed. A future is completed if it ends successfully, threw an
   * exception or was canceled.<br>
   * If there is at least one {@link Future} that ended exceptionally, this method throws an exception as well.
   *
   * @param futures
   *          The futures to wait for
   * @throws CompositeException
   *           if at least one future completed exceptionally. This exception holds all {@link Throwable}s of the failed
   *           {@link Future}s.
   */
  @SuppressWarnings("squid:S1166") // "Exception handlers should preserve the original exceptions". this is given as the exceptions are collected and then thrown at once
  public static void awaitAll(Iterable<? extends Future<?>> futures) {
    if (futures == null) {
      return;
    }

    Collection<Throwable> errors = new ArrayList<>();
    for (Future<?> future : futures) {
      if (future == null) {
        continue;
      }

      try {
        future.get();
      }
      catch (ExecutionException e) {
        errors.add(e.getCause());
      }
      catch (InterruptedException e) {
        throw new SdkException(e);
      }
      catch (CancellationException e) {
        SdkLog.debug("Cancellation silently ignored", e);
      }
    }
    if (errors.isEmpty()) {
      return;
    }
    throw new CompositeException(errors);
  }

  /**
   * Waits until all of the futures specified have completed. A future is completed if it ends successfully, threw an
   * exception or was canceled.<br>
   * Futures that end exceptionally are logged with level error. The log is only written when all the futures have
   * completed.
   * 
   * @param futures
   *          The futures to wait for
   */
  public static void awaitAllLoggingOnError(Iterable<IFuture<?>> futures) {
    try {
      awaitAll(futures);
    }
    catch (CompositeException e) {
      e.exceptions().forEach(SdkLog::error);
    }
  }

  public static class CompositeException extends RuntimeException {

    private static final long serialVersionUID = -2565677977298841153L;
    private final Collection<Throwable> m_throwables;

    public CompositeException(Collection<Throwable> throwables) {
      super("Composite exception was thrown with embedded exceptions (see details before)");
      m_throwables = new ArrayList<>(throwables);
    }

    public Collection<Throwable> exceptions() {
      return unmodifiableCollection(m_throwables);
    }

    @Override
    public String toString() {
      StringBuilder builder = new StringBuilder("Nested Exception: [").append(lineSeparator());
      for (Throwable throwable : m_throwables) {
        builder.append(throwable.toString()).append(Strings.fromThrowable(throwable)).append(lineSeparator());
      }
      builder.append(']').append(lineSeparator()).append(super.toString());
      return builder.toString();
    }
  }

  @Override
  public SdkFuture<V> awaitDoneThrowingOnErrorOrCancel() {
    result();
    return this;
  }

  @Override
  public SdkFuture<V> awaitDoneThrowingOnError() {
    try {
      join();
    }
    catch (CancellationException e) {
      SdkLog.debug("Cancellation silently ignored", e);
    }
    return this;
  }

  protected SdkFuture<V> doCompletion(boolean isCanceled, Throwable error, Supplier<V> resultExtractor) {
    if (isCanceled) {
      completeExceptionally(new CancellationException());
    }
    else {
      if (error == null) {
        if (resultExtractor == null) {
          complete(() -> null); // the supplier should never be null. only the result provided by the supplier may be null
        }
        else {
          FinalValue<V> cachedResult = new FinalValue<>(); // use a final value so that the extractor is only executed once
          complete(() -> cachedResult.computeIfAbsentAndGet(resultExtractor));
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
      return join().get();
    }
    catch (CompletionException e) {
      SdkLog.debug("Future completed exceptionally.", e);
      Throwable cause = e.getCause();
      if (cause instanceof RuntimeException) {
        throw (RuntimeException) cause;
      }
      throw e;
    }
  }
}
