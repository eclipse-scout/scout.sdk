package org.eclipse.scout.sdk.ws.jaxws.technology;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.commons.TriState;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.ui.extensions.technology.AbstractScoutTechnologyHandler;
import org.eclipse.scout.sdk.ui.extensions.technology.IScoutTechnologyResource;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutProject;

public class JaxWsServerProdTechnologyHandler extends AbstractScoutTechnologyHandler {

  public JaxWsServerProdTechnologyHandler() {
  }

  @Override
  public void selectionChanged(IScoutTechnologyResource[] resources, boolean selected, IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    selectionChangedProductFiles(resources, selected, new String[]{JaxWsServerManifestTechnologyHandler.JAXWS_RUNTIME_PLUGIN});
  }

  @Override
  public TriState getSelection(IScoutProject project) {
    return getSelectionProductFiles(project, new String[]{RuntimeClasses.ScoutServerBundleId}, new String[]{JaxWsServerManifestTechnologyHandler.JAXWS_RUNTIME_PLUGIN});
  }

  @Override
  protected void contributeResources(IScoutProject project, List<IScoutTechnologyResource> list) {
    contributeProductFiles(project, list, RuntimeClasses.ScoutServerBundleId);
  }

  @Override
  public boolean isActive(IScoutProject project) {
    return project.getServerBundle() != null && project.getServerBundle().getProject().exists();
  }
}
