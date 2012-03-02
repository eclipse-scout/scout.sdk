package org.eclipse.scout.sdk.ui.internal.extensions.bundle;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.extensions.bundle.IScoutBundleProvider;
import org.eclipse.scout.sdk.ui.extensions.project.IScoutBundleExtension.BundleTypes;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.wizard.project.IScoutProjectWizard;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.IScoutProject;

public class SwtScoutBundleExtension implements IScoutBundleProvider {

  public final static String ID = "org.eclipse.scout.sdk.ui.UiSwtBundle";

  @Override
  public void init(IScoutProjectWizard wizard, IScoutProject project) {
    IScoutBundle swt = project.getUiSwtBundle();
    wizard.getProjectWizardPage().setBundleNodeAvailable(swt == null, ID);
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
}
