package org.eclipse.scout.sdk.help.internal;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

public class ScoutSdkHelpActivator extends Plugin {

  private static ScoutSdkHelpActivator instance;

  public static ScoutSdkHelpActivator getInstance() {
    return instance;
  }

  @Override
  public void start(BundleContext bundleContext) throws Exception {
    instance = this;
  }

  @Override
  public void stop(BundleContext bundleContext) throws Exception {
    instance = null;
  }
}
