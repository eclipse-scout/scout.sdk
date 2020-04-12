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

import java.util.Optional;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.scout.sdk.core.log.SdkLog;
import org.eclipse.scout.sdk.core.s.environment.Future;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.SdkException;

/**
 * <h3>{@link JobFuture}</h3>
 *
 * @since 7.0.0
 */
public final class JobFuture<V> extends Future<V> {

  private final AbstractJob m_job;

  JobFuture(AbstractJob job, Supplier<V> resultExtractor) {
    m_job = Ensure.notNull(job);
    job.addJobChangeListener(new JobChangeAdapter() {
      @Override
      @SuppressWarnings("squid:S1181") // Throwable and Error should not be caught
      public void done(IJobChangeEvent event) {
        try {
          AbstractJob j = (AbstractJob) event.getJob();
          j.removeJobChangeListener(this);
          doCompletion(j.isCanceled(), exception().orElse(null), resultExtractor);
        }
        catch (Throwable t) {
          completeExceptionally(t); // we must complete here. otherwise the future will never be done (deadlock)
        }
      }
    });
  }

  /**
   * @return The {@link AbstractJob} associated with this {@link JobFuture}.
   */
  public AbstractJob job() {
    return m_job;
  }

  @Override
  public boolean cancel(boolean mayInterruptIfRunning) {
    m_job.cancel();
    return super.cancel(mayInterruptIfRunning);
  }

  /**
   * @return The exception if the task did not complete successfully. An empty {@link Optional} if the task is not done
   *         yet (and can therefore not have an exception) or the task completed successfully.
   */
  public Optional<Throwable> exception() {
    IStatus result = m_job.getResult();
    if (result == null) {
      return Optional.empty(); // job is not finished
    }
    Optional<Throwable> exception = Optional.ofNullable(result.getException());
    if (exception.isPresent()) {
      return exception;
    }
    if (m_job.getResult().getSeverity() == IStatus.ERROR) {
      // the job reported an error but has no associated exception -> create one
      return Optional.of(new SdkException("Job '{}' completed with Status Error.", m_job));
    }
    return Optional.empty(); // everything ok
  }

  /**
   * Waits if necessary for at most the given time for the computation to complete, and then retrieves its result, if
   * available.
   *
   * @param timeout
   *          the maximum time to wait
   * @param unit
   *          the time unit of the timeout argument
   * @param monitor
   *          the progress monitor that can be used to cancel the waiting, or {@code null} if cancellation is not
   *          required. No progress is reported on this monitor.
   * @return A {@link Supplier} that returns the computed result
   * @throws CancellationException
   *           if the computation was canceled
   * @throws ExecutionException
   *           if the computation threw an exception
   * @throws InterruptedException
   *           if the current thread was interrupted while waiting
   * @throws TimeoutException
   *           if the wait timed out
   */
  public Supplier<V> get(long timeout, TimeUnit unit, IProgressMonitor monitor) throws InterruptedException, ExecutionException, TimeoutException {
    try {
      detectDeadLock();
      boolean completed = m_job.join(unit.toMillis(timeout), monitor);
      if (!completed) {
        throw new TimeoutException();
      }
      if (isCancelled()) {
        throw new CancellationException();
      }
      return get();
    }
    catch (OperationCanceledException e) {
      SdkLog.debug("Waiting for job {} has been aborted.", m_job, e);
      return null; // no result
    }
  }

  void detectDeadLock() {
    Ensure.isFalse(isCurrentContextConflictingWithJobRule(),
        "DEADLOCK detected: Cannot wait for future because the scheduling rule of the calling thread is conflicting with the scheduling rule of the future!");
  }

  boolean isCurrentContextConflictingWithJobRule() {
    ISchedulingRule rule = m_job.getRule();
    if (rule == null) {
      return false;
    }

    ISchedulingRule currentRule = Job.getJobManager().currentRule();
    if (currentRule == null) {
      return false;
    }
    return currentRule.isConflicting(rule);
  }
}
