package org.eclipse.scout.sdk.compatibility;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.sdk.compatibility.internal.ScoutCompatibilityActivator;
import org.eclipse.scout.sdk.compatibility.internal.service.ITargetPlatformCompatService;

public final class TargetPlatformUtility {
  private TargetPlatformUtility() {
  }

  public static void resolveTargetPlatform(IFile targetFile, IProgressMonitor monitor) throws CoreException {
    resolveTargetPlatform(targetFile, false, monitor);
  }

  public static void resolveTargetPlatform(IFile targetFile, boolean loadPlatform, IProgressMonitor monitor) throws CoreException {
    ITargetPlatformCompatService svc = getService();
    svc.resolveTargetPlatform(targetFile, loadPlatform, monitor);
  }

  private static ITargetPlatformCompatService getService() {
    return ScoutCompatibilityActivator.getDefault().acquireCompatibilityService(ITargetPlatformCompatService.class);
  }

  public static void addInstallableUnitsToTarget(IFile targetFile, String[] unitIds, String[] versions, String[] repositories) throws CoreException {
    ITargetPlatformCompatService svc = getService();
    svc.addInstallableUnitsToTarget(targetFile, unitIds, versions, repositories);
  }

  public static void removeInstallableUnitsFromTarget(IFile targetFile, String[] unitIds) throws CoreException {
    ITargetPlatformCompatService svc = getService();
    svc.removeInstallableUnitsFromTarget(targetFile, unitIds);
  }
}
