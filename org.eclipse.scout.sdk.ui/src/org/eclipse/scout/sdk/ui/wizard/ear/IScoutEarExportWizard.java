package org.eclipse.scout.sdk.ui.wizard.ear;

import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.scout.sdk.ui.wizard.AbstractScoutWizardPage;
import org.eclipse.scout.sdk.workspace.IScoutProject;

public interface IScoutEarExportWizard extends IWizard {

  IScoutProject getProject();

  IScoutEarExportWizardPage getExportWizardPage();

  void addPage(IWizardPage page);

  @Override
  AbstractScoutWizardPage getPage(String name);
}
