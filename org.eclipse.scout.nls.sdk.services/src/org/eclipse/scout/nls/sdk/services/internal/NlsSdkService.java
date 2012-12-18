package org.eclipse.scout.nls.sdk.services.internal;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class NlsSdkService extends AbstractUIPlugin {

  private static NlsSdkService plugin;
  public static final String PLUGIN_ID = "org.eclipse.scout.nls.sdk.services";

  static NlsSdkService getDefault() {
    return plugin;
  }

  /*
   * (non-Javadoc)
   * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
   */
  @Override
  public void start(BundleContext bundleContext) throws Exception {
    plugin = this;
  }

  /*
   * (non-Javadoc)
   * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
   */
  @Override
  public void stop(BundleContext bundleContext) throws Exception {
    plugin = null;
  }
}
