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
package org.eclipse.scout.sdk.ui.extensions.technology;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.URIUtil;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.pde.internal.core.ICoreConstants;
import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.TriState;
import org.eclipse.scout.sdk.ScoutSdkCore;
import org.eclipse.scout.sdk.compatibility.License;
import org.eclipse.scout.sdk.compatibility.P2Utility;
import org.eclipse.scout.sdk.compatibility.TargetPlatformUtility;
import org.eclipse.scout.sdk.operation.project.CreateTargetProjectOperation;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.internal.dialog.LicenseDialog;
import org.eclipse.scout.sdk.ui.util.UiUtility;
import org.eclipse.scout.sdk.ui.view.outline.IScoutExplorerPart;
import org.eclipse.scout.sdk.ui.view.outline.pages.INodeVisitor;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.util.jdt.JdtUtility;
import org.eclipse.scout.sdk.util.log.ScoutStatus;
import org.eclipse.scout.sdk.util.pde.PluginModelHelper;
import org.eclipse.scout.sdk.util.pde.ProductFileModelHelper;
import org.eclipse.scout.sdk.util.resources.ResourceFilters;
import org.eclipse.scout.sdk.util.resources.ResourceUtility;
import org.eclipse.scout.sdk.workspace.IScoutBundle;

/**
 * <h3>{@link AbstractScoutTechnologyHandler}</h3>
 * 
 * @author Matthias Villiger
 * @since 3.8.0 11.02.2012
 */
@SuppressWarnings("restriction")
public abstract class AbstractScoutTechnologyHandler implements IScoutTechnologyHandler {

  protected enum FeatureInstallResult {
    InstallationNotNecessary,
    InstallationSuccessful,
    LicenseNotAccepted
  }

  protected AbstractScoutTechnologyHandler() {
  }

  @Override
  public List<IScoutTechnologyResource> getModifactionResourceCandidates(IScoutBundle project) throws CoreException {
    ArrayList<IScoutTechnologyResource> ret = new ArrayList<IScoutTechnologyResource>();
    contributeResources(project, ret);
    return ret;
  }

  protected abstract void contributeResources(IScoutBundle project, List<IScoutTechnologyResource> list) throws CoreException;

  protected boolean showLicenseDialog(boolean selected, IProgressMonitor monitor, String[] featureIds, String[] featureUrls) throws CoreException {
    if (!selected) {
      return true;
    }
    try {
      final AtomicBoolean licAccepted = new AtomicBoolean(false);
      URI[] uris = new URI[featureUrls.length];
      for (int i = 0; i < uris.length; i++) {
        uris[i] = URIUtil.fromString(featureUrls[i]);
      }
      final Map<String, License[]> lic = P2Utility.getLicenses(featureIds, uris, monitor);

      ScoutSdkUi.getDisplay().syncExec(new Runnable() {
        @Override
        public void run() {
          LicenseDialog licDialog = new LicenseDialog(ScoutSdkUi.getShell(), lic);
          if (licDialog.open() == Dialog.OK) {
            licAccepted.set(true);
          }
        }
      });
      return licAccepted.get();
    }
    catch (URISyntaxException e) {
      throw new CoreException(new ScoutStatus(e));
    }
  }

  protected void contributeProductFiles(List<IScoutTechnologyResource> list, String... bundleFilter) throws CoreException {
    contributeProductFiles(list, true, bundleFilter);
  }

  protected void contributeProductFiles(List<IScoutTechnologyResource> list, boolean defaultSelection, String... bundleFilter) throws CoreException {
    for (P_TechProductFile prodFile : getFilteredProductFiles(bundleFilter)) {
      list.add(new ScoutTechnologyResource(prodFile.bundle, prodFile.productFile, defaultSelection));
    }
  }

  protected void contributeManifestFiles(Set<IScoutBundle> bundles, List<IScoutTechnologyResource> list) {
    for (IScoutBundle bundle : bundles) {
      contributeManifestFile(bundle, list);
    }
  }

  protected void contributeManifestFile(IScoutBundle bundle, List<IScoutTechnologyResource> list) {
    if (bundle != null && !bundle.isBinary()) {
      IScoutTechnologyResource res = getManifestResource(bundle);
      if (res.getResource() != null) {
        list.add(res);
      }
    }
  }

  protected TriState getSelectionManifestsImportPackage(Set<IScoutBundle> projects, String... importPackages) {
    if (projects == null || projects.size() == 0) {
      return null;
    }

    TriState ret = null;
    for (IScoutBundle b : projects) {
      TriState tmp = getSelectionManifestsImportPackage(b, importPackages);
      if (ret == null) {
        ret = tmp;
      }
      else if (tmp != ret) {
        return TriState.UNDEFINED;
      }
    }
    return ret;
  }

  protected TriState getSelectionManifestsImportPackage(IScoutBundle project, String... importPackages) {
    if (importPackages == null || importPackages.length == 0) {
      return TriState.FALSE;
    }

    PluginModelHelper pluginModel = new PluginModelHelper(project.getSymbolicName());
    TriState ret = TriState.parseTriState(pluginModel.Manifest.existsImportPackage(importPackages[0]));
    for (int i = 1; i < importPackages.length; i++) {
      TriState tmp = TriState.parseTriState(pluginModel.Manifest.existsImportPackage(importPackages[i]));
      if (tmp != ret) {
        return TriState.UNDEFINED;
      }
    }
    return ret;
  }

  protected TriState getSelectionManifests(Set<IScoutBundle> projects, String... pluginIds) {
    if (projects == null || projects.size() == 0) {
      return TriState.FALSE;
    }

    TriState ret = null;
    for (IScoutBundle b : projects) {
      TriState tmp = getSelectionManifest(b, pluginIds);
      if (ret == null) {
        ret = tmp;
      }
      else if (tmp != ret) {
        return TriState.UNDEFINED;
      }
    }
    return ret;
  }

  protected TriState getSelectionManifest(IScoutBundle project, String... pluginIds) {
    if (pluginIds == null || pluginIds.length == 0) {
      return TriState.FALSE;
    }

    PluginModelHelper pluginModel = new PluginModelHelper(project.getSymbolicName());
    TriState ret = TriState.parseTriState(pluginModel.Manifest.existsDependency(pluginIds[0]));
    for (int i = 1; i < pluginIds.length; i++) {
      TriState tmp = TriState.parseTriState(pluginModel.Manifest.existsDependency(pluginIds[i]));
      if (tmp != ret) {
        return TriState.UNDEFINED;
      }
    }
    return ret;
  }

  protected TriState getSelectionProductFiles(String[] filterPluginIds, String[]... pluginIds) throws CoreException {
    List<P_TechProductFile> productFiles = getFilteredProductFiles(filterPluginIds);
    if (productFiles.size() == 0) {
      return null;
    }

    TriState ret = TriState.parseTriState(containsProductDependencies(productFiles.get(0).productFile, pluginIds));
    for (int i = 1; i < productFiles.size(); i++) {
      TriState tmp = TriState.parseTriState(containsProductDependencies(productFiles.get(i).productFile, pluginIds));
      if (ret != tmp) {
        return TriState.UNDEFINED;
      }
    }
    return ret;
  }

  protected TriState getSelectionTargetFileContainsFeature(List<ScoutTechnologyResource> files, String... featureIds) throws CoreException {
    if (files.size() < 1) {
      return null;
    }

    TriState ret = TriState.parseTriState(isTargetContainingFeature(files.get(0).getResource(), featureIds));
    for (int i = 1; i < files.size(); i++) {
      TriState tmp = TriState.parseTriState(isTargetContainingFeature(files.get(i).getResource(), featureIds));
      if (ret != tmp) {
        return TriState.UNDEFINED;
      }
    }
    return ret;
  }

  private boolean isTargetContainingFeature(IFile targetFile, String... featureIds) throws CoreException {
    String content = ResourceUtility.getContent(targetFile);
    for (String id : featureIds) {
      if (!content.contains(id)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public boolean preSelectionChanged(Set<IScoutTechnologyResource> resources, boolean selected, IProgressMonitor monitor) throws CoreException {
    return true;
  }

  @Override
  public void postSelectionChanged(boolean selected, IProgressMonitor monitor) throws CoreException {
  }

  protected void selectionChangedProductFiles(Set<IScoutTechnologyResource> resources, boolean selected, String[]... pluginIds) throws CoreException {
    for (IScoutTechnologyResource r : resources) {
      selectionChangedProductFile(r, selected, pluginIds);
    }
  }

  protected void selectionChangedProductFile(IScoutTechnologyResource r, boolean selected, String[]... pluginIds) throws CoreException {
    ProductFileModelHelper h = new ProductFileModelHelper(r.getResource());
    if (pluginIds != null) {
      for (String[] list : pluginIds) {
        if (list != null) {
          for (String pluginId : list) {
            if (selected) {
              h.ProductFile.addDependency(pluginId);
            }
            else {
              h.ProductFile.removeDependency(pluginId);
            }
          }
        }
      }
    }
    h.save();
  }

  protected boolean closeTargetEditors(Set<IScoutTechnologyResource> resources) {
    final AtomicBoolean success = new AtomicBoolean(false);
    final Set<IFile> files = new HashSet<IFile>(resources.size());
    for (IScoutTechnologyResource r : resources) {
      files.add(r.getResource());
    }
    ScoutSdkUi.getDisplay().syncExec(new Runnable() {
      @Override
      public void run() {
        success.set(UiUtility.closeEditors("org.eclipse.pde.ui.targetEditor", files));
      }
    });
    return success.get();
  }

  protected void selectionChangedTargetFiles(Set<IScoutTechnologyResource> resources, boolean selected, IProgressMonitor monitor, String[] featureIds, String[] featureVersions, String[] featureUrls) throws CoreException {
    for (IScoutTechnologyResource r : resources) {
      if (selected) {
        for (int i = 0; i < featureIds.length; i++) {
          TargetPlatformUtility.addInstallableUnitToTarget(r.getResource(), featureIds[i], featureVersions[i], featureUrls[i], monitor);
        }
      }
      else {
        TargetPlatformUtility.removeInstallableUnitsFromTarget(r.getResource(), featureIds);
      }
    }
    if (resources.size() == 1) {
      IFile file = CollectionUtility.firstElement(resources).getResource();
      try {
        TargetPlatformUtility.resolveTargetPlatform(file, true, monitor);
      }
      catch (IllegalStateException e) {
        ScoutSdkUi.logError("Unable to resolve target file '" + file.getFullPath().toOSString() + "'.", e);
      }
    }
  }

  protected void selectionChangedManifest(Set<IScoutTechnologyResource> resources, boolean selected, String... pluginsToHandle) throws CoreException {
    for (IScoutTechnologyResource r : resources) {
      selectionChangedManifest(r, selected, pluginsToHandle);
    }
  }

  protected void selectionChangedManifest(IScoutTechnologyResource r, boolean selected, String... pluginsToHandle) throws CoreException {
    PluginModelHelper pluginModel = new PluginModelHelper(r.getBundle().getSymbolicName());
    for (String pluginId : pluginsToHandle) {
      if (selected) {
        pluginModel.Manifest.addDependency(pluginId);
      }
      else {
        pluginModel.Manifest.removeDependency(pluginId);
      }
    }
    pluginModel.save();
  }

  protected void selectionChangedManifestImportPackage(Set<IScoutTechnologyResource> resources, boolean selected, String[] packages, String[] versions) throws CoreException {
    for (IScoutTechnologyResource r : resources) {
      PluginModelHelper pluginModel = new PluginModelHelper(r.getBundle().getSymbolicName());
      for (int i = 0; i < Math.min(packages.length, versions.length); i++) {
        if (selected) {
          pluginModel.Manifest.addImportPackage(packages[i], versions[i]);
        }
        else {
          pluginModel.Manifest.removeImportPackage(packages[i]);
        }
      }
      pluginModel.save();
    }
  }

  protected boolean containsProductDependencies(IFile productFile, String[]... pluginFilters) {
    try {
      ProductFileModelHelper h = new ProductFileModelHelper(productFile);
      if (pluginFilters != null) {
        for (String[] list : pluginFilters) {
          if (list != null) {
            for (String pluginId : list) {
              if (!h.ProductFile.existsDependency(pluginId)) {
                return false;
              }
            }
          }
        }
      }
      return true;
    }
    catch (CoreException e) {
      ScoutSdkUi.logError("cannot parse product file: " + productFile, e);
      return false;
    }
  }

  protected List<ScoutTechnologyResource> getTargetFiles() throws CoreException {
    final ArrayList<ScoutTechnologyResource> ret = new ArrayList<ScoutTechnologyResource>();
    for (IResource r : ResourceUtility.getAllResources(ResourceFilters.getTargetFileFilter())) {
      IProject p = r.getProject();
      IScoutBundle sb = ScoutSdkCore.getScoutWorkspace().getBundleGraph().getBundle(p);
      boolean checked = p.getName().endsWith(CreateTargetProjectOperation.TARGET_PROJECT_NAME_SUFFIX);
      ret.add(new ScoutTechnologyResource(sb, (IFile) r, checked));
    }
    return ret;
  }

  protected List<P_TechProductFile> getProductFiles() throws CoreException {
    ArrayList<P_TechProductFile> list = new ArrayList<P_TechProductFile>();
    for (IResource r : ResourceUtility.getAllResources(ResourceFilters.getProductFileFilter())) {
      IProject p = r.getProject();
      IScoutBundle sb = ScoutSdkCore.getScoutWorkspace().getBundleGraph().getBundle(p);

      P_TechProductFile tpf = new P_TechProductFile();
      tpf.bundle = sb;
      tpf.productFile = (IFile) r;
      list.add(tpf);
    }
    return list;
  }

  protected IScoutTechnologyResource getManifestResource(IScoutBundle bundle) {
    return new ScoutTechnologyResource(bundle, bundle.getProject().getFile(ICoreConstants.BUNDLE_FILENAME_DESCRIPTOR));
  }

  protected List<P_TechProductFile> getFilteredProductFiles(String... pluginFilter) throws CoreException {
    List<P_TechProductFile> candidates = getProductFiles();
    List<P_TechProductFile> ret = new ArrayList<P_TechProductFile>(candidates.size());
    for (P_TechProductFile candidate : candidates) {
      if (containsProductDependencies(candidate.productFile, pluginFilter)) {
        ret.add(candidate);
      }
    }
    return ret;
  }

  /**
   * checks whether a given feature is already present and installs it if not.<br>
   * this is done by validating if the plugins specified by 'definingPlugins' can be found in the platform.
   * if yes, this method does nothing. if at least one of the given plugins are missing, the complete feature is
   * installed from the given p2 repository URL. Before the installation a license agreement dialog is presented and the
   * feature is only installed if all licenses are accepted.
   * 
   * @param featureId
   *          The feature Id that will be installed if the definingPlugins are not found on the platform
   * @param featureUrl
   *          The P2 repository URL where the feature should be installed from.
   * @param monitor
   *          the monitor
   * @param definingPlugins
   *          the plugins that define this feature. If all of them are present, nothing is installed even if the feature
   *          itself is not present.
   * @return the result of the feature installation.
   * @throws CoreException
   */
  protected FeatureInstallResult ensureFeatureInstalled(final String featureId, final String featureUrl, final IProgressMonitor monitor, String... definingPlugins) throws CoreException {
    return ensureFeaturesInstalled(new String[]{featureId}, new String[]{featureUrl}, monitor, new String[][]{definingPlugins});
  }

  /**
   * checks whether a given features are already present and installs them if not.<br>
   * this is done by validating if the plugins specified by 'definingPlugins' can be found in the platform.
   * if yes, this method does nothing. if at least one of the given plugins are missing, the complete feature is
   * installed from the given p2 repository URL. Before the installation a license agreement dialog is presented and the
   * feature is only installed if all licenses are accepted.
   * 
   * @param featureIds
   *          the features that will be installed if the corresponding definingPlugins are not found on the platform.
   * @param featureUrls
   *          The P2 repository URLs where the features should be installed from.
   * @param monitor
   *          the monitor
   * @param definingPlugins
   *          The plugins that define the corresponding feature. If all of them are present, nothing is installed even
   *          if the feature itself is not present.
   * @return the result of the feature installation.
   * @throws CoreException
   */
  protected FeatureInstallResult ensureFeaturesInstalled(final String[] featureIds, final String[] featureUrls, final IProgressMonitor monitor, String[]... definingPlugins) throws CoreException {
    ArrayList<URI> repos = new ArrayList<URI>();
    ArrayList<String> featureIdsToInstall = new ArrayList<String>();

    try {
      int maxNum = Math.min(Math.min(featureIds.length, featureUrls.length), definingPlugins.length);
      for (int i = 0; i < maxNum; i++) {
        if (!JdtUtility.areAllPluginsInstalled(definingPlugins[i])) {
          // remember which of the given features require installation
          repos.add(URIUtil.fromString(featureUrls[i]));
          featureIdsToInstall.add(featureIds[i]);
        }
      }
    }
    catch (URISyntaxException e) {
      throw new CoreException(new ScoutStatus(e));
    }

    if (featureIdsToInstall.size() == 0) {
      return FeatureInstallResult.InstallationNotNecessary;
    }

    final URI[] repoURIs = repos.toArray(new URI[repos.size()]);
    final String[] ius = featureIdsToInstall.toArray(new String[featureIdsToInstall.size()]);
    final AtomicBoolean licAccepted = new AtomicBoolean(false);
    final Map<String, License[]> licenses = P2Utility.getLicenses(ius, repoURIs, monitor);

    // show license dialog
    ScoutSdkUi.getDisplay().syncExec(new Runnable() {
      @Override
      public void run() {
        LicenseDialog licDialog = new LicenseDialog(ScoutSdkUi.getShell(), licenses);
        if (licDialog.open() == Dialog.OK) {
          licAccepted.set(true);
        }
      }
    });

    if (licAccepted.get()) {
      P2Utility.installUnits(ius, repoURIs, monitor);
      return FeatureInstallResult.InstallationSuccessful;
    }
    return FeatureInstallResult.LicenseNotAccepted;
  }

  protected void refreshScoutExplorerPageAsync(final Class<? extends IPage> pageToRefresh) {
    ScoutSdkUi.getDisplay().asyncExec(new Runnable() {
      @Override
      public void run() {
        try {
          IScoutExplorerPart explorer = ScoutSdkUi.getExplorer(false);
          if (explorer != null) {
            IPage root = explorer.getRootPage();
            if (root != null) {
              INodeVisitor visitor = new INodeVisitor() {
                @Override
                public int visit(IPage page) {
                  if (pageToRefresh.isAssignableFrom(page.getClass())) {
                    page.markStructureDirty();
                    return CANCEL_SUBTREE;
                  }
                  return CONTINUE;
                }
              };
              JdtUtility.waitForSilentWorkspace();
              root.accept(visitor);
            }
          }
        }
        catch (Exception e) {
          ScoutSdkUi.logWarning("Unable to refresh explorer page '" + pageToRefresh.getName() + "'.", e);
        }
      }
    });
  }

  protected static class P_TechProductFile {
    public IFile productFile;
    public IScoutBundle bundle;
  }
}
