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
    ITargetPlatformCompatService svc = ScoutCompatibilityActivator.getDefault().acquireCompatibilityService(ITargetPlatformCompatService.class);
    svc.resolveTargetPlatform(targetFile, loadPlatform, monitor);
  }
}
