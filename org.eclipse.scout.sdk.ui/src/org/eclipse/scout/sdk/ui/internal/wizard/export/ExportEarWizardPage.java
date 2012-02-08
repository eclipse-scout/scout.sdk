package org.eclipse.scout.sdk.ui.internal.wizard.export;

import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizardPage;
import org.eclipse.scout.sdk.workspace.IScoutProject;
import org.eclipse.swt.widgets.Composite;

public class ExportEarWizardPage extends AbstractWorkspaceWizardPage {

  private final IScoutProject m_scoutProject;

  public ExportEarWizardPage(IScoutProject scoutProject) {
    super(ExportEarWizardPage.class.getName());
    m_scoutProject = scoutProject;
    setTitle(Texts.get("ExportEnterpriseArchive"));
    setDescription(Texts.get("ExportEnterpriseArchiveMessage"));
  }

  @Override
  protected void createContent(Composite parent) {
  }

}
