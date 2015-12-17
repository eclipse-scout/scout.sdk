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
package org.eclipse.scout.sdk.s2e.nls.internal.ui.action;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Display;

/**
 * <h4>AbstractWorkspaceOperation</h4>
 */
public abstract class AbstractWorkspaceAction extends Action {

  private Job m_job;
  private final boolean m_interactWithUi;

  protected AbstractWorkspaceAction(String name, boolean interactWithUi) {
    super(name);
    m_interactWithUi = interactWithUi;
  }

  @Override
  public final void run() {
    if (Thread.currentThread() != Display.getDefault().getThread()) {
      Display.getDefault().syncExec(new Runnable() {
        @Override
        public void run() {
          AbstractWorkspaceAction.this.run();
        }
      });
    }
    if (m_interactWithUi) {
      if (interactWithUi()) {
        processInternal();
      }
    }
    else {
      processInternal();
    }
  }

  protected boolean interactWithUi() {
    return true;
  }

  /**
   * @param monitor
   */
  protected void execute(IProgressMonitor monitor) {
  }

  private void processInternal() {
    m_job = new Job(getText()) {
      @Override
      protected IStatus run(IProgressMonitor monitor) {
        execute(monitor);
        return Status.OK_STATUS;
      }
    };
    m_job.setSystem(true);
    m_job.setUser(false);
    m_job.schedule();
  }

  public final void join() throws InterruptedException {
    if (m_job != null) {
      m_job.join();
    }
  }

}
