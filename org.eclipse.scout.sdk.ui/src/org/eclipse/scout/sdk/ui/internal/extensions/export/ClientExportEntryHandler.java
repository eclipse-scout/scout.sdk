package org.eclipse.scout.sdk.ui.internal.extensions.export;

import java.io.File;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.scout.commons.IOUtility;
import org.eclipse.scout.sdk.operation.export.ExportClientZipOperation;
import org.eclipse.scout.sdk.ui.extensions.export.IExportScoutProjectEntryHandler;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.internal.wizard.export.ExportClientWizardPage;
import org.eclipse.scout.sdk.ui.wizard.AbstractScoutWizardPage;
import org.eclipse.scout.sdk.ui.wizard.export.IExportScoutProjectWizard;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.ScoutBundleFilters;

public class ClientExportEntryHandler implements IExportScoutProjectEntryHandler {

  public static final String ID = "ui.client";

  public ClientExportEntryHandler() {
  }

  @Override
  public IStatus getStatus(IExportScoutProjectWizard wizard) {
    return Status.OK_STATUS;
  }

  @Override
  public boolean getDefaultSelection() {
    return false;
  }

  @Override
  public File createModule(IExportScoutProjectWizard wizard, IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    if (!wizard.getExportWizardPage().isNodesSelected(ClientExportEntryHandler.ID)) {
      return null;
    }
    try {
      ExportClientWizardPage clientPage = (ExportClientWizardPage) wizard.getPage(ExportClientWizardPage.class.getName());

      File tmpFolder = IOUtility.createTempDirectory("earExportClientBuildDir");

      ExportClientZipOperation op = new ExportClientZipOperation(clientPage.getClientProductFile());
      op.setTargetDirectory(tmpFolder.getAbsolutePath());
      op.setHtmlFolder(clientPage.getClientExportFolder());
      op.validate();
      op.run(monitor, workingCopyManager);
      return op.getResultingZipFile();
    }
    catch (Exception e) {
      throw new CoreException(new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, "could not export server war file", e));
    }
  }

  @Override
  public boolean isAvailable(IExportScoutProjectWizard wizard) {
    return wizard.getProject().getChildBundle(ScoutBundleFilters.getBundlesOfTypeFilter(IScoutBundle.TYPE_UI_SWING, IScoutBundle.TYPE_UI_SWT), true) != null;
  }

  @Override
  public void selectionChanged(IExportScoutProjectWizard wizard, boolean selected) {
    AbstractScoutWizardPage page = wizard.getPage(ExportClientWizardPage.class.getName());
    if (page == null) {
      page = new ExportClientWizardPage(wizard.getProject());
      wizard.addPage(page);
    }
    page.setExcludePage(!selected && !wizard.getExportWizardPage().isNodesSelected(ServerExportEntryHandler.ID));
  }
}
