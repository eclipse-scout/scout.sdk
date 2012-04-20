/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.rap;

import org.eclipse.core.runtime.Plugin;
import org.eclipse.scout.sdk.util.log.SdkLogManager;
import org.osgi.framework.BundleContext;

public class ScoutSdkRap extends Plugin {

  public static final String PLUGIN_ID = "org.eclipse.scout.sdk.rap";

  private static ScoutSdkRap plugin;
  private static SdkLogManager logManager;

  @Override
  public void start(BundleContext bundleContext) throws Exception {
    super.start(bundleContext);
    plugin = this;
    logManager = new SdkLogManager(this);
  }

  @Override
  public void stop(BundleContext bundleContext) throws Exception {
    super.stop(bundleContext);
    plugin = null;
    logManager = null;
  }

  public static ScoutSdkRap getDefault() {
    return plugin;
  }

  public static void logInfo(Throwable t) {
    logManager.logInfo(t);
  }

  public static void logInfo(String message) {
    logManager.logInfo(message);
  }

  public static void logInfo(String message, Throwable t) {
    logManager.logInfo(message, t);
  }

  public static void logWarning(String message) {
    logManager.logWarning(message);
  }

  public static void logWarning(Throwable t) {
    logManager.logWarning(t);
  }

  public static void logWarning(String message, Throwable t) {
    logManager.logWarning(message, t);
  }

  public static void logError(Throwable t) {
    logManager.logError(t);
  }

  public static void logError(String message) {
    logManager.logError(message);
  }

  public static void logError(String message, Throwable t) {
    logManager.logError(message, t);
  }
}
