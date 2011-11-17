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

import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.action.AbstractWizardAction;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.wizard.services.CustomServiceNewWizard;
import org.eclipse.scout.sdk.workspace.IScoutBundle;

/**
 *
 */
public class CustomServiceNewAction extends AbstractWizardAction {

  private IScoutBundle m_bundle;
  private IPackageFragment m_pck;

  public CustomServiceNewAction() {
    super(Texts.get("Action_newTypeX", "Custom Service"), ScoutSdkUi.getImageDescriptor(ScoutSdkUi.ServiceAdd), null, false, Category.NEW);
  }

  public void init(IScoutBundle bundle, IPackageFragment pck) {
    m_bundle = bundle;
    m_pck = pck;
  }

  @Override
  protected IWizard getNewWizardInstance() {
    return new CustomServiceNewWizard(m_bundle, m_pck);
  }
}
