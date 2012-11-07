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
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.pde.internal.core.ICoreConstants;
import org.eclipse.scout.commons.TriState;
import org.eclipse.scout.commons.holders.BooleanHolder;
import org.eclipse.scout.sdk.compatibility.License;
import org.eclipse.scout.sdk.compatibility.P2Utility;
import org.eclipse.scout.sdk.ui.fields.bundletree.TreeUtility;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.internal.dialog.LicenseDialog;
import org.eclipse.scout.sdk.ui.view.outline.IScoutExplorerPart;
import org.eclipse.scout.sdk.ui.view.outline.pages.INodeVisitor;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.util.jdt.JdtUtility;
import org.eclipse.scout.sdk.util.log.ScoutStatus;
import org.eclipse.scout.sdk.util.pde.PluginModelHelper;
import org.eclipse.scout.sdk.util.pde.ProductFileModelHelper;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.IScoutBundleFilter;
import org.eclipse.scout.sdk.workspace.IScoutProject;

/**
 * <h3>{@link AbstractScoutTechnologyHandler}</h3> ...
 * 
 * @author mvi
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
  public IScoutTechnologyResource[] getModifactionResourceCandidates(IScoutProject project) {
    ArrayList<IScoutTechnologyResource> ret = new ArrayList<IScoutTechnologyResource>();
    contributeResources(project, ret);
    return ret.toArray(new IScoutTechnologyResource[ret.size()]);
  }

  protected abstract void contributeResources(IScoutProject project, List<IScoutTechnologyResource> list);

  protected void contributeProductFiles(IScoutProject project, List<IScoutTechnologyResource> list, String... bundleFilter) {
    contributeProductFiles(project, list, true, bundleFilter);
  }

  protected void contributeProductFiles(IScoutProject project, List<IScoutTechnologyResource> list, boolean defaultSelection, String... bundleFilter) {
    P_TechProductFile[] productFiles = getFilteredProductFiles(project, bundleFilter);
    for (int i = 0; i < productFiles.length; i++) {
      P_TechProductFile prodFile = productFiles[i];
      list.add(new ScoutTechnologyResource(prodFile.bundle, prodFile.productFile, defaultSelection));
    }
  }

  protected void contributeManifestFile(IScoutBundle bundle, List<IScoutTechnologyResource> list) {
    if (bundle != null) {
      IScoutTechnologyResource res = getManifestResource(bundle);
      if (res.getResource() != null) {
        list.add(res);
      }
    }
  }

  protected TriState getSelectionManifest(IScoutBundle project, String... pluginIds) {
    if (pluginIds == null || pluginIds.length == 0) {
      return TriState.FALSE;
    }

    PluginModelHelper pluginModel = new PluginModelHelper(project.getProject());
    TriState ret = TriState.parseTriState(pluginModel.Manifest.existsDependency(pluginIds[0]));
    for (int i = 1; i < pluginIds.length; i++) {
      TriState tmp = TriState.parseTriState(pluginModel.Manifest.existsDependency(pluginIds[i]));
      if (tmp != ret) {
        return TriState.UNDEFINED;
      }
    }
    return ret;
  }

  protected TriState getSelectionProductFiles(IScoutProject project, String[] filterPluginIds, String[]... pluginIds) {
    P_TechProductFile[] productFiles = getFilteredProductFiles(project, filterPluginIds);
    if (productFiles == null || productFiles.length == 0) {
      return TriState.FALSE;
    }

    TriState ret = TriState.parseTriState(containsProductDependencies(productFiles[0].productFile, pluginIds));
    for (int i = 1; i < productFiles.length; i++) {
      TriState tmp = TriState.parseTriState(containsProductDependencies(productFiles[i].productFile, pluginIds));
      if (ret != tmp) {
        return TriState.UNDEFINED;
      }
    }
    return ret;
  }

  @Override
  public boolean preSelectionChanged(boolean selected, IProgressMonitor monitor) throws CoreException {
    return true;
  }

  @Override
  public void postSelectionChanged(boolean selected, IProgressMonitor monitor) throws CoreException {
  }

  protected void selectionChangedProductFiles(IScoutTechnologyResource[] resources, boolean selected, String[]... pluginIds) throws CoreException {
    for (IScoutTechnologyResource r : resources) {
      selectionChangedProductFiles(r, selected, pluginIds);
    }
  }

  protected void selectionChangedProductFiles(IScoutTechnologyResource r, boolean selected, String[]... pluginIds) throws CoreException {
    ProductFileModelHelper h = new ProductFileModelHelper(r.getResource());
    for (String[] list : pluginIds) {
      for (String pluginId : list) {
        if (selected) {
          h.ProductFile.addDependency(pluginId);
        }
        else {
          h.ProductFile.removeDependency(pluginId);
        }
      }
    }
    h.save();
  }

  protected void selectionChangedManifest(IScoutTechnologyResource[] resources, boolean selected, String... pluginsToHandle) throws CoreException {
    for (IScoutTechnologyResource r : resources) {
      PluginModelHelper pluginModel = new PluginModelHelper(r.getBundle().getProject());
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
  }

  protected boolean containsProductDependencies(IFile productFile, String[]... pluginFilters) {
    try {
      ProductFileModelHelper h = new ProductFileModelHelper(productFile);
      for (String[] list : pluginFilters) {
        for (String pluginId : list) {
          if (!h.ProductFile.existsDependency(pluginId)) {
            return false;
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

  protected P_TechProductFile[] getProductFiles(IScoutProject project) {
    IFile[] productFiles = TreeUtility.getAllProductFiles(project);
    ArrayList<P_TechProductFile> list = new ArrayList<P_TechProductFile>(productFiles.length);
    for (int i = 0; i < productFiles.length; i++) {
      final IFile prodFile = productFiles[i];
      IScoutBundle[] b = project.getBundles(new IScoutBundleFilter() {
        @Override
        public boolean accept(IScoutBundle bundle) {
          return bundle.getProject().equals(prodFile.getProject());
        }
      });

      if (b != null && b.length > 0) {
        P_TechProductFile tpf = new P_TechProductFile();
        tpf.bundle = b[0];
        tpf.productFile = prodFile;
        list.add(tpf);
      }
    }
    return list.toArray(new P_TechProductFile[list.size()]);
  }

  protected IScoutTechnologyResource getManifestResource(IScoutBundle bundle) {
    return new ScoutTechnologyResource(bundle, bundle.getProject().getFile(ICoreConstants.BUNDLE_FILENAME_DESCRIPTOR));
  }

  protected P_TechProductFile[] getFilteredProductFiles(IScoutProject project, String... pluginFilter) {
    P_TechProductFile[] candidates = getProductFiles(project);
    ArrayList<P_TechProductFile> ret = new ArrayList<P_TechProductFile>(candidates.length);
    for (P_TechProductFile candidate : candidates) {
      if (containsProductDependencies(candidate.productFile, pluginFilter)) {
        ret.add(candidate);
      }
    }
    return ret.toArray(new P_TechProductFile[ret.size()]);
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
    if (JdtUtility.areAllPluginsInstalled(definingPlugins)) {
      return FeatureInstallResult.InstallationNotNecessary;
    }

    try {
      final BooleanHolder licAccepted = new BooleanHolder(false);
      final URI uri = new URI(featureUrl);
      final Map<String, License[]> lic = P2Utility.getLicense(featureId, uri, monitor);
      ScoutSdkUi.getDisplay().syncExec(new Runnable() {
        @Override
        public void run() {
          LicenseDialog licDialog = new LicenseDialog(ScoutSdkUi.getShell(), lic);
          if (licDialog.open() == Dialog.OK) {
            licAccepted.setValue(true);
          }
        }
      });

      if (licAccepted.getValue()) {
        P2Utility.installUnit(featureId, uri, monitor);
        return FeatureInstallResult.InstallationSuccessful;
      }
      else {
        return FeatureInstallResult.LicenseNotAccepted;
      }
    }
    catch (URISyntaxException e) {
      throw new CoreException(new ScoutStatus(e));
    }
  }

  protected void refreshScoutExplorerPageAsync(final Class<? extends IPage> pageToRefresh) {
    ScoutSdkUi.getDisplay().asyncExec(new Runnable() {
      @Override
      public void run() {
        IScoutExplorerPart explorer = ScoutSdkUi.getExplorer(false);
        if (explorer != null) {
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
          explorer.getRootPage().accept(visitor);
        }
      }
    });
  }

  protected static class P_TechProductFile {
    public IFile productFile;
    public IScoutBundle bundle;
  }
}
