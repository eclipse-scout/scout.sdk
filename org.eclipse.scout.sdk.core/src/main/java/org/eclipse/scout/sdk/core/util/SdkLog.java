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
package org.eclipse.scout.sdk.core.util;

import java.util.logging.Level;

import org.apache.commons.lang3.StringUtils;

public final class SdkLog {

  public static final String LOG_LEVEL_PROPERTY_NAME = "scout.sdk.loglevel";
  private static final Level DEFAULT_LOG_LEVEL = Level.WARNING;

  private SdkLog() {
  }

  public static void log(Level level, String msg, Throwable... throwables) {
    if (level == null) {
      level = DEFAULT_LOG_LEVEL;
    }

    if (level.intValue() < getLogLevel() || level.intValue() == Level.OFF.intValue()) {
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

  public static int getLogLevel() {
    String lvl = System.getProperty(LOG_LEVEL_PROPERTY_NAME);
    if (StringUtils.isBlank(lvl)) {
      return DEFAULT_LOG_LEVEL.intValue();
    }

    try {
      Level parsedLevel = Level.parse(lvl);
      if (parsedLevel != null) {
        return parsedLevel.intValue();
      }
    }
    catch (Exception e) {
      SdkConsole.println("Unable to parse log level '" + lvl + "'. Fallback to default: '" + DEFAULT_LOG_LEVEL.getName() + "'.", e);
    }

    return DEFAULT_LOG_LEVEL.intValue();
  }
}
