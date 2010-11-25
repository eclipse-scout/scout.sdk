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
package org.eclipse.scout.sdk.internal.test;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.scout.sdk.LogStatus;
import org.eclipse.scout.sdk.ScoutSdk;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class ScoutSdkTest extends Plugin {

  // The plug-in ID
  public static final String PLUGIN_ID = "org.eclipse.scout.sdk.test";

  // The shared instance
  private static ScoutSdkTest plugin;

  /**
   * The constructor
   */
  public ScoutSdkTest() {
  }

  public static void log(IStatus log) {
    if (log instanceof LogStatus) {
      logImpl((LogStatus) log);
    }
    else {
      logImpl(new LogStatus(ScoutSdk.class, log.getSeverity(), log.getPlugin(), log.getMessage(), log.getException()));
    }
  }

  private static void logImpl(LogStatus log) {
    getDefault().getLog().log(log);
  }

  public static void logInfo(String message) {
    logInfo(message, null);
  }

  public static void logInfo(String message, Throwable t) {
    if (message == null) {
      message = "";
    }
    logImpl(new LogStatus(ScoutSdk.class, IStatus.INFO, PLUGIN_ID, message, t));
  }

  public static void logWarning(String message) {
    logWarning(message, null);
  }

  public static void logWarning(Throwable t) {
    logWarning(null, t);
  }

  public static void logWarning(String message, Throwable t) {
    if (message == null) {
      message = "";
    }
    logImpl(new LogStatus(ScoutSdk.class, IStatus.WARNING, PLUGIN_ID, message, t));
  }

  public static void logError(Throwable t) {
    logError("", t);
  }

  public static void logError(String message) {
    logError(message, null);
  }

  public static void logError(String message, Throwable t) {
    if (message == null) {
      message = "";
    }
    logImpl(new LogStatus(ScoutSdk.class, IStatus.ERROR, PLUGIN_ID, message, t));
  }

  @Override
  public void start(BundleContext context) throws Exception {
    super.start(context);
    plugin = this;
  }

  @Override
  public void stop(BundleContext context) throws Exception {
    plugin = null;
    super.stop(context);
  }

  /**
   * Returns the shared instance
   * 
   * @return the shared instance
   */
  public static ScoutSdkTest getDefault() {
    return plugin;
  }

}
