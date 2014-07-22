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

import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.CompositeObject;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.swt.widgets.Display;

/**
 * <h3>AbstractProcessWizard</h3>
 */
public abstract class AbstractWorkspaceWizard extends AbstractWizard implements IWorkspaceWizard {

  private final Map<CompositeObject, IOperation> m_performFinishOperations;

  public AbstractWorkspaceWizard() {
    m_performFinishOperations = new TreeMap<CompositeObject, IOperation>();
  }

  @Override
  public IOperation addAdditionalPerformFinishOperation(IOperation op, double orderNr) {
    return m_performFinishOperations.put(new CompositeObject(orderNr, op), op);
  }

  @Override
  public IOperation removeAdditionalPerformFinishOperation(IOperation op) {
    CompositeObject key = null;
    for (Entry<CompositeObject, IOperation> e : m_performFinishOperations.entrySet()) {
      if (CompareUtility.equals(e.getValue(), op)) {
        key = e.getKey();
        break;
      }
    }
    if (key != null) {
      return m_performFinishOperations.remove(key);
    }
    return null;
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
      m_performFinishOperations.put(new CompositeObject(IWorkspaceWizard.ORDER_DEFAULT, performFinishOperation), performFinishOperation);

      OperationJob job = new OperationJob(m_performFinishOperations.values());
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
   * @throws
   */
  protected boolean performFinish(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
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

  private final class P_PerformFinishOperation implements IOperation {
    private final Display m_display;

    private P_PerformFinishOperation(Display display) {
      m_display = display;
    }

    @Override
    public String getOperationName() {
      return getWindowTitle();
    }

    @Override
    public void validate() {

    }

    @Override
    public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
      try {
        performFinish(monitor, workingCopyManager);
        m_display.asyncExec(new Runnable() {

          @Override
          public void run() {
            postFinishDisplayThread();
          }
        });
      }
      catch (CoreException e) {
        throw e;
      }
      catch (Exception e) {
        ScoutSdkUi.logError("exception during perfoming finish on wizard page '" + AbstractWorkspaceWizard.this.getClass().getName() + "'.", e);
      }
    }
  } // end class P_PerformFinishOperation
}
