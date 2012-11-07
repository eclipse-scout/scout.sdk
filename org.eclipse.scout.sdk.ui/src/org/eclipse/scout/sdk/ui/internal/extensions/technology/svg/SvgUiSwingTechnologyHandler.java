package org.eclipse.scout.sdk.ui.internal.extensions.technology.svg;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.commons.TriState;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.ui.extensions.technology.AbstractScoutTechnologyHandler;
import org.eclipse.scout.sdk.ui.extensions.technology.IScoutTechnologyResource;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutProject;

public class SvgUiSwingTechnologyHandler extends AbstractScoutTechnologyHandler {

  public final static String[] SWING_SVG_PLUGIN = new String[]{"org.eclipse.scout.svg.ui.swing"};

  public SvgUiSwingTechnologyHandler() {
  }

  @Override
  public void selectionChanged(IScoutTechnologyResource[] resources, boolean selected, IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    selectionChangedProductFiles(resources, selected, SvgClientTechnologyHandler.COMMON_SVG_PLUGINS, SWING_SVG_PLUGIN);
  }

  @Override
  public boolean isActive(IScoutProject project) {
    return project.getClientBundle() != null && project.getClientBundle().getProject().exists() &&
        project.getUiSwingBundle() != null && project.getUiSwingBundle().getProject().exists();
  }

  @Override
  public TriState getSelection(IScoutProject project) {
    return getSelectionProductFiles(project,
        new String[]{RuntimeClasses.ScoutClientBundleId, RuntimeClasses.ScoutUiSwingBundleId},
        SvgClientTechnologyHandler.COMMON_SVG_PLUGINS, SWING_SVG_PLUGIN);
  }

  @Override
  protected void contributeResources(IScoutProject project, List<IScoutTechnologyResource> list) {
    contributeProductFiles(project, list, RuntimeClasses.ScoutClientBundleId, RuntimeClasses.ScoutUiSwingBundleId);
  }
}
