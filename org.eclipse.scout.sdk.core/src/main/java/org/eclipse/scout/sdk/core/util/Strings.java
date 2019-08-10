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
package org.eclipse.scout.sdk.core.util;

import java.beans.Introspector;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.Objects;
import java.util.Optional;

/**
 * <h3>{@link Strings}</h3><br>
 * Static utility methods to work with {@link String}s.<br>
 *
 * @since 6.1.0
 */
public final class Strings {

  private static final int INDEX_NOT_FOUND = -1;

  private Strings() {
  }

  /**
   * Converts the {@link StringBuilder} specified into a {@code char[]}.
   *
   * @param s
   *          The {@link StringBuilder} to convert. Must not be {@code null}.
   * @return The full contents as {@code char[]}.
   * @throws IllegalArgumentException
   *           if the {@link StringBuilder} is {@code null}.
   */
  public static char[] toCharArray(StringBuilder s) {
    Ensure.notNull(s);
    char[] buf = new char[s.length()];
    s.getChars(0, buf.length, buf, 0);
    return buf;
  }

  /**
   * <p>
   * Repeat a String n times to form a new {@link String}.
   * </p>
   * <b>Examples:</b>
   *
   * <pre>
   * repeat(null, 2) = null
   * repeat("", 0)   = ""
   * repeat("", 2)   = ""
   * repeat("a", 3)  = "aaa"
   * repeat("ab", 2) = "abab"
   * repeat("a", -2) = ""
   * </pre>
   *
   * @param str
   *          the {@link String} to repeat, may be {@code null}.
   * @param n
   *          number of times to repeat, negative treated as zero.
   * @return a new String consisting of the original String repeated
   */
  public static String repeat(String str, int n) {
    if (str == null) {
      return null;
    }
    if (n < 1 || str.length() < 1) {
      return "";
    }
    StringBuilder b = new StringBuilder(str.length() * n);
    for (int i = 0; i < n; i++) {
      b.append(str);
    }
    return b.toString();
  }

  /**
   * <p>
   * Replaces a String with another String inside a larger String
   * </p>
   * <p>
   * A {@code null} reference passed to this method is a no-op.
   * </p>
   * <p>
   * The difference of this method to {@link String#replace(CharSequence, CharSequence)} is that this implementation
   * does not make use of a regular-expression-pattern and is {@code null} safe.
   * </p>
   * <b>Examples:</b>
   *
   * <pre>
   * replace(null, *, *)         = null
   * replace("", *, *)           = ""
   * replace("any", null, *)     = "any"
   * replace("any", *, null)     = "any"
   * replace("any", "", *)       = "any"
   * replace("abaa", "a", null)  = "abaa"
   * replace("abaa", "a", "")    = "b"
   * replace("abaa", "a", "z")   = "abaa"
   * </pre>
   *
   * @param text
   *          text to search and replace in, may be {@code null}.
   * @param searchString
   *          the String to search for, may be {@code null}.
   * @param replacement
   *          the String to replace it with, may be {@code null}.
   * @return the text with any replacements processed, {@code null} if {@code null} String input.
   */
  public static String replace(String text, String searchString, String replacement) {
    if (isEmpty(text) || isEmpty(searchString) || replacement == null || Objects.equals(searchString, replacement)) {
      return text;
    }
    int start = 0;
    int end = text.indexOf(searchString, start);
    if (end == INDEX_NOT_FOUND) {
      return text;
    }
    int replLength = searchString.length();
    int increase = replacement.length() - replLength;
    increase = increase < 0 ? 0 : increase;
    increase *= 16;
    StringBuilder buf = new StringBuilder(text.length() + increase);
    while (end != INDEX_NOT_FOUND) {
      buf.append(text, start, end).append(replacement);
      start = end + replLength;
      end = text.indexOf(searchString, start);
    }
    buf.append(text.substring(start));
    return buf.toString();
  }

  /**
   * Replaces all occurrences of a character in the specified {@link CharSequence} with another character.
   *
   * @param text
   *          The text in which the characters should be replaced.
   * @param search
   *          The character to be replaced.
   * @param replacement
   *          The new character to insert instead.
   * @return A {@link String} with the new content or {@code null} if the input text is {@code null}.
   */
  public static String replace(CharSequence text, char search, char replacement) {
    if (text == null) {
      return null;
    }
    if (text.length() < 1) {
      return "";
    }

    StringBuilder result = new StringBuilder(text.length());
    for (int i = 0; i < text.length(); i++) {
      char c = text.charAt(i);
      if (c == search) {
        result.append(replacement);
      }
      else {
        result.append(c);
      }
    }
    return result.toString();
  }

  /**
   * <p>
   * Counts how many times the substring appears in the larger {@link String}.
   * </p>
   * <p>
   * A {@code null} or empty ("") {@link String} input returns {@code 0}.
   * </p>
   * <p>
   * <p>
   * <p>
   *
   * <pre>
   * countMatches(null, *)       = 0
   * countMatches("", *)         = 0
   * countMatches("abba", null)  = 0
   * countMatches("abba", "")    = 0
   * countMatches("abba", "a")   = 2
   * countMatches("abba", "ab")  = 1
   * countMatches("abba", "xxx") = 0
   * </pre>
   *
   * @param str
   *          the {@link String} to check, may be {@code null}.
   * @param sub
   *          the substring to count, may be {@code null}.
   * @return the number of occurrences, 0 if either {@link String} is {@code null}.
   */
  public static int countMatches(String str, String sub) {
    if (isEmpty(str) || isEmpty(sub)) {
      return 0;
    }
    int count = 0;
    int idx = 0;
    while ((idx = str.indexOf(sub, idx)) != INDEX_NOT_FOUND) {
      count++;
      idx += sub.length();
    }
    return count;
  }

  /**
   * <p>
   * Checks if a CharSequence is empty ("") or {@code null}.
   * </p>
   * <p>
   * <p>
   * <p>
   *
   * <pre>
   * Strings.isEmpty(null)      = true
   * Strings.isEmpty("")        = true
   * Strings.isEmpty(" ")       = false
   * Strings.isEmpty("bob")     = false
   * Strings.isEmpty("  bob  ") = false
   * </pre>
   *
   * @param cs
   *          the CharSequence to check, may be {@code null}
   * @return {@code true} if the CharSequence is empty or {@code null}
   */
  public static boolean isEmpty(CharSequence cs) {
    return cs == null || cs.length() == 0;
  }

  /**
   * Reads all bytes from the given {@link InputStream} and converts them into a {@link StringBuilder} using the given
   * charset name.<br>
   *
   * @param is
   *          The data source. Must not be {@code null}.
   * @param charsetName
   *          The name of the {@link Charset} to use. Must be supported by the platform.
   * @return A {@link StringBuilder} holding the contents.
   * @throws IOException
   *           While reading data from the stream or if the given charsetName does not exist on this platform.
   * @see Charset#isSupported(String)
   */
  public static StringBuilder fromInputStream(InputStream is, String charsetName) throws IOException {
    if (!Charset.isSupported(charsetName)) {
      throw new IOException("Charset '" + charsetName + "' is not supported.");
    }
    return fromInputStream(is, Charset.forName(charsetName));
  }

  /**
   * Reads all bytes from the given {@link InputStream} and converts them into a {@link StringBuilder} using the given
   * {@link Charset}.<br>
   * The specified {@link InputStream} is not closed!
   *
   * @param is
   *          The data source. Must not be {@code null}.
   * @param charset
   *          The {@link Charset} to use for the byte-to-char conversion.
   * @return A {@link StringBuilder} holding the contents.
   * @throws IOException
   *           While reading data from the stream.
   */
  public static StringBuilder fromInputStream(InputStream is, Charset charset) throws IOException {
    char[] buffer = new char[8192];
    StringBuilder out = new StringBuilder(buffer.length);
    int length;
    Reader in = new InputStreamReader(is, charset);
    while ((length = in.read(buffer)) != INDEX_NOT_FOUND) {
      out.append(buffer, 0, length);
    }
    return out;
  }

  /**
   * Converts the given input string literal into the representing original string.<br>
   * This is the inverse function of {@link #toStringLiteral(String)}.
   *
   * @param s
   *          The literal with leading and ending double-quotes
   * @return the original (un-escaped) string. if it is no valid literal string, {@code null} is returned.
   */
  public static String fromStringLiteral(String s) {
    if (s == null) {
      return null;
    }

    int len = s.length();
    if (len < 2 || s.charAt(0) != '"' || s.charAt(len - 1) != '"') {
      return null;
    }

    return replaceLiterals(s.substring(1, len - 1), true);
  }

  /**
   * Converts the given {@link Throwable} into a {@link String}. The resulting string includes a leading new line.
   *
   * @param t
   *          The {@link Throwable}. Must not be {@code null}.
   * @return The {@link String} describing the given {@link Throwable}.
   */
  @SuppressWarnings({"squid:S1148", "squid:S1166"})
  public static String fromThrowable(Throwable t) {
    try (StringWriter w = new StringWriter(); PrintWriter p = new PrintWriter(w)) {
      p.println();
      t.printStackTrace(p);
      return w.toString();
    }
    catch (IOException e) {
      return '[' + e.toString() + ']' + t;
    }
  }

  private static String replaceLiterals(String result, boolean fromLiteral) {
    String[] a = {"\b", "\t", "\n", "\f", "\r", "\"", "\\", "\0", "\1", "\2", "\3", "\4", "\5", "\6", "\7"};
    String[] b = {"\\b", "\\t", "\\n", "\\f", "\\r", "\\\"", "\\\\", "\\0", "\\1", "\\2", "\\3", "\\4", "\\5", "\\6", "\\7"};

    if (fromLiteral) {
      return replaceEach(result, b, a);
    }
    return replaceEach(result, a, b);
  }

  /**
   * Converts the given string into a string literal with leading and ending double-quotes including escaping of the
   * given string.<br>
   * This is the inverse function of {@link #fromStringLiteral(String)}.
   *
   * @param s
   *          the string to convert.
   * @return the literal string ready to be directly inserted into java source or null if the input string is null.
   */
  public static String toStringLiteral(String s) {
    if (s == null) {
      return null;
    }

    StringBuilder b = new StringBuilder(s.length() * 2);
    b.append('"'); // opening delimiter
    b.append(replaceLiterals(s, false));
    b.append('"'); // closing delimiter
    return b.toString();
  }

  /**
   * ensures the given java name starts with an upper case character.<br>
   * <br>
   * <b>Note:</b><br>
   * To ensure the first char starts with a lower case letter use {@link Introspector#decapitalize(String)}
   *
   * @param name
   *          The name to handle.
   * @return null if the input is null, an empty string if the given string is empty or only contains white spaces.
   *         Otherwise the input string is returned with the first character modified to upper case.
   */
  public static String ensureStartWithUpperCase(String name) {
    if (isEmpty(name) || Character.isUpperCase(name.charAt(0))) {
      return name;
    }

    char[] chars = name.toCharArray();
    chars[0] = Character.toUpperCase(chars[0]);
    return new String(chars);
  }

  /**
   * Returns the given input HTML with all necessary characters escaped.
   *
   * @param html
   *          The input HTML.
   * @return The escaped version.
   */
  public static String escapeHtml(String html) {
    return replaceEach(html,
        new String[]{"\"", "&", "<", ">", "'", "/"},
        new String[]{"&quot;", "&amp;", "&lt;", "&gt;", "&apos;", "&#47;"});
  }

  /**
   * <p>
   * Replaces all occurrences of Strings within another String.
   * </p>
   * <p>
   * A {@code null} reference passed to this method is a no-op, or if any "search string" or "string to replace" is
   * {@code null}, that replace will be ignored.
   * </p>
   * <p>
   * <p>
   * <p>
   *
   * <pre>
   *  replaceEach(null, *, *) = null
   *  replaceEach("", *, *) = ""
   *  replaceEach("aba", null, null) = "aba"
   *  replaceEach("aba", new String[0], null) = "aba"
   *  replaceEach("aba", null, new String[0]) = "aba"
   *  replaceEach("aba", new String[]{"a"}, null) = "aba"
   *  replaceEach("aba", new String[]{"a"}, new String[]{""}) = "b"
   *  replaceEach("aba", new String[]{null}, new String[]{"a"}) = "aba"
   *  replaceEach("abcde", new String[]{"ab", "d"}, new String[]{"w", "t"}) = "wcte"
   * </pre>
   *
   * @param text
   *          text to search and replace in, no-op if {@code null}.
   * @param searchList
   *          the Strings to search for, no-op if {@code null}.
   * @param replacementList
   *          the Strings to replace them with, no-op if {@code null}.
   * @return the text with any replacements processed, {@code null} if {@code null} String input.
   * @throws IllegalArgumentException
   *           if the lengths of the arrays are not the same ({@code null} is ok, and/or size 0)
   */
  @SuppressWarnings("pmd:NPathComplexity")
  public static String replaceEach(String text, String[] searchList, String[] replacementList) {
    if (text == null || text.isEmpty()) {
      return text;
    }
    if (searchList == null || searchList.length == 0) {
      return text;
    }
    if (replacementList == null || replacementList.length == 0) {
      return text;
    }

    int searchLength = searchList.length;
    int replacementLength = replacementList.length;

    // make sure lengths are ok, these need to be equal
    if (searchLength != replacementLength) {
      throw new IllegalArgumentException("Search and Replace array lengths don't match: "
          + searchLength
          + " vs "
          + replacementLength);
    }

    // keep track of which still have matches
    boolean[] noMoreMatchesForReplIndex = new boolean[searchLength];

    // index on index that the match was found
    int textIndex = -1;
    int replaceIndex = -1;
    int tempIndex;

    // index of replace array that will replace the search string found
    for (int i = 0; i < searchLength; i++) {
      if (noMoreMatchesForReplIndex[i] || searchList[i] == null ||
          searchList[i].isEmpty() || replacementList[i] == null) {
        continue;
      }
      tempIndex = text.indexOf(searchList[i]);

      // see if we need to keep searching for this
      if (tempIndex == -1) {
        noMoreMatchesForReplIndex[i] = true;
      }
      else {
        if (textIndex == -1 || tempIndex < textIndex) {
          textIndex = tempIndex;
          replaceIndex = i;
        }
      }
    }

    // no search strings found, we are done
    if (textIndex == -1) {
      return text;
    }

    int start = 0;

    // get a good guess on the size of the result buffer so it doesn't have to double if it goes over a bit
    int increase = getLengthIncreaseGuess(text, searchList, replacementList);

    StringBuilder result = new StringBuilder(text.length() + increase);
    while (textIndex != -1) {

      for (int i = start; i < textIndex; i++) {
        result.append(text.charAt(i));
      }
      result.append(replacementList[replaceIndex]);

      start = textIndex + searchList[replaceIndex].length();

      textIndex = -1;
      replaceIndex = -1;
      // find the next earliest match
      for (int i = 0; i < searchLength; i++) {
        if (noMoreMatchesForReplIndex[i] || searchList[i] == null ||
            searchList[i].isEmpty() || replacementList[i] == null) {
          continue;
        }
        tempIndex = text.indexOf(searchList[i], start);

        // see if we need to keep searching for this
        if (tempIndex == -1) {
          noMoreMatchesForReplIndex[i] = true;
        }
        else {
          if (textIndex == -1 || tempIndex < textIndex) {
            textIndex = tempIndex;
            replaceIndex = i;
          }
        }
      }
    }
    int textLength = text.length();
    for (int i = start; i < textLength; i++) {
      result.append(text.charAt(i));
    }
    return result.toString();
  }

  private static int getLengthIncreaseGuess(CharSequence text, String[] searchList, String[] replacementList) {
    int increase = 0;
    // count the replacement text elements that are larger than their corresponding text being replaced
    for (int i = 0; i < searchList.length; i++) {
      if (searchList[i] == null || replacementList[i] == null) {
        continue;
      }
      int greater = replacementList[i].length() - searchList[i].length();
      if (greater > 0) {
        increase += 3 * greater; // assume 3 matches
      }
    }
    // have upper-bound at 20% increase, then let Java take over
    increase = Math.min(increase, text.length() / 5);
    return increase;
  }

  /**
   * Checks if a {@link CharSequence} contains only invisible characters.
   * <p>
   *
   * <pre>
   * isBlank(null)      = true
   * isBlank("")        = true
   * isBlank(" ")       = true
   * isBlank("bob")     = false
   * isBlank("  bob  ") = false
   * </pre>
   *
   * @param cs
   *          the CharSequence to check, may be null
   * @return {@code true} if the CharSequence is null, empty or whitespace
   * @see #hasText(CharSequence)
   */
  public static boolean isBlank(CharSequence cs) {
    int strLen;
    if (cs == null || (strLen = cs.length()) == 0) {
      return true;
    }
    for (int i = 0; i < strLen; i++) {
      if (!Character.isWhitespace(cs.charAt(i))) {
        return false;
      }
    }
    return true;
  }

  /**
   * Tests if the string specified ends with the specified suffix.
   *
   * @param string
   *          The {@link CharSequence} to test
   * @param suffix
   *          The suffix to search
   * @return {@code true} if the string ends with the specified suffix or the suffix has length 0. If the string or
   *         suffix are {@code null} or the string does not end with the suffix specified, {@code false} is returned.
   */
  public static boolean endsWith(CharSequence string, CharSequence suffix) {
    if (string == null || suffix == null) {
      return false;
    }
    if (suffix.length() == 0) {
      return true;
    }
    if (string.length() < suffix.length()) {
      return false;
    }
    for (int i = string.length() - 1, j = suffix.length() - 1; j >= 0; i--, j--) {
      if (string.charAt(i) != suffix.charAt(j)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Checks if a {@link CharSequence} contains visible characters.
   * <p>
   *
   * <pre>
   * hasText(null)      = false
   * hasText("")        = false
   * hasText(" ")       = false
   * hasText("bob")     = true
   * hasText("  bob  ") = true
   * </pre>
   *
   * @param cs
   *          the CharSequence to check, may be null
   * @return {@code true} if the CharSequence is null, empty or whitespace
   * @see #isBlank(CharSequence)
   */
  public static boolean hasText(CharSequence cs) {
    return !isBlank(cs);
  }

  /**
   * Wraps a {@link CharSequence} into an {@link Optional} holding a value if the given sequence is not empty.
   *
   * @param value
   *          The {@link CharSequence} to wrap
   * @return If the given {@link CharSequence} is neither {@code null} nor of length zero, an {@link Optional} holding
   *         the value. Otherwise an empty {@link Optional} is returned.
   * @see #isEmpty(CharSequence)
   */
  public static <T extends CharSequence> Optional<T> notEmpty(T value) {
    if (isEmpty(value)) {
      return Optional.empty();
    }
    return Optional.of(value);
  }

  /**
   * Wraps a {@link CharSequence} into an {@link Optional} holding a value if the given sequence is not blank.
   *
   * @param value
   *          The {@link CharSequence} to wrap
   * @return An {@link Optional} holding the value if the given {@link CharSequence} contains visible characters.
   *         Otherwise an empty {@link Optional} is returned.
   * @see #isBlank(CharSequence)
   */
  public static <T extends CharSequence> Optional<T> notBlank(T value) {
    if (isBlank(value)) {
      return Optional.empty();
    }
    return Optional.of(value);
  }

  /**
   * Compares two {@link CharSequence}s lexicographically.
   *
   * @param a
   *          The first {@link CharSequence}. Must not be {@code null}.
   * @param b
   *          The second {@link CharSequence}. Must not be {@code null}.
   * @return the value {@code 0} if the two {@link CharSequence}s are equal on every character they contain. A value
   *         less than {@code 0} if the first {@link CharSequence} is lexicographically less than the second. A value
   *         greater than {@code 0} if the first {@link CharSequence} is lexicographically greater than the second.
   * @see String#compareTo(String)
   */
  public static int compareTo(CharSequence a, CharSequence b) {
    int limit = Math.min(a.length(), b.length());
    for (int i = 0; i < limit; i++) {
      char x = a.charAt(i);
      char y = b.charAt(i);
      int diff = x - y;
      if (diff != 0) {
        return diff;
      }
    }
    return a.length() - b.length();
  }
}
