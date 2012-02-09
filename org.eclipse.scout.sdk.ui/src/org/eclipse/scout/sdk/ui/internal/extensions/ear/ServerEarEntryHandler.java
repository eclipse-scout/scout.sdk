package org.eclipse.scout.sdk.ui.internal.extensions.ear;

import java.io.File;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.scout.commons.IOUtility;
import org.eclipse.scout.sdk.operation.export.ExportServerWarOperation;
import org.eclipse.scout.sdk.ui.extensions.ear.IEarEntryHandler;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.internal.wizard.export.ExportEarClientWizardPage;
import org.eclipse.scout.sdk.ui.internal.wizard.export.ExportEarServerWizardPage;
import org.eclipse.scout.sdk.ui.wizard.AbstractScoutWizardPage;
import org.eclipse.scout.sdk.ui.wizard.ear.IScoutEarExportWizard;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;

public class ServerEarEntryHandler implements IEarEntryHandler {

  public final static String ID = "server";

  public ServerEarEntryHandler() {
  }

  @Override
  public IStatus getStatus(IScoutEarExportWizard wizard) {
    return Status.OK_STATUS;
  }

  @Override
  public File createModule(IScoutEarExportWizard wizard, IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    try {
      ExportEarServerWizardPage serverPage = (ExportEarServerWizardPage) wizard.getPage(ExportEarServerWizardPage.class.getName());
      File tmpFolder = IOUtility.createTempDirectory("earExportServerBuildDir");

      ExportServerWarOperation op = new ExportServerWarOperation(serverPage.getProductFile());
      op.setWarFileName(new File(tmpFolder, serverPage.getWarName()).getAbsolutePath());
      if (wizard.getExportWizardPage().isNodesSelected(ClientEarEntryHandler.ID)) {
        ExportEarClientWizardPage clientPage = (ExportEarClientWizardPage) wizard.getPage(ExportEarClientWizardPage.class.getName());
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
  public boolean isAvailable(IScoutEarExportWizard wizard) {
    return wizard.getProject().getServerBundle() != null;
  }

  @Override
  public void selectionChanged(IScoutEarExportWizard wizard, boolean selected) {
    AbstractScoutWizardPage page = wizard.getPage(ExportEarServerWizardPage.class.getName());
    if (page == null) {
      page = new ExportEarServerWizardPage(wizard.getProject());
      wizard.addPage(page);
    }
    page.setExcludePage(!selected);
  }
}
