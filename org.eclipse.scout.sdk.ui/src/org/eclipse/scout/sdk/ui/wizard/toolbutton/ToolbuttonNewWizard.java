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
package org.eclipse.scout.sdk.ui.wizard.toolbutton;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.extensions.AbstractInnerTypeWizard;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizardPage;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.ui.IWorkbench;

public class ToolbuttonNewWizard extends AbstractInnerTypeWizard {

  private ToolbuttonNewWizardPage1 m_page1;
  private ToolbuttonNewWizardPage2 m_page2a;
  private OutlineToolbuttonNewWizardPage m_page2b;

  @Override
  public void init(IWorkbench workbench, IStructuredSelection selection) {
    super.init(workbench, selection);

    setWindowTitle(Texts.get("NewToolButton"));

    m_page1 = new ToolbuttonNewWizardPage1(getDeclaringType());
    if (getSuperType() != null) {
      m_page1.setSuperType(getSuperType());
    }
    addPage(m_page1);
    m_page2a = new ToolbuttonNewWizardPage2(getDeclaringType());
    if (getTypeName() != null) {
      m_page2a.setTypeName(getTypeName());
    }
    addPage(m_page2a);
    m_page2b = new OutlineToolbuttonNewWizardPage(getDeclaringType());
    if (getSiblingProposal() != null) {
      m_page2b.setSibling(getSiblingProposal());
    }
    if (getTypeName() != null) {
      m_page2b.setTypeName(getTypeName());
    }
    addPage(m_page2b);
  }

  @Override
  public boolean canFinish() {
    return m_page1.isPageComplete() && m_page1.getNextPage() != null && m_page1.getNextPage().isPageComplete();
  }

  @Override
  protected boolean performFinish(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) {
    boolean validPage = false;
    try {
      validPage = m_page1.performFinish(monitor, workingCopyManager);
      if (validPage) {
        validPage = ((AbstractWorkspaceWizardPage) m_page1.getNextPage()).performFinish(monitor, workingCopyManager);
      }
    }
    catch (Exception e) {
      ScoutSdkUi.logError("exception during perfoming finish on wizard.", e);
    }
    return validPage;
  }
}
