package org.eclipse.scout.sdk.ui.internal.extensions.export;

import java.io.File;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.scout.commons.IOUtility;
import org.eclipse.scout.sdk.operation.export.ExportServerWarOperation;
import org.eclipse.scout.sdk.ui.extensions.export.IExportScoutProjectEntryHandler;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.internal.wizard.export.ExportClientWizardPage;
import org.eclipse.scout.sdk.ui.internal.wizard.export.ExportServerWizardPage;
import org.eclipse.scout.sdk.ui.wizard.AbstractScoutWizardPage;
import org.eclipse.scout.sdk.ui.wizard.export.IExportScoutProjectWizard;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;

public class ServerExportEntryHandler implements IExportScoutProjectEntryHandler {

  public final static String ID = "server";

  public ServerExportEntryHandler() {
  }

  @Override
  public IStatus getStatus(IExportScoutProjectWizard wizard) {
    return Status.OK_STATUS;
  }

  @Override
  public File createModule(IExportScoutProjectWizard wizard, IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    try {
      ExportServerWizardPage serverPage = (ExportServerWizardPage) wizard.getPage(ExportServerWizardPage.class.getName());
      File tmpFolder = IOUtility.createTempDirectory("earExportServerBuildDir");

      ExportServerWarOperation op = new ExportServerWarOperation(serverPage.getProductFile());
      op.setWarFileName(new File(tmpFolder, serverPage.getWarName()).getAbsolutePath());
      if (wizard.getExportWizardPage().isNodesSelected(ClientExportEntryHandler.ID)) {
        ExportClientWizardPage clientPage = (ExportClientWizardPage) wizard.getPage(ExportClientWizardPage.class.getName());
        op.setClientProduct(clientPage.getClientProductFile());
        op.setHtmlFolder(clientPage.getClientExportFolder());
      }
      op.validate();
      op.run(monitor, workingCopyManager);
      return op.getResultingWarFile();
    }
    catch (Exception e) {
      throw new CoreException(new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, "could not export server war file", e));
    }
  }

  @Override
  public boolean isAvailable(IExportScoutProjectWizard wizard) {
    return wizard.getProject().getServerBundle() != null;
  }

  @Override
  public void selectionChanged(IExportScoutProjectWizard wizard, boolean selected) {
    AbstractScoutWizardPage page = wizard.getPage(ExportServerWizardPage.class.getName());
    if (page == null) {
      page = new ExportServerWizardPage(wizard.getProject());
      wizard.addPage(page);
    }
    page.setExcludePage(!selected);
  }
}
