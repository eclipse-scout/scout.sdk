package org.eclipse.scout.sdk.compatibility.v35.internal;

import java.net.URI;
import java.text.MessageFormat;
import java.util.Iterator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.equinox.internal.provisional.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.internal.provisional.p2.metadata.query.InstallableUnitQuery;
import org.eclipse.equinox.internal.provisional.p2.metadata.query.LatestIUVersionQuery;
import org.eclipse.equinox.internal.provisional.p2.metadata.repository.IMetadataRepository;
import org.eclipse.equinox.internal.provisional.p2.metadata.repository.IMetadataRepositoryManager;
import org.eclipse.equinox.internal.provisional.p2.query.Collector;
import org.eclipse.equinox.internal.provisional.p2.query.CompositeQuery;
import org.eclipse.equinox.internal.provisional.p2.query.Query;
import org.eclipse.pde.internal.core.PDECore;
import org.eclipse.scout.sdk.compatibility.internal.ScoutCompatibilityActivator;
import org.eclipse.scout.sdk.compatibility.internal.service.IP2CompatService;

@SuppressWarnings("restriction")
public class P2CompatService implements IP2CompatService {
  @Override
  public String getLatestVersion(String rootIU, URI p2RepositoryURI, IProgressMonitor monitor) throws CoreException {
    String[] latestVersions = getLatestVersions(new String[]{rootIU}, p2RepositoryURI, monitor);
    if (latestVersions != null && latestVersions.length > 0) {
      return latestVersions[0];
    }
    return null;
  }

  private String[] getLatestVersions(String[] rootIUs, URI p2RepositoryURI, IProgressMonitor monitor) throws CoreException {
	IMetadataRepositoryManager manager = (IMetadataRepositoryManager) PDECore.getDefault().acquireService(IMetadataRepositoryManager.class.getName());
	IMetadataRepository metadataRepository = manager.loadRepository(p2RepositoryURI, monitor);
    String[] result = selectLatestVersions(rootIUs, metadataRepository, monitor);
    return result;
  }

  @SuppressWarnings("unchecked")
  private static String[] selectLatestVersions(final String[] rootIUs, final IMetadataRepository metadataRepository, IProgressMonitor monitor) throws CoreException {
    String[] result = new String[rootIUs.length];
    for (int i = 0; i < rootIUs.length; i++) {
      String rootIuId = rootIUs[i];
      
      Collector queryResult = new Collector();
      CompositeQuery latestIUQuery = new CompositeQuery(new Query[]{new InstallableUnitQuery(rootIuId),  new LatestIUVersionQuery()});
      metadataRepository.query(latestIUQuery, queryResult, monitor);
      
      if (queryResult.isEmpty()) {
        String messag = "Feature <{0}> not found"; //$NON-NLS-1$
        Object[] arguments = new Object[]{
            rootIuId
        };
        String fmtMessage = MessageFormat.format(messag, arguments);
        IStatus status = new Status(IStatus.ERROR, ScoutCompatibilityActivator.PLUGIN_ID, fmtMessage);
        throw new CoreException(status);
      }
      Iterator iterator = queryResult.iterator();
      IInstallableUnit iu = (IInstallableUnit) iterator.next();

      result[i] = iu.getVersion().toString();
    }
    return result;
  }
}
