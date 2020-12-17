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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.scout.sdk.core.log.SdkLog;
import org.eclipse.scout.sdk.core.util.SdkException;
import org.eclipse.scout.sdk.s2e.S2ESdkActivator;
import org.junit.jupiter.api.Test;

/**
 * <h3>{@link AbstractJobTest}</h3>
 *
 * @since 7.0.0
 */
public class AbstractJobTest {

  @Test
  public void testCallerStackTraceSchedule() throws InterruptedException {
    SdkLog.setLogLevel(Level.FINE); // so that detailed log is created (caller trace)
    try {
      var trace = new AtomicReference<String>();
      var j = new RunnableJob("", () -> {
      }) {
        @Override
        protected String getCallerStackTrace() {
          var result = super.getCallerStackTrace();
          trace.set(result);
          return result;
        }
      };
      j.schedule();
      j.join();
      assertTrue(trace.get().trim().startsWith("at " + AbstractJobTest.class.getName() + ".testCallerStackTraceSchedule(" + AbstractJobTest.class.getSimpleName() + ".java:"));
    }
    finally {
      SdkLog.setLogLevel(SdkLog.DEFAULT_LOG_LEVEL);
    }
  }

  @Test
  public void testCallerStackTraceRun() {
    SdkLog.setLogLevel(Level.FINE); // so that detailed log is created (caller trace)
    try {
      var trace = new AtomicReference<String>();
      var j = new RunnableJob("", () -> {
      }) {
        @Override
        protected String getCallerStackTrace() {
          var result = super.getCallerStackTrace();
          trace.set(result);
          return result;
        }
      };
      j.run(new NullProgressMonitor());
      assertTrue(j.getCallerStackTrace().trim().startsWith("at " + AbstractJobTest.class.getName() + ".testCallerStackTraceRun(" + AbstractJobTest.class.getSimpleName() + ".java:"));
    }
    finally {
      SdkLog.setLogLevel(SdkLog.DEFAULT_LOG_LEVEL);
    }
  }

  @Test
  public void testCancelByException() throws InterruptedException {
    var j = new RunnableJob("", () -> {
      throw new OperationCanceledException();
    });
    j.schedule();
    j.join();
    assertEquals(Status.CANCEL_STATUS, j.getResult());
  }

  @Test
  public void testCancelByMonitor() throws InterruptedException {
    var j = new AbstractJob("") {
      @Override
      protected void execute(IProgressMonitor monitor) {
        assertTrue(monitor().isPresent());
        monitor.setCanceled(true);
      }
    };
    j.schedule();
    j.join();
    assertTrue(j.monitor().isPresent());
    assertEquals(Status.CANCEL_STATUS, j.getResult());
  }

  @Test
  public void testCheckedExceptionInExecute() throws InterruptedException {
    var j = new AbstractJob("") {
      @Override
      protected void execute(IProgressMonitor monitor) throws CoreException {
        throw new CoreException(new Status(IStatus.INFO, S2ESdkActivator.PLUGIN_ID, "test exception"));
      }
    };
    j.schedule();
    j.join();
    assertEquals(IStatus.ERROR, j.getResult().getSeverity());
  }

  @Test
  public void testRuntimeExceptionInExecute() throws InterruptedException {
    var j = new AbstractJob("") {
      @Override
      protected void execute(IProgressMonitor monitor) {
        throw new IllegalArgumentException("test exception");
      }
    };
    j.schedule();
    j.join();
    assertEquals(IStatus.ERROR, j.getResult().getSeverity());
  }

  @Test
  public void testWaitForFamily() throws InterruptedException {
    var family = "testfamily";
    var familyJobStarted = new CountDownLatch(1);
    var waitingJobStarted = new CountDownLatch(1);
    var blockCondition = new CountDownLatch(1);

    var j = new AbstractJob("a") {
      @Override
      protected void execute(IProgressMonitor monitor) {
        familyJobStarted.countDown();
        try {
          blockCondition.await(1, TimeUnit.MINUTES);
        }
        catch (InterruptedException e) {
          throw new SdkException(e);
        }
      }

      @Override
      public boolean belongsTo(Object f) {
        return family.equals(f);
      }
    };
    j.schedule();
    familyJobStarted.await(1, TimeUnit.MINUTES);

    var waitingThread = new AtomicReference<Thread>();
    var waitingJob = new AbstractJob("waiting on a") {
      @Override
      protected void execute(IProgressMonitor monitor) {
        waitingThread.set(Thread.currentThread());
        waitingJobStarted.countDown();
        AbstractJob.waitForJobFamily(family);
      }
    };
    waitingJob.schedule();
    waitingJobStarted.await(1, TimeUnit.MINUTES);

    assertNull(waitingJob.getResult()); // ensure not finished
    assertNull(j.getResult()); // ensure not finished
    waitingThread.get().interrupt();
    assertNull(waitingJob.getResult()); // ensure not finished
    assertNull(j.getResult()); // ensure not finished

    blockCondition.countDown(); // release
    j.join(TimeUnit.MINUTES.toMillis(1), null);
    assertTrue(j.getResult().isOK());
    waitingJob.join(TimeUnit.MINUTES.toMillis(1), null);
    assertTrue(waitingJob.getResult().isOK());

  }
}
