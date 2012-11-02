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
package org.eclipse.scout.nls.sdk.internal.ui.wizard.importExport;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.scout.nls.sdk.extension.NlsExportImportExtensionPoints;
import org.eclipse.scout.nls.sdk.model.workspace.project.INlsProject;

/** <h4> NlsImportWizard </h4>
 *
 * @author Andreas Hoegger
 * @since 1.1.0 (12.11.2010)
 *
 */
public class NlsImportWizard extends Wizard {

  private final INlsProject m_project;

  public NlsImportWizard(INlsProject project) {
    super();
    m_project = project;
    setWindowTitle("Import");
  }

  @Override
  public void addPages() {
    addPage(new ImportExportWizardPage("Import NLS Entries", "Choose a exporter to import NLS entries.", m_project, NlsExportImportExtensionPoints.EXTENSION_POINT_ID_NLS_IMPORTER));
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
