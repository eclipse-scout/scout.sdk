package org.eclipse.scout.sdk.ui.action.export;

import org.eclipse.jface.wizard.IWizard;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.action.AbstractWizardAction;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.internal.wizard.export.ExportEarWizard;
import org.eclipse.scout.sdk.workspace.IScoutProject;

public class ExportEarAction extends AbstractWizardAction {

  private IScoutProject m_res;

  public ExportEarAction() {
    super(Texts.get("ExportAsEarFile"), ScoutSdkUi.getImageDescriptor(ScoutSdkUi.ScoutProjectExport), null, false, Category.IMPORT);
  }

  public void setScoutProject(IScoutProject res) {
    m_res = res;
  }

  @Override
  protected IWizard getNewWizardInstance() {
    return new ExportEarWizard(m_res);
  }
}
