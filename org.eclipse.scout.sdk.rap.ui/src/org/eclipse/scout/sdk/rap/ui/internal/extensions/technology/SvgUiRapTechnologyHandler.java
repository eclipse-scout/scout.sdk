package org.eclipse.scout.sdk.rap.ui.internal.extensions.technology;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.commons.TriState;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.rap.RapRuntimeClasses;
import org.eclipse.scout.sdk.rap.ui.internal.extensions.UiRapBundleNodeFactory;
import org.eclipse.scout.sdk.ui.extensions.technology.AbstractScoutTechnologyHandler;
import org.eclipse.scout.sdk.ui.extensions.technology.IScoutTechnologyResource;
import org.eclipse.scout.sdk.ui.internal.extensions.technology.svg.SvgClientTechnologyHandler;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.IScoutProject;

public class SvgUiRapTechnologyHandler extends AbstractScoutTechnologyHandler {

  public final static String[] RAP_SVG_PLUGIN = new String[]{"org.eclipse.scout.svg.ui.rap"};

  public SvgUiRapTechnologyHandler() {
  }

  @Override
  public void selectionChanged(IScoutTechnologyResource[] resources, boolean selected, IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    selectionChangedProductFiles(resources, selected, SvgClientTechnologyHandler.COMMON_SVG_PLUGINS, RAP_SVG_PLUGIN);
  }

  @Override
  public boolean isActive(IScoutProject project) {
    IScoutBundle[] rapBundles = project.getAllBundles(UiRapBundleNodeFactory.BUNDLE_UI_RAP);
    return project.getClientBundle() != null && rapBundles != null && rapBundles.length > 0;
  }

  @Override
  public TriState getSelection(IScoutProject project) {
    return getSelectionProductFiles(project,
        new String[]{RuntimeClasses.ScoutClientBundleId, RapRuntimeClasses.ScoutUiRapBundleId},
        SvgClientTechnologyHandler.COMMON_SVG_PLUGINS, RAP_SVG_PLUGIN);
  }

  @Override
  protected void contributeResources(IScoutProject project, List<IScoutTechnologyResource> list) {
    contributeProductFiles(project, list, false, RuntimeClasses.ScoutClientBundleId, RapRuntimeClasses.ScoutUiRapBundleId);
  }
}
