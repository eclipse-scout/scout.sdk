package org.eclipse.scout.sdk.ui.internal.extensions.bundle;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.operation.project.CreateUiSwingPluginOperation;
import org.eclipse.scout.sdk.operation.project.FillUiSwingPluginOperation;
import org.eclipse.scout.sdk.operation.template.ITemplateVariableSet;
import org.eclipse.scout.sdk.typecache.IScoutWorkingCopyManager;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.extensions.bundle.IScoutBundleProvider;
import org.eclipse.scout.sdk.ui.extensions.project.IScoutBundleExtension.BundleTypes;
import org.eclipse.scout.sdk.ui.wizard.project.IScoutProjectWizard;

public class SwingScoutBundleExtension implements IScoutBundleProvider {
  public static final String BUNDLE_ID = "org.eclipse.scout.sdk.ui.UiSwingBundle";

  public SwingScoutBundleExtension() {
  }

  @Override
  public IStatus getStatus(IScoutProjectWizard wizard) {
    if (!wizard.getProjectWizardPage().hasSelectedBundle(BundleTypes.Client_Bundle)) {
      return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("NoSwingWithoutClient"));
    }
    return Status.OK_STATUS;
  }

  @Override
  public void bundleSelectionChanged(IScoutProjectWizard wizard, boolean selected) {
  }

  @Override
  public IJavaProject createBundle(ITemplateVariableSet variables, IProgressMonitor monitor, IScoutWorkingCopyManager workingCopyManager) {
    try {
      CreateUiSwingPluginOperation swingBundleOp = new CreateUiSwingPluginOperation(variables);
      swingBundleOp.run(monitor, workingCopyManager);
      new FillUiSwingPluginOperation(swingBundleOp.getCreatedProject(), variables).run(monitor, workingCopyManager);
      return swingBundleOp.getJavaProject();
    }
    catch (CoreException e) {
      ScoutSdkUi.logError("could not create UI SWT bundle", e);
      return null;
    }
  }

}
