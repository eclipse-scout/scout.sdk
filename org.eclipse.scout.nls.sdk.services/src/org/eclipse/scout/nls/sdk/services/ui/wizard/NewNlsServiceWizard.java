package org.eclipse.scout.nls.sdk.services.ui.wizard;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.nls.sdk.services.operation.CreateServiceNlsProjectOperation;
import org.eclipse.scout.nls.sdk.services.operation.NewNlsServiceModel;
import org.eclipse.scout.sdk.typecache.IScoutWorkingCopyManager;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizard;
import org.eclipse.scout.sdk.workspace.IScoutBundle;

public class NewNlsServiceWizard extends AbstractWorkspaceWizard {

  private final NewNlsServiceModel m_desc;

  public NewNlsServiceWizard(IScoutBundle b) {
    m_desc = new NewNlsServiceModel(b);
  }

  @Override
  protected boolean performFinish(IProgressMonitor monitor, IScoutWorkingCopyManager workingCopyManager) {
    try {
      CreateServiceNlsProjectOperation op = new CreateServiceNlsProjectOperation(m_desc);
      op.validate();
      op.run(monitor, workingCopyManager);
      return true;
    }
    catch (CoreException e) {
      ScoutSdkUi.logError("Error during creation of new Text Provider Service.", e);
      return false;
    }
  }

  @Override
  public void addPages() {
    addPage(new NewTextProviderServiceWizardPage("Page 1", m_desc));
  }
}
