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

import java.io.File;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.sdk.typecache.IScoutWorkingCopyManager;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizard;
import org.eclipse.scout.sdk.ws.jaxws.Texts;
import org.eclipse.scout.sdk.ws.jaxws.swt.wizard.page.AdditionalResourcesWizardPage;

public class AdditionalResourcesWizard extends AbstractWorkspaceWizard {

  private File[] m_files;

  private AdditionalResourcesWizardPage m_wizardPage;

  public AdditionalResourcesWizard(File[] files) {
    setWindowTitle(Texts.get("AdditionalResources"));
    m_files = files;
  }

  @Override
  public void addPages() {
    m_wizardPage = new AdditionalResourcesWizardPage();
    m_wizardPage.setFiles(m_files);
    addPage(m_wizardPage);
  }

  @Override
  protected boolean beforeFinish() throws CoreException {
    m_files = m_wizardPage.getFiles();
    return true;
  }

  @Override
  protected boolean performFinish(IProgressMonitor monitor, IScoutWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    return true;
  }

  public File[] getFiles() {
    return m_files;
  }
}
