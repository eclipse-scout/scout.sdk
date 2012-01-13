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
package org.eclipse.scout.sdk.rap.internal;

import java.net.URI;
import java.text.MessageFormat;
import java.util.Iterator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.equinox.p2.core.IProvisioningAgent;
import org.eclipse.equinox.p2.core.IProvisioningAgentProvider;
import org.eclipse.equinox.p2.core.ProvisionException;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.query.IQuery;
import org.eclipse.equinox.p2.query.IQueryResult;
import org.eclipse.equinox.p2.query.QueryUtil;
import org.eclipse.equinox.p2.repository.metadata.IMetadataRepository;
import org.eclipse.equinox.p2.repository.metadata.IMetadataRepositoryManager;
import org.eclipse.scout.sdk.rap.ScoutSdkRap;

/**
 * <h3>{@link P2Utility}</h3> ...
 * 
 * @author aho
 * @since 3.8.0 13.01.2012
 */
public final class P2Utility {

  private P2Utility() {
  }

  public static String getLatestVersion(String rootIU, URI p2RepositoryURI, IProgressMonitor monitor) throws CoreException {
    String[] latestVersions = getLatestVersions(new String[]{rootIU}, p2RepositoryURI, monitor);
    if (latestVersions != null && latestVersions.length > 0) {
      return latestVersions[0];
    }
    return null;
  }

  public static String[] getLatestVersions(String[] rootIUs, URI p2RepositoryURI, IProgressMonitor monitor) throws CoreException {
    IProvisioningAgent agent = getAgent();
    IMetadataRepositoryManager repoManager = (IMetadataRepositoryManager) agent.getService(IMetadataRepositoryManager.SERVICE_NAME);
    IMetadataRepository metadataRepository = loadRepository(p2RepositoryURI, repoManager, monitor);
    String[] result = selectLatestVersions(rootIUs, metadataRepository);
    return result;
  }

  public static IProvisioningAgent getAgent() throws CoreException {
    IPath stateLocation = ScoutSdkRap.getDefault().getStateLocation();
    URI stateLocationURI = stateLocation.toFile().toURI();
    IProvisioningAgentProvider agentProvider = (IProvisioningAgentProvider) ScoutSdkRap.getDefault().acquireService(IProvisioningAgentProvider.SERVICE_NAME);
    if (agentProvider == null) {
      IStatus status = new Status(IStatus.ERROR, ScoutSdkRap.PLUGIN_ID, "Agent provider service not available");
      ScoutSdkRap.log(status);
      throw new CoreException(status);
    }
    return agentProvider.createAgent(stateLocationURI);
  }

  private static IMetadataRepository loadRepository(final URI p2RepositoryURI, final IMetadataRepositoryManager repoManager, final IProgressMonitor monitor) throws CoreException {
    IMetadataRepository result;
    SubMonitor subMonitor = SubMonitor.convert(monitor);
    try {
      SubMonitor repositoryMonitor = subMonitor.newChild(1);
      result = repoManager.loadRepository(p2RepositoryURI, repositoryMonitor);
    }
    catch (ProvisionException e) {
      String message = "Failed to load repository <{0}>"; //$NON-NLS-1$
      Object[] arguments = new Object[]{p2RepositoryURI};
      String fmtMessage = MessageFormat.format(message, arguments);
      IStatus status = new Status(IStatus.ERROR, ScoutSdkRap.PLUGIN_ID, fmtMessage, e);
      ScoutSdkRap.log(status);
      throw new CoreException(status);
    }
    return result;
  }

  private static String[] selectLatestVersions(final String[] rootIUs, final IMetadataRepository metadataRepository) throws CoreException {
    String[] result = new String[rootIUs.length];
    for (int i = 0; i < rootIUs.length; i++) {
      String rootIuId = rootIUs[i];
      IQuery<IInstallableUnit> latestQuery = QueryUtil.createLatestQuery(QueryUtil.createIUQuery(rootIuId));
      IQueryResult queryResult = metadataRepository.query(latestQuery, new NullProgressMonitor());
      if (queryResult.isEmpty()) {
        String messag = "Feature <{0}> not found"; //$NON-NLS-1$
        Object[] arguments = new Object[]{
            rootIuId
        };
        String fmtMessage = MessageFormat.format(messag, arguments);
        IStatus status = new Status(IStatus.ERROR, ScoutSdkRap.PLUGIN_ID, fmtMessage);
        ScoutSdkRap.log(status);
        throw new CoreException(status);
      }
      Iterator iterator = queryResult.iterator();
      IInstallableUnit iu = (IInstallableUnit) iterator.next();

      result[i] = iu.getVersion().toString();
    }
    return result;
  }
}
