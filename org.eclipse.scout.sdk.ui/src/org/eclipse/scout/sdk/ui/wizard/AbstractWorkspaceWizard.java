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
package org.eclipse.scout.sdk.ui.wizard;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.typecache.IScoutWorkingCopyManager;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.swt.widgets.Display;

/**
 * <h3>AbstractProcessWizard</h3> ...
 */
public class AbstractWorkspaceWizard extends AbstractWizard {

  public AbstractWorkspaceWizard() {
  }

  @Override
  public void addPage(IWizardPage page) {
    if (page instanceof AbstractWorkspaceWizardPage) {
      super.addPage(page);
    }
    else {
      throw new IllegalArgumentException("Expecting an instance of '" + AbstractWorkspaceWizardPage.class.getName() + "'.");
    }
  }

  @Override
  public final boolean performFinish() {
    try {
      if (!beforeFinish()) {
        ScoutSdkUi.logInfo("Wizard '" + getWindowTitle() + "' before finish canceled.");
        return false;
      }
      P_PerformFinishOperation performFinishOperation = new P_PerformFinishOperation(getShell().getDisplay());
      OperationJob job = new OperationJob(performFinishOperation);
      job.schedule();
      return true;
    }
    catch (CoreException e) {
      ScoutSdkUi.logError("Wizard finished abnormally", e);
      return false;
    }
  }

  /**
   * The default implementation iterates over the associated wizard pages and calls
   * {@link AbstractWorkspaceWizardPage#performFinish()} of each page. Overwrite this method to implement a specific
   * finish behavior.
   * 
   * @param monitor
   * @param workingCopyManager
   * @return
   * @throws CoreException
   * @throws IllegalArgumentException
   */
  protected boolean performFinish(IProgressMonitor monitor, IScoutWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    for (IWizardPage page : getPages()) {
      boolean validPage = false;
      AbstractWorkspaceWizardPage bcPage = (AbstractWorkspaceWizardPage) page;
      try {
        validPage = bcPage.performFinish(monitor, workingCopyManager);
      }
      catch (Exception e) {
        ScoutSdkUi.logError("exception during perfoming finish on wizard page '" + page.getClass().getName() + "'.", e);
        return false;
      }
      if (!validPage) {
        return false;
      }
    }
    return true;
  }

  protected void postFinishDisplayThread() {
  }

  /**
   * Is called before the wizard is finished and disposed. Typically, this method is overwritten to extract values from
   * within UI fields of the wizard pages to parameterize an associated operation.
   * 
   * @return true to continue or false to cancel
   * @throws CoreException
   */
  protected boolean beforeFinish() throws CoreException {
    return true;
  }

  private class P_PerformFinishOperation implements IOperation {
    private boolean m_success;
    private final Display m_display;

    private P_PerformFinishOperation(Display display) {
      m_display = display;
    }

    public boolean isSuccess() {
      return m_success;
    }

    public String getOperationName() {
      return getWindowTitle();
    }

    @Override
    public void validate() throws IllegalArgumentException {

    }

    public void run(IProgressMonitor monitor, IScoutWorkingCopyManager workingCopyManager) throws CoreException {
      try {
        m_success = performFinish(monitor, workingCopyManager);
        m_display.asyncExec(new Runnable() {

          @Override
          public void run() {
            postFinishDisplayThread();
          }
        });
      }
      catch (Exception e) {
        ScoutSdkUi.logError("exception during perfoming finish on wizard page '" + AbstractWorkspaceWizard.this.getClass().getName() + "'.", e);
        m_success = false;
      }
    }
  } // end class P_PerformFinishOperation

}
