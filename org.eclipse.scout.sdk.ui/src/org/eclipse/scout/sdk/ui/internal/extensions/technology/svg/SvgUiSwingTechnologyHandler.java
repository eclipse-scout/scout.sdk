package org.eclipse.scout.sdk.ui.internal.extensions.technology.svg;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.commons.TriState;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.ui.extensions.technology.AbstractScoutTechnologyHandler;
import org.eclipse.scout.sdk.ui.extensions.technology.IScoutTechnologyResource;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.ScoutBundleFilters;

public class SvgUiSwingTechnologyHandler extends AbstractScoutTechnologyHandler {

  public final static String[] SWING_SVG_PLUGIN = new String[]{"org.eclipse.scout.svg.ui.swing"};

  public SvgUiSwingTechnologyHandler() {
  }

  @Override
  public void selectionChanged(IScoutTechnologyResource[] resources, boolean selected, IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    selectionChangedProductFiles(resources, selected, SvgClientTechnologyHandler.SCOUT_ONLY_SVG_PLUGINS, SvgClientTechnologyHandler.CORE_SVG_PLUGINS, SWING_SVG_PLUGIN);
  }

  @Override
  public boolean isActive(IScoutBundle project) {
    return project.getChildBundle(ScoutBundleFilters.getBundlesOfTypeFilter(IScoutBundle.TYPE_UI_SWING), false) != null;
  }

  @Override
  public TriState getSelection(IScoutBundle project) throws CoreException {
    return getSelectionProductFiles(getSwingBundlesBelow(project),
        new String[]{RuntimeClasses.ScoutClientBundleId, RuntimeClasses.ScoutUiSwingBundleId},
        SvgClientTechnologyHandler.SCOUT_ONLY_SVG_PLUGINS, SvgClientTechnologyHandler.CORE_SVG_PLUGINS, SWING_SVG_PLUGIN);
  }

  @Override
  protected void contributeResources(IScoutBundle project, List<IScoutTechnologyResource> list) throws CoreException {
    contributeProductFiles(getSwingBundlesBelow(project), list, RuntimeClasses.ScoutClientBundleId, RuntimeClasses.ScoutUiSwingBundleId);
  }

  private IScoutBundle[] getSwingBundlesBelow(IScoutBundle start) {
    return start.getChildBundles(ScoutBundleFilters.getBundlesOfTypeFilter(IScoutBundle.TYPE_UI_SWING), true);
  }
}
