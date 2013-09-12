package org.eclipse.scout.sdk.ui.internal.extensions.bundle;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.ui.extensions.bundle.IProductLauncherContributor;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.internal.view.properties.model.links.AbstractLink;
import org.eclipse.scout.sdk.ui.internal.view.properties.model.links.LinksPresenterModel;
import org.eclipse.scout.sdk.ui.view.properties.presenter.single.ProductLaunchPresenter;
import org.eclipse.scout.sdk.util.pde.ProductFileModelHelper;
import org.eclipse.scout.sdk.util.resources.ResourceUtility;

public class ServerProductLauncherContributor implements IProductLauncherContributor {

  public final static String JETTY_PORT_CONFIG_KEY = "org.eclipse.equinox.http.jetty.http.port";
  public final static String JETTY_PATH_CONFIG_KEY = "org.eclipse.equinox.http.jetty.context.path";

  private JettyProductUrlOpenLink m_startPageLink;
  private JettyProductUrlOpenLink m_processServletLink;

  public ServerProductLauncherContributor() {
  }

  @Override
  public void contributeLinks(final IFile productFile, LinksPresenterModel model) throws CoreException {
    boolean isJettyConfigured = getJettyBaseUrl(productFile) != null;
    if (isJettyConfigured) {
      m_startPageLink = new JettyProductUrlOpenLink("Start Page", productFile, "", 30);
      m_processServletLink = new JettyProductUrlOpenLink("Process Servlet", productFile, "process", 40);
      model.addGlobalLink(m_startPageLink);
      model.addGlobalLink(m_processServletLink);
    }
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

  public static class JettyProductUrlOpenLink extends AbstractLink {
    private final String m_suffix;
    private final IFile m_productFile;
    private boolean m_enabled;

    public JettyProductUrlOpenLink(String name, IFile productFile, String suffix, int order) {
      super(name, ScoutSdkUi.getImage(ScoutSdkUi.Web), order);
      m_suffix = suffix;
      m_productFile = productFile;
      setEnabled(true);
    }

    @Override
    public void execute() {
      if (isEnabled()) {
        try {
          String baseUrl = getJettyBaseUrl(m_productFile);
          ResourceUtility.showUrlInBrowser(baseUrl + m_suffix);
        }
        catch (CoreException e) {
          ScoutSdkUi.logError("Unable to parse Jetty URL.", e);
        }
      }
    }

    public boolean isEnabled() {
      return m_enabled;
    }

    public void setEnabled(boolean enabled) {
      m_enabled = enabled;
      if (isEnabled()) {
        setImage(ScoutSdkUi.getImage(ScoutSdkUi.Web));
      }
      else {
        setImage(ScoutSdkUi.getImage(ScoutSdkUi.WebDisabled));
      }
    }
  }

  @Override
  public void refreshLaunchState(String mode) {
    if (m_startPageLink != null && m_processServletLink != null) {
      boolean running = !ProductLaunchPresenter.TERMINATED_MODE.equals(mode);
      m_startPageLink.setEnabled(running);
      m_processServletLink.setEnabled(running);
    }
  }
}
