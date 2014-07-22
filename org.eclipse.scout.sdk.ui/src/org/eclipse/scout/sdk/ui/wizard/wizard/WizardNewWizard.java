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

import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizard;
import org.eclipse.scout.sdk.workspace.IScoutBundle;

/**
 * <h3>{@link WizardNewWizard}</h3>
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 19.05.2011
 */
public class WizardNewWizard extends AbstractWorkspaceWizard {

  private WizardNewWizardPage m_page1;
  private final IScoutBundle m_clientBundle;

  public WizardNewWizard(IScoutBundle clientBundle) {
    setWindowTitle(Texts.get("NewWizard"));
    m_clientBundle = clientBundle;
    m_page1 = new WizardNewWizardPage(getClientBundle());
    addPage(m_page1);
  }

  public IScoutBundle getClientBundle() {
    return m_clientBundle;
  }

}
