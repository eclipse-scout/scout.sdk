/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Daniel Wiehl (BSI Business Systems Integration AG) - initial API and implementation
 ******************************************************************************/
/**
 *
 */
package org.eclipse.scout.sdk.ws.jaxws.worker;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsSdk;

public abstract class AbstractQueueManager<T> {

  private final Lock m_workerLock;
  private final Lock m_suspendLock;
  private final Lock m_refuseLock;
  private final ConcurrentLinkedQueue<T> m_requests;

  private AtomicInteger m_suspendCounter;
  private AtomicInteger m_refuseCounter;

  public AbstractQueueManager() {
    m_workerLock = new ReentrantLock();
    m_suspendLock = new ReentrantLock();
    m_refuseLock = new ReentrantLock();
    m_requests = new ConcurrentLinkedQueue<T>();
    m_suspendCounter = new AtomicInteger();
    m_refuseCounter = new AtomicInteger();
  }

  public void queueRequest(T request) {
    if (!isRefuseRequests()) {
      if (!m_requests.contains(request)) {
        m_requests.offer(request); // put request into queue
      }
      startWorker();
    }
  }

  public void refuseRequests(boolean refuseRequests) {
    m_refuseLock.lock();
    try {
      if (refuseRequests) {
        m_refuseCounter.incrementAndGet();
      }
      else {
        if (m_refuseCounter.decrementAndGet() < 0) { // negative values are not allowed
          m_refuseCounter.set(0);
        }
      }
    }
    finally {
      m_refuseLock.unlock();
    }
  }

  public void resume() {
    m_suspendLock.lock();
    try {
      if (m_suspendCounter.decrementAndGet() < 0) { // negative values are not allowed
        m_suspendCounter.set(0);
      }

      if (m_suspendCounter.get() == 0) { // only notify the worker about the request if compilation is not suspended
        startWorker();
      }
    }
    finally {
      m_suspendLock.unlock();
    }
  }

  public void suspend() {
    m_suspendLock.lock();
    try {
      m_suspendCounter.incrementAndGet();
      cancelWorker();
    }
    finally {
      m_suspendLock.unlock();
    }
  }

  private void startWorker() {
    if (m_workerLock.tryLock()) {
      try {
        if (!isWorkerActive() && !isSuspend()) {
          Job worker = new P_Worker();
          worker.setSystem(true);
          worker.setUser(false);
          worker.setPriority(Job.BUILD);
          worker.schedule(200);
        }
      }
      finally {
        m_workerLock.unlock();
      }
    }
  }

  public void cancelWorker() {
    if (m_workerLock.tryLock()) {
      try {
        if (isWorkerActive()) {
          for (Job job : Job.getJobManager().find(AbstractQueueManager.this)) {
            job.cancel();
          }
        }
      }
      finally {
        m_workerLock.unlock();
      }
    }
  }

  private final class P_Worker extends Job {

    public P_Worker() {
      super(AbstractQueueManager.this.getClass().getSimpleName());
    }

    @Override
    public boolean belongsTo(Object family) {
      return CompareUtility.equals(family, AbstractQueueManager.this);
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {
      m_workerLock.lock();
      try {
        T request;
        while ((request = m_requests.poll()) != null) {
          if (monitor.isCanceled()) {
            return Status.CANCEL_STATUS;
          }
          handleRequest(request, monitor);
        }
      }
      catch (Exception e) {
        JaxWsSdk.logError("Error occured while processing queued job", e);
      }
      finally {
        m_workerLock.unlock();
      }
      return Status.OK_STATUS;
    }
  }

  private boolean isRefuseRequests() {
    return m_refuseCounter.get() > 0;
  }

  private boolean isSuspend() {
    return m_suspendCounter.get() > 0;
  }

  private boolean isWorkerActive() {
    return Job.getJobManager().find(AbstractQueueManager.this).length > 0;
  }

  protected abstract void handleRequest(T request, IProgressMonitor monitor) throws Exception;
}
