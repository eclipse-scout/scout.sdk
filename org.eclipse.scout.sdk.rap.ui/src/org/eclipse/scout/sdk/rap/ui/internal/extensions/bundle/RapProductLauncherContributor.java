package org.eclipse.scout.sdk.rap.ui.internal.extensions.bundle;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.scout.sdk.ui.extensions.bundle.IProductLauncherContributor;
import org.eclipse.scout.sdk.ui.internal.extensions.bundle.ServerProductLauncherContributor;
import org.eclipse.scout.sdk.ui.internal.view.properties.model.links.LinksPresenterModel;
import org.eclipse.scout.sdk.ui.internal.view.properties.model.links.UrlOpenLink;

public class RapProductLauncherContributor implements IProductLauncherContributor {

  public RapProductLauncherContributor() {
  }

  @Override
  public void contributeLinks(IFile productFile, LinksPresenterModel model) throws CoreException {
    String baseUrl = ServerProductLauncherContributor.getJettyBaseUrl(productFile);
    if (baseUrl != null) {
      model.addGlobalLink(new UrlOpenLink("Automatic Device Dispatch", baseUrl, 30));
      model.addGlobalLink(new UrlOpenLink("Desktop Devices", baseUrl + "web", 40));
      model.addGlobalLink(new UrlOpenLink("Smartphone Devices", baseUrl + "mobile", 50));
      model.addGlobalLink(new UrlOpenLink("Tablet Devices", baseUrl + "tablet", 60));
    }
  }

  @Override
  public void refreshLaunchState(String mode) {
  }

}
