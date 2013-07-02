package org.eclipse.scout.sdk.ui.internal.extensions.technology.f2;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.commons.TriState;
import org.eclipse.scout.sdk.compatibility.P2Utility;
import org.eclipse.scout.sdk.ui.extensions.technology.AbstractScoutTechnologyHandler;
import org.eclipse.scout.sdk.ui.extensions.technology.IScoutTechnologyResource;
import org.eclipse.scout.sdk.ui.internal.extensions.technology.IMarketplaceConstants;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.ScoutBundleFilters;

/**
 * <h3>{@link F2ManifestTechnologyHandler}</h3> ...
 * 
 * @author jgu
 * @since 3.10.0 02.07.2013
 */
public class F2ManifestTechnologyHandler extends AbstractScoutTechnologyHandler implements IMarketplaceConstants {

  private boolean m_newPluginsInstalled;

  @Override
  public boolean preSelectionChanged(boolean selected, IProgressMonitor monitor) throws CoreException {
    m_newPluginsInstalled = false;
    if (selected) {
      FeatureInstallResult result = ensureFeatureInstalled(F2_FEATURE, F2_FEATURE_URL, monitor, F2_PLUGIN);
      if (FeatureInstallResult.LicenseNotAccepted.equals(result)) {
        return false; // abort processing if the installation would be necessary but the license was not accepted.
      }
      else if (FeatureInstallResult.InstallationSuccessful.equals(result)) {
        m_newPluginsInstalled = true; // remember if we have installed new plugins so that we can ask for a restart later on.
      }
    }
    return true;
  }

  @Override
  public void selectionChanged(IScoutTechnologyResource[] resources, boolean selected, IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    selectionChangedManifest(resources, selected, F2_PLUGIN);
  }

  @Override
  public void postSelectionChanged(boolean selected, IProgressMonitor monitor) throws CoreException {
    if (m_newPluginsInstalled) {
      P2Utility.promptForRestart();
    }
  }

  @Override
  public TriState getSelection(IScoutBundle project) {
    return getSelectionManifests(getSwingBundlesBelow(project), F2_PLUGIN);
  }

  @Override
  public boolean isActive(IScoutBundle project) {
    return project.getChildBundle(ScoutBundleFilters.getBundlesOfTypeFilter(IScoutBundle.TYPE_UI_SWING, IScoutBundle.TYPE_UI_SWT), false) != null;
  }

  @Override
  protected void contributeResources(IScoutBundle project, List<IScoutTechnologyResource> list) {
    contributeManifestFiles(getSwingBundlesBelow(project), list);
  }

  private IScoutBundle[] getSwingBundlesBelow(IScoutBundle start) {
    return start.getChildBundles(ScoutBundleFilters.getBundlesOfTypeFilter(IScoutBundle.TYPE_UI_SWING, IScoutBundle.TYPE_UI_SWT), true);
  }

}
