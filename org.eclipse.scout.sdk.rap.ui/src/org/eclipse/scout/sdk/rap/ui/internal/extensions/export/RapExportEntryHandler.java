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
package org.eclipse.scout.sdk.rap.ui.internal.extensions.export;

import java.io.File;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.scout.commons.IOUtility;
import org.eclipse.scout.sdk.operation.export.ExportServerWarOperation;
import org.eclipse.scout.sdk.rap.IScoutSdkRapConstants;
import org.eclipse.scout.sdk.rap.ui.internal.wizard.export.ExportRapWizardPage;
import org.eclipse.scout.sdk.ui.extensions.export.IExportScoutProjectEntryHandler;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.wizard.AbstractScoutWizardPage;
import org.eclipse.scout.sdk.ui.wizard.export.IExportScoutProjectWizard;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.ScoutBundleFilters;

public class RapExportEntryHandler implements IExportScoutProjectEntryHandler {

  public RapExportEntryHandler() {
  }

  @Override
  public IStatus getStatus(IExportScoutProjectWizard wizard) {
    return Status.OK_STATUS;
  }

  @Override
  public boolean isAvailable(IExportScoutProjectWizard wizard) {
    return wizard.getProject().getChildBundle(ScoutBundleFilters.getBundlesOfTypeFilter(IScoutSdkRapConstants.TYPE_UI_RAP), true) != null;
  }

  @Override
  public boolean getDefaultSelection() {
    return true;
  }

  @Override
  public File createModule(IExportScoutProjectWizard wizard, IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    try {
      ExportRapWizardPage rapPage = (ExportRapWizardPage) wizard.getPage(ExportRapWizardPage.class.getName());
      File tmpFolder = IOUtility.createTempDirectory("earExportRapBuildDir");

      ExportServerWarOperation op = new ExportServerWarOperation(rapPage.getProductFile());
      op.setWarFileName(new File(tmpFolder, rapPage.getWarName()).getAbsolutePath());
      op.validate();
      op.run(monitor, workingCopyManager);
      return op.getResultingWarFile();
    }
    catch (Exception e) {
      throw new CoreException(new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, "could not export server war file", e));
    }
  }

  @Override
  public void selectionChanged(IExportScoutProjectWizard wizard, boolean selected) {
    AbstractScoutWizardPage page = wizard.getPage(ExportRapWizardPage.class.getName());
    if (page == null) {
      page = new ExportRapWizardPage(wizard.getProject());
      wizard.addPage(page);
    }
    page.setExcludePage(!selected);
  }
}