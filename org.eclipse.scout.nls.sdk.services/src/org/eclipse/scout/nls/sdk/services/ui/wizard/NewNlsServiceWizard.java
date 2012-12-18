package org.eclipse.scout.nls.sdk.services.ui.wizard;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.nls.sdk.services.operation.CreateServiceNlsProjectOperation;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizard;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;

public class NewNlsServiceWizard extends AbstractWorkspaceWizard {

  private final IScoutBundle m_bundle;
  private final CreateServiceNlsProjectOperation m_op;
  private final NewTextProviderServiceWizardPage m_page1;

  public NewNlsServiceWizard(IScoutBundle b) {
    m_bundle = b;
    m_op = new CreateServiceNlsProjectOperation();
    m_page1 = new NewTextProviderServiceWizardPage(m_bundle);
    addPage(m_page1);
  }

  @Override
  protected boolean beforeFinish() throws CoreException {
    m_op.setBundle(m_bundle);
    m_op.setLanguages(m_page1.getLanguages());
    m_op.setPackageName(m_bundle.getPackageName(m_page1.getTargetPackage()));
    m_op.setServiceName(m_page1.getClassName());
    m_op.setSuperType(m_page1.getSuperType());
    m_op.setTranslationFilePrefix(m_page1.getTranlationFileName());
    m_op.setTranslationFolder(m_page1.getTranslationFolder());
    return true;
  }

  @Override
  protected String getDialogSettingsKey() {
    return super.getDialogSettingsKey() + "01"; // invalidate cached legacy dialog sizes
  }

  @Override
  protected boolean performFinish(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) {
    try {
      m_op.validate();
      m_op.run(monitor, workingCopyManager);
      return true;
    }
    catch (CoreException e) {
      ScoutSdkUi.logError("Error during creation of new Text Provider Service.", e);
      return false;
    }
  }
}
