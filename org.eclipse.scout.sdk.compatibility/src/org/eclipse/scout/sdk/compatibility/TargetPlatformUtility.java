package org.eclipse.scout.sdk.compatibility;

import java.io.File;
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
   * Resolves (and optionaly loads) the target specified by the given .target file.<br>
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
   * Adds the given installable units to the given .target file.<br>
   * Please note that unitIds, versions and repositories must be of equal size.
   * 
   * @param targetFile
   *          The file that should be modified.
   * @param unitIds
   *          The Ids of the installable units to add.
   * @param versions
   *          The version constraints of the units.
   * @param repositories
   *          The URI where to find the corresponding installable unit.
   * @throws CoreException
   */
  public static void addInstallableUnitsToTarget(IFile targetFile, String[] unitIds, String[] versions, String[] repositories) throws CoreException {
    ITargetPlatformCompatService svc = getService();
    svc.addInstallableUnitsToTarget(targetFile, unitIds, versions, repositories);
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
