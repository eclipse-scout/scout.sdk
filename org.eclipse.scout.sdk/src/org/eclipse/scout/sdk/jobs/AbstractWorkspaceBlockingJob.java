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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.scout.commons.TuningUtility;
import org.eclipse.scout.commons.job.JobEx;
import org.eclipse.scout.sdk.ScoutSdkCore;
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;

public abstract class AbstractWorkspaceBlockingJob extends JobEx {

  private boolean m_debug = false;
  private Exception m_callerTrace;

  public AbstractWorkspaceBlockingJob(String name) {
    super(name);
    setRule(new OptionalWorkspaceBlockingRule(true));
  }

  @Override
  public boolean shouldSchedule() {
    m_callerTrace = new Exception("Job scheduled by:");
    return super.shouldSchedule();
  }

  @Override
  protected final IStatus run(IProgressMonitor monitor) {
    if (isDebug()) {
      return debugDoRun(monitor);
    }
    return doRun(monitor);
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
    IWorkingCopyManager workingCopyManager = ScoutSdkCore.createWorkingCopyManger();
    try {
      try {
        validate();
        run(monitor, workingCopyManager);
      }
      catch (Exception e) {
        if (e.getCause() == e || e.getCause() == null) {
          e.initCause(m_callerTrace);
        }
        Status errorStatus = new Status(Status.ERROR, ScoutSdk.PLUGIN_ID, e.getMessage(), e);
        ScoutSdk.log(errorStatus);
        monitor.setCanceled(true);
        return errorStatus;
      }
    }
    finally {
      workingCopyManager.unregisterAll(monitor);
      monitor.done();
    }
    return Status.OK_STATUS;
  }

  protected void validate() throws IllegalArgumentException {
  }

  protected abstract void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException;

  public void setDebug(boolean debug) {
    m_debug = debug;
  }

  public boolean isDebug() {
    return m_debug;
  }
}
