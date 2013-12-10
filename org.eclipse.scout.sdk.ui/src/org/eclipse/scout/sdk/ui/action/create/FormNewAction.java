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
package org.eclipse.scout.sdk.ui.action.create;

import org.eclipse.jface.wizard.IWizard;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.action.AbstractWizardAction;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.wizard.form.FormNewWizard;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.ui.PlatformUI;

/**
 *
 */
public class FormNewAction extends AbstractWizardAction {

  private IScoutBundle m_scoutBundle;

  public FormNewAction() {
    super(Texts.get("Action_newTypeX", "Form"), ScoutSdkUi.getImageDescriptor(ScoutSdkUi.FormAdd), null, false, Category.NEW);
  }

  public void setScoutBundle(IScoutBundle scoutBundle) {
    m_scoutBundle = scoutBundle;
  }

  @Override
  protected IWizard getNewWizardInstance() {
    FormNewWizard fnw = new FormNewWizard(m_scoutBundle);
    fnw.init(PlatformUI.getWorkbench(), null);
    return fnw;
  }

  @Override
  public boolean isVisible() {
    return !m_scoutBundle.isBinary();
  }
}
