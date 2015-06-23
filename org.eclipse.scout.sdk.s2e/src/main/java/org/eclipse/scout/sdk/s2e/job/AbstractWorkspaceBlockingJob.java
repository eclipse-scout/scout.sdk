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

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.scout.sdk.s2e.IWorkingCopyManager;
import org.eclipse.scout.sdk.s2e.internal.S2ESdkActivator;

public abstract class AbstractWorkspaceBlockingJob extends JobEx {

  private boolean m_debug;
  private Exception m_callerTrace;

  public AbstractWorkspaceBlockingJob(String name) {
    super(name);
    setRule(ResourcesPlugin.getWorkspace().getRoot());
    m_debug = Platform.inDevelopmentMode();
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

  private IStatus debugDoRun(IProgressMonitor monitor) {
    long start = System.currentTimeMillis();
    try {
      return doRun(monitor);
    }
    finally {
      S2ESdkActivator.logInfo("Operation job '" + getName() + "' took " + (System.currentTimeMillis() - start) + "ms to execute.");
    }
  }

  private IStatus doRun(IProgressMonitor monitor) {
    IWorkingCopyManager workingCopyManager = new WorkingCopyManager();
    try {
      try {
        validate();
        run(monitor, workingCopyManager);
      }
      catch (Exception e) {
        if (e.getCause() == e || e.getCause() == null) {
          e.initCause(m_callerTrace);
        }
        Status errorStatus = new Status(Status.ERROR, S2ESdkActivator.PLUGIN_ID, e.getMessage(), e);
        S2ESdkActivator.log(errorStatus);
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

  protected void validate() {
  }

  protected abstract void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException;

  public void setDebug(boolean debug) {
    m_debug = debug;
  }

  public boolean isDebug() {
    return m_debug;
  }
}
