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
package org.eclipse.scout.sdk.core.log;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.Level;

import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.Strings;

/**
 * <h3>{@link SdkLog}</h3>
 *
 * @see SdkConsole
 * @since 5.2.0
 */
public final class SdkLog {

  /**
   * system property for the initial log level
   */
  public static final String LOG_LEVEL_PROPERTY_NAME = "org.eclipse.scout.sdk.propLogLevel";

  /**
   * The default log level: {@link Level#WARNING}.
   */
  public static final Level DEFAULT_LOG_LEVEL = Level.WARNING;

  private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
  static Clock clock = Clock.systemDefaultZone();
  private static volatile Level curLevel = getInitialLogLevel();

  private SdkLog() {
  }

  public static void log(Level level, Object... args) {
    log(level, null, args);
  }

  /**
   * Logs the given message with the given log {@link Level} and given arguments to the {@link SdkConsole}.<br>
   * This method has no effect if the currently active level ({@link #getLogLevel()}) is higher than the given level.
   * <br>
   * <br>
   * <b>Examples:</b><br>
   * <ul>
   * <li>{@code SdkLog.info("Unable to parse value");}</li>
   * <li>{@code SdkLog.error("No more disk space.", myException);}</li>
   * <li>{@code SdkLog.warning("File {} does not exist.", myFile);}</li>
   * <li>{@code SdkLog.error("File {} does not exist.", myFile, myFileNotFoundException, myException);}</li>
   * <li>{@code SdkLog.error(exception);}</li>
   * </ul>
   *
   * @param level
   *          The {@link Level} to log. May be {@code null} in which case the {@link #DEFAULT_LOG_LEVEL} is used.
   * @param msg
   *          The message to log. May be {@code null}.
   * @param args
   *          Arguments to use for logging. May be {@code null}. If the message contains place holders
   *          ({@value MessageFormatter#ARG_REPLACE_PATTERN}), the arguments are used to fill the place holders. If
   *          there are more arguments than place holders in the message, the remaining arguments are searched for
   *          {@link Throwable}s whose stack trace is logged as well (see examples above).
   */
  public static void log(Level level, String msg, Object... args) {
    if (level == null) {
      level = DEFAULT_LOG_LEVEL;
    }

    if (level.intValue() < curLevel.intValue() || level.intValue() == Level.OFF.intValue()) {
      return;
    }

    FormattingTuple tuple = MessageFormatter.arrayFormat(msg, args);
    StringBuilder message = new StringBuilder().append(logTime()).append(" [").append(level.getName()).append("]  ");
    message.append(tuple.message());

    List<Throwable> throwables = tuple.throwables();
    SdkConsole.println(level, message.toString(), throwables.toArray(new Throwable[0]));
  }

  static String logTime() {
    LocalDateTime now = LocalDateTime.now(clock);
    return now.format(TIME_FORMATTER);
  }

  /**
   * Logs the given {@link Throwable}s with {@link Level#FINE}.
   *
   * @param args
   *          The {@link Throwable}s to log. See {@link #log(Level, String, Object...)} for details.
   */
  public static void debug(Object... args) {
    debug(null, args);
  }

  /**
   * Logs the given message with {@link Level#FINE}.
   *
   * @param message
   *          The message. May be {@code null}.
   * @param args
   *          Arguments. See {@link #log(Level, String, Object...)} for details.
   */
  public static void debug(String message, Object... args) {
    log(Level.FINE, message, args);
  }

  /**
   * Logs the given {@link Throwable}s with {@link Level#INFO}.
   *
   * @param args
   *          The {@link Throwable}s to log. See {@link #log(Level, String, Object...)} for details.
   */
  public static void info(Object... args) {
    info(null, args);
  }

  /**
   * Logs the given message with {@link Level#INFO}.
   *
   * @param message
   *          The message. May be {@code null}.
   * @param args
   *          Arguments. See {@link #log(Level, String, Object...)} for details.
   */
  public static void info(String message, Object... args) {
    log(Level.INFO, message, args);
  }

  /**
   * Logs the given {@link Throwable}s with {@link Level#WARNING}.
   *
   * @param args
   *          The {@link Throwable}s to log. See {@link #log(Level, String, Object...)} for details.
   */
  public static void warning(Object... args) {
    warning(null, args);
  }

  /**
   * Logs the given message with {@link Level#WARNING}.
   *
   * @param message
   *          The message. May be {@code null}.
   * @param args
   *          Arguments. See {@link #log(Level, String, Object...)} for details.
   */
  public static void warning(String message, Object... args) {
    log(Level.WARNING, message, args);
  }

  /**
   * Logs the given {@link Throwable}s with {@link Level#SEVERE}.
   *
   * @param args
   *          The {@link Throwable}s to log. See {@link #log(Level, String, Object...)} for details.
   */
  public static void error(Object... args) {
    error("", args);
  }

  /**
   * Logs the given message with {@link Level#SEVERE}.
   *
   * @param message
   *          The message. May be {@code null}.
   * @param args
   *          Arguments. See {@link #log(Level, String, Object...)} for details.
   */
  public static void error(String message, Object... args) {
    log(Level.SEVERE, message, args);
  }

  /**
   * @return The current log {@link Level}.
   */
  public static Level getLogLevel() {
    return curLevel;
  }

  /**
   * Sets the log level to the given value.
   *
   * @param newLevel
   *          the new log {@link Level}.
   */
  public static void setLogLevel(Level newLevel) {
    curLevel = Ensure.notNull(newLevel);
  }

  /**
   * Sets the log level to the given name.<br>
   * If the given name cannot be parsed to a level, the {@link #DEFAULT_LOG_LEVEL} is used.
   *
   * @param newLevelName
   *          one of the {@link Level} constant names.
   */
  public static void setLogLevel(String newLevelName) {
    curLevel = parseLevel(newLevelName);
  }

  /**
   * Resets the logger to the initial log level as specified by the system property {@link #LOG_LEVEL_PROPERTY_NAME}.
   */
  public static void setInitialLogLevel() {
    setLogLevel(getInitialLogLevel());
  }

  /**
   * Checks if at least the given {@link Level} is enabled. If this method returns {@code true} this means that a
   * message with given {@link Level} would be printed.
   *
   * @param level
   *          The level to check.
   * @return {@code true} if at least the given {@link Level} is currently active.
   */
  public static boolean isLevelEnabled(Level level) {
    return level != null && isLevelEnabled(level.intValue());
  }

  /**
   * Checks if at least the given level is enabled. If this method returns {@code true} this means that a message with
   * given {@link Level} would be printed.
   *
   * @param level
   *          The level to check. Must be the {@link Level#intValue()} of one of the constants defined in {@link Level}.
   * @return {@code true} if at least the given {@link Level} is currently active.
   */
  public static boolean isLevelEnabled(int level) {
    return getLogLevel().intValue() <= level;
  }

  /**
   * Checks if the Debug level is currently active. See {@link #isLevelEnabled(Level)}.
   *
   * @return {@code true} if the Debug {@link Level} is currently enabled and would be printed to the log.
   */
  public static boolean isDebugEnabled() {
    return isLevelEnabled(Level.FINE);
  }

  /**
   * Checks if the Info level is currently active. See {@link #isLevelEnabled(Level)}.
   *
   * @return {@code true} if the Info {@link Level} is currently enabled and would be printed to the log.
   */
  public static boolean isInfoEnabled() {
    return isLevelEnabled(Level.INFO);
  }

  /**
   * Checks if the Warning level is currently active. See {@link #isLevelEnabled(Level)}.
   *
   * @return {@code true} if the Warning {@link Level} is currently enabled and would be printed to the log.
   */
  public static boolean isWarningEnabled() {
    return isLevelEnabled(Level.WARNING);
  }

  /**
   * Checks if the Error level is currently active. See {@link #isLevelEnabled(Level)}.
   *
   * @return {@code true} if the Error {@link Level} is currently enabled and would be printed to the log.
   */
  public static boolean isErrorEnabled() {
    return isLevelEnabled(Level.SEVERE);
  }

  static Level parseLevel(String lvl) {
    if (Strings.isBlank(lvl)) {
      return DEFAULT_LOG_LEVEL;
    }

    try {
      Level parsedLevel = Level.parse(lvl);
      if (parsedLevel != null) {
        return parsedLevel;
      }
    }
    catch (Exception e) {
      SdkConsole.println(Level.SEVERE, "Unable to parse log level '" + lvl + "'. Fallback to default: '" + DEFAULT_LOG_LEVEL.getName() + "'.", e);
    }
    return DEFAULT_LOG_LEVEL;
  }

  static Level getInitialLogLevel() {
    String lvl = System.getProperty(LOG_LEVEL_PROPERTY_NAME);
    if (Strings.isBlank(lvl)) {
      return DEFAULT_LOG_LEVEL;
    }
    return parseLevel(lvl);
  }
}