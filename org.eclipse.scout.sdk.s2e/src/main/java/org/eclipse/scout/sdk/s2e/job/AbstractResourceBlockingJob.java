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

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.MultiRule;
import org.eclipse.scout.sdk.core.util.SdkLog;
import org.eclipse.scout.sdk.s2e.ScoutSdkCore;
import org.eclipse.scout.sdk.s2e.internal.S2ESdkActivator;
import org.eclipse.scout.sdk.s2e.workspace.IWorkingCopyManager;

/**
 * <h3>{@link AbstractResourceBlockingJob}</h3> Job which runs with a specific resource lock.
 *
 * @since 5.1.0
 */
public abstract class AbstractResourceBlockingJob extends AbstractJob {

  private Exception m_callerTrace;

  /**
   * Creates a new job blocking on the given resources.
   *
   * @param name
   *          The name of the job
   * @param blockedResources
   *          The resources this job should block or <code>null</code> if the full workspace should be blocked.
   */
  public AbstractResourceBlockingJob(String name, IResource... blockedResources) {
    super(name);
    if (blockedResources == null || blockedResources.length < 1) {
      setRule(ResourcesPlugin.getWorkspace().getRoot());
    }
    else if (blockedResources.length == 1) {
      setRule(blockedResources[0]);
    }
    else {
      setRule(new MultiRule(blockedResources));
    }
  }

  /**
   * Creates a new job blocking the full workspace.
   *
   * @param name
   *          Name of the job
   */
  public AbstractResourceBlockingJob(String name) {
    this(name, (IResource[]) null);
  }

  @Override
  public boolean shouldSchedule() {
    m_callerTrace = new Exception("Job scheduled by:");
    return super.shouldSchedule();
  }

  @Override
  protected final IStatus run(IProgressMonitor monitor) {
    long start = System.currentTimeMillis();
    try {
      return doRun(monitor);
    }
    finally {
      SdkLog.debug("Operation job '" + getName() + "' took " + (System.currentTimeMillis() - start) + "ms to execute.");
    }
  }

  private IStatus doRun(IProgressMonitor monitor) {
    IWorkingCopyManager workingCopyManager = ScoutSdkCore.createWorkingCopyManager();
    boolean save = true;
    try {
      try {
        validate();
        run(monitor, workingCopyManager);
      }
      catch (Exception e) {
        save = false;
        if (e.getCause() == e || e.getCause() == null) {
          e.initCause(m_callerTrace);
        }
        Status errorStatus = new Status(IStatus.ERROR, S2ESdkActivator.PLUGIN_ID, e.getMessage(), e);
        SdkLog.error(e.getMessage(), e);
        return errorStatus;
      }
    }
    finally {
      workingCopyManager.unregisterAll(monitor, save);
      monitor.done();
    }
    return Status.OK_STATUS;
  }

  protected void validate() {
  }

  protected abstract void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException;
}
