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
package org.eclipse.scout.sdk.util.log;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.scout.commons.StringUtility;
import org.osgi.framework.BundleContext;

public class SdkLogManager {

  private static final String LOG_LEVEL_SUFFIX = ".loglevel";

  private final int m_logLevel;
  private final Plugin m_plugin;

  public SdkLogManager(Plugin p) {
    this(p, getLogLevelProperty(p));
  }

  public SdkLogManager(Plugin p, String logLevel) {
    this(p, parseLogLevel(logLevel));
  }

  public SdkLogManager(Plugin p, int logLevel) {
    m_plugin = p;
    m_logLevel = logLevel;
  }

  protected IStatus createStatus(IStatus log) {
    if (log instanceof LogStatus) {
      return log;
    }
    else {
      return createStatus(log.getSeverity(), log.getMessage(), log.getException());
    }
  }

  protected IStatus createStatus(int severity, String message, Throwable t) {
    String msg = message;
    if (msg == null) {
      msg = "";
    }
    return new LogStatus(getPlugin().getClass(), severity, getPlugin().getBundle().getSymbolicName(), msg, t);
  }

  private void logImpl(IStatus log) {
    if ((log.getSeverity() & getLogLevel()) != 0) {
      getPlugin().getLog().log(log);
    }

  }

  public void log(IStatus log) {
    logImpl(createStatus(log));
  }

  public void logInfo(Throwable t) {
    logInfo(null, t);
  }

  public void logInfo(String message) {
    logInfo(message, null);
  }

  public void logInfo(String message, Throwable t) {
    logImpl(createStatus(IStatus.INFO, message, t));
  }

  public void logWarning(String message) {
    logWarning(message, null);
  }

  public void logWarning(Throwable t) {
    logWarning(null, t);
  }

  public void logWarning(String message, Throwable t) {
    logImpl(createStatus(IStatus.WARNING, message, t));
  }

  public void logError(Throwable t) {
    logError("", t);
  }

  public void logError(String message) {
    logError(message, null);
  }

  public void logError(String message, Throwable t) {
    logImpl(createStatus(IStatus.ERROR, message, t));
  }

  /**
   * @see SdkLogManager#log(int, String, Throwable)
   */
  public void log(int logLevel, Throwable t) {
    log(logLevel, "", t);
  }

  /**
   * @see SdkLogManager#log(int, String, Throwable)
   */
  public void log(int logLevel, String message) {
    log(logLevel, message, null);
  }

  /**
   * @param logLevel
   *          one of {@link IStatus#INFO}, {@link IStatus#WARNING} or {@link IStatus#ERROR}
   * @param message
   * @param t
   */
  public void log(int logLevel, String message, Throwable t) {
    logImpl(createStatus(logLevel, message, t));
  }

  private static String getLogLevelProperty(Plugin p) {
    if (p != null && p.getBundle() != null) {
      return getLogLevelProperty(p.getBundle().getBundleContext());
    }
    return null;
  }

  private static String getLogLevelProperty(BundleContext context) {
    if (context == null) return null;
    else return context.getProperty(context.getBundle().getSymbolicName() + LOG_LEVEL_SUFFIX);
  }

  private static int parseLogLevel(String loglevel) {
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

  protected int getLogLevel() {
    return m_logLevel;
  }

  protected Plugin getPlugin() {
    return m_plugin;
  }
}
