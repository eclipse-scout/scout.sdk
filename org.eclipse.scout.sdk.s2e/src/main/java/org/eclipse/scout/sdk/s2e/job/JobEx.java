/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.s2e.job;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;

/**
 * Extended job which adds the following features:
 * <ul>
 * <li>access to canceled property</li>
 * <li>access to progress monitor</li>
 * <li>run convenience methods</li>
 * </ul>
 */
public abstract class JobEx extends Job {
  private IStatus m_runNowStatus;

  /**
   * Waits until all jobs of the given family are finished. This method will block the calling thread until all such
   * jobs have finished executing. If there are no jobs in the family that are currently waiting, running, or sleeping,
   * this method returns immediately.
   */
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

  public JobEx(String name) {
    super(name);
  }

  @Override
  public final boolean shouldRun() {
    m_runNowStatus = null;
    return super.shouldRun();
  }

  /**
   * Some times it is useful to run a job "manually", for example when the job
   * can be run immediately.
   */
  public IStatus runNow(IProgressMonitor monitor) {
    m_runNowStatus = null;
    m_runNowStatus = run(monitor);
    return m_runNowStatus;
  }

  /**
   * @return status of the last result executed with {@link #runNow(IProgressMonitor)} or as a regular job.<br>
   *         {@link #getResult()} does not include results of {@link #runNow(IProgressMonitor)}
   */
  public IStatus getLastResult() {
    if (m_runNowStatus == null) {
      return getResult();
    }
    return m_runNowStatus;
  }

  public IProgressMonitor getMonitor() {
    IProgressMonitor monitor = null;
    if (getState() == Job.RUNNING) {
      try {
        Method m = Job.class.getSuperclass().getDeclaredMethod("getProgressMonitor");
        m.setAccessible(true);
        Object result = m.invoke(this, new Object[0]);
        if (result instanceof IProgressMonitor) {
          monitor = (IProgressMonitor) result;
        }
      }
      catch (Throwable t) {
        // nop
      }
    }
    return monitor;
  }

  /**
   * @return {@link #isCanceled()} property if the current job is of instance {@link JobEx}
   */
  public static boolean isCurrentJobCanceled() {
    Job job = getJobManager().currentJob();
    if (job instanceof JobEx) {
      IProgressMonitor monitor = ((JobEx) job).getMonitor();
      if (monitor != null) {
        return monitor.isCanceled();
      }
    }
    return false;
  }

  /**
   * Similar to {@link #join()} but with the difference that it waits at most the specified time.
   * <p>
   * A value of &lt;= 0 is equivalent to calling {@link #join()}.
   *
   * @throws InterruptedException
   */
  public final void join(final long millis) throws InterruptedException {
    if (millis <= 0) {
      join();
    }
    else {
      int state = getState();
      if (state == Job.NONE) {
        return;
      }
      //it's an error for a job to join itself
      if (state == Job.RUNNING && getThread() == Thread.currentThread()) {
        throw new IllegalStateException("Job attempted to join itself");
      }
      final AtomicBoolean jobDoneLock = new AtomicBoolean();
      JobChangeAdapter listener = new JobChangeAdapter() {
        @Override
        public void done(IJobChangeEvent event) {
          synchronized (jobDoneLock) {
            jobDoneLock.set(true);
            jobDoneLock.notifyAll();
          }
        }
      };
      try {
        addJobChangeListener(listener);
        //
        if (getState() == Job.NONE) {
          return;
        }
        long endTime = System.currentTimeMillis() + millis;
        synchronized (jobDoneLock) {
          while (!jobDoneLock.get()) {
            long dt = endTime - System.currentTimeMillis();
            if (dt <= 0) {
              return;
            }
            try {
              jobDoneLock.wait(dt);
            }
            catch (InterruptedException e) {
              throw e;
            }
          }
        }
      }
      finally {
        removeJobChangeListener(listener);
      }
    }
  }
}
