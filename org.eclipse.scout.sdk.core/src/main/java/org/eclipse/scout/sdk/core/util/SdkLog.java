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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

/**
 * <h3>{@link SdkLog}</h3>
 *
 * @author Matthias Villiger
 * @since 5.2.0
 * @see SdkConsole
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

  /**
   * The pattern for argument place holders: <code>{}</code>
   */
  public static final String ARG_REPLACE_PATTERN = "{}";

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
   * <li><code>SdkLog.info("Unable to parse value");</code></li>
   * <li><code>SdkLog.error("No more disk space.", myException);</code></li>
   * <li><code>SdkLog.warning("File {} does not exist.", myFile);</code></li>
   * <li><code>SdkLog.error("File {} does not exist.", myFile, myFileNotFoundException, myException);</code></li>
   * <li><code>SdkLog.error(exception);</code></li>
   * </ul>
   *
   * @param level
   *          The {@link Level} to log. May be <code>null</code> in which case the {@link #DEFAULT_LOG_LEVEL} is used.
   * @param msg
   *          The message to log. May be <code>null</code>.
   * @param args
   *          Arguments to use for logging. May be <code>null</code>. If the message contains place holders (
   *          {@link #ARG_REPLACE_PATTERN}), the arguments are used to fill the place holders. If there are more
   *          arguments than place holders in the message, the remaining arguments are searched for {@link Throwable}s
   *          whose stack trace is logged as well (see examples above).
   */
  public static void log(Level level, String msg, Object... args) {
    if (level == null) {
      level = DEFAULT_LOG_LEVEL;
    }

    if (level.intValue() < curLevel.intValue() || level.intValue() == Level.OFF.intValue()) {
      return;
    }

    StringBuilder message = new StringBuilder().append('[').append(level.getName()).append("]: ");

    int argstartIndex = 0;
    if (StringUtils.isNotBlank(msg)) {
      StringBuilder messageBuilder = new StringBuilder(msg);
      argstartIndex = handlePlaceholders(messageBuilder, args);
      message.append(messageBuilder);
    }

    SdkConsole.println(message.toString(), extractThrowables(argstartIndex, args));
  }

  static Throwable[] extractThrowables(int startIndex, Object... args) {
    if (args == null || args.length <= startIndex) {
      return null;
    }

    List<Throwable> result = new ArrayList<>(args.length - startIndex);
    for (int i = startIndex; i < args.length; i++) {
      Object cur = args[i];
      searchForThrowables(cur, result);
    }
    if (result.isEmpty()) {
      return null;
    }
    return result.toArray(new Throwable[result.size()]);
  }

  private static void searchForThrowables(Object o, Collection<Throwable> collector) {
    if (o == null) {
      return;
    }
    if (o instanceof Throwable) {
      collector.add((Throwable) o);
    }
    else if (o.getClass().isArray()) {
      Object[] elements = (Object[]) o;
      for (Object element : elements) {
        searchForThrowables(element, collector);
      }
    }
    else if (o instanceof Iterable<?>) {
      Iterable<?> it = (Iterable<?>) o;
      for (Object element : it) {
        searchForThrowables(element, collector);
      }
    }
  }

  static int handlePlaceholders(StringBuilder messageBuilder, Object[] args) {
    int nextIndex = 0;
    int lastPos = 0;
    int curIndex = -1;

    if (args == null || args.length < 1) {
      return nextIndex;
    }

    while ((curIndex = messageBuilder.indexOf(ARG_REPLACE_PATTERN, lastPos)) >= 0 && nextIndex < args.length) {
      int endPos = curIndex + ARG_REPLACE_PATTERN.length();
      messageBuilder.replace(curIndex, endPos, toString(args[nextIndex++]));
      lastPos = endPos;
    }

    return nextIndex;
  }

  private static String toString(Object o) {
    if (o == null) {
      return "null";
    }
    if (o.getClass().isArray()) {
      Object[] arr = (Object[]) o;
      if (arr.length < 1) {
        return "[]";
      }

      int maxSize = 1000;
      int printSize = Math.min(arr.length, maxSize);
      StringBuilder b = new StringBuilder();
      b.append('[');
      b.append(String.valueOf(arr[0]));
      for (int i = 1; i < printSize; i++) {
        b.append(", ");
        b.append(String.valueOf(arr[i]));
      }
      if (arr.length > maxSize) {
        b.append(",...");
      }
      b.append(']');
      return b.toString();
    }
    return o.toString();
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
   *          The message. May be <code>null</code>.
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
   *          The message. May be <code>null</code>.
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
   *          The message. May be <code>null</code>.
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
   *          The message. May be <code>null</code>.
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
    curLevel = Validate.notNull(newLevel);
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

  static Level parseLevel(String lvl) {
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

  static Level getInitialLogLevel() {
    String lvl = System.getProperty(LOG_LEVEL_PROPERTY_NAME);
    if (StringUtils.isBlank(lvl)) {
      return DEFAULT_LOG_LEVEL;
    }
    return parseLevel(lvl);
  }
}
