/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2e.ui.wizard;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.scout.sdk.core.s.nls.manager.TranslationManager;

/**
 * <h4>AbstractImportExportWizard</h4>
 *
 * @since 1.1.0 (12.11.2010)
 */
public abstract class AbstractImportExportWizard extends Wizard {

  private TranslationManager m_nlsProject;

  public void setNlsProject(TranslationManager nlsProject) {
    m_nlsProject = nlsProject;
  }

  public TranslationManager getNlsProject() {
    return m_nlsProject;
  }
}
