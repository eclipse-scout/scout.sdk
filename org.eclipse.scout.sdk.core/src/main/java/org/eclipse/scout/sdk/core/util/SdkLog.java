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

import org.apache.commons.lang3.ArrayUtils;
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

    int argStartIndex = 0;
    if (StringUtils.isNotBlank(msg)) {
      StringBuilder messageBuilder = new StringBuilder(msg.length() + 50).append(msg);
      argStartIndex = handlePlaceholders(messageBuilder, args);
      message.append(messageBuilder);
    }

    SdkConsole.println(level, message.toString(), extractThrowables(argStartIndex, args));
  }

  @SuppressWarnings("squid:S1168")
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
      messageBuilder.replace(curIndex, endPos, toString(args[nextIndex]));
      nextIndex++;
      lastPos = endPos;
    }

    return nextIndex;
  }

  private static String toString(Object o) {
    if (o == null || !o.getClass().isArray()) {
      return safeObjectToString(o);
    }

    Object[] arr = toObjectArray(o);
    if (arr.length < 1) {
      return "[]";
    }

    int maxSize = 1000;
    int printSize = Math.min(arr.length, maxSize);
    StringBuilder b = new StringBuilder();
    b.append('[');
    b.append(safeObjectToString(arr[0]));
    for (int i = 1; i < printSize; i++) {
      b.append(", ");
      b.append(safeObjectToString(arr[i]));
    }
    if (arr.length > maxSize) {
      b.append(",...");
    }
    b.append(']');
    return b.toString();
  }

  @SuppressWarnings("pmd:NPathComplexity")
  private static Object[] toObjectArray(Object arr) {
    if (arr instanceof Object[]) {
      return (Object[]) arr;
    }
    if (arr instanceof boolean[]) {
      return ArrayUtils.toObject((boolean[]) arr);
    }
    if (arr instanceof byte[]) {
      return ArrayUtils.toObject((byte[]) arr);
    }
    if (arr instanceof char[]) {
      return ArrayUtils.toObject((char[]) arr);
    }
    if (arr instanceof short[]) {
      return ArrayUtils.toObject((short[]) arr);
    }
    if (arr instanceof int[]) {
      return ArrayUtils.toObject((int[]) arr);
    }
    if (arr instanceof long[]) {
      return ArrayUtils.toObject((long[]) arr);
    }
    if (arr instanceof float[]) {
      return ArrayUtils.toObject((float[]) arr);
    }
    if (arr instanceof double[]) {
      return ArrayUtils.toObject((double[]) arr);
    }

    throw new IllegalArgumentException();
  }

  @SuppressWarnings({"squid:S1181", "squid:S1166", "squid:S1148"})
  private static String safeObjectToString(Object o) {
    if (o == null) {
      return "null";
    }

    try {
      return o.toString();
    }
    catch (Throwable t) {
      System.err.println("Scout SdkLog: Failed toString() invocation on an object of type [" + o.getClass().getName() + "]");
      t.printStackTrace();
      return "[FAILED toString() of " + o.getClass() + ']';
    }
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

  /**
   * Checks if at least the given {@link Level} is enabled. If this method returns <code>true</code> this means that a
   * message with given {@link Level} would be printed.
   *
   * @param level
   *          The level to check.
   * @return <code>true</code> if at least the given {@link Level} is currently active.
   */
  public static boolean isLevelEnabled(Level level) {
    if (level == null) {
      return false;
    }
    return isLevelEnabled(level.intValue());
  }

  /**
   * Checks if at least the given level is enabled. If this method returns <code>true</code> this means that a message
   * with given {@link Level} would be printed.
   *
   * @param level
   *          The level to check. Must be the {@link Level#intValue()} of one of the constants defined in {@link Level}.
   * @return <code>true</code> if at least the given {@link Level} is currently active.
   */
  public static boolean isLevelEnabled(int level) {
    return getLogLevel().intValue() <= level;
  }

  /**
   * Checks if the Debug level is currently active. See {@link #isLevelEnabled(Level)}.
   *
   * @return <code>true</code> if the Debug {@link Level} is currently enabled and would be printed to the log.
   */
  public static boolean isDebugEnabled() {
    return isLevelEnabled(Level.FINE);
  }

  /**
   * Checks if the Info level is currently active. See {@link #isLevelEnabled(Level)}.
   *
   * @return <code>true</code> if the Info {@link Level} is currently enabled and would be printed to the log.
   */
  public static boolean isInfoEnabled() {
    return isLevelEnabled(Level.INFO);
  }

  /**
   * Checks if the Warning level is currently active. See {@link #isLevelEnabled(Level)}.
   *
   * @return <code>true</code> if the Warning {@link Level} is currently enabled and would be printed to the log.
   */
  public static boolean isWarningEnabled() {
    return isLevelEnabled(Level.WARNING);
  }

  /**
   * Checks if the Error level is currently active. See {@link #isLevelEnabled(Level)}.
   *
   * @return <code>true</code> if the Error {@link Level} is currently enabled and would be printed to the log.
   */
  public static boolean isErrorEnabled() {
    return isLevelEnabled(Level.SEVERE);
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
      SdkConsole.println(Level.SEVERE, "Unable to parse log level '" + lvl + "'. Fallback to default: '" + DEFAULT_LOG_LEVEL.getName() + "'.", e);
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
