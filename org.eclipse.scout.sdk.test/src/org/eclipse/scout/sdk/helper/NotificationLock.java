/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.helper;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;

/**
 *
 */
public final class NotificationLock {

  private final static NotificationLock instance = new NotificationLock();
  private static final String NotifyJobName = "org.eclipse.core.internal.events.NotificationManager.NotifyJob";

  private P_JobChangeListener m_jobManagerListener;
  private Object lock = new Object();
  private int m_notifyJobCounter = 0;

  private NotificationLock() {
    m_jobManagerListener = new P_JobChangeListener();
    Job.getJobManager().addJobChangeListener(m_jobManagerListener);
  }

  public static void waitForNotification() throws InterruptedException {
    instance.waitForNotificationImpl();
  }

  public void waitForNotificationImpl() throws InterruptedException {
    synchronized (lock) {
      if (m_notifyJobCounter > 0) {
        lock.wait();
      }
    }
  }

  private class P_JobChangeListener extends JobChangeAdapter {

    @Override
    public void scheduled(IJobChangeEvent event) {
      Job job = event.getJob();
      String jobName = job.getClass().getName().replace('$', '.');
      if (NotifyJobName.equals(jobName)) {
        synchronized (lock) {
          m_notifyJobCounter++;
          System.out.println("added size " + m_notifyJobCounter + "  tree locked " + ResourcesPlugin.getWorkspace().getRoot().isDerived(IResource.CHECK_ANCESTORS));
        }
      }
    }

    @Override
    public void done(IJobChangeEvent event) {
      final Job job = event.getJob();
      String jobName = job.getClass().getName().replace('$', '.');
      if (NotifyJobName.equals(jobName)) {
        Job removeJob = new Job("") {
          @Override
          protected IStatus run(IProgressMonitor monitor) {
            synchronized (lock) {

              if (--m_notifyJobCounter <= 0) {
                lock.notifyAll();
              }
              System.out.println("removed size " + m_notifyJobCounter + " locked ws  " + ResourcesPlugin.getWorkspace().getRoot().isDerived(IResource.CHECK_ANCESTORS));
            }
            return Status.OK_STATUS;
          }
        };
        removeJob.schedule(100);
      }
    }

  }

}
