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
package org.eclipse.scout.sdk.jobs;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.scout.commons.TuningUtility;
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.util.typecache.TypeCacheAccessor;

public abstract class AbstractWorkspaceBlockingJob extends Job {

  private boolean m_debug = false;

  public AbstractWorkspaceBlockingJob(String name) {
    super(name);
    setRule(ResourcesPlugin.getWorkspace().getRoot());
  }

  @Override
  protected final IStatus run(IProgressMonitor monitor) {
    if (isDebug()) {
      return debugDoRun(monitor);
    }
    else {
      return doRun(monitor);
    }
  }

  private final IStatus debugDoRun(IProgressMonitor monitor) {
    try {
      TuningUtility.startTimer();
      return doRun(monitor);
    }
    finally {
      String name = getName();
      TuningUtility.stopTimer("Operation job '" + name + "' execution.");
    }
  }

  private final IStatus doRun(IProgressMonitor monitor) {
    Throwable exception = null;
    IWorkingCopyManager level = TypeCacheAccessor.createWorkingCopyManger();
    try {
      try {
        validate();
        try {
          run(monitor, level);
        }
        catch (Throwable e) {
          ScoutSdk.logError("Error occured while running Operation job '" + getName() + "'.", e);
          exception = e;
        }
      }
      catch (IllegalArgumentException e) {
        ScoutSdk.logError("validation of job '" + getName() + "' failed.", e);
      }
    }
    finally {
      level.unregisterAll(monitor);
      monitor.done();
    }
    if (exception != null) {
      return new Status(IStatus.ERROR, ScoutSdk.PLUGIN_ID, "Error occured while running operation job.", exception);
    }
    else {
      return Status.OK_STATUS;
    }
  }

  protected void validate() throws IllegalArgumentException {
  }

  protected abstract void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException;

  /**
   * Avoid deadlock: check if currently running job is blocking this one
   */
  @Override
  public boolean shouldSchedule() {
    Job job = Job.getJobManager().currentJob();
    if (job != null) {
      ISchedulingRule activeRule = job.getRule();
      ISchedulingRule myRule = getRule();
      if (activeRule != null && myRule != null && activeRule.isConflicting(myRule)) {
        ScoutSdk.logWarning("POTENTIAL DEAD LOCK DETECTED: running job " + job + " and scheduling " + this + " (check that no join() is done)");
      }
    }
    return super.shouldSchedule();
  }

  public void setDebug(boolean debug) {
    m_debug = debug;
  }

  public boolean isDebug() {
    return m_debug;
  }

}
