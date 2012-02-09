package org.eclipse.scout.sdk.ui.internal.wizard.export;

import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.IScoutProject;

public class ExportEarServerWizardPage extends AbstractExportEarProductWizardPage {
  public ExportEarServerWizardPage(IScoutProject scoutProject) {
    super(scoutProject, ExportEarServerWizardPage.class.getName(), Texts.get("ExportWebArchive"), IScoutBundle.BUNDLE_SERVER);
  }
}
