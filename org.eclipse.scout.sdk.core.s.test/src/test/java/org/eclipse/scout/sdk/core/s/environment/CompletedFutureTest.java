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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

/**
 * <h3>{@link CompletedFutureTest}</h3>
 *
 * @since 7.0.0
 */
public class CompletedFutureTest {
  @Test
  public void testResultPresent() throws InterruptedException, ExecutionException, TimeoutException {
    String input = "abc";
    IFuture<String> f = SdkFuture.completed(input);
    assertFalse(f.cancel(true));
    assertFalse(f.isCancelled());
    assertFalse(f.isCompletedExceptionally());
    assertTrue(f.isDone());
    assertEquals(input, f.get().get());
    assertEquals(input, f.result());
    assertEquals(input, f.get(1, TimeUnit.SECONDS).get());

    StringBuilder done = new StringBuilder();
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
    RuntimeException t = new RuntimeException();
    IFuture<Object> f = SdkFuture.completed(null, t);
    assertSame(t, assertThrows(RuntimeException.class, f::result));
  }

  @Test
  public void testException() {
    Exception t = new Exception("msg");
    IFuture<Object> f = SdkFuture.completed(null, t);
    assertFalse(f.cancel(true));
    assertFalse(f.isCancelled());
    assertTrue(f.isCompletedExceptionally());
    assertTrue(f.isDone());
    assertSame(t, assertThrows(ExecutionException.class, f::get).getCause());
    assertSame(t, assertThrows(ExecutionException.class, () -> f.get(1, TimeUnit.SECONDS)).getCause());
    assertSame(t, assertThrows(CompletionException.class, f::awaitDoneThrowingOnErrorOrCancel).getCause());

    String input = "abc";
    StringBuilder done = new StringBuilder();
    f.exceptionally(ex -> {
      done.append(input);
      return () -> null;
    });
    assertEquals(input, done.toString());

    assertSame(t, assertThrows(RuntimeException.class, f::awaitDoneThrowingOnErrorOrCancel).getCause());
  }
}
