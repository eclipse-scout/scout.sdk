/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.testing.internal;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.scout.sdk.util.log.SdkLogManager;
import org.osgi.framework.BundleContext;

public class SdkTestingApi extends Plugin {

  private static BundleContext context;
  private static SdkLogManager logManager;

  public static BundleContext getContext() {
    return context;
  }

  @Override
  public void start(BundleContext bundleContext) throws Exception {
    super.start(bundleContext);
    SdkTestingApi.context = bundleContext;
    logManager = new SdkLogManager(this);
  }

  @Override
  public void stop(BundleContext bundleContext) throws Exception {
    SdkTestingApi.context = null;
    logManager = null;
    super.stop(bundleContext);
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

  /**
   * @see SdkLogManager#log(int, String, Throwable)
   */
  public static void log(int level, Throwable t) {
    logManager.log(level, t);
  }

  /**
   * @see SdkLogManager#log(int, String, Throwable)
   */
  public static void log(int level, String message) {
    logManager.log(level, message);
  }

  /**
   * @see SdkLogManager#log(int, String, Throwable)
   */
  public static void log(int level, String message, Throwable t) {
    logManager.log(level, message, t);
  }

  public static void log(IStatus status) {
    logManager.log(status);
  }
}
