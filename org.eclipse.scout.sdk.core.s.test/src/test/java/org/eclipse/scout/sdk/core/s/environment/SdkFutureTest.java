/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.environment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

import org.eclipse.scout.sdk.core.s.environment.SdkFuture.CompositeException;
import org.junit.jupiter.api.Test;

/**
 * <h3>{@link SdkFutureTest}</h3>
 *
 * @since 7.0.0
 */
public class SdkFutureTest {
  @Test
  public void testResultPresent() throws InterruptedException, ExecutionException, TimeoutException {
    var input = "abc";
    var f = SdkFuture.completed(input);
    assertFalse(f.cancel(true));
    assertFalse(f.isCancelled());
    assertFalse(f.isCompletedExceptionally());
    assertTrue(f.isDone());
    assertEquals(input, f.get().get());
    assertEquals(input, f.result());
    assertEquals(input, f.get(1, TimeUnit.SECONDS).get());

    var done = new StringBuilder();
    f
        .thenApply(Supplier::get)
        .thenAccept(done::append);
    f.awaitDoneThrowingOnErrorOrCancel();
    assertEquals(input, done.toString());
  }

  @Test
  public void testNoResult() throws InterruptedException, ExecutionException {
    IFuture<String> f = SdkFuture.completed(null);
    assertNull(f.get().get());
    assertNull(f.result());
  }

  @Test
  public void testRuntimeException() {
    var t = new RuntimeException();
    var f = SdkFuture.completed(null, t);
    assertSame(t, assertThrows(RuntimeException.class, f::result));
  }

  @Test
  public void testException() {
    var t = new Exception("msg");
    var f = SdkFuture.completed(null, t);
    assertFalse(f.cancel(true));
    assertFalse(f.isCancelled());
    assertTrue(f.isCompletedExceptionally());
    assertTrue(f.isDone());
    assertSame(t, assertThrows(ExecutionException.class, f::get).getCause());
    assertSame(t, assertThrows(ExecutionException.class, () -> f.get(1, TimeUnit.SECONDS)).getCause());
    assertSame(t, assertThrows(CompletionException.class, f::awaitDoneThrowingOnErrorOrCancel).getCause());

    var input = "abc";
    var done = new StringBuilder();
    f.exceptionally(ex -> {
      done.append(input);
      return () -> null;
    });
    assertEquals(input, done.toString());

    assertSame(t, assertThrows(RuntimeException.class, f::awaitDoneThrowingOnErrorOrCancel).getCause());
  }

  @Test
  public void testCompositeException() {
    var a = new RuntimeException("first exception");
    var nested = new IllegalStateException("nested");
    var b = new IllegalArgumentException("second", nested);

    var exceptionText = new CompositeException(Arrays.asList(a, b)).toString();
    assertTrue(exceptionText.contains(a.getMessage()));
    assertTrue(exceptionText.contains(b.getMessage()));
    assertTrue(exceptionText.contains(nested.getMessage()));
  }
}
