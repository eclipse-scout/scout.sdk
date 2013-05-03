package org.eclipse.scout.sdk.compatibility;

import java.net.URI;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.sdk.compatibility.internal.ScoutCompatibilityActivator;
import org.eclipse.scout.sdk.compatibility.internal.service.IP2CompatService;

public final class P2Utility {
  private P2Utility() {
  }

  /**
   * Gets the newest version of an installable unit in a specific P2 repository.
   * 
   * @param rootIU
   *          The installable unit for which the latest version should be searched.
   * @param p2RepositoryURI
   *          The P2 repository URI in which the newest version should be searched.
   * @param monitor
   *          The progress monitor.
   * @return A String containing the newest (highest) version of the given IU in the given P2 repository.
   * @throws CoreException
   */
  public static String getLatestVersion(String rootIU, URI p2RepositoryURI, IProgressMonitor monitor) throws CoreException {
    return getP2CompatService().getLatestVersion(rootIU, p2RepositoryURI, monitor);
  }

  /**
   * Installs the given installable units in the running platform.
   * 
   * @param rootIUs
   *          Array of all installable unit Ids to install.
   * @param p2RepositoryURIs
   *          The P2 repository URIs in which the given installable units should be searched.
   * @param monitor
   *          The progress monitor
   * @throws CoreException
   */
  public static void installUnits(String[] rootIUs, URI[] p2RepositoryURIs, IProgressMonitor monitor) throws CoreException {
    getP2CompatService().installUnits(rootIUs, p2RepositoryURIs, monitor);
  }

  /**
   * Gets the licenses of the given installable units.
   * 
   * @param rootIUs
   *          Array of all installable units for which the licenses should be loaded.
   * @param p2RepositoryURIs
   *          The P2 repository URIs in which the given installable units should be searched.
   * @param monitor
   *          The progress monitor
   * @return A map containing all licenses for all installable unit names.
   * @throws CoreException
   */
  public static Map<String /* IU id */, License[]> getLicenses(String[] rootIUs, URI[] p2RepositoryURIs, IProgressMonitor monitor) throws CoreException {
    return getP2CompatService().getLicenses(rootIUs, p2RepositoryURIs, monitor);
  }

  /**
   * Instructs the ProvisioningUI (P2 of the running platform) to ask the user for a platform restart.
   */
  public static void promptForRestart() {
    getP2CompatService().promptForRestart();
  }

  private static IP2CompatService getP2CompatService() {
    return ScoutCompatibilityActivator.getDefault().acquireCompatibilityService(IP2CompatService.class);
  }
}
