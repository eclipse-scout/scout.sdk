package org.eclipse.scout.sdk.ui.internal.extensions.bundle;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.operation.project.CreateUiSwtPluginOperation;
import org.eclipse.scout.sdk.operation.project.FillUiSwtPluginOperation;
import org.eclipse.scout.sdk.operation.template.TemplateVariableSet;
import org.eclipse.scout.sdk.ui.extensions.bundle.IScoutBundleProvider;
import org.eclipse.scout.sdk.ui.extensions.project.IScoutBundleExtension.BundleTypes;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.wizard.project.IScoutProjectWizard;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;

public class SwtScoutBundleExtension implements IScoutBundleProvider {
  public static final String BUNDLE_ID = "org.eclipse.scout.sdk.ui.UiSwtBundle";

  public SwtScoutBundleExtension() {
  }

  @Override
  public IStatus getStatus(IScoutProjectWizard wizard) {
    if (!wizard.getProjectWizardPage().hasSelectedBundle(BundleTypes.Client_Bundle)) {
      return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("NoSwtWithoutAClient"));
    }
    return Status.OK_STATUS;
  }

  @Override
  public void bundleSelectionChanged(IScoutProjectWizard wizard, boolean selected) {
  }

  @Override
  public IJavaProject createBundle(IScoutProjectWizard wizard, IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) {
    TemplateVariableSet variables = TemplateVariableSet.createNew(wizard.getProjectWizardPage().getProjectName(), wizard.getProjectWizardPage().getProjectNamePostfix(), wizard.getProjectWizardPage().getProjectAlias());
    try {
      CreateUiSwtPluginOperation swtOp = new CreateUiSwtPluginOperation(variables);
      swtOp.run(monitor, workingCopyManager);
      new FillUiSwtPluginOperation(swtOp.getCreatedProject(), variables).run(monitor, workingCopyManager);
      return swtOp.getJavaProject();
    }
    catch (CoreException e) {
      ScoutSdkUi.logError("could not create UI SWT bundle", e);
      return null;
    }

  }

}
