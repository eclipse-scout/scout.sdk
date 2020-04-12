/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.slf4j.impl;

import java.util.logging.Level;

import org.eclipse.scout.sdk.core.log.SdkLog;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MarkerIgnoringBase;
import org.slf4j.helpers.MessageFormatter;

/**
 * <h3>{@link SimpleLogger}</h3>
 *
 * @since 5.2.0
 */
public class SimpleLogger extends MarkerIgnoringBase {

  private static final long serialVersionUID = 1L;
  private final String m_name;

  static void init() {
    // required! see org.slf4j.impl.MavenSlf4jSimpleFriend
  }

  public SimpleLogger(String n) {
    m_name = n;
  }

  @Override
  public String getName() {
    return m_name;
  }

  @Override
  public boolean isTraceEnabled() {
    return SdkLog.isDebugEnabled();
  }

  @Override
  public void trace(String msg) {
    log(Level.FINE, msg, null);
  }

  @Override
  public void trace(String format, Object param1) {
    formatAndLog(Level.FINE, format, param1, null);
  }

  @Override
  public void trace(String format, Object param1, Object param2) {
    formatAndLog(Level.FINE, format, param1, param2);
  }

  @Override
  public void trace(String format, Object... argArray) {
    formatAndLog(Level.FINE, format, argArray);
  }

  @Override
  public void trace(String msg, Throwable t) {
    log(Level.FINE, msg, t);
  }

  @Override
  public boolean isDebugEnabled() {
    return SdkLog.isDebugEnabled();
  }

  @Override
  public void debug(String msg) {
    log(Level.FINE, msg, null);
  }

  @Override
  public void debug(String format, Object param1) {
    formatAndLog(Level.FINE, format, param1, null);
  }

  @Override
  public void debug(String format, Object param1, Object param2) {
    formatAndLog(Level.FINE, format, param1, param2);
  }

  @Override
  public void debug(String format, Object... argArray) {
    formatAndLog(Level.FINE, format, argArray);
  }

  @Override
  public void debug(String msg, Throwable t) {
    log(Level.FINE, msg, t);
  }

  @Override
  public boolean isInfoEnabled() {
    return SdkLog.isInfoEnabled();
  }

  @Override
  public void info(String msg) {
    log(Level.INFO, msg, null);
  }

  @Override
  public void info(String format, Object arg) {
    formatAndLog(Level.INFO, format, arg, null);
  }

  @Override
  public void info(String format, Object arg1, Object arg2) {
    formatAndLog(Level.INFO, format, arg1, arg2);
  }

  @Override
  public void info(String format, Object... argArray) {
    formatAndLog(Level.INFO, format, argArray);
  }

  @Override
  public void info(String msg, Throwable t) {
    log(Level.INFO, msg, t);
  }

  @Override
  public boolean isWarnEnabled() {
    return SdkLog.isWarningEnabled();
  }

  @Override
  public void warn(String msg) {
    log(Level.WARNING, msg, null);
  }

  @Override
  public void warn(String format, Object arg) {
    formatAndLog(Level.WARNING, format, arg, null);
  }

  @Override
  public void warn(String format, Object arg1, Object arg2) {
    formatAndLog(Level.WARNING, format, arg1, arg2);
  }

  @Override
  public void warn(String format, Object... argArray) {
    formatAndLog(Level.WARNING, format, argArray);
  }

  @Override
  public void warn(String msg, Throwable t) {
    log(Level.WARNING, msg, t);
  }

  @Override
  public boolean isErrorEnabled() {
    return SdkLog.isErrorEnabled();
  }

  @Override
  public void error(String msg) {
    log(Level.SEVERE, msg, null);
  }

  @Override
  public void error(String format, Object arg) {
    formatAndLog(Level.SEVERE, format, arg, null);
  }

  @Override
  public void error(String format, Object arg1, Object arg2) {
    formatAndLog(Level.SEVERE, format, arg1, arg2);
  }

  @Override
  public void error(String format, Object... argArray) {
    formatAndLog(Level.SEVERE, format, argArray);
  }

  @Override
  public void error(String msg, Throwable t) {
    log(Level.SEVERE, msg, t);
  }

  private static void log(Level level, String message, Throwable t) {
    SdkLog.log(level, message, t);
  }

  private static void formatAndLog(Level level, String format, Object arg1, Object arg2) {
    if (!SdkLog.isLevelEnabled(level)) {
      return;
    }
    FormattingTuple tp = MessageFormatter.format(format, arg1, arg2);
    log(level, tp.getMessage(), tp.getThrowable());
  }

  private static void formatAndLog(Level level, String format, Object... arguments) {
    if (!SdkLog.isLevelEnabled(level)) {
      return;
    }
    FormattingTuple tp = MessageFormatter.arrayFormat(format, arguments);
    log(level, tp.getMessage(), tp.getThrowable());
  }
}
