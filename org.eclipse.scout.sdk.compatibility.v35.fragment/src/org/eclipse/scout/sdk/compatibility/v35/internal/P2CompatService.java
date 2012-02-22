package org.eclipse.scout.sdk.compatibility.v35.internal;

import java.net.URI;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.equinox.internal.p2.ui.PlanAnalyzer;
import org.eclipse.equinox.internal.p2.ui.ProvUIMessages;
import org.eclipse.equinox.internal.p2.ui.sdk.ProvSDKUIActivator;
import org.eclipse.equinox.internal.provisional.p2.director.ProfileChangeRequest;
import org.eclipse.equinox.internal.provisional.p2.engine.ProvisioningContext;
import org.eclipse.equinox.internal.provisional.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.internal.provisional.p2.metadata.ILicense;
import org.eclipse.equinox.internal.provisional.p2.metadata.query.InstallableUnitQuery;
import org.eclipse.equinox.internal.provisional.p2.metadata.query.LatestIUVersionQuery;
import org.eclipse.equinox.internal.provisional.p2.metadata.repository.IMetadataRepository;
import org.eclipse.equinox.internal.provisional.p2.metadata.repository.IMetadataRepositoryManager;
import org.eclipse.equinox.internal.provisional.p2.query.Collector;
import org.eclipse.equinox.internal.provisional.p2.query.CompositeQuery;
import org.eclipse.equinox.internal.provisional.p2.query.Query;
import org.eclipse.equinox.internal.provisional.p2.ui.ProvisioningOperationRunner;
import org.eclipse.equinox.internal.provisional.p2.ui.actions.InstallAction;
import org.eclipse.equinox.internal.provisional.p2.ui.operations.PlannerResolutionOperation;
import org.eclipse.equinox.internal.provisional.p2.ui.operations.ProfileModificationOperation;
import org.eclipse.pde.internal.core.PDECore;
import org.eclipse.scout.sdk.compatibility.License;
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

  @Override
  public Map<String, License[]> getLicense(String rootIU, URI p2RepositoryURI, IProgressMonitor monitor) throws CoreException {
    IInstallableUnit[] units = getInstallableUnits(new String[]{rootIU}, getMetadataRepository(p2RepositoryURI, monitor), monitor);
    HashMap<String, License[]> ret = new HashMap<String, License[]>(units.length);
    for (IInstallableUnit iu : units) {
      ILicense license = iu.getLicense();
      if (license.getBody() != null && license.getBody().trim().length() > 0) {
        ret.put(iu.getId(), new License[]{new License(license.getBody(), iu.getId())});
      }
    }
    return ret;
  }

  @Override
  public void installUnit(String rootIU, URI p2RepositoryURI, IProgressMonitor monitor) throws CoreException {
    IInstallableUnit[] units = getInstallableUnits(new String[]{rootIU}, getMetadataRepository(p2RepositoryURI, monitor), monitor);

    String profileId = ProvSDKUIActivator.getSelfProfileId();
    ProfileChangeRequest request = InstallAction.computeProfileChangeRequest(units, profileId, PlanAnalyzer.getProfileChangeAlteredStatus(), monitor);

    PlannerResolutionOperation resolutionOp = new PlannerResolutionOperation(ProvUIMessages.ProfileModificationWizardPage_ResolutionOperationLabel,
          profileId, request, new ProvisioningContext(), PlanAnalyzer.getProfileChangeAlteredStatus(), false);
    IStatus resolutionResult = resolutionOp.execute(monitor);
    if (!resolutionResult.isOK()) {
      throw new CoreException(resolutionResult);
    }

    ProvisioningContext context = resolutionOp.getProvisioningContext();
    context.setArtifactRepositories(new URI[]{p2RepositoryURI});

    ProfileModificationOperation modificationOp = new ProfileModificationOperation(ProvUIMessages.InstallIUOperationLabel, profileId,
          resolutionOp.getProvisioningPlan(), context);
    IStatus modificationResult = modificationOp.execute(monitor);
    if (!modificationResult.isOK()) {
      throw new CoreException(modificationResult);
    }
  }

  @SuppressWarnings("deprecation")
  @Override
  public void promptForRestart() {
    ProvisioningOperationRunner.suppressRestart(true);
    ProvisioningOperationRunner.requestRestart(true);
    ProvisioningOperationRunner.suppressRestart(false);
    ProvisioningOperationRunner.requestRestart(true);
  }

  @SuppressWarnings("unchecked")
  private IInstallableUnit[] getInstallableUnits(final String[] rootIUs, final IMetadataRepository metadataRepository, IProgressMonitor monitor) throws CoreException {
    IInstallableUnit[] result = new IInstallableUnit[rootIUs.length];
    for (int i = 0; i < rootIUs.length; i++) {
      String rootIuId = rootIUs[i];

      Collector queryResult = new Collector();
      CompositeQuery latestIUQuery = new CompositeQuery(new Query[]{new InstallableUnitQuery(rootIuId), new LatestIUVersionQuery()});
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

      result[i] = iu;
    }
    return result;
  }

  private IMetadataRepository getMetadataRepository(URI p2RepositoryURI, IProgressMonitor monitor) throws CoreException {
    IMetadataRepositoryManager manager = (IMetadataRepositoryManager) PDECore.getDefault().acquireService(IMetadataRepositoryManager.class.getName());
    return manager.loadRepository(p2RepositoryURI, monitor);
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
