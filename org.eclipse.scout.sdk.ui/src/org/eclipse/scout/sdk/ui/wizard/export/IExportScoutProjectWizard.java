package org.eclipse.scout.sdk.ui.wizard.export;

import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.scout.sdk.ui.wizard.AbstractScoutWizardPage;
import org.eclipse.scout.sdk.workspace.IScoutBundle;

public interface IExportScoutProjectWizard extends IWizard {

  IScoutBundle getProject();

  IExportScoutProjectWizardPage getExportWizardPage();

  void addPage(IWizardPage page);

  @Override
  AbstractScoutWizardPage getPage(String name);

  String getProjectAlias();
}
