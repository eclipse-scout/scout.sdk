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
package org.eclipse.scout.sdk.compatibility.v43.internal;

import java.net.URI;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.equinox.p2.core.IProvisioningAgent;
import org.eclipse.equinox.p2.core.IProvisioningAgentProvider;
import org.eclipse.equinox.p2.core.ProvisionException;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.metadata.ILicense;
import org.eclipse.equinox.p2.operations.InstallOperation;
import org.eclipse.equinox.p2.operations.ProvisioningJob;
import org.eclipse.equinox.p2.query.IQuery;
import org.eclipse.equinox.p2.query.IQueryResult;
import org.eclipse.equinox.p2.query.QueryUtil;
import org.eclipse.equinox.p2.repository.metadata.IMetadataRepository;
import org.eclipse.equinox.p2.repository.metadata.IMetadataRepositoryManager;
import org.eclipse.equinox.p2.ui.Policy;
import org.eclipse.equinox.p2.ui.ProvisioningUI;
import org.eclipse.scout.sdk.compatibility.License;
import org.eclipse.scout.sdk.compatibility.internal.ScoutCompatibilityActivator;
import org.eclipse.scout.sdk.compatibility.internal.service.IP2CompatService;

public class P2CompatService implements IP2CompatService {
  @Override
  public String getLatestVersion(String rootIU, URI p2RepositoryURI, IProgressMonitor monitor) throws CoreException {
    String[] latestVersions = getLatestVersions(new String[]{rootIU}, p2RepositoryURI, monitor);
    if (latestVersions != null && latestVersions.length > 0) {
      return latestVersions[0];
    }
    return null;
  }

  @Override
  public void promptForRestart() {
    Job dummy = new Job("") {
      @Override
      protected IStatus run(IProgressMonitor monitor) {
        return Status.OK_STATUS;
      }
    };
    ProvisioningUI.getDefaultUI().manageJob(dummy, Policy.RESTART_POLICY_PROMPT);
    dummy.schedule();
  }

  @Override
  public void installUnits(String[] rootIUs, URI[] p2RepositoryURIs, IProgressMonitor monitor) throws CoreException {
    ArrayList<IInstallableUnit> toInstall = new ArrayList<IInstallableUnit>(rootIUs.length);
    for (int i = 0; i < Math.min(rootIUs.length, p2RepositoryURIs.length); i++) {
      IInstallableUnit[] units = getInstallableUnits(new String[]{rootIUs[i]}, getMetadataRepository(p2RepositoryURIs[i], monitor), monitor);
      for (IInstallableUnit iu : units) {
        toInstall.add(iu);
      }
    }

    ProvisioningUI ui = ProvisioningUI.getDefaultUI();
    InstallOperation op = ui.getInstallOperation(toInstall, p2RepositoryURIs);

    ProvisioningJob resJob = op.getResolveJob(monitor);
    IStatus resResult = resJob.runModal(monitor);
    if (resResult.isOK()) {
      ProvisioningJob provJob = op.getProvisioningJob(monitor);
      if (provJob != null) {
        resResult = provJob.runModal(monitor);
        if (!resResult.isOK()) {
          throw new CoreException(resResult);
        }
      }
    }
    else {
      throw new CoreException(resResult);
    }
  }

  @Override
  public Map<String /* IU id */, License[] /*license bodies */> getLicenses(String[] rootIUs, URI[] p2RepositoryURIs, IProgressMonitor monitor) throws CoreException {
    ArrayList<IInstallableUnit> units = new ArrayList<IInstallableUnit>(rootIUs.length);
    for (int i = 0; i < Math.min(rootIUs.length, p2RepositoryURIs.length); i++) {
      IInstallableUnit[] ius = getInstallableUnits(new String[]{rootIUs[i]}, getMetadataRepository(p2RepositoryURIs[i], monitor), monitor);
      if (ius != null && ius.length > 0) {
        for (IInstallableUnit iu : ius) {
          units.add(iu);
        }
      }
    }

    LinkedHashMap<String, License[]> ret = new LinkedHashMap<String, License[]>(units.size());
    for (IInstallableUnit iu : units) {
      Collection<ILicense> licenses = iu.getLicenses(null);
      ArrayList<License> licList = new ArrayList<License>(licenses.size());
      for (ILicense l : licenses) {
        if (l.getBody() != null && l.getBody().trim().length() > 0) {
          License lic = new License(l.getBody(), iu.getId());
          licList.add(lic);
        }
      }
      ret.put(iu.getId(), licList.toArray(new License[licList.size()]));
    }
    return ret;
  }

  private IProvisioningAgent getAgent() throws CoreException {
    IPath stateLocation = ScoutCompatibilityActivator.getDefault().getStateLocation();
    URI stateLocationURI = stateLocation.toFile().toURI();
    IProvisioningAgentProvider agentProvider = ScoutCompatibilityActivator.getDefault().acquireService(IProvisioningAgentProvider.class);
    if (agentProvider == null) {
      IStatus status = new Status(IStatus.ERROR, ScoutCompatibilityActivator.PLUGIN_ID, "Agent provider service not available");
      throw new CoreException(status);
    }
    return agentProvider.createAgent(stateLocationURI);
  }

  private IMetadataRepository getMetadataRepository(URI p2RepositoryURI, IProgressMonitor monitor) throws CoreException {
    IProvisioningAgent agent = getAgent();
    IMetadataRepositoryManager repoManager = (IMetadataRepositoryManager) agent.getService(IMetadataRepositoryManager.SERVICE_NAME);
    IMetadataRepository metadataRepository = loadRepository(p2RepositoryURI, repoManager, monitor);
    return metadataRepository;
  }

  private IMetadataRepository loadRepository(final URI p2RepositoryURI, final IMetadataRepositoryManager repoManager, final IProgressMonitor monitor) throws CoreException {
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
      IStatus status = new Status(IStatus.ERROR, ScoutCompatibilityActivator.PLUGIN_ID, fmtMessage, e);
      throw new CoreException(status);
    }
    return result;
  }

  private IInstallableUnit[] getInstallableUnits(final String[] rootIUs, final IMetadataRepository metadataRepository, IProgressMonitor monitor) throws CoreException {
    IInstallableUnit[] result = new IInstallableUnit[rootIUs.length];
    for (int i = 0; i < rootIUs.length; i++) {
      String rootIuId = rootIUs[i];
      IQuery<IInstallableUnit> latestQuery = QueryUtil.createLatestQuery(QueryUtil.createIUQuery(rootIuId));
      IQueryResult<IInstallableUnit> queryResult = metadataRepository.query(latestQuery, monitor);
      if (queryResult.isEmpty()) {
        String messag = "Feature <{0}> not found"; //$NON-NLS-1$
        Object[] arguments = new Object[]{
            rootIuId
        };
        String fmtMessage = MessageFormat.format(messag, arguments);
        IStatus status = new Status(IStatus.ERROR, ScoutCompatibilityActivator.PLUGIN_ID, fmtMessage);
        throw new CoreException(status);
      }
      Iterator<IInstallableUnit> iterator = queryResult.iterator();
      IInstallableUnit iu = iterator.next();

      result[i] = iu;
    }
    return result;
  }

  private String[] getLatestVersions(String[] rootIUs, URI p2RepositoryURI, IProgressMonitor monitor) throws CoreException {
    String[] result = getLatestVersions(rootIUs, getMetadataRepository(p2RepositoryURI, monitor), monitor);
    return result;
  }

  private String[] getLatestVersions(final String[] rootIUs, final IMetadataRepository metadataRepository, IProgressMonitor monitor) throws CoreException {
    IInstallableUnit[] units = getInstallableUnits(rootIUs, metadataRepository, monitor);
    String[] result = new String[units.length];
    for (int i = 0; i < result.length; i++) {
      result[i] = units[i].getVersion().toString();
    }
    return result;
  }
}
