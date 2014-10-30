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
package org.eclipse.scout.sdk.ui.wizard.code.type;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.action.create.CodeTypeNewAction;
import org.eclipse.scout.sdk.ui.executor.AbstractWizardExecutor;
import org.eclipse.scout.sdk.ui.extensions.executor.ExecutorExtensionPoint;
import org.eclipse.scout.sdk.ui.extensions.executor.IExecutor;
import org.eclipse.scout.sdk.ui.util.UiUtility;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizard;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.ScoutBundleFilters;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

public class CodeTypeNewWizard extends AbstractWorkspaceWizard {

  private CodeTypeNewWizardPage m_page1;

  @Override
  public void init(IWorkbench workbench, IStructuredSelection selection) {
    setWindowTitle(Texts.get("NewCodeType"));

    INewWizard wizard = getWizard();
    if (wizard == null || wizard.getClass().equals(getClass())) {
      IScoutBundle sharedBundle = UiUtility.getScoutBundleFromSelection(selection, ScoutBundleFilters.getBundlesOfTypeFilter(IScoutBundle.TYPE_SHARED));
      String pck = UiUtility.getPackageSuffix(selection);

      m_page1 = new CodeTypeNewWizardPage(sharedBundle);
      m_page1.setTargetPackage(pck);
      addPage(m_page1);
    }
    else {
      wizard.init(workbench, selection);
      addPage(wizard.getStartingPage());
    }
  }

  protected INewWizard getWizard() {
    IExecutor executor = ExecutorExtensionPoint.getExecutorFor(CodeTypeNewAction.class.getName());
    if (executor instanceof AbstractWizardExecutor) {
      AbstractWizardExecutor wizardExecutor = (AbstractWizardExecutor) executor;
      return wizardExecutor.getNewWizardInstance();
    }
    return null;
  }
}
