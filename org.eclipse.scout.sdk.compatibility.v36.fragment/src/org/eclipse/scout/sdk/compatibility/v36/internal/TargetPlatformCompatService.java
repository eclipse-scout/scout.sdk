package org.eclipse.scout.sdk.compatibility.v36.internal;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.pde.internal.core.PDECore;
import org.eclipse.pde.internal.core.target.IUBundleContainer;
import org.eclipse.pde.internal.core.target.TargetDefinition;
import org.eclipse.pde.internal.core.target.provisional.IBundleContainer;
import org.eclipse.pde.internal.core.target.provisional.ITargetDefinition;
import org.eclipse.pde.internal.core.target.provisional.ITargetHandle;
import org.eclipse.pde.internal.core.target.provisional.ITargetPlatformService;
import org.eclipse.pde.internal.core.target.provisional.LoadTargetDefinitionJob;
import org.eclipse.scout.sdk.compatibility.internal.ScoutCompatibilityActivator;
import org.eclipse.scout.sdk.compatibility.internal.service.ITargetPlatformCompatService;

@SuppressWarnings("restriction")
public class TargetPlatformCompatService implements ITargetPlatformCompatService {
  @Override
  public void resolveTargetPlatform(IFile targetFile, boolean loadPlatform, IProgressMonitor monitor) throws CoreException {
    ITargetPlatformService targetService = (ITargetPlatformService) PDECore.getDefault().acquireService(ITargetPlatformService.class.getName());
    ITargetHandle handle = targetService.getTarget(targetFile);
    ITargetDefinition def = handle.getTargetDefinition();
    def.resolve(monitor);
    if (loadPlatform) {
      LoadTargetDefinitionJob loadJob = new LoadTargetDefinitionJob(def);
      loadJob.schedule();
    }
  }

  @Override
  public void removeInstallableUnitsFromTarget(IFile targetFile, String[] unitIds) throws CoreException {
    ITargetPlatformService svc = (ITargetPlatformService) PDECore.getDefault().acquireService(ITargetPlatformService.class.getName());
    ITargetHandle handle = svc.getTarget(targetFile);
    TargetDefinition td = (TargetDefinition) handle.getTargetDefinition();
    IBundleContainer[] features = td.getBundleContainers();
    if (features != null && features.length > 0) {
      ArrayList<IBundleContainer> newList = new ArrayList<IBundleContainer>(features.length);
      for (IBundleContainer container : features) {
        if (container instanceof IUBundleContainer) {
          IUBundleContainer iuContainer = (IUBundleContainer) container;
          IInstallableUnit[] units = iuContainer.getInstallableUnits(td.getProfile());
          for (IInstallableUnit unit : units) {
            if (!isInList(unit.getId(), unitIds)) {
              newList.add(container);
            }
          }
        }
        else {
          newList.add(container);
        }
      }
      td.setBundleContainers(newList.toArray(new IBundleContainer[newList.size()]));
      svc.saveTargetDefinition(td);
    }
  }

  private boolean isInList(String search, String[] list) {
    for (String s : list) {
      if (search.equals(s)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public void addInstallableUnitsToTarget(IFile targetFile, String[] unitIds, String[] versions, String[] repositories) throws CoreException {
    URI[] uris = new URI[repositories.length];
    try {
      for (int i = 0; i < repositories.length; i++) {
        uris[i] = new URI(repositories[i]);
      }
    }
    catch (URISyntaxException e) {
      throw new CoreException(new Status(IStatus.ERROR, ScoutCompatibilityActivator.PLUGIN_ID, "invalid URI provided", e));
    }

    ITargetPlatformService svc = (ITargetPlatformService) PDECore.getDefault().acquireService(ITargetPlatformService.class.getName());
    ITargetHandle handle = svc.getTarget(targetFile);
    ITargetDefinition td = handle.getTargetDefinition();
    IBundleContainer[] features = td.getBundleContainers();

    int s = unitIds.length;
    if (features != null) {
      s += features.length;
    }
    ArrayList<IBundleContainer> newList = new ArrayList<IBundleContainer>(s);
    if (features != null && features.length > 0) {
      for (IBundleContainer container : features) {
        newList.add(container);
      }
    }
    for (int i = 0; i < unitIds.length; i++) {
      IUBundleContainer group = (IUBundleContainer) svc.newIUContainer(new String[]{unitIds[i]}, new String[]{versions[i]}, new URI[]{uris[i]});
      group.setIncludeAllEnvironments(false, td);
      group.setIncludeAllRequired(false, td);
      newList.add(group);
    }
    td.setBundleContainers(newList.toArray(new IBundleContainer[newList.size()]));
    svc.saveTargetDefinition(td);
  }
}
