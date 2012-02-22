package org.eclipse.scout.sdk.ui.internal.extensions.export;

import java.io.File;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.extensions.export.IExportScoutProjectEntryHandler;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.internal.wizard.export.ExportClientWizardPage;
import org.eclipse.scout.sdk.ui.wizard.AbstractScoutWizardPage;
import org.eclipse.scout.sdk.ui.wizard.export.IExportScoutProjectWizard;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;

public class ClientExportEntryHandler implements IExportScoutProjectEntryHandler {

  public final static String ID = "ui.client";

  public ClientExportEntryHandler() {
  }

  @Override
  public IStatus getStatus(IExportScoutProjectWizard wizard) {
    if (!wizard.getExportWizardPage().isNodesSelected(ServerExportEntryHandler.ID)) {
      return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("NoClientEarWithoutServer"));
    }
    return Status.OK_STATUS;
  }

  @Override
  public File createModule(IExportScoutProjectWizard wizard, IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) {
    return null;
  }

  @Override
  public boolean isAvailable(IExportScoutProjectWizard wizard) {
    return wizard.getProject().getServerBundle() != null &&
        (wizard.getProject().getUiSwingBundle() != null || wizard.getProject().getUiSwtBundle() != null);
  }

  @Override
  public void selectionChanged(IExportScoutProjectWizard wizard, boolean selected) {
    AbstractScoutWizardPage page = wizard.getPage(ExportClientWizardPage.class.getName());
    if (page == null) {
      page = new ExportClientWizardPage(wizard.getProject());
      wizard.addPage(page);
    }
    page.setExcludePage(!selected);
  }
}
