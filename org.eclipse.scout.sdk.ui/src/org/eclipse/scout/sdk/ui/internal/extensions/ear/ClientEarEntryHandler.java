package org.eclipse.scout.sdk.ui.internal.extensions.ear;

import java.io.File;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.extensions.ear.IEarEntryHandler;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.internal.wizard.export.ExportEarClientWizardPage;
import org.eclipse.scout.sdk.ui.wizard.AbstractScoutWizardPage;
import org.eclipse.scout.sdk.ui.wizard.ear.IScoutEarExportWizard;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;

public class ClientEarEntryHandler implements IEarEntryHandler {

  public final static String ID = "ui.client";

  public ClientEarEntryHandler() {
  }

  @Override
  public IStatus getStatus(IScoutEarExportWizard wizard) {
    if (!wizard.getExportWizardPage().isNodesSelected(ServerEarEntryHandler.ID)) {
      return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("NoClientEarWithoutServer"));
    }
    return Status.OK_STATUS;
  }

  @Override
  public File createModule(IScoutEarExportWizard wizard, IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) {
    return null;
  }

  @Override
  public boolean isAvailable(IScoutEarExportWizard wizard) {
    return wizard.getProject().getServerBundle() != null &&
        (wizard.getProject().getUiSwingBundle() != null || wizard.getProject().getUiSwtBundle() != null);
  }

  @Override
  public void selectionChanged(IScoutEarExportWizard wizard, boolean selected) {
    AbstractScoutWizardPage page = wizard.getPage(ExportEarClientWizardPage.class.getName());
    if (page == null) {
      page = new ExportEarClientWizardPage(wizard.getProject());
      wizard.addPage(page);
    }
    page.setExcludePage(!selected);
  }
}
