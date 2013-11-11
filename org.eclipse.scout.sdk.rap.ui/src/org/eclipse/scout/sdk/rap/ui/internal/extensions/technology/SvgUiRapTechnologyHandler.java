package org.eclipse.scout.sdk.rap.ui.internal.extensions.technology;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.commons.TriState;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.rap.IScoutSdkRapConstants;
import org.eclipse.scout.sdk.ui.extensions.technology.AbstractScoutTechnologyHandler;
import org.eclipse.scout.sdk.ui.extensions.technology.IScoutTechnologyResource;
import org.eclipse.scout.sdk.ui.internal.extensions.technology.svg.SvgClientTechnologyHandler;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.ScoutBundleFilters;

public class SvgUiRapTechnologyHandler extends AbstractScoutTechnologyHandler {

  public final static String[] RAP_SVG_PLUGIN = new String[]{"org.eclipse.scout.svg.ui.rap"};

  public SvgUiRapTechnologyHandler() {
  }

  @Override
  public void selectionChanged(IScoutTechnologyResource[] resources, boolean selected, IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    selectionChangedProductFiles(resources, selected, SvgClientTechnologyHandler.SCOUT_ONLY_SVG_PLUGINS, SvgClientTechnologyHandler.CORE_SVG_PLUGINS, RAP_SVG_PLUGIN);
  }

  @Override
  public boolean isActive(IScoutBundle project) {
    return project.getChildBundle(ScoutBundleFilters.getBundlesOfTypeFilter(IScoutSdkRapConstants.TYPE_UI_RAP), false) != null;
  }

  @Override
  public TriState getSelection(IScoutBundle project) throws CoreException {
    return getSelectionProductFiles(getRapUiBundlesBelow(project),
        new String[]{RuntimeClasses.ScoutClientBundleId, IScoutSdkRapConstants.ScoutUiRapBundleId},
        SvgClientTechnologyHandler.SCOUT_ONLY_SVG_PLUGINS, SvgClientTechnologyHandler.CORE_SVG_PLUGINS, RAP_SVG_PLUGIN);
  }

  @Override
  protected void contributeResources(IScoutBundle project, List<IScoutTechnologyResource> list) throws CoreException {
    for (IScoutBundle e : getRapUiBundlesBelow(project)) {
      contributeProductFiles(e, list, false, RuntimeClasses.ScoutClientBundleId, IScoutSdkRapConstants.ScoutUiRapBundleId);
    }
  }

  private IScoutBundle[] getRapUiBundlesBelow(IScoutBundle start) {
    return start.getChildBundles(ScoutBundleFilters.getBundlesOfTypeFilter(IScoutSdkRapConstants.TYPE_UI_RAP), true);
  }
}
