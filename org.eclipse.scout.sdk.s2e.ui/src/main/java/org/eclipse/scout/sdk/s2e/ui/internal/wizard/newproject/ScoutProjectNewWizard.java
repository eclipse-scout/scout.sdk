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
package org.eclipse.scout.sdk.s2e.ui.internal.wizard.newproject;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.scout.sdk.s2e.job.ResourceBlockingOperationJob;
import org.eclipse.scout.sdk.s2e.ui.internal.ISdkIcons;
import org.eclipse.scout.sdk.s2e.ui.internal.S2ESdkUiActivator;
import org.eclipse.scout.sdk.s2e.ui.wizard.AbstractWizard;
import org.eclipse.scout.sdk.s2e.workspace.newproject.ScoutProjectNewOperation;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

/**
 * <h3>{@link ScoutProjectNewWizard}</h3> Wizard that creates a new Scout project
 *
 * @author Matthias Villiger
 * @since 5.1.0
 */
public class ScoutProjectNewWizard extends AbstractWizard implements INewWizard {

  private ScoutProjectNewWizardPage m_page1;

  @Override
  public void init(IWorkbench workbench, IStructuredSelection selection) {
    setWindowTitle("New Scout Project");
    setHelpAvailable(false);
    setDefaultPageImageDescriptor(S2ESdkUiActivator.getImageDescriptor(ISdkIcons.ScoutProjectNewWizBanner));

    m_page1 = new ScoutProjectNewWizardPage();
    addPage(m_page1);
  }

  @Override
  public boolean performFinish() {
    // prepare operation
    ScoutProjectNewOperation op = new ScoutProjectNewOperation();
    op.setDisplayName(m_page1.getDisplayName());
    op.setSymbolicName(m_page1.getSymbolicName());
    if (m_page1.isUseWorkspaceLocation()) {
      op.setTargetDirectory(ScoutProjectNewWizardPage.getWorkspaceLocation());
    }
    else {
      op.setTargetDirectory(m_page1.getTargetDirectory());
    }

    // remember folder
    String path = null;
    if (m_page1.getTargetDirectory() != null) {
      path = m_page1.getTargetDirectory().getAbsolutePath();
    }
    getDialogSettings().put(ScoutProjectNewWizardPage.SETTINGS_TARGET_DIR, path);

    // run operation
    new ResourceBlockingOperationJob(op).schedule();

    return true;
  }
}
