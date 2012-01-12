package org.eclipse.scout.sdk.util;

import java.lang.reflect.Method;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.pde.internal.core.PDECore;
import org.eclipse.scout.sdk.util.internal.SdkUtilActivator;
import org.eclipse.scout.sdk.util.log.ScoutStatus;
import org.osgi.framework.Version;

@SuppressWarnings("restriction")
public final class PlatformUtility {

  private final static Version FRAMEWORK_VERSION = Platform.getProduct().getDefiningBundle().getVersion();

  private PlatformUtility() {
  }

  public static void resolveTargetPlatform(IFile targetFile, IProgressMonitor monitor) throws CoreException {
    resolveTargetPlatform(targetFile, false, monitor);
  }

  public static void resolveTargetPlatform(IFile targetFile, boolean loadPlatform, IProgressMonitor monitor) throws CoreException {
    // Platform version safe implementation of the following code (because the used classes have been moved):
    //   ITargetPlatformService targetService = (ITargetPlatformService) PDECore.getDefault().acquireService(ITargetPlatformService.class.getName());
    //   ITargetHandle handle = targetService.getTarget(targetFile);
    //   handle.getTargetDefinition().resolve(monitor);
    //   if(loadPlatform) {
    //     LoadTargetDefinitionJob loadJob = new LoadTargetDefinitionJob(handle.getTargetDefinition());
    //     loadJob.schedule();
    //     loadJob.join();
    //   }
    String iTargetPlatformServiceFqn = null;
    String loadTargetDefinitionJobFqn = null;
    String iTargetDefinitionFqn = null;

    if (isPlatformJuno()) {
      iTargetPlatformServiceFqn = "org.eclipse.pde.core.target.ITargetPlatformService";
      loadTargetDefinitionJobFqn = "org.eclipse.pde.core.target.LoadTargetDefinitionJob";
      iTargetDefinitionFqn = "org.eclipse.pde.core.target.ITargetDefinition";
    }
    else {
      iTargetPlatformServiceFqn = "org.eclipse.pde.internal.core.target.provisional.ITargetPlatformService";
      loadTargetDefinitionJobFqn = "org.eclipse.pde.internal.core.target.provisional.LoadTargetDefinitionJob";
      iTargetDefinitionFqn = "org.eclipse.pde.internal.core.target.provisional.ITargetDefinition";
    }

    try {
      Object targetService = PDECore.getDefault().acquireService(iTargetPlatformServiceFqn);

      Method getTarget = targetService.getClass().getMethod("getTarget", IFile.class);
      Object handle = getTarget.invoke(targetService, targetFile);

      Method getTargetDefinition = handle.getClass().getMethod("getTargetDefinition");
      Object targetDefinition = getTargetDefinition.invoke(handle);

      Method resolve = targetDefinition.getClass().getMethod("resolve", IProgressMonitor.class);
      resolve.invoke(targetDefinition, monitor);

      if (loadPlatform) {
        Class<?> loadJobClass = Class.forName(loadTargetDefinitionJobFqn);
        Class<?> iTargetDefClass = Class.forName(iTargetDefinitionFqn);
        WorkspaceJob loadJob = (WorkspaceJob) loadJobClass.getConstructor(iTargetDefClass).newInstance(targetDefinition);
        loadJob.schedule();
        try {
          loadJob.join();
        }
        catch (InterruptedException e) {
          SdkUtilActivator.logError("load target definition to file '" + targetFile.getProjectRelativePath().toString() + "' got interrupted.", e);
        }
      }
    }
    catch (Exception e) {
      throw new CoreException(new ScoutStatus("Unable to resolve RAP target platform", e));
    }
  }

  public static boolean isPlatformE4() {
    return FRAMEWORK_VERSION.getMajor() == 4;
  }

  public static boolean isPlatformJuno() {
    return (FRAMEWORK_VERSION.getMajor() == 4 && FRAMEWORK_VERSION.getMinor() > 1) ||
        (FRAMEWORK_VERSION.getMajor() == 3 && FRAMEWORK_VERSION.getMinor() > 7);
  }

  public static boolean isPlatformIndigo() {
    return FRAMEWORK_VERSION.getMajor() == 3 && FRAMEWORK_VERSION.getMinor() == 7;
  }

  public static boolean isPlatformHelios() {
    return FRAMEWORK_VERSION.getMajor() == 3 && FRAMEWORK_VERSION.getMinor() == 6;
  }

  public static Version getPlatformVersion() {
    return FRAMEWORK_VERSION;
  }
}
