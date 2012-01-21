package org.eclipse.scout.sdk.compatibility;

import java.net.URI;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.sdk.compatibility.internal.ScoutCompatibilityActivator;
import org.eclipse.scout.sdk.compatibility.internal.service.IP2CompatService;

public final class P2Utility {
  private P2Utility() {
  }

  public static String getLatestVersion(String rootIU, URI p2RepositoryURI, IProgressMonitor monitor) throws CoreException {
    IP2CompatService svc = ScoutCompatibilityActivator.getDefault().acquireCompatibilityService(IP2CompatService.class);
    return svc.getLatestVersion(rootIU, p2RepositoryURI, monitor);
  }
}
