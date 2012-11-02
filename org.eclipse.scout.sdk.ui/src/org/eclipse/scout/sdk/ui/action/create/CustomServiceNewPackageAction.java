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

import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.action.AbstractWizardAction;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.wizard.services.CustomServiceNewPackageWizard;
import org.eclipse.scout.sdk.workspace.IScoutBundle;

/**
 *
 */
public class CustomServiceNewPackageAction extends AbstractWizardAction {

  private IScoutBundle m_bundle;

  public CustomServiceNewPackageAction() {
    super(Texts.get("Action_newTypeX", "Custom Service Package"), ScoutSdkUi.getImageDescriptor(ScoutSdkUi.ToolAdd), null, false, Category.NEW);
  }

  public void setScoutBundle(IScoutBundle bundle) {
    m_bundle = bundle;
  }

  @Override
  protected IWizard getNewWizardInstance() {
    CustomServiceNewPackageWizard w = null;
    try {
      w = new CustomServiceNewPackageWizard(m_bundle);
    }
    catch (JavaModelException e) {
      ScoutSdkUi.logError(e);
    }
    return w;
  }
}
