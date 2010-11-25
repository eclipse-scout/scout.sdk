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
package org.eclipse.scout.sdk.ui.action;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizard;
import org.eclipse.scout.sdk.ui.wizard.ScoutWizardDialog;

public class WizardAction extends Action {
  private final IWizard m_wizard;
  private int m_result;

  public WizardAction(String name, AbstractWorkspaceWizard wizard) {
    this(name, null, wizard);
  }

  public WizardAction(String name, ImageDescriptor imageDescriptor, IWizard wizard) {
    super(name);
    setImageDescriptor(imageDescriptor);
    m_wizard = wizard;
  }

  @Override
  public void run() {
    ScoutWizardDialog wizardDialog = new ScoutWizardDialog(m_wizard);
    m_result = wizardDialog.open();
  }

  public IWizard getWizard() {
    return m_wizard;
  }

  public int getResult() {
    return m_result;
  }

}
