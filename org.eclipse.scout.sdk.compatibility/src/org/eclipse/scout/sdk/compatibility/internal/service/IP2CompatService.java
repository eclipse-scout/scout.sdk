package org.eclipse.scout.sdk.compatibility.internal.service;

import java.net.URI;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.sdk.compatibility.License;

/**
 * <h3>{@link IP2CompatService}</h3> P2 Compatibility service interface.
 * 
 * @author mvi
 * @since 3.9.0 03.05.2013
 */
public interface IP2CompatService {

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
  String getLatestVersion(String rootIU, URI p2RepositoryURI, IProgressMonitor monitor) throws CoreException;

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
  void installUnits(String[] rootIUs, URI[] p2RepositoryURIs, IProgressMonitor monitor) throws CoreException;

  /**
   * Gets the licenses of the given installable units.
   * 
   * @param rootIUs
   *          Array of all installable unit Ids for which the licenses should be loaded.
   * @param p2RepositoryURIs
   *          The P2 repository URIs in which the given installable units should be searched.
   * @param monitor
   *          The progress monitor
   * @return A map containing all licenses for all installable unit names.
   * @throws CoreException
   */
  Map<String, License[]> getLicenses(String[] rootIUs, URI[] p2RepositoryURIs, IProgressMonitor monitor) throws CoreException;

  /**
   * Instructs the ProvisioningUI (P2 of the running platform) to ask the user for a platform restart.
   */
  void promptForRestart();
}
