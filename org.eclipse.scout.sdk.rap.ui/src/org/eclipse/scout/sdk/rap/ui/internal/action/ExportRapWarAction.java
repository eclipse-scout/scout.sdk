package org.eclipse.scout.sdk.rap.ui.internal.action;

import org.eclipse.jface.wizard.IWizard;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.rap.ui.internal.wizard.export.ExportRapWarWizard;
import org.eclipse.scout.sdk.ui.action.AbstractWizardAction;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.workspace.IScoutBundle;

public class ExportRapWarAction extends AbstractWizardAction {

  private IScoutBundle m_res;

  public ExportRapWarAction() {
    super(Texts.get("ExportRapAsWarFile"), ScoutSdkUi.getImageDescriptor(ScoutSdkUi.ServerBundleExport), null, false, Category.IMPORT);
  }

  public void setScoutBundle(IScoutBundle res) {
    m_res = res;
  }

  @Override
  protected IWizard getNewWizardInstance() {
    return new ExportRapWarWizard(m_res);
  }
}
