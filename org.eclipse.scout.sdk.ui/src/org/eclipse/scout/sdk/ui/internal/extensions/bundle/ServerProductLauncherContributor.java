package org.eclipse.scout.sdk.ui.internal.extensions.bundle;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.ui.extensions.bundle.IProductLauncherContributor;
import org.eclipse.scout.sdk.ui.internal.view.properties.model.links.LinksPresenterModel;
import org.eclipse.scout.sdk.ui.internal.view.properties.model.links.UrlOpenLink;
import org.eclipse.scout.sdk.util.pde.ProductFileModelHelper;

public class ServerProductLauncherContributor implements IProductLauncherContributor {

  public final static String JETTY_PORT_CONFIG_KEY = "org.eclipse.equinox.http.jetty.http.port";
  public final static String JETTY_PATH_CONFIG_KEY = "org.eclipse.equinox.http.jetty.context.path";

  public ServerProductLauncherContributor() {
  }

  public static String getJettyBaseUrl(IFile productFile) throws CoreException {
    ProductFileModelHelper pfmh = new ProductFileModelHelper(productFile);
    String port = StringUtility.trim(pfmh.ConfigurationFile.getEntry(JETTY_PORT_CONFIG_KEY));
    String path = StringUtility.trim(pfmh.ConfigurationFile.getEntry(JETTY_PATH_CONFIG_KEY));
    final String URL_DELIM = "/";
    if (StringUtility.hasText(port)) {
      if (path == null || !StringUtility.hasText(path)) {
        path = URL_DELIM;
      }
      else {
        if (!path.startsWith(URL_DELIM)) {
          path = URL_DELIM + path;
        }
        if (!path.endsWith(URL_DELIM)) {
          path = path + URL_DELIM;
        }
      }
      return "http://localhost:" + port + path;
    }
    return null;
  }

  @Override
  public void contributeLinks(IFile productFile, LinksPresenterModel model) throws CoreException {
    String baseUrl = getJettyBaseUrl(productFile);
    if (baseUrl != null) {
      model.addGlobalLink(new UrlOpenLink("Start Page", baseUrl, 30));
      model.addGlobalLink(new UrlOpenLink("Process Servlet", baseUrl + "process", 40));
    }
  }

  @Override
  public void refreshLaunchState(String mode) {
  }
}
