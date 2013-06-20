/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.ui.wizard.workingset;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.internal.view.outline.ScoutExplorerSettingsSupport;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.PlatformUI;

/**
 * <h3>{@link NewScoutWorkingSetWizard}</h3>
 * 
 * @author mvi
 * @since 3.10.0 20.06.2013
 */
public class NewScoutWorkingSetWizard extends Wizard implements INewWizard {

  private ScoutWorkingSetWizardPage m_page1;

  public NewScoutWorkingSetWizard() {
    setWindowTitle(Texts.get("NewScoutWorkingSet"));
  }

  @Override
  public void addPages() {
    super.addPages();
    if (m_page1 == null) {
      m_page1 = new ScoutWorkingSetWizardPage();
    }
    addPage(m_page1);
  }

  @Override
  public void init(IWorkbench workbench, IStructuredSelection selection) {
    //nop
  }

  @Override
  public boolean isHelpAvailable() {
    return false;
  }

  @Override
  public boolean performFinish() {
    m_page1.finish();
    IWorkingSet newSet = m_page1.getSelection();
    if (newSet != null) {
      newSet.setId(ScoutExplorerSettingsSupport.SCOUT_WOKRING_SET_ID);
      PlatformUI.getWorkbench().getWorkingSetManager().addWorkingSet(newSet);
    }
    return true;
  }
}
