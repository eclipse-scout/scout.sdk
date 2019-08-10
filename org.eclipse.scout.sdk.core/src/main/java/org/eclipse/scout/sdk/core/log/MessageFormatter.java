/*
 * Copyright (c) 2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.log;

import static java.util.Collections.emptyList;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * <h3>{@link MessageFormatter}</h3><br>
 * Formatter class that can build message strings and replaces the value of formatting anchors
 * ({@value #ARG_REPLACE_PATTERN}) with the given arguments.
 *
 * @since 6.1.0
 */
public final class MessageFormatter {

  /**
   * The pattern for argument place holders: {@code {}}
   */
  public static final String ARG_REPLACE_PATTERN = "{}";

  private MessageFormatter() {
  }

  /**
   * Formats the given message string and replaces all formatting anchors ({@value #ARG_REPLACE_PATTERN}) with the
   * corresponding argument in the given array.
   *
   * @param msg
   *          The {@link String} that holds the message with the formatting anchors.
   * @param args
   *          The arguments.
   * @return A {@link FormattingTuple} with the parsed values. In this result all formatting anchors have been replaced
   *         with the corresponding values.
   */
  public static FormattingTuple arrayFormat(String msg, Object... args) {
    String rawMessage = msg;
    if (rawMessage == null) {
      rawMessage = "";
    }

    int nextIndex = 0;
    StringBuilder messageBuilder = new StringBuilder(rawMessage);
    if (args != null) {
      int curIndex;
      int lastPos = 0;
      while ((curIndex = messageBuilder.indexOf(ARG_REPLACE_PATTERN, lastPos)) >= 0 && nextIndex < args.length) {
        int endPos = curIndex + ARG_REPLACE_PATTERN.length();
        String replacement = toString(args[nextIndex]);
        messageBuilder.replace(curIndex, endPos, replacement);
        nextIndex++;
        lastPos = curIndex + replacement.length();
      }
    }
    return new FormattingTuple(messageBuilder.toString(), extractThrowables(nextIndex, args));
  }

  static List<Throwable> extractThrowables(int startIndex, Object... args) {
    if (args == null || args.length <= startIndex) {
      return emptyList();
    }

    List<Throwable> result = new ArrayList<>(args.length - startIndex);
    for (int i = startIndex; i < args.length; i++) {
      Object cur = args[i];
      searchForThrowables(cur, result);
    }
    if (result.isEmpty()) {
      return emptyList();
    }
    return result;
  }

  static void searchForThrowables(Object o, Collection<Throwable> collector) {
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

  static String toString(Object o) {
    if (o == null || !o.getClass().isArray()) {
      return safeObjectToString(o);
    }

    Object[] arr = toObjectArray(o);
    if (arr.length < 1) {
      return "[]";
    }

    int maxSize = 100;
    int printSize = Math.min(arr.length, maxSize);
    StringBuilder b = new StringBuilder();
    b.append('[');
    b.append(toString(arr[0]));
    for (int i = 1; i < printSize; i++) {
      b.append(", ");
      b.append(toString(arr[i]));
    }
    if (arr.length > maxSize) {
      b.append(",...");
    }
    b.append(']');
    return b.toString();
  }

  static Object[] toObjectArray(Object arr) {
    if (arr instanceof Object[]) {
      return (Object[]) arr;
    }

    // must be a primitive array
    int len = Array.getLength(arr);
    Object[] result = new Object[len];
    for (int i = 0; i < len; i++) {
      result[i] = Array.get(arr, i);
    }
    return result;
  }

  @SuppressWarnings({"squid:S1181", "squid:S1166", "squid:S1148"})
  static String safeObjectToString(Object o) {
    if (o == null) {
      return "null";
    }

    try {
      return o.toString();
    }
    catch (Throwable t) {
      System.err.println(MessageFormatter.class.getSimpleName() + ": Failed toString() invocation on an object of type [" + o.getClass().getName() + ']');
      t.printStackTrace();
      return "[FAILED toString() of " + o.getClass() + ']';
    }
  }
}
