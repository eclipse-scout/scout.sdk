package org.eclipse.scout.sdk.ui.internal.wizard.export;

import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizard;
import org.eclipse.scout.sdk.workspace.IScoutProject;

public class ExportEarWizard extends AbstractWorkspaceWizard {
  private ExportEarWizardPage m_page1;

  public ExportEarWizard(IScoutProject project) {
    setWindowTitle(Texts.get("ExportToEAR"));
    m_page1 = new ExportEarWizardPage(project);
    addPage(m_page1);
  }
}
