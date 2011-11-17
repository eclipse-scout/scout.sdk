package org.eclipse.scout.sdk.util.log;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.scout.commons.StringUtility;
import org.osgi.framework.BundleContext;

public class SdkLogManager {

  private static final String LOG_LEVEL_SUFFIX = ".loglevel";

  private int m_loglevel;
  private final Plugin m_plugin;

  public SdkLogManager(Plugin p) {
    this(p, getLogLevelProperty(p));
  }

  public SdkLogManager(Plugin p, String logLevel) {
    this(p, parseLogLevel(logLevel));
  }

  public SdkLogManager(Plugin p, int logLevel) {
    m_plugin = p;
    m_loglevel = logLevel;
  }

  private LogStatus createLogStatus(int severity, String message, Throwable t) {
    return new LogStatus(m_plugin.getClass(), IStatus.INFO, m_plugin.getBundle().getSymbolicName(), message, t);
  }

  private LogStatus createLogStatus(IStatus log) {
    if (log instanceof LogStatus) {
      return (LogStatus) log;
    }
    else {
      return new LogStatus(m_plugin.getClass(), log.getSeverity(), log.getPlugin(), log.getMessage(), log.getException());
    }
  }

  private void logImpl(LogStatus log) {
    if ((log.getSeverity() & m_loglevel) != 0) {
      m_plugin.getLog().log(log);
    }
  }

  public void log(IStatus log) {
    logImpl(createLogStatus(log));
  }

  public void logInfo(Throwable t) {
    logInfo(null, t);
  }

  public void logInfo(String message) {
    logInfo(message, null);
  }

  public void logInfo(String message, Throwable t) {
    if (message == null) {
      message = "";
    }
    logImpl(createLogStatus(IStatus.INFO, message, t));
  }

  public void logWarning(String message) {
    logWarning(message, null);
  }

  public void logWarning(Throwable t) {
    logWarning(null, t);
  }

  public void logWarning(String message, Throwable t) {
    if (message == null) {
      message = "";
    }
    logImpl(createLogStatus(IStatus.WARNING, message, t));
  }

  public void logError(Throwable t) {
    logError("", t);
  }

  public void logError(String message) {
    logError(message, null);
  }

  public void logError(String message, Throwable t) {
    if (message == null) {
      message = "";
    }
    logImpl(createLogStatus(IStatus.ERROR, message, t));
  }

  private static String getLogLevelProperty(Plugin p) {
    if (p != null && p.getBundle() != null) {
      return getLogLevelProperty(p.getBundle().getBundleContext());
    }
    else {
      return null;
    }
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
}
