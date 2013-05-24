package org.eclipse.scout.sdk.compatibility.internal.service;

import java.io.File;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

public interface ITargetPlatformCompatService {
  IStatus resolveTargetPlatform(IFile targetFile, boolean loadPlatform, IProgressMonitor monitor) throws CoreException;

  IStatus resolveTargetPlatform(Set<File> absolutePaths, String targetName, boolean loadPlatform, IProgressMonitor monitor) throws CoreException;

  void addInstallableUnitsToTarget(IFile targetFile, String[] unitIds, String[] versions, String[] repositories) throws CoreException;

  void removeInstallableUnitsFromTarget(IFile targetFile, String[] unitIds) throws CoreException;
}
