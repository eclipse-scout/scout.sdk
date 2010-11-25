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
package org.eclipse.scout.sdk.ui.wizard.bundle;

import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizard;
import org.eclipse.scout.sdk.workspace.IScoutProject;

public class BundleImportWizard extends AbstractWorkspaceWizard {

  private IScoutProject m_project;
  private BundleImportWizardPage m_importBundlePage;

  public BundleImportWizard() {
    this(null);
  }

  public BundleImportWizard(IScoutProject project) {
    // create pages
    m_importBundlePage = new BundleImportWizardPage();
    addPage(m_importBundlePage);
    setProject(project);
  }

  public void setProject(IScoutProject project) {
    m_project = project;
    if (project != null) {
      // m_importBundlePage.setProjectAlias(project.getAlias());
      m_importBundlePage.setProjectId(project.getProjectName());
    }
  }

  public IScoutProject getProject() {
    return m_project;
  }

}
