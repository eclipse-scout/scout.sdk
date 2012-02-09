package org.eclipse.scout.sdk.rap.ui.internal.wizard.export;

import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.rap.ui.internal.extensions.UiRapBundleNodeFactory;
import org.eclipse.scout.sdk.ui.internal.wizard.export.AbstractExportEarProductWizardPage;
import org.eclipse.scout.sdk.workspace.IScoutProject;

public class ExportEarRapWizardPage extends AbstractExportEarProductWizardPage {

  public ExportEarRapWizardPage(IScoutProject scoutProject) {
    super(scoutProject, ExportEarRapWizardPage.class.getName(), Texts.get("ExportRapWebArchive"), UiRapBundleNodeFactory.BUNDLE_UI_RAP);
  }
}
