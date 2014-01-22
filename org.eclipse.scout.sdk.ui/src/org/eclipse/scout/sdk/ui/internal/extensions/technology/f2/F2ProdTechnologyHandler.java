package org.eclipse.scout.sdk.ui.internal.extensions.technology.f2;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.commons.TriState;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.ui.extensions.technology.AbstractScoutTechnologyHandler;
import org.eclipse.scout.sdk.ui.extensions.technology.IScoutTechnologyResource;
import org.eclipse.scout.sdk.ui.internal.extensions.technology.IMarketplaceConstants;
import org.eclipse.scout.sdk.ui.internal.extensions.technology.IOrbitConstants;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.ScoutBundleFilters;

/**
 * <h3>{@link F2ProdTechnologyHandler}</h3>
 * 
 * @author jgu
 * @since 3.10.0 02.07.2013
 */
public class F2ProdTechnologyHandler extends AbstractScoutTechnologyHandler implements IMarketplaceConstants, IOrbitConstants {

  public F2ProdTechnologyHandler() {
  }

  @Override
  public void selectionChanged(IScoutTechnologyResource[] resources, boolean selected, IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    for (IScoutTechnologyResource r : resources) {
      selectionChangedProductFile(r, selected, new String[]{F2_PLUGIN});
    }
  }

  @Override
  public TriState getSelection(IScoutBundle project) throws CoreException {
    return getSelectionProductFiles(getProductBundles(project),
        new String[]{IRuntimeClasses.ScoutClientBundleId},
        new String[]{F2_PLUGIN});
  }

  @Override
  public boolean isActive(IScoutBundle project) {
    return project.getChildBundle(ScoutBundleFilters.getBundlesOfTypeFilter(IScoutBundle.TYPE_UI_SWING, IScoutBundle.TYPE_UI_SWT), true) != null;
  }

  @Override
  protected void contributeResources(IScoutBundle project, List<IScoutTechnologyResource> list) throws CoreException {
    contributeProductFiles(getProductBundles(project), list, IRuntimeClasses.ScoutSharedBundleId);
  }

  private IScoutBundle[] getProductBundles(IScoutBundle start) {
    return start.getChildBundles(ScoutBundleFilters.getBundlesOfTypeFilter(IScoutBundle.TYPE_UI_SWING, IScoutBundle.TYPE_UI_SWT), false);
  }
}
