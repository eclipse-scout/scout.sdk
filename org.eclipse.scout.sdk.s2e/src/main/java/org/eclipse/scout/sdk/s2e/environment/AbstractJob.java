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
package org.eclipse.scout.sdk.s2e.environment;

import static java.util.stream.Collectors.joining;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CancellationException;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.zip.ZipError;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.scout.sdk.core.log.SdkLog;
import org.eclipse.scout.sdk.core.s.environment.IFuture;
import org.eclipse.scout.sdk.s2e.S2ESdkActivator;

/**
 * Extended job which adds the following features:
 * <ul>
 * <li>helper method to wait for a certain job family</li>
 * <li>method to schedule returning an {@link IFuture}.</li>
 * </ul>
 */
public abstract class AbstractJob extends Job {

  private StackTraceElement[] m_callerTrace;
  private volatile IProgressMonitor m_monitor;

  protected AbstractJob(String name) {
    super(name);
  }

  @Override
  public boolean shouldSchedule() {
    m_callerTrace = Thread.currentThread().getStackTrace();
    return super.shouldSchedule();
  }

  /**
   * Waits until all jobs of the given family are finished. This method will block the calling thread until all such
   * jobs have finished executing. If there are no jobs in the family that are currently waiting, running, or sleeping,
   * this method returns immediately.
   */
  @SuppressWarnings("squid:S1166")
  public static void waitForJobFamily(Object family) {
    boolean wasInterrupted;
    do {
      wasInterrupted = false;
      try {
        Job.getJobManager().join(family, null);
      }
      catch (OperationCanceledException e) {
        //nop
      }
      catch (InterruptedException e) {
        SdkLog.debug(e);
        wasInterrupted = true;
      }
    }
    while (wasInterrupted);
  }

  /**
   * @return {@code true} if this {@link AbstractJob} has been started and was canceled. This can either be from outside
   *         (Job{@link #cancel()} or by the internal monitor ({@link IProgressMonitor#setCanceled(boolean)}).<br>
   *         If this {@link AbstractJob} did not start yet or was not canceled yet, this method returns {@code false}.
   */
  public boolean isCanceled() {
    return monitor()
        .map(IProgressMonitor::isCanceled)
        .orElse(Boolean.FALSE);
  }

  /**
   * @return The {@link IProgressMonitor} of this {@link AbstractJob} if it has already started executing. An empty
   *         {@link Optional} otherwise.
   */
  public Optional<IProgressMonitor> monitor() {
    return Optional.ofNullable(m_monitor);
  }

  /**
   * Schedules this {@link AbstractJob} for immediate execution.
   *
   * @return An {@link IFuture} to get the result and waiting for completion.
   */
  public IFuture<Void> scheduleWithFuture() {
    return scheduleWithFuture(0L, TimeUnit.MILLISECONDS);
  }

  /**
   * Schedules this {@link AbstractJob} to start after the specified delay.
   *
   * @param delay
   *          The number of units to wait until the job is scheduled. {@code 0} means it is scheduled immediately.
   * @param unit
   *          The time unit (e.g. {@link TimeUnit#SECONDS}). Must not be {@code null}.
   * @return An {@link IFuture} to get the result and waiting for completion.
   */
  public IFuture<Void> scheduleWithFuture(long delay, TimeUnit unit) {
    return scheduleWithFuture(delay, unit, null);
  }

  /**
   * Schedules this {@link AbstractJob} to start after the specified delay.
   *
   * @param delay
   *          The number of units to wait until the job is scheduled. {@code 0} means it is scheduled immediately.
   * @param unit
   *          The time unit (e.g. {@link TimeUnit#SECONDS}). Must not be {@code null}.
   * @param resultExtractor
   *          An optional callback to provide the result of this {@link AbstractJob}. May be {@code null}.
   * @return An {@link IFuture} to get the result and waiting for completion.
   */
  public <T> IFuture<T> scheduleWithFuture(long delay, TimeUnit unit, Supplier<T> resultExtractor) {
    IFuture<T> result = new JobFuture<>(this, resultExtractor);
    schedule(unit.toMillis(delay));
    return result;
  }

  @Override
  public final IStatus run(IProgressMonitor monitor) {
    var start = System.currentTimeMillis();
    try {
      m_monitor = monitor;
      return runInternal(monitor);
    }
    finally {
      var duration = System.currentTimeMillis() - start;
      var logMsg = "Job '{}' finished after {}ms.";
      if (SdkLog.isDebugEnabled()) {
        // more details on debug level
        SdkLog.debug(logMsg + " It has been scheduled by:{}", getName(), duration, getCallerStackTrace());
      }
      else {
        SdkLog.info(logMsg, getName(), duration);
      }
    }
  }

  protected String getCallerStackTrace() {
    var numElementsToRemove = 4;
    if (m_callerTrace == null || m_callerTrace.length <= numElementsToRemove) {
      // can happen if run() is called directly without scheduling using job manager.
      m_callerTrace = Thread.currentThread().getStackTrace();
      numElementsToRemove = 3;
    }
    var cleaned = new StackTraceElement[m_callerTrace.length - numElementsToRemove];
    System.arraycopy(m_callerTrace, numElementsToRemove, cleaned, 0, cleaned.length);

    //noinspection HardcodedLineSeparator
    return Arrays.stream(cleaned)
        .map(traceElement -> "\n\tat " + traceElement)
        .collect(joining());
  }

  private IStatus runInternal(IProgressMonitor monitor) {
    try {
      execute(monitor);
      if (monitor.isCanceled()) {
        SdkLog.debug("Job '{}' has been canceled by monitor.", getName());
        return Status.CANCEL_STATUS;
      }
      return Status.OK_STATUS;
    }
    catch (CancellationException ce) {
      SdkLog.debug("Job '{}' has been canceled.", getName(), ce);
      return Status.CANCEL_STATUS;
    }
    catch (OperationCanceledException ce) {
      SdkLog.debug("Job '{}' has been canceled.", getName(), ce);
      throw ce; // handled by job manager
    }
    catch (LinkageError | CoreException | RuntimeException | ZipError e) {
      SdkLog.error(e);
      return new Status(IStatus.ERROR, S2ESdkActivator.PLUGIN_ID, e.getMessage(), e);
    }
    finally {
      monitor.done();
    }
  }

  protected abstract void execute(IProgressMonitor monitor) throws CoreException;
}
