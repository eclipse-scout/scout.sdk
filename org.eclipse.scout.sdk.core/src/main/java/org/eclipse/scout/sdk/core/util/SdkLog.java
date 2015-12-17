/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.core.util;

import java.util.logging.Level;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

public final class SdkLog {

  public static final String LOG_LEVEL_PROPERTY_NAME = "org.eclipse.scout.sdk.propLogLevel";
  public static final Level DEFAULT_LOG_LEVEL = Level.WARNING;
  private static volatile Level curLevel = getInitialLogLevel();

  private SdkLog() {
  }

  public static void log(Level level, String msg, Throwable... throwables) {
    if (level == null) {
      level = DEFAULT_LOG_LEVEL;
    }

    if (level.intValue() < curLevel.intValue() || level.intValue() == Level.OFF.intValue()) {
      return;
    }

    String prefix = new StringBuilder().append('[').append(level.getName()).append("]: ").toString();

    if (StringUtils.isBlank(msg)) {
      msg = prefix;
    }
    else {
      msg = prefix + msg;
    }
    SdkConsole.println(msg, throwables);
  }

  public static void log(Level level, Throwable... throwables) {
    log(level, null, throwables);
  }

  public static void debug(Throwable... throwables) {
    debug(null, throwables);
  }

  public static void debug(String message, Throwable... throwables) {
    log(Level.FINE, message, throwables);
  }

  public static void info(Throwable... throwables) {
    info(null, throwables);
  }

  public static void info(String message, Throwable... throwables) {
    log(Level.INFO, message, throwables);
  }

  public static void warning(Throwable... throwables) {
    warning(null, throwables);
  }

  public static void warning(String message, Throwable... throwables) {
    log(Level.WARNING, message, throwables);
  }

  public static void error(Throwable... throwables) {
    error("", throwables);
  }

  public static void error(String message, Throwable... throwables) {
    log(Level.SEVERE, message, throwables);
  }

  public static Level getLogLevel() {
    return curLevel;
  }

  public static void setLogLevel(Level newLevel) {
    curLevel = Validate.notNull(newLevel);
  }

  public static void setLogLevel(String newLevelName) {
    curLevel = parseLevel(newLevelName);
  }

  public static void setDefaultLogLevel() {
    setLogLevel(getInitialLogLevel());
  }

  private static Level parseLevel(String lvl) {
    if (StringUtils.isBlank(lvl)) {
      return DEFAULT_LOG_LEVEL;
    }

    try {
      Level parsedLevel = Level.parse(lvl);
      if (parsedLevel != null) {
        return parsedLevel;
      }
    }
    catch (Exception e) {
      SdkConsole.println("Unable to parse log level '" + lvl + "'. Fallback to default: '" + DEFAULT_LOG_LEVEL.getName() + "'.", e);
    }
    return DEFAULT_LOG_LEVEL;
  }

  private static Level getInitialLogLevel() {
    String lvl = System.getProperty(LOG_LEVEL_PROPERTY_NAME);
    if (StringUtils.isBlank(lvl)) {
      return DEFAULT_LOG_LEVEL;
    }
    return parseLevel(lvl);
  }
}
