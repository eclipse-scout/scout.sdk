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

  public final static String BUNDLE_ID_HTTP_REGISTRY = "org.eclipse.equinox.http.registry";
  public final static String BUNDLE_ID_HTTP_SERVLETBRIDGE = "org.eclipse.equinox.http.servletbridge";

  private final int m_nodeType;

  public DeployableProductFileNodeFilter(int nodeType) {
    m_nodeType = nodeType;
  }

  @Override
  public boolean accept(ITreeNode node) {
    if (node.getType() == m_nodeType) {
      return true;
    }
    else if (node.getType() == TreeUtility.TYPE_PRODUCT_NODE) {
      return getServletBridgeProductStatus((IFile) node.getData()).isOK();
    }
    else {
      return false;
    }
  }

  /**
   * @param productFile
   * @return {@link Status#OK_STATUS} if the given product is valid to deploy on a app server using the servlet bridge
   */
  private IStatus getServletBridgeProductStatus(IFile productFile) {
    if (productFile == null) {
      return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, "product file is null.");
    }
    ProductFileModelHelper h = null;
    try {
      h = new ProductFileModelHelper(productFile);

      if (!h.ProductFile.isValid()) {
        return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, "product file is not valid.");
      }

      // check required plugins
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
    }
    catch (CoreException e) {
      return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, "could not parse product file.");
    }
    return Status.OK_STATUS;
  }
}
