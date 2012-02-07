package org.eclipse.scout.sdk.rap.ui.internal.wizard.export;

import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizard;
import org.eclipse.scout.sdk.workspace.IScoutBundle;

public class ExportRapWarWizard extends AbstractWorkspaceWizard {
  private ExportRapWarWizardPage m_page1;
  private IType m_declaringType;

  public ExportRapWarWizard(IScoutBundle serverBundle) {
    setWindowTitle(Texts.get("ExportToWAR"));
    m_page1 = new ExportRapWarWizardPage(serverBundle.getScoutProject());
    addPage(m_page1);
  }
}
