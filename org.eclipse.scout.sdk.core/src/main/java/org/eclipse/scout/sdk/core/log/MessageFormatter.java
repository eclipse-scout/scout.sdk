/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.log;

import static java.util.Collections.emptyList;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.IntStream;

/**
 * <h3>{@link MessageFormatter}</h3><br>
 * Formatter class that can build message strings and replaces the value of formatting anchors
 * ({@value #ARG_REPLACE_PATTERN}) with the given arguments.
 *
 * @since 6.1.0
 */
public final class MessageFormatter {

  /**
   * The pattern for argument placeholders: {@code {}}
   */
  public static final String ARG_REPLACE_PATTERN = "{}";

  private MessageFormatter() {
  }

  /**
   * Formats the given message string and replaces all formatting anchors ({@value #ARG_REPLACE_PATTERN}) with the
   * corresponding argument in the given array.
   *
   * @param msg
   *          The {@link CharSequence} that holds the message with the formatting anchors.
   * @param args
   *          The arguments.
   * @return A {@link FormattingTuple} with the parsed values. In this result all formatting anchors have been replaced
   *         with the corresponding values.
   */
  public static FormattingTuple arrayFormat(CharSequence msg, Object... args) {
    var rawMessage = msg;
    if (rawMessage == null) {
      rawMessage = "";
    }

    if (args == null || args.length == 0) {
      return new FormattingTuple(rawMessage.toString(), emptyList());
    }

    int curIndex;
    var nextIndex = 0;
    var lastPos = 0;
    var messageBuilder = new StringBuilder(rawMessage);
    while ((curIndex = messageBuilder.indexOf(ARG_REPLACE_PATTERN, lastPos)) >= 0 && nextIndex < args.length) {
      var endPos = curIndex + ARG_REPLACE_PATTERN.length();
      var replacement = toString(args[nextIndex]);
      messageBuilder.replace(curIndex, endPos, replacement);
      nextIndex++;
      lastPos = curIndex + replacement.length();
    }
    return new FormattingTuple(messageBuilder.toString(), extractThrowables(nextIndex, args));
  }

  static List<Throwable> extractThrowables(int startIndex, Object... args) {
    if (args == null || args.length <= startIndex) {
      return emptyList();
    }

    List<Throwable> result = new ArrayList<>(args.length - startIndex);
    for (var i = startIndex; i < args.length; i++) {
      var cur = args[i];
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
      var elements = (Object[]) o;
      for (var element : elements) {
        searchForThrowables(element, collector);
      }
    }
    else if (o instanceof Iterable<?> it) {
      for (var element : it) {
        searchForThrowables(element, collector);
      }
    }
  }

  static String toString(Object o) {
    if (o == null || !o.getClass().isArray()) {
      return safeObjectToString(o);
    }

    var arr = toObjectArray(o);
    if (arr.length < 1) {
      return "[]";
    }

    var maxSize = 100;
    var printSize = Math.min(arr.length, maxSize);
    var b = new StringBuilder();
    b.append('[');
    b.append(toString(arr[0]));
    for (var i = 1; i < printSize; i++) {
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
    var len = Array.getLength(arr);
    return IntStream.range(0, len).mapToObj(i -> Array.get(arr, i)).toArray();
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
      //noinspection UseOfSystemOutOrSystemErr
      System.err.println(MessageFormatter.class.getSimpleName() + ": Failed toString() invocation on an object of type [" + o.getClass().getName() + ']');
      //noinspection CallToPrintStackTrace
      t.printStackTrace();
      return "[FAILED toString() of " + o.getClass() + ']';
    }
  }
}
