package empty.project.ui.swt;

import org.eclipse.scout.rt.ui.swt.ISwtEnvironment;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import empty.project.client.ClientSession;
import empty.project.ui.swt.perspective.Perspective;
import empty.project.ui.swt.views.CenterView;
import empty.project.ui.swt.views.DetailView;
import empty.project.ui.swt.views.EastView;
import empty.project.ui.swt.views.OutlineView;
import empty.project.ui.swt.views.SearchView;
import empty.project.ui.swt.views.TableView;

public class Activator implements BundleActivator {

  // the plugin id
  public static final String BUNDLE_ID = "empty.project.ui.swt";

  // all view ID's commodity to access.
  public static final String CENTER_VIEW_ID = CenterView.class.getName();
  public static final String DETAIL_VIEW_ID = DetailView.class.getName();
  public static final String EAST_VIEW_ID = EastView.class.getName();
  public static final String OUTLINE_VIEW_ID = OutlineView.class.getName();
  public static final String TABLE_VIEW_ID = TableView.class.getName();
  public static final String SEARCH_VIEW_ID = SearchView.class.getName();

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
