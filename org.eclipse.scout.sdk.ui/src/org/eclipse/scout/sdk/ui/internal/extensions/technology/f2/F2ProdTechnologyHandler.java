package org.eclipse.scout.sdk.ui.internal.extensions.technology.f2;

import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.commons.CompareUtility;
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
 * @author Judith Gull
 * @since 3.10.0 02.07.2013
 */
public class F2ProdTechnologyHandler extends AbstractScoutTechnologyHandler implements IMarketplaceConstants, IOrbitConstants {

  public F2ProdTechnologyHandler() {
  }

  @Override
  public void selectionChanged(Set<IScoutTechnologyResource> resources, boolean selected, IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    for (IScoutTechnologyResource r : resources) {
      selectionChangedProductFile(r, selected, new String[]{F2_PLUGIN});
    }
  }

  @Override
  public TriState getSelection(IScoutBundle project) throws CoreException {
    TriState swingSelection = getSelectionProductFiles(new String[]{IRuntimeClasses.ScoutUiSwingBundleId}, new String[]{F2_PLUGIN});
    TriState swtSelection = getSelectionProductFiles(new String[]{IRuntimeClasses.ScoutUiSwtBundleId}, new String[]{F2_PLUGIN});
    if (CompareUtility.equals(swingSelection, swtSelection)) {
      return swingSelection;
    }
    else {
      return TriState.UNDEFINED;
    }
  }

  @Override
  public boolean isActive(IScoutBundle project) {
    return project.getChildBundle(ScoutBundleFilters.getBundlesOfTypeFilter(IScoutBundle.TYPE_UI_SWING, IScoutBundle.TYPE_UI_SWT), true) != null;
  }

  @Override
  protected void contributeResources(IScoutBundle project, List<IScoutTechnologyResource> list) throws CoreException {
    contributeProductFiles(list, IRuntimeClasses.ScoutUiSwtBundleId);
    contributeProductFiles(list, IRuntimeClasses.ScoutUiSwingBundleId);
  }
}
