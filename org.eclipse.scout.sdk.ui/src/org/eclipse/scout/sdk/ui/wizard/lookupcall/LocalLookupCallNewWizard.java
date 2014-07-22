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
package org.eclipse.scout.sdk.ui.wizard.lookupcall;

import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizard;
import org.eclipse.scout.sdk.workspace.IScoutBundle;

/**
 * <h3>{@link LocalLookupCallNewWizard}</h3>
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 31.08.2010
 */
public class LocalLookupCallNewWizard extends AbstractWorkspaceWizard {

  private final IScoutBundle m_clientBundle;

  public LocalLookupCallNewWizard(IScoutBundle clientBundle) {
    setWindowTitle(Texts.get("NewLocalLookupCallNoPopup"));
    m_clientBundle = clientBundle;
    addPage(new LocalLookupCallNewWizardPage(clientBundle));
  }

  /**
   * @return the clientBundle
   */
  public IScoutBundle getClientBundle() {
    return m_clientBundle;
  }
}
