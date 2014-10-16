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
import org.eclipse.scout.sdk.ui.util.UiUtility;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizard;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.ScoutBundleFilters;
import org.eclipse.ui.IWorkbench;

public class CodeTypeNewWizard extends AbstractWorkspaceWizard {

  private CodeTypeNewWizardPage m_page1;

  @Override
  public void init(IWorkbench workbench, IStructuredSelection selection) {
    IScoutBundle sharedBundle = UiUtility.getScoutBundleFromSelection(selection, ScoutBundleFilters.getBundlesOfTypeFilter(IScoutBundle.TYPE_SHARED));
    String pck = UiUtility.getPackageSuffix(selection);

    setWindowTitle(Texts.get("NewCodeType"));

    m_page1 = new CodeTypeNewWizardPage(sharedBundle);
    m_page1.setTargetPackage(pck);
    addPage(m_page1);
  }
}
