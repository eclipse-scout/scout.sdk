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
package org.eclipse.scout.nls.sdk.extension;

import org.eclipse.scout.nls.sdk.internal.NlsCore;

/**
 * <h4>ImportExportExtensionPoint</h4>
 *
 * @author Andreas Hoegger
 * @since 1.1.0 (11.11.2010)
 */
public class WizardExtension {
  private Class<? extends AbstractImportExportWizard> m_wizard;
  private String m_name;

  /**
   * @return the wizard
   */
  public Class<? extends AbstractImportExportWizard> getWizard() {
    return m_wizard;
  }

  public void setWizard(Class<? extends AbstractImportExportWizard> wizard) {
    m_wizard = wizard;
  }

  public AbstractImportExportWizard createWizard() {
    try {
      return m_wizard.newInstance();
    }
    catch (Exception e) {
      NlsCore.logError("could not create a new instance of wizard '" + getName() + "'.", e);
      return null;
    }
  }

  public void setName(String name) {
    m_name = name;
  }

  public String getName() {
    return m_name;
  }
}
