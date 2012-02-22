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

  public static String getLatestVersion(String rootIU, URI p2RepositoryURI, IProgressMonitor monitor) throws CoreException {
    return getP2CompatService().getLatestVersion(rootIU, p2RepositoryURI, monitor);
  }

  public static void installUnit(String rootIU, URI p2RepositoryURI, IProgressMonitor monitor) throws CoreException {
    getP2CompatService().installUnit(rootIU, p2RepositoryURI, monitor);
  }

  public static Map<String /* IU id */, License[]> getLicense(String rootIU, URI p2RepositoryURI, IProgressMonitor monitor) throws CoreException {
    return getP2CompatService().getLicense(rootIU, p2RepositoryURI, monitor);
  }

  public static void promptForRestart() {
    getP2CompatService().promptForRestart();
  }

  private static IP2CompatService getP2CompatService() {
    return ScoutCompatibilityActivator.getDefault().acquireCompatibilityService(IP2CompatService.class);
  }
}
