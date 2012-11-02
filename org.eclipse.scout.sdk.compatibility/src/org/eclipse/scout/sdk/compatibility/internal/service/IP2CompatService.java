package org.eclipse.scout.sdk.compatibility.internal.service;

import java.net.URI;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.sdk.compatibility.License;

public interface IP2CompatService {
  String getLatestVersion(String rootIU, URI p2RepositoryURI, IProgressMonitor monitor) throws CoreException;

  void installUnit(String rootIU, URI p2RepositoryURI, IProgressMonitor monitor) throws CoreException;

  Map<String, License[]> getLicense(String rootIU, URI p2RepositoryURI, IProgressMonitor monitor) throws CoreException;

  void promptForRestart();
}
