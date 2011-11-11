/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Daniel Wiehl (BSI Business Systems Integration AG) - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.ws.jaxws.swt.wizard;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.sdk.typecache.IScoutWorkingCopyManager;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizard;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.ws.jaxws.Texts;
import org.eclipse.scout.sdk.ws.jaxws.swt.wizard.page.SourceFolderNewWizardPage;

public class SourceFolderNewWizard extends AbstractWorkspaceWizard {

  private IScoutBundle m_bundle;
  private SourceFolderNewWizardPage m_wizardPage;
  private IPath m_sourceFolder;

  public SourceFolderNewWizard(IScoutBundle bundle) {
    m_bundle = bundle;
    setWindowTitle(Texts.get("CreateSourceFolder"));
  }

  @Override
  public void addPages() {
    m_wizardPage = new SourceFolderNewWizardPage();
    m_wizardPage.setBundle(m_bundle);
    addPage(m_wizardPage);
  }

  @Override
  protected boolean beforeFinish() throws CoreException {
    m_sourceFolder = m_wizardPage.getSourceFolderPath();
    return true;
  }

  @Override
  protected boolean performFinish(IProgressMonitor monitor, IScoutWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    return true;
  }

  public IPath getSourceFolder() {
    return m_sourceFolder;
  }
}
