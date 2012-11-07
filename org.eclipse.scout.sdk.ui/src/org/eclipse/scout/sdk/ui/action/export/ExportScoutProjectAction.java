package org.eclipse.scout.sdk.ui.action.export;

import org.eclipse.jface.wizard.IWizard;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.action.AbstractWizardAction;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.internal.wizard.export.ExportScoutProjectWizard;
import org.eclipse.scout.sdk.workspace.IScoutProject;

public class ExportScoutProjectAction extends AbstractWizardAction {

  private IScoutProject m_res;

  public ExportScoutProjectAction() {
    super(Texts.get("ExportScoutProjectMenu"), ScoutSdkUi.getImageDescriptor(ScoutSdkUi.ScoutProjectExport), null, false, Category.IMPORT);
  }

  public void setScoutProject(IScoutProject res) {
    m_res = res;
  }

  @Override
  protected IWizard getNewWizardInstance() {
    return new ExportScoutProjectWizard(m_res);
  }
}
