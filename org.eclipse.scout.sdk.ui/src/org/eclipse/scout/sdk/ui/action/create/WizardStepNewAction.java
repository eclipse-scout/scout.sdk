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

import org.eclipse.jdt.core.IType;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.action.AbstractWizardAction;
import org.eclipse.scout.sdk.ui.wizard.wizard.step.WizardStepNewWizard;

public class WizardStepNewAction extends AbstractWizardAction {

  private IType m_type;

  public WizardStepNewAction() {
    super(Texts.get("Action_newTypeX", "Wizard step"), ScoutSdkUi.getImageDescriptor(ScoutSdkUi.WizardStepAdd), null, false, Category.NEW);
  }

  public void init(IType type) {
    m_type = type;
  }

  @Override
  protected IWizard getNewWizardInstance() {
    WizardStepNewWizard wizard = new WizardStepNewWizard();
    wizard.initWizard(m_type);
    return wizard;
  }
}
