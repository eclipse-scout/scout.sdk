package org.eclipse.scout.sdk.compatibility.v36.internal;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
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
  public IStatus resolveTargetPlatform(IFile targetFile, boolean loadPlatform, IProgressMonitor monitor) throws CoreException {
    ITargetPlatformService targetService = (ITargetPlatformService) PDECore.getDefault().acquireService(ITargetPlatformService.class.getName());
    ITargetHandle handle = targetService.getTarget(targetFile);
    ITargetDefinition def = handle.getTargetDefinition();
    return resolveTarget(def, loadPlatform, monitor);
  }

  @Override
  public IStatus resolveTargetPlatform(Set<File> absolutePaths, String targetName, boolean loadPlatform, IProgressMonitor monitor) throws CoreException {
    ITargetPlatformService tpService = (ITargetPlatformService) PDECore.getDefault().acquireService(ITargetPlatformService.class.getName());
    ITargetDefinition targetDef = tpService.newTarget();
    targetDef.setName(targetName);

    List<IBundleContainer> bundleContainers = new ArrayList<IBundleContainer>();
    for (File dir : absolutePaths) {
      bundleContainers.add(tpService.newDirectoryContainer(dir.getAbsolutePath()));
    }
    targetDef.setBundleContainers(bundleContainers.toArray(new IBundleContainer[bundleContainers.size()]));
    targetDef.setArch(Platform.getOSArch());
    targetDef.setOS(Platform.getOS());
    targetDef.setWS(Platform.getWS());
    targetDef.setNL(Platform.getNL());
    tpService.saveTargetDefinition(targetDef);

    return resolveTarget(targetDef, loadPlatform, monitor);
  }

  private IStatus resolveTarget(ITargetDefinition def, boolean loadPlatform, IProgressMonitor monitor) {
    IStatus result = def.resolve(monitor);
    if (loadPlatform && result.isOK()) {
      LoadTargetDefinitionJob loadJob = new LoadTargetDefinitionJob(def);
      result = loadJob.run(monitor);
    }
    return result;
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
