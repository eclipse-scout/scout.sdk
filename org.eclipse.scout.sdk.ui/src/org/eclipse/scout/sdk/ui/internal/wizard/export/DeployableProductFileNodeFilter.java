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
package org.eclipse.scout.sdk.ui.internal.wizard.export;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.scout.sdk.ui.fields.bundletree.ITreeNode;
import org.eclipse.scout.sdk.ui.fields.bundletree.ITreeNodeFilter;
import org.eclipse.scout.sdk.ui.fields.bundletree.TreeUtility;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.util.pde.ProductFileModelHelper;

public class DeployableProductFileNodeFilter implements ITreeNodeFilter {

  public static final String BUNDLE_ID_HTTP_REGISTRY = "org.eclipse.equinox.http.registry";
  public static final String BUNDLE_ID_HTTP_SERVLETBRIDGE = "org.eclipse.equinox.http.servletbridge";

  private final String m_bundleSymbolicNameFilter;

  public DeployableProductFileNodeFilter(String bundleSymbolicNameFilter) {
    m_bundleSymbolicNameFilter = bundleSymbolicNameFilter;
  }

  @Override
  public boolean accept(ITreeNode node) {
    if (TreeUtility.TYPE_PRODUCT_NODE.equals(node.getType())) {
      IFile productFile = (IFile) node.getData();
      try {
        ProductFileModelHelper pfmh = new ProductFileModelHelper(productFile);
        return pfmh.ProductFile.existsDependency(m_bundleSymbolicNameFilter) && getServletBridgeProductStatus(pfmh).isOK();
      }
      catch (CoreException e) {
        ScoutSdkUi.logError("Unable to parse product '" + productFile.getFullPath().toOSString() + "'.", e);
        return false;
      }
    }
    else {
      return true;
    }
  }

  /**
   * @param productFile
   * @return {@link Status#OK_STATUS} if the given product is valid to deploy on a app server using the servlet bridge
   * @throws CoreException
   */
  private IStatus getServletBridgeProductStatus(ProductFileModelHelper h) throws CoreException {
    if (!h.ProductFile.isValid()) {
      return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, "product file is not valid.");
    }

    // check required plug-ins
    if (!h.ProductFile.existsDependency(BUNDLE_ID_HTTP_REGISTRY)) {
      return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, "product must contain '" + BUNDLE_ID_HTTP_REGISTRY + "' as required bundle.");
    }
    if (!h.ProductFile.existsDependency(BUNDLE_ID_HTTP_SERVLETBRIDGE)) {
      return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, "product must contain '" + BUNDLE_ID_HTTP_SERVLETBRIDGE + "' as required bundle.");
    }

    // check osgi.bundles entries
    String osgiBundleEntry = h.ConfigurationFile.getOsgiBundlesEntry();
    if (osgiBundleEntry == null) {
      return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, "osgi.bundles entry in config.ini is missing.");
    }
    else {
      // org.eclipse.equinox.common@2:start, org.eclipse.update.configurator@start, org.eclipse.equinox.http.servletbridge@start, org.eclipse.equinox.http.registry@start, org.eclipse.core.runtime@start
      if (!osgiBundleEntry.contains(BUNDLE_ID_HTTP_SERVLETBRIDGE)) {
        return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, "osgi.bundles entry in config.ini file must conatin '" + BUNDLE_ID_HTTP_SERVLETBRIDGE + "' bundle.");
      }
      if (!osgiBundleEntry.contains(BUNDLE_ID_HTTP_REGISTRY)) {
        return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, "osgi.bundles entry in config.ini file must conatin '" + BUNDLE_ID_HTTP_REGISTRY + "' bundle.");
      }
    }
    return Status.OK_STATUS;
  }
}
