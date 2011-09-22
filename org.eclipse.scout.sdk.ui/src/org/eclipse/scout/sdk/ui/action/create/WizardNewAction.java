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
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.action.AbstractWizardAction;
import org.eclipse.scout.sdk.ui.wizard.wizard.WizardNewWizard;
import org.eclipse.scout.sdk.workspace.IScoutBundle;

/**
 *
 */
public class WizardNewAction extends AbstractWizardAction {
  private IScoutBundle m_bundle;

  public WizardNewAction() {
    super(Texts.get("Action_newTypeX", "Wizard"), ScoutSdkUi.getImageDescriptor(ScoutSdkUi.WizardAdd), null, false, Category.NEW);
  }

  public void setScoutResource(IScoutBundle b) {
    m_bundle = b;
  }

  @Override
  protected IWizard getNewWizardInstance() {
    return new WizardNewWizard(m_bundle);
  }
}
