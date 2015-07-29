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
package org.eclipse.scout.sdk.s2e.nls.internal.ui.importexport;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.scout.sdk.s2e.nls.importexport.NlsExportImportExtensionPoints;
import org.eclipse.scout.sdk.s2e.nls.project.INlsProject;

/**
 * <h4>NlsExportWizard</h4>
 *
 * @author Andreas Hoegger
 * @since 1.1.0 (11.11.2010)
 */
public class NlsExportWizard extends Wizard {

  private final INlsProject m_project;

  public NlsExportWizard(INlsProject project) {
    super();
    m_project = project;
    setWindowTitle("Export");
  }

  @Override
  public void addPages() {
    addPage(new ImportExportWizardPage("Export NLS Entries", "Please choose an exporter.", m_project, NlsExportImportExtensionPoints.EXTENSION_POINT_ID_NLS_EXPORTER));
  }

  @Override
  public boolean performFinish() {
    return false;
  }

  @Override
  public boolean needsPreviousAndNextButtons() {
    return true;
  }

  @Override
  public boolean canFinish() {
    return false;

  }

}
