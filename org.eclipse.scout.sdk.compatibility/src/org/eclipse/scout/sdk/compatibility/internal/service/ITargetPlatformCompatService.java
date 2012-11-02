package org.eclipse.scout.sdk.compatibility.internal.service;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

public interface ITargetPlatformCompatService {
  void resolveTargetPlatform(IFile targetFile, boolean loadPlatform, IProgressMonitor monitor) throws CoreException;

  void addInstallableUnitsToTarget(IFile targetFile, String[] unitIds, String[] versions, String[] repositories) throws CoreException;

  void removeInstallableUnitsFromTarget(IFile targetFile, String[] unitIds) throws CoreException;
}
