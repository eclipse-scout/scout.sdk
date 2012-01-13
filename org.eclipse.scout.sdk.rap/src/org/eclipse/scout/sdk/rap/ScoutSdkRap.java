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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.util.log.LogStatus;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class ScoutSdkRap extends Plugin {

  public static final String PLUGIN_ID = "org.eclipse.scout.sdk.rap";
  private static final String LOG_LEVEL = PLUGIN_ID + ".loglevel";

  private static ScoutSdkRap plugin;
  private int m_loglevel;

  @Override
  public void start(BundleContext bundleContext) throws Exception {
    super.start(bundleContext);
    m_loglevel = parseLogLevel(bundleContext.getProperty(LOG_LEVEL));
    plugin = this;
  }

  @Override
  public void stop(BundleContext bundleContext) throws Exception {
    super.stop(bundleContext);
    plugin = null;
  }

  public static ScoutSdkRap getDefault() {
    return plugin;
  }

  private int parseLogLevel(String loglevel) {
    int level = IStatus.INFO | IStatus.WARNING | IStatus.ERROR | IStatus.CANCEL;
    if (!StringUtility.isNullOrEmpty(loglevel)) {
      String lowerLoglevel = loglevel.toLowerCase();
      if (lowerLoglevel.equals("warning")) {
        level = IStatus.WARNING | IStatus.ERROR | IStatus.CANCEL;
      }
      else if (lowerLoglevel.equals("error")) {
        level = IStatus.ERROR | IStatus.CANCEL;
      }
      else if (lowerLoglevel.equals("cancel")) {
        level = IStatus.CANCEL;
      }
    }
    return level;
  }

  public static void log(IStatus log) {
    if (log instanceof LogStatus) {
      getDefault().logImpl((LogStatus) log);
    }
    else {
      getDefault().logImpl(new LogStatus(ScoutSdkRap.class, log.getSeverity(), log.getPlugin(), log.getMessage(), log.getException()));
    }
  }

  private void logImpl(LogStatus log) {
    if ((log.getSeverity() & m_loglevel) != 0) {
      getLog().log(log);
    }
  }

  public static void logInfo(String message) {
    logInfo(message, null);
  }

  public static void logInfo(String message, Throwable t) {
    if (message == null) {
      message = "";
    }
    getDefault().logImpl(new LogStatus(ScoutSdkRap.class, IStatus.INFO, PLUGIN_ID, message, t));
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
    getDefault().logImpl(new LogStatus(ScoutSdkRap.class, IStatus.WARNING, PLUGIN_ID, message, t));
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
    getDefault().logImpl(new LogStatus(ScoutSdkRap.class, IStatus.ERROR, PLUGIN_ID, message, t));
  }

  /**
   * Returns a service with the specified name or <code>null</code> if none.
   * 
   * @param serviceName
   *          name of service
   * @return service object or <code>null</code> if none
   */
  public Object acquireService(final String serviceName) {
    Object service = null;
    BundleContext context = getDefault().getBundle().getBundleContext();
    ServiceReference reference = context.getServiceReference(serviceName);
    if (reference != null) {
      service = context.getService(reference);
      if (service != null) {
        context.ungetService(reference);
      }
    }
    return service;
  }

}
