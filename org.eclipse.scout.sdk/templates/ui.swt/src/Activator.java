package @@BUNDLE_SWT_NAME@@;

import org.eclipse.scout.rt.ui.swt.ISwtEnvironment;
import @@BUNDLE_CLIENT_NAME@@.ClientSession;
import @@BUNDLE_SWT_NAME@@.perspective.Perspective;
import @@BUNDLE_SWT_NAME@@.views.CenterView;
import @@BUNDLE_SWT_NAME@@.views.DetailView;
import @@BUNDLE_SWT_NAME@@.views.EastView;
import @@BUNDLE_SWT_NAME@@.views.OutlineView;
import @@BUNDLE_SWT_NAME@@.views.SearchView;
import @@BUNDLE_SWT_NAME@@.views.TableView;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator{

  // the plugin id
  public static final String BUNDLE_ID = "@@BUNDLE_SWT_NAME@@";

  private ISwtEnvironment m_environment;

  // the shared instance
  private static Activator m_bundle;

  @Override
  public void start(BundleContext context) throws Exception {
    m_bundle = this;
    m_environment = new SwtEnvironment(context.getBundle(), Perspective.ID, ClientSession.class);
  }

  @Override
  public void stop(BundleContext context) throws Exception {
    m_bundle = null;
  }

  public static Activator getDefault() {
    return m_bundle;
  }

  public ISwtEnvironment getEnvironment() {
    return m_environment;
  }
}
