/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.s2e.job;

import java.util.logging.Level;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.scout.sdk.core.util.SdkLog;
import org.eclipse.scout.sdk.s2e.internal.S2ESdkActivator;

/**
 * Extended job which adds the following features:
 * <ul>
 * <li>helper method to wait for a certain job family</li>
 * </ul>
 */
public abstract class AbstractJob extends Job {

  private StackTraceElement[] m_callerTrace;

  public AbstractJob(String name) {
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
  public static void waitForJobFamily(final Object family) {
    boolean wasInterrupted = false;
    do {
      try {
        Job.getJobManager().join(family, null);
        wasInterrupted = false;
      }
      catch (OperationCanceledException e) {
        //nop
      }
      catch (InterruptedException e) {
        wasInterrupted = true;
      }
    }
    while (wasInterrupted);
  }

  @Override
  public final IStatus run(IProgressMonitor monitor) {
    long start = System.currentTimeMillis();
    try {
      return runInternal(monitor);
    }
    finally {
      long duration = System.currentTimeMillis() - start;
      String logMsg = "Job '{}' finished after {}ms.";
      if (SdkLog.isDebugEnabled()) {
        // more details on debug level
        SdkLog.debug(logMsg + " It has been scheduled by:{}", getName(), duration, getCallerStackTrace());
      }
      else {
        SdkLog.debug(logMsg, getName(), duration);
      }
    }
  }

  protected String getCallerStackTrace() {
    int numElementsToRemove = 4;
    StackTraceElement[] cleaned = new StackTraceElement[m_callerTrace.length - numElementsToRemove];
    System.arraycopy(m_callerTrace, numElementsToRemove, cleaned, 0, cleaned.length);

    StringBuilder callerStack = new StringBuilder();
    for (StackTraceElement traceElement : cleaned) {
      callerStack.append("\n\tat ").append(traceElement);
    }
    return callerStack.toString();
  }

  private IStatus runInternal(IProgressMonitor monitor) {
    Throwable exc = null;
    try {
      validate();
      execute(monitor);
    }
    catch (LinkageError e) {
      exc = e;
    }
    catch (Exception e) {
      exc = e;
    }
    finally {
      monitor.done();
    }

    // log
    if (exc != null) {
      Level lvl = Level.SEVERE;
      int severity = IStatus.ERROR;
      String msg = exc.getMessage();
      if (exc instanceof OperationCanceledException) {
        lvl = Level.FINE;
        severity = IStatus.CANCEL;
      }
      SdkLog.log(lvl, msg, exc);
      return new Status(severity, S2ESdkActivator.PLUGIN_ID, msg, exc);
    }
    if (monitor.isCanceled()) {
      return Status.CANCEL_STATUS;
    }
    return Status.OK_STATUS;
  }

  protected void validate() {
  }

  protected abstract void execute(IProgressMonitor monitor) throws CoreException;
}
