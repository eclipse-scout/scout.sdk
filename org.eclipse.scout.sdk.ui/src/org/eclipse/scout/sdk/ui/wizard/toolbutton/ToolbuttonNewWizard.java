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
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.typecache.IScoutWorkingCopyManager;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.extensions.AbstractFormFieldWizard;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizardPage;

public class ToolbuttonNewWizard extends AbstractFormFieldWizard {

  private ToolbuttonNewWizardPage1 m_page1;
  private ToolbuttonNewWizardPage2 m_page2a;
  private OutlineToolbuttonNewWizardPage m_page2b;

  public ToolbuttonNewWizard(IType formType) {
    initWizard(formType);
  }

  @Override
  public void initWizard(IType declaringType) {
    super.initWizard(declaringType);
    m_page1 = new ToolbuttonNewWizardPage1(getDeclaringType());
    addPage(m_page1);
    m_page2a = new ToolbuttonNewWizardPage2(getDeclaringType());
    addPage(m_page2a);
    m_page2b = new OutlineToolbuttonNewWizardPage(getDeclaringType());
    addPage(m_page2b);
  }

  @Override
  public void setSuperType(IType superType) {
    m_page1.setSuperType(superType);
  }

  @Override
  public boolean canFinish() {
    return m_page1.isPageComplete() && m_page1.getNextPage() != null && m_page1.getNextPage().isPageComplete();
  }

  @Override
  protected boolean performFinish(IProgressMonitor monitor, IScoutWorkingCopyManager workingCopyManager) {
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
