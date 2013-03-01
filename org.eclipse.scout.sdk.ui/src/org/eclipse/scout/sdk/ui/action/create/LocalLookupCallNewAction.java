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
import org.eclipse.scout.sdk.ui.wizard.lookupcall.LocalLookupCallNewWizard;
import org.eclipse.scout.sdk.workspace.IScoutBundle;

/**
 *
 */
public class LocalLookupCallNewAction extends AbstractWizardAction {

  private IScoutBundle m_scoutBundle;

  public LocalLookupCallNewAction() {
    super(Texts.get("NewLocalLookupCall"), ScoutSdkUi.getImageDescriptor(ScoutSdkUi.LookupCallAdd), null, false, Category.NEW);
  }

  @Override
  public boolean isVisible() {
    return !m_scoutBundle.isBinary();
  }

  public void setScoutBundle(IScoutBundle scoutBundle) {
    m_scoutBundle = scoutBundle;
  }

  @Override
  protected IWizard getNewWizardInstance() {
    return new LocalLookupCallNewWizard(m_scoutBundle);
  }
}
