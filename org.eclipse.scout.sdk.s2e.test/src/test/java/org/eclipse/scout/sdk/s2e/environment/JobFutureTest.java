/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.s2e.environment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.scout.sdk.core.util.SdkException;
import org.eclipse.scout.sdk.s2e.S2ESdkActivator;
import org.junit.jupiter.api.Test;

/**
 * <h3>{@link JobFutureTest}</h3>
 *
 * @since 7.0.0
 */
public class JobFutureTest {

  @Test
  public void testWhenDoneAfterDone() {
    var result = "testresult";
    var i = new AtomicInteger(0);
    var future = createFixtureJobFuture(result);

    assertEquals(0, i.get());
    assertFalse(future.isDone());
    future.job().schedule();
    assertEquals(result, future.result());
    assertEquals(0, i.get());

    future.thenRun(i::incrementAndGet);
    assertEquals(1, i.get());
    assertTrue(future.isDone());
    assertFalse(future.isCompletedExceptionally());
    future.awaitDoneThrowingOnErrorOrCancel();
  }

  @Test
  public void testCheckedExceptionResult() {
    var e = new CoreException(new Status(IStatus.ERROR, S2ESdkActivator.PLUGIN_ID, "test exception", new Exception("test exception")));
    var j = new AbstractJob("") {
      @Override
      protected void execute(IProgressMonitor monitor) throws CoreException {
        throw e;
      }
    };
    j.setRule(new P_FixtureSchedulingRule());
    var future = createFixtureJobFuture(j);
    future.job().schedule();

    assertSame(e, assertThrows(CompletionException.class, future::result).getCause());
  }

  @Test
  public void testDeadlockDetection() throws InterruptedException {
    var expected = new AtomicReference<IllegalArgumentException>();
    var main = new AbstractJob("main") {
      @Override
      protected void execute(IProgressMonitor monitor) {
        var inner = new AbstractJob("inner") {
          @Override
          protected void execute(IProgressMonitor m) {
          }
        };
        inner.setRule(new P_FixtureSchedulingRule());
        var future = createFixtureJobFuture(inner);
        inner.schedule();

        try {
          future.get(30, TimeUnit.SECONDS, null);
        }
        catch (IllegalArgumentException e) {
          expected.compareAndSet(null, e);
        }
        catch (Exception e) {
          throw new SdkException(e);
        }
      }
    };
    main.setRule(new P_FixtureSchedulingRule());
    main.schedule();
    main.join(TimeUnit.MINUTES.toMillis(1), null);

    assertNotNull(expected.get());
    assertNull(main.getResult().getException());
  }

  private static final class P_FixtureSchedulingRule implements ISchedulingRule {
    @Override
    public boolean contains(ISchedulingRule rule) {
      return rule instanceof P_FixtureSchedulingRule;
    }

    @Override
    public boolean isConflicting(ISchedulingRule rule) {
      return rule instanceof P_FixtureSchedulingRule;
    }
  }

  @Test
  public void testWaitingAborted() throws InterruptedException, ExecutionException, TimeoutException {
    var release = new CountDownLatch(1);
    var future = createFixtureJobFuture("", () -> {
      try {
        release.await(1, TimeUnit.MINUTES);
      }
      catch (InterruptedException e) {
        throw new SdkException(e);
      }
    });
    future.job().schedule();

    IProgressMonitor monitor = new NullProgressMonitor();
    new AbstractJob("cancel waiting monitor") {
      @Override
      protected void execute(IProgressMonitor m) {
        monitor.setCanceled(true);
      }
    }.schedule(100);

    assertNull(future.get(1, TimeUnit.MINUTES, monitor));

    release.countDown();
    future.awaitDoneThrowingOnErrorOrCancel();
  }

  @Test
  public void testWaitTimeout() throws InterruptedException {
    var jobStarted = new CountDownLatch(1);
    var release = new CountDownLatch(1);
    var future = createFixtureJobFuture("", () -> {
      jobStarted.countDown();
      try {
        release.await(1, TimeUnit.MINUTES);
      }
      catch (InterruptedException e) {
        throw new SdkException(e);
      }
    });
    future.job().schedule();

    jobStarted.await(1, TimeUnit.MINUTES);
    TimeoutException te = null;
    try {
      future.get(100, TimeUnit.MILLISECONDS);
    }
    catch (ExecutionException e) {
      assertNull(e);
    }
    catch (TimeoutException e) {
      te = e;
    }
    assertNotNull(te);
    release.countDown();
    future.awaitDoneThrowingOnErrorOrCancel();
  }

  @Test
  public void testRuntimeExceptionResult() {
    var e = new RuntimeException("test exception");
    var future = createFixtureJobFuture("", () -> {
      throw e;
    });
    future.job().schedule();

    assertSame(e, assertThrows(RuntimeException.class, future::awaitDoneThrowingOnErrorOrCancel));
    assertSame(e, future.exception().get());
    assertTrue(future.isCompletedExceptionally());
    assertTrue(future.isDone());
    assertSame(e, assertThrows(ExecutionException.class, future::get).getCause());
  }

  @Test
  public void testCancel() throws InterruptedException {
    var futureStarted = new CountDownLatch(1);
    var futureCanceled = new CountDownLatch(1);
    var finished = new AtomicBoolean(false);
    var future = createFixtureJobFuture(new AbstractJob("") {
      @Override
      protected void execute(IProgressMonitor monitor) {
        futureStarted.countDown();
        try {
          futureCanceled.await(1, TimeUnit.MINUTES);
        }
        catch (InterruptedException e) {
          throw new SdkException(e);
        }
        if (monitor.isCanceled()) {
          return;
        }
        finished.set(true);
      }
    });
    future.job().schedule();
    futureStarted.await(1, TimeUnit.MINUTES);
    future.cancel(false);
    assertTrue(future.isCancelled());
    futureCanceled.countDown();
    assertThrows(CancellationException.class, future::awaitDoneThrowingOnErrorOrCancel);
    assertFalse(finished.get());
  }

  @Test
  public void testNoResultExtractor() {
    Runnable r = () -> {
    };
    var result = new RunnableJob("", r).scheduleWithFuture(0, TimeUnit.MILLISECONDS, null).result();
    assertNull(result);
  }

  @Test
  public void testWhenDoneBeforeDone() {
    var result = "testresult";
    var i = new AtomicInteger(0);
    var future = createFixtureJobFuture(result);
    var postTask = future.thenRun(i::incrementAndGet);

    assertEquals(0, i.get());
    future.job().schedule();
    assertEquals(result, future.result());
    postTask.join();
    assertEquals(1, i.get());
    future.awaitDoneThrowingOnErrorOrCancel();
  }

  @Test
  public void testLazyResult() throws ExecutionException, InterruptedException {
    var resultSupplierInvokeCounter = new AtomicInteger();
    var expectedResult = "test result";
    Supplier<String> resultSupplier = () -> {
      resultSupplierInvokeCounter.incrementAndGet();
      return expectedResult;
    };
    var jf = createFixtureJobFuture(resultSupplier);
    assertEquals(0, resultSupplierInvokeCounter.get());
    jf.job().schedule();

    jf.get(); // wait until completed
    assertEquals(0, resultSupplierInvokeCounter.get()); // resultSupplier must not yet been executed because the result has not yet been retrieved (lazy computation)

    assertEquals(expectedResult, jf.result()); // here the resultSupplier is invoked
    assertEquals(1, resultSupplierInvokeCounter.get());

    var result = jf.get().get();
    assertEquals(expectedResult, result); // here the resultSupplier is invoked
    assertSame(result, jf.result());

    // even the result was requested multiple times, it has only been computed once
    assertEquals(1, resultSupplierInvokeCounter.get());
  }

  private static JobFuture<String> createFixtureJobFuture(String result) {
    return createFixtureJobFuture(result, () -> {
    });
  }

  private static JobFuture<String> createFixtureJobFuture(String result, Runnable r) {
    AbstractJob j = new RunnableJob(result, r);
    return createFixtureJobFuture(j);
  }

  private static JobFuture<String> createFixtureJobFuture(Supplier<String> resultSupplier) {
    AbstractJob j = new RunnableJob("", () -> {
    });
    return createFixtureJobFuture(j, resultSupplier);
  }

  private static JobFuture<String> createFixtureJobFuture(AbstractJob j) {
    return createFixtureJobFuture(j, () -> j.getResult() == null ? null : j.getName());
  }

  private static JobFuture<String> createFixtureJobFuture(AbstractJob j, Supplier<String> resultSupplier) {
    return new JobFuture<>(j, resultSupplier);
  }
}
