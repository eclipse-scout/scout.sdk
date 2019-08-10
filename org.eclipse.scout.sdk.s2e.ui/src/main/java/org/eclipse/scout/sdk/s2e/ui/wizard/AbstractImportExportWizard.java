/*
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.s2e.ui.wizard;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.scout.sdk.core.s.nls.TranslationStoreStack;

/**
 * <h4>AbstractImportExportWizard</h4>
 *
 * @since 1.1.0 (12.11.2010)
 */
public abstract class AbstractImportExportWizard extends Wizard {

  private TranslationStoreStack m_nlsProject;

  public void setNlsProject(TranslationStoreStack nlsProject) {
    m_nlsProject = nlsProject;
  }

  public TranslationStoreStack getNlsProject() {
    return m_nlsProject;
  }
}
