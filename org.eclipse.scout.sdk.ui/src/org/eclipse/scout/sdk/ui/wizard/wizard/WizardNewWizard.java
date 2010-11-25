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
package org.eclipse.scout.sdk.ui.wizard.wizard;

import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizard;
import org.eclipse.scout.sdk.workspace.IScoutBundle;

public class WizardNewWizard extends AbstractWorkspaceWizard {

  private WizardNewWizardPage m_page1;
  private final IScoutBundle m_clientBundle;

  public WizardNewWizard(IScoutBundle clientBundle) {
    m_clientBundle = clientBundle;
    m_page1 = new WizardNewWizardPage(getClientBundle());
    addPage(m_page1);
  }

  public IScoutBundle getClientBundle() {
    return m_clientBundle;
  }

}
