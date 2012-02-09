package org.eclipse.scout.sdk.rap.ui.internal.wizard.export;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.rap.ui.internal.extensions.UiRapBundleNodeFactory;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.internal.wizard.export.AbstractExportEarProductWizardPage;
import org.eclipse.scout.sdk.ui.internal.wizard.export.ExportEarServerWizardPage;
import org.eclipse.scout.sdk.workspace.IScoutProject;

public class ExportEarRapWizardPage extends AbstractExportEarProductWizardPage {

  public ExportEarRapWizardPage(IScoutProject scoutProject) {
    super(scoutProject, ExportEarRapWizardPage.class.getName(), Texts.get("ExportRapWebArchive"), UiRapBundleNodeFactory.BUNDLE_UI_RAP);
  }

  @Override
  protected void validatePage(MultiStatus multiStatus) {
    super.validatePage(multiStatus);
    ExportEarServerWizardPage server = (ExportEarServerWizardPage) getWizard().getPage(ExportEarServerWizardPage.class.getName());
    if (server != null && CompareUtility.equals(server.getWarName(), getWarName())) {
      multiStatus.add(new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("WarFileNameUsed")));
    }
  }
}
