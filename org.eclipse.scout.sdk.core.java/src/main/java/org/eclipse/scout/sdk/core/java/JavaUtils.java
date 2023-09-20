/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.java;

import java.util.Locale;
import java.util.regex.Pattern;

import org.eclipse.scout.sdk.core.util.Strings;

public final class JavaUtils {

  @SuppressWarnings("HardcodedLineSeparator")
  private static final Pattern REGEX_COMMENT_REMOVE_1 = Pattern.compile("//.*?\r\n");
  @SuppressWarnings("HardcodedLineSeparator")
  private static final Pattern REGEX_COMMENT_REMOVE_2 = Pattern.compile("//.*?\n");
  private static final Pattern REGEX_COMMENT_REMOVE_3 = Pattern.compile("(?s)/\\*.*?\\*/");

  private JavaUtils() {
  }

  /**
   * Removes all comments in the given java source.
   *
   * @param methodBody
   *          The java source
   * @return The source with all comments (single line & multi line) removed.
   */
  public static String removeComments(CharSequence methodBody) {
    if (methodBody == null) {
      return null;
    }
    if (Strings.isBlank(methodBody)) {
      return methodBody.toString();
    }
    var retVal = REGEX_COMMENT_REMOVE_1.matcher(methodBody).replaceAll("");
    retVal = REGEX_COMMENT_REMOVE_2.matcher(retVal).replaceAll("");
    retVal = REGEX_COMMENT_REMOVE_3.matcher(retVal).replaceAll("");
    return retVal;
  }

  /**
   * Converts the given input string literal into the representing original string.<br>
   * Escaped character sequences e.g. {@code "\n"} are un-escaped and results in e.g. a new-line character.<br>
   * Leading and trailing single-quotes, double-quotes and backticks are removed. <br>
   * This is the inverse function of {@link #toStringLiteral(CharSequence)}.
   * <p>
   * Example:
   *
   * <pre>
   *     A string with the following content (quotes are content of the string):
   *     "line 1\nline2"
   *     is converted to:
   *     line 1
   *     line 2
   * </pre>
   *
   * @param literal
   *          The literal with optional leading and ending quotes (single, double or backtick) or {@code null}.
   * @return the un-escaped string or {@code null} if the input is {@code null}.
   */
  @SuppressWarnings({"HardcodedLineSeparator", "squid:ForLoopCounterChangedCheck"})
  public static CharSequence fromStringLiteral(CharSequence literal) {
    if (literal == null) {
      return null;
    }
    var s = Strings.withoutQuotes(literal);
    var length = s.length();
    var buffer = new StringBuilder(length);
    var escaped = false;
    for (var idx = 0; idx < length; idx++) {
      var ch = s.charAt(idx);
      if (!escaped) {
        if (ch == '\\') {
          escaped = true;
        }
        else {
          buffer.append(ch);
        }
      }
      else {
        var octalEscapeMaxLength = 2;
        switch (ch) {
          case 'n':
            buffer.append('\n');
            break;
          case 'r':
            buffer.append('\r');
            break;
          case 'b':
            buffer.append('\b');
            break;
          case 't':
            buffer.append('\t');
            break;
          case 'f':
            buffer.append('\f');
            break;
          case '\'':
            buffer.append('\'');
            break;
          case '\"':
            buffer.append('\"');
            break;
          case '\\':
            buffer.append('\\');
            break;
          case 'u':
            if (idx + 4 < length) {
              try {
                var code = Integer.parseInt(s, idx + 1, idx + 5, 16);
                //noinspection AssignmentToForLoopParameter
                idx += 4;
                //noinspection NumericCastThatLosesPrecision
                buffer.append((char) code);
              }
              catch (NumberFormatException e) {
                buffer.append("\\u");
              }
            }
            else {
              buffer.append("\\u");
            }
            break;
          case '0':
          case '1':
          case '2':
          case '3':
            octalEscapeMaxLength = 3;
            //noinspection fallthrough
          case '4':
          case '5':
          case '6':
          case '7':
            var escapeEnd = idx + 1;
            while (escapeEnd < length && escapeEnd < idx + octalEscapeMaxLength && isOctalDigit(s.charAt(escapeEnd))) {
              escapeEnd++;
            }
            try {
              //noinspection NumericCastThatLosesPrecision
              buffer.append((char) Integer.parseInt(s, idx, escapeEnd, 8));
            }
            catch (NumberFormatException e) {
              throw new IllegalStateException("Couldn't parse " + s.subSequence(idx, escapeEnd), e); // shouldn't happen
            }
            //noinspection AssignmentToForLoopParameter
            idx = escapeEnd - 1;
            break;
          default:
            buffer.append(ch);
            break;
        }
        escaped = false;
      }
    }
    if (escaped) {
      buffer.append('\\');
    }
    return buffer;
  }

  private static boolean isOctalDigit(char c) {
    //noinspection CharacterComparison
    return '0' <= c && c <= '7';
  }

  /**
   * Converts the given {@link CharSequence} into a string literal with leading and ending double-quotes including
   * escaping of the given string content.<br>
   * This is the inverse function of {@link #fromStringLiteral(CharSequence)}.
   * <p>
   * Example:
   *
   * <pre>
   *         A string with the following content:
   *         >line 1
   *         line 2<
   *         is converted to  (quotes are content of the string):
   *         >"line 1\nline 2"<
   * </pre>
   *
   * @param s
   *          the string to convert or {@code null}.
   * @return the literal string ready to be directly inserted into Java source or {@code null} if the input string is
   *         {@code null}.
   */
  public static CharSequence toStringLiteral(CharSequence s) {
    return toStringLiteral(s, "\"", true);
  }

  /**
   * Converts the given {@link CharSequence} into a string literal including escaping of the given string content.
   * Optionally the resulting {@link CharSequence} is surrounded with the string delimiter.<br>
   * This is the inverse function of {@link #fromStringLiteral(CharSequence)}.
   *
   * @param s
   *          the string to convert or {@code null}.
   * @param stringDelimiter
   *          The string delimiter (e.g. " or '). The corresponding character will be escaped.
   * @param surroundWithStringDelimiter
   *          Specifies if the resulting {@link CharSequence} should be surrounded with the stringDelimiter given.
   * @return the literal string or {@code null} if the input string is {@code null}.
   */
  @SuppressWarnings({"HardcodedLineSeparator", "squid:S881" /* -- operators should not be mixed with other operators */})
  public static CharSequence toStringLiteral(CharSequence s, CharSequence stringDelimiter, boolean surroundWithStringDelimiter) {
    if (s == null) {
      return null;
    }
    var length = s.length();
    var buffer = new StringBuilder(length * 2);
    if (surroundWithStringDelimiter && stringDelimiter != null) {
      buffer.append(stringDelimiter); // opening delimiter
    }
    for (var idx = 0; idx < length; idx++) {
      var ch = s.charAt(idx);
      switch (ch) {
        case '\b' -> buffer.append("\\b");
        case '\t' -> buffer.append("\\t");
        case '\n' -> buffer.append("\\n");
        case '\f' -> buffer.append("\\f");
        case '\r' -> buffer.append("\\r");
        default -> {
          if (ch == '\\') {
            buffer.append("\\\\");
          }
          else if (stringDelimiter != null && Strings.indexOf(ch, stringDelimiter) > -1) {
            buffer.append("\\").append(ch);
          }
          else if (!isPrintableUnicode(ch)) {
            var hexCode = Integer.toHexString(ch).toUpperCase(Locale.US);
            buffer.append("\\u");
            var paddingCount = 4 - hexCode.length();
            while (paddingCount-- > 0) {
              buffer.append(0);
            }
            buffer.append(hexCode);
          }
          else {
            buffer.append(ch);
          }
        }
      }
    }
    if (surroundWithStringDelimiter && stringDelimiter != null) {
      buffer.append(stringDelimiter); // closing delimiter
    }
    return buffer;
  }

  @SuppressWarnings("squid:S1067") // Reduce the number of conditional operators
  private static boolean isPrintableUnicode(char c) {
    var t = Character.getType(c);
    return t != Character.UNASSIGNED && t != Character.LINE_SEPARATOR && t != Character.PARAGRAPH_SEPARATOR
        && t != Character.CONTROL && t != Character.FORMAT && t != Character.PRIVATE_USE && t != Character.SURROGATE;
  }
}
