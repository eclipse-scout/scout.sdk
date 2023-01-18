/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.testing;

import java.util.regex.Pattern;

import org.eclipse.scout.sdk.core.util.Strings;

/**
 * Testing helpers
 */
public final class CoreTestingUtils {

  private static final Pattern WHITESPACE_PAT = Pattern.compile("[\\s\0\1\2\3\4\5\6\7\u0008]+");

  private CoreTestingUtils() {
  }

  /**
   * Removes all white spaces of the given {@link String}.
   *
   * @param s
   *          The string in which the white spaces should be removed.
   * @return The input {@link String} without any white spaces.
   */
  public static String removeWhitespace(CharSequence s) {
    if (s == null) {
      return null;
    }
    return WHITESPACE_PAT.matcher(s).replaceAll("");
  }

  /**
   * Normalizes all new lines to unix style.
   *
   * @param text
   *          The text in which the new line characters should be normalized.
   * @return The input text with all {@code \r} removed.
   */
  public static CharSequence normalizeNewLines(CharSequence text) {
    //noinspection HardcodedLineSeparator
    return Strings.replace(text, "\r", "");
  }

  /**
   * normalizes all white space characters to one space. This removes any tabs, new-lines, etc.
   *
   * @param s
   *          The input {@link String} for witch the white spaces should be normalized.
   * @return The input {@link String} with the white spaces normalized.
   */
  public static String normalizeWhitespace(CharSequence s) {
    if (s == null) {
      return null;
    }
    return Strings.trim(WHITESPACE_PAT.matcher(s).replaceAll(" ")).toString();
  }
}
