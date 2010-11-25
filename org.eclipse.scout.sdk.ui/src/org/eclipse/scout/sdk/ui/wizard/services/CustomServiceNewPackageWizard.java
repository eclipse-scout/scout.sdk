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
package org.eclipse.scout.sdk.ui.wizard.services;

import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.wizards.NewPackageCreationWizard;
import org.eclipse.jdt.ui.wizards.NewPackageWizardPage;
import org.eclipse.scout.sdk.ScoutIdeProperties;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.operation.util.PackageNewOperation;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.workspace.IScoutBundle;

@SuppressWarnings("restriction")
public class CustomServiceNewPackageWizard extends NewPackageCreationWizard {

  private NewPackageWizardPage m_page1;
  private IJavaProject m_javaProject;
  private final IScoutBundle m_serverBundle;

  public CustomServiceNewPackageWizard(IScoutBundle serverBundle) throws JavaModelException {
    m_serverBundle = serverBundle;
    m_javaProject = getServerBundle().getJavaProject();
  }

  @Override
  public void addPages() {
    super.addPages();
    try {
      String srcPath = "/" + m_javaProject.getElementName() + "/" + ScoutIdeProperties.DEFAULT_SOURCE_FOLDER_NAME;
      m_page1 = (NewPackageWizardPage) getPages()[0];

      m_page1.setPackageFragmentRoot(m_javaProject.findPackageFragmentRoot(new Path(srcPath)), false);
      m_page1.setPackageText(getServerBundle().getPackageName(IScoutBundle.SERVER_PACKAGE_APPENDIX_SERVICES_CUSTOM), true);
    }
    catch (Exception e) {
      ScoutSdkUi.logError(e);
      performCancel();
    }

  }

  @Override
  public boolean performFinish() {
    String packageName = m_page1.getPackageText();
    PackageNewOperation packageNewOperation = new PackageNewOperation(m_javaProject, ScoutIdeProperties.DEFAULT_SOURCE_FOLDER_NAME, packageName);
    OperationJob job = new OperationJob(packageNewOperation);
    job.schedule();
    try {
      job.join();
    }
    catch (InterruptedException e) {
      ScoutSdkUi.logWarning(e);
    }
    return true;
  }

  public IScoutBundle getServerBundle() {
    return m_serverBundle;
  }
}
