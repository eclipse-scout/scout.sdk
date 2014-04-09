package org.eclipse.scout.sdk.compatibility.internal.service;

import java.io.File;
import java.net.URI;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

public interface ITargetPlatformCompatService {
  IStatus resolveTargetPlatform(IFile targetFile, boolean loadPlatform, IProgressMonitor monitor) throws CoreException;

  IStatus resolveTargetPlatform(Set<File> absolutePaths, String targetName, boolean loadPlatform, IProgressMonitor monitor) throws CoreException;

  void addInstallableUnitToTarget(IFile targetFile, final String unitId, final String version, final String repository, IProgressMonitor monitor) throws CoreException;

  void removeInstallableUnitsFromTarget(IFile targetFile, String[] unitIds) throws CoreException;

  void addDirectoryLocationToTarget(IFile targetFile, String[] dirs) throws CoreException;

  URI getCurrentTargetFile() throws CoreException;
}
