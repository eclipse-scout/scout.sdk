/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.compatibility;

import java.io.File;
import java.net.URI;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.scout.sdk.compatibility.internal.ScoutCompatibilityActivator;
import org.eclipse.scout.sdk.compatibility.internal.service.ITargetPlatformCompatService;

/**
 * <h3>{@link TargetPlatformUtility}</h3> Utility to (eclipse platform independent) handle target platform tasks.
 *
 * @author Matthias Villiger
 * @since 3.9.0 24.05.2013
 */
public final class TargetPlatformUtility {
  private TargetPlatformUtility() {
  }

  /**
   * Resolves the target specified by the given .target file.
   *
   * @param targetFile
   *          The .target file that should be resolved.
   * @param monitor
   *          The progress monitor
   * @return The resolve status.
   * @throws CoreException
   */
  public static IStatus resolveTargetPlatform(IFile targetFile, IProgressMonitor monitor) throws CoreException {
    return resolveTargetPlatform(targetFile, false, monitor);
  }

  /**
   * @return Gets the {@link URI} pointing to the file that defines the current target platform or null.
   * @throws CoreException
   */
  public static URI getCurrentTargetFile() throws CoreException {
    ITargetPlatformCompatService svc = getService();
    return svc.getCurrentTargetFile();
  }

  /**
   * Resolves (and optionally loads) the target specified by the given .target file.<br>
   * If the platform defined by the given file cannot be resolved, it will not be loaded even if loadPlatform is set to
   * true.
   *
   * @param targetFile
   *          The .target file that should be resolved.
   * @param loadPlatform
   *          true if the platform defined by the file should also be loaded (activated).
   * @param monitor
   *          The progress monitor
   * @return the status of the resolve or the load of the platform.
   * @throws CoreException
   */
  public static IStatus resolveTargetPlatform(IFile targetFile, boolean loadPlatform, IProgressMonitor monitor) throws CoreException {
    ITargetPlatformCompatService svc = getService();
    return svc.resolveTargetPlatform(targetFile, loadPlatform, monitor);
  }

  /**
   * Creates and resolves a new target with given name and the given directories as locations.
   *
   * @param absolutePaths
   *          The directories that should be added to the target.
   * @param targetName
   *          The name of the target
   * @param monitor
   *          The progress monitor.
   * @return The resolve status.
   * @throws CoreException
   */
  public static IStatus resolveTargetPlatform(Set<File> absolutePaths, String targetName, IProgressMonitor monitor) throws CoreException {
    return resolveTargetPlatform(absolutePaths, targetName, false, monitor);
  }

  /**
   * Creates and resolves a new target with given name and the given directories as locations.<br>
   * If the platform defined by the given directories cannot be resolved, it will not be loaded even if loadPlatform is
   * set to
   * true.
   *
   * @param absolutePaths
   *          The directories that should be added to the target.
   * @param targetName
   *          The name of the target
   * @param loadPlatform
   *          true if the platform defined by the given directories should be loaded (activated).
   * @param monitor
   *          The progress monitor
   * @return the status of the resolve or the load of the platform.
   * @throws CoreException
   */
  public static IStatus resolveTargetPlatform(Set<File> absolutePaths, String targetName, boolean loadPlatform, IProgressMonitor monitor) throws CoreException {
    ITargetPlatformCompatService svc = getService();
    return svc.resolveTargetPlatform(absolutePaths, targetName, loadPlatform, monitor);
  }

  private static ITargetPlatformCompatService getService() {
    return ScoutCompatibilityActivator.getDefault().acquireCompatibilityService(ITargetPlatformCompatService.class);
  }

  /**
   * Adds the given installable unit to the given .target file.
   *
   * @param targetFile
   *          The target file to which the given unit should be added.
   * @param unitId
   *          The id of the unit to add
   * @param version
   *          The version of the unit. Use a concrete version if desired or
   *          {@link PlatformVersionUtility#EMPTY_VERSION_STR} if no special version is required. When passing
   *          <code>null</code> the given repository is queried for the currently newest version and this version is
   *          written into the target file.
   * @param repository
   *          The repository where to find the unit to install.
   * @param monitor
   *          The progress monitor used when <code>null</code> is given for the version parameter and the newest version
   *          must be calculated from the given repository.
   * @throws CoreException
   */
  public static void addInstallableUnitToTarget(IFile targetFile, final String unitId, final String version, final String repository, IProgressMonitor monitor) throws CoreException {
    ITargetPlatformCompatService svc = getService();
    svc.addInstallableUnitToTarget(targetFile, unitId, version, repository, monitor);
  }

  /**
   * Adds the given directory entries to the given .target file.
   *
   * @param targetFile
   *          The file that should be modified.
   * @param dirs
   *          The directories to add
   * @throws CoreException
   */
  public static void addDirectoryToTarget(IFile targetFile, String[] dirs) throws CoreException {
    ITargetPlatformCompatService svc = getService();
    svc.addDirectoryLocationToTarget(targetFile, dirs);
  }

  /**
   * Removes the given installable units from the given .target file.
   *
   * @param targetFile
   *          The .target file to be modified.
   * @param unitIds
   *          The Ids of the installable units to remove from the file.
   * @throws CoreException
   */
  public static void removeInstallableUnitsFromTarget(IFile targetFile, String[] unitIds) throws CoreException {
    ITargetPlatformCompatService svc = getService();
    svc.removeInstallableUnitsFromTarget(targetFile, unitIds);
  }
}
