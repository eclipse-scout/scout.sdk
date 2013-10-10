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
package org.eclipse.scout.sdk.ui.internal.wizard.newbundle;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.operation.project.IScoutProjectNewOperation;
import org.eclipse.scout.sdk.operation.project.add.ScoutProjectAddOperation;
import org.eclipse.scout.sdk.ui.internal.wizard.newproject.ScoutProjectNewWizard;
import org.eclipse.scout.sdk.ui.wizard.project.IScoutProjectWizardPage;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.ui.IWorkbench;

/**
 * <h3>{@link ScoutBundleAddWizard}</h3> ...
 * 
 * @author mvi
 * @since 3.8.0 02.03.2012
 */
public class ScoutBundleAddWizard extends ScoutProjectNewWizard {
  private final ScoutBundleAddWizardPage m_page1;
  private ProductFileSelectionWizardPage m_page2;
  private final IScoutBundle m_scoutProject;

  public ScoutBundleAddWizard(IScoutBundle project) {
    setWindowTitle(Texts.get("CreateNewScoutBundles"));
    m_scoutProject = project;

    m_page1 = new ScoutBundleAddWizardPage(project);
    addPage(m_page1);

    m_page2 = new ProductFileSelectionWizardPage(project);
    addPage(m_page2);
  }

  @Override
  public void init(IWorkbench workbench, IStructuredSelection selection) {
    //nop
  }

  @Override
  public boolean needsPreviousAndNextButtons() {
    return true;
  }

  @Override
  public boolean performFinish() {
    new P_FinishJob().schedule();
    return true;
  }

  @Override
  public IScoutProjectWizardPage getProjectWizardPage() {
    return m_page1;
  }

  @Override
  public IScoutBundle getScoutProject() {
    return m_scoutProject;
  }

  private class P_FinishJob extends P_PerformFinishJob {

    public P_FinishJob() {
      super(getContainer().getShell().getDisplay());
    }

    @Override
    protected void switchPerspective() {
      //nop
    }

    @Override
    protected IScoutProjectNewOperation getFinishOperation() {
      return new ScoutProjectAddOperation(getScoutProject());
    }
  } // end class  P_PerformFinishJob
}
