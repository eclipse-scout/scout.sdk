/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.util;

import static java.lang.System.lineSeparator;
import static org.eclipse.scout.sdk.core.util.Ensure.newFail;

import java.beans.Introspector;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

/**
 * <h3>{@link Strings}</h3> Static utility methods to work with character sequences like {@link String},
 * {@link CharSequence}, {@link StringBuilder} or {@code char[]}.
 *
 * @since 6.1.0
 */
public final class Strings {

  private static final int INDEX_NOT_FOUND = -1;

  private Strings() {
  }

  /**
   * Checks if the two arrays have the same content comparing the character case sensitive.
   *
   * <pre>
   *   first=null & second=null -> true
   *   first=a & second=a -> true
   *   first=abc & second=def -> false
   *   first=null & second=a -> false
   * </pre>
   *
   * @param first
   *          The first array
   * @param second
   *          The second array
   * @return {@code true} if both have equal content or both are {@code null}.
   */
  public static boolean equals(char[] first, char[] second) {
    //noinspection ArrayEquality
    if (first == second) {
      return true;
    }
    if (first == null || second == null) {
      return false;
    }
    if (first.length != second.length) {
      return false;
    }
    //noinspection Convert2streamapi
    for (var i = first.length - 1; i >= 0; i--) {
      if (first[i] != second[i]) {
        return false;
      }
    }
    return true;
  }

  /**
   * Checks if the two arrays have the same content comparing the character using the case sensitivity given. See
   * {@link #equals(char[], char[])} for more details.
   *
   * @param first
   *          The first array
   * @param second
   *          The second array
   * @param isCaseSensitive
   *          specifies whether or not the equality should be case sensitive
   * @return {@code true} if the two arrays are identical character by character according to the value of
   *         isCaseSensitive or if both are {@code null}.
   */
  public static boolean equals(char[] first, char[] second, boolean isCaseSensitive) {
    if (isCaseSensitive) {
      return equals(first, second);
    }
    //noinspection ArrayEquality
    if (first == second) {
      return true;
    }
    if (first == null || second == null) {
      return false;
    }
    if (first.length != second.length) {
      return false;
    }
    //noinspection Convert2streamapi
    for (var i = first.length - 1; i >= 0; i--) {
      if (Character.toLowerCase(first[i]) != Character.toLowerCase(second[i])) {
        return false;
      }
    }
    return true;
  }

  /**
   * Checks if the two {@link CharSequence}s have the same content comparing the character case sensitive.
   *
   * <pre>
   *   first=null & second=null -> true
   *   first=a & second=a -> true
   *   first=abc & second=def -> false
   *   first=null & second=a -> false
   * </pre>
   *
   * @param first
   *          The first {@link CharSequence}
   * @param second
   *          The second {@link CharSequence}
   * @return {@code true} if both have equal content or both are {@code null}.
   */
  public static boolean equals(CharSequence first, CharSequence second) {
    if (first == second) {
      return true;
    }
    if (first == null || second == null) {
      return false;
    }
    if (first.length() != second.length()) {
      return false;
    }
    //noinspection Convert2streamapi
    for (var i = first.length() - 1; i >= 0; i--) {
      if (first.charAt(i) != second.charAt(i)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Checks if the two {@link CharSequence}s have the same content comparing the character using the case sensitivity
   * given. See {@link #equals(CharSequence, CharSequence)} for more details.
   *
   * @param first
   *          The first {@link CharSequence}
   * @param second
   *          The second {@link CharSequence}
   * @param isCaseSensitive
   *          specifies whether or not the equality should be case sensitive
   * @return {@code true} if the two sequences are identical character by character according to the value of
   *         isCaseSensitive or if both are {@code null}.
   */
  public static boolean equals(CharSequence first, CharSequence second, boolean isCaseSensitive) {
    if (isCaseSensitive) {
      return equals(first, second);
    }

    if (first == second) {
      return true;
    }
    if (first == null || second == null) {
      return false;
    }
    if (first.length() != second.length()) {
      return false;
    }
    //noinspection Convert2streamapi
    for (var i = first.length() - 1; i >= 0; i--) {
      if (Character.toLowerCase(first.charAt(i)) != Character.toLowerCase(second.charAt(i))) {
        return false;
      }
    }
    return true;
  }

  /**
   * Gets the first index having the character given.
   *
   * @param toBeFound
   *          The character to search
   * @param searchIn
   *          The array to search in. Must not be {@code null}.
   * @return The first zero based index having the character given.
   * @throws NullPointerException
   *           if the array is {@code null}.
   */
  public static int indexOf(char toBeFound, char[] searchIn) {
    return indexOf(toBeFound, searchIn, 0);
  }

  /**
   * Gets the first index having the character given. It starts searching at index start (inclusive) and searches to the
   * end of the array.
   *
   * @param toBeFound
   *          The character to search
   * @param searchIn
   *          The array to search in. Must not be {@code null}.
   * @param start
   *          The first index to consider.
   * @return The first zero based index between start and the end of the array.
   * @throws NullPointerException
   *           if the array is {@code null}.
   */
  public static int indexOf(char toBeFound, char[] searchIn, int start) {
    return indexOf(toBeFound, searchIn, start, searchIn.length);
  }

  /**
   * Gets the first index having the character given. It starts searching at index start (inclusive) and stops before
   * index end (exclusive).
   *
   * @param toBeFound
   *          The character to search
   * @param searchIn
   *          The array to search in. Must not be {@code null}.
   * @param start
   *          The first index to consider.
   * @param end
   *          Where to stop searching (exclusive)
   * @return The first zero based index between start and end having the character given.
   * @throws NullPointerException
   *           if the array is {@code null}.
   */
  public static int indexOf(char toBeFound, char[] searchIn, int start, int end) {
    var limit = Math.min(end, searchIn.length);
    //noinspection Convert2streamapi
    for (var i = start; i < limit; ++i) {
      if (toBeFound == searchIn[i]) {
        return i;
      }
    }
    return INDEX_NOT_FOUND;
  }

  /**
   * Gets the first index having the character given.
   *
   * @param toBeFound
   *          The character to search
   * @param searchIn
   *          The {@link CharSequence} to search in. Must not be {@code null}.
   * @return The first zero based index having the character given.
   * @throws NullPointerException
   *           if the {@link CharSequence} is {@code null}.
   */
  public static int indexOf(char toBeFound, CharSequence searchIn) {
    return indexOf(toBeFound, searchIn, 0);
  }

  /**
   * Gets the first index having the character given. It starts searching at index start (inclusive) and searches to the
   * end of the array.
   *
   * @param toBeFound
   *          The character to search
   * @param searchIn
   *          The {@link CharSequence} to search in. Must not be {@code null}.
   * @param start
   *          The first index to consider.
   * @return The first zero based index between start and the end of the array.
   * @throws NullPointerException
   *           if the {@link CharSequence} is {@code null}.
   */
  public static int indexOf(char toBeFound, CharSequence searchIn, int start) {
    return indexOf(toBeFound, searchIn, start, searchIn.length());
  }

  /**
   * Gets the first index having the character given. It starts searching at index start (inclusive) and stops before
   * index end (exclusive).
   *
   * @param toBeFound
   *          The character to search
   * @param searchIn
   *          The {@link CharSequence} to search in. Must not be {@code null}.
   * @param start
   *          The first index to consider.
   * @param end
   *          Where to stop searching (exclusive)
   * @return The first zero based index between start and end having the character given.
   * @throws NullPointerException
   *           if the {@link CharSequence} is {@code null}.
   */
  public static int indexOf(char toBeFound, CharSequence searchIn, int start, int end) {
    var limit = Math.max(Math.min(end, searchIn.length()), 0);
    //noinspection Convert2streamapi
    for (var i = start; i < limit; i++) {
      if (toBeFound == searchIn.charAt(i)) {
        return i;
      }
    }
    return INDEX_NOT_FOUND;
  }

  /**
   * Like {@link #indexOf(char[], char[])}.
   */
  public static int indexOf(CharSequence toBeFound, CharSequence searchIn) {
    return indexOf(toBeFound, searchIn, 0);
  }

  /**
   * Like {@link #indexOf(char[], char[], int)}.
   */
  public static int indexOf(CharSequence toBeFound, CharSequence searchIn, int start) {
    return indexOf(toBeFound, searchIn, start, searchIn.length());
  }

  /**
   * Like {@link #indexOf(char[], char[], int, int)}.
   */
  public static int indexOf(CharSequence toBeFound, CharSequence searchIn, int start, int end) {
    var toBeFoundLength = toBeFound.length();
    if (toBeFoundLength > end || start < 0) {
      return INDEX_NOT_FOUND;
    }
    if (toBeFoundLength == 0) {
      return 0;
    }
    arrayLoop: for (int i = start, max = end - toBeFoundLength + 1; i < max; i++) {
      if (searchIn.charAt(i) == toBeFound.charAt(0)) {
        for (var j = 1; j < toBeFoundLength; j++) {
          if (searchIn.charAt(i + j) != toBeFound.charAt(j)) {
            continue arrayLoop;
          }
        }
        return i;
      }
    }
    return INDEX_NOT_FOUND;
  }

  /**
   * Like {@link #indexOf(char[], char[], int, int, boolean)} but performs a case sensitive search in the full array.
   */
  public static int indexOf(char[] toBeFound, char[] searchIn) {
    return indexOf(toBeFound, searchIn, 0);
  }

  /**
   * Like {@link #indexOf(char[], char[], int, int, boolean)} but performs a case sensitive search from the given start
   * (inclusive) to the end of the array.
   */
  public static int indexOf(char[] toBeFound, char[] searchIn, int start) {
    return indexOf(toBeFound, searchIn, start, searchIn.length);
  }

  /**
   * Like {@link #indexOf(char[], char[], int, int, boolean)} but performs a case sensitive search.
   */
  public static int indexOf(char[] toBeFound, char[] searchIn, int start, int end) {
    return indexOf(toBeFound, searchIn, start, end, true);
  }

  /**
   * Answers the first index in searchIn for which toBeFound is a matching followup array. Answers -1 if no match is
   * found.<br>
   * Examples:
   * <ol>
   * <li>
   *
   * <pre>
   * toBeFound = { 'c' }
   * searchIn = { ' a', 'b', 'c', 'd' }
   * result => 2
   * </pre>
   *
   * </li>
   * <li>
   *
   * <pre>
   * toBeFound = { 'e' }
   * searchIn = { ' a', 'b', 'c', 'd' }
   * result => -1
   * </pre>
   *
   * </li>
   * <li>
   *
   * <pre>
   * toBeFound = { 'b', 'c' }
   * searchIn = { ' a', 'b', 'c', 'd' }
   * result => 1
   * </pre>
   *
   * </li>
   * </ol>
   *
   * @param toBeFound
   *          the subarray to search. Must not be {@code null}.
   * @param searchIn
   *          the array to be searched in. Must not be {@code null}.
   * @param start
   *          the starting index (inclusive) describing where in searchIn to begin searching.
   * @param end
   *          the end index (exclusive) describing where in searchIn to stop searching.
   * @param isCaseSensitive
   *          describes if the comparation should be case sensitive or not.
   * @return the first index in searchIn for which the toBeFound array is a matching followup array or -1 if it cannot
   *         be found.
   * @throws NullPointerException
   *           if searchIn is {@code null} or toBeFound is {@code null}
   */
  @SuppressWarnings("IfStatementWithIdenticalBranches")
  public static int indexOf(char[] toBeFound, char[] searchIn, int start, int end, boolean isCaseSensitive) {
    var toBeFoundLength = toBeFound.length;
    if (toBeFoundLength > end || start < 0) {
      return INDEX_NOT_FOUND;
    }
    if (toBeFoundLength == 0) {
      return 0;
    }
    if (isCaseSensitive) {
      arrayLoop: for (int i = start, max = end - toBeFoundLength + 1; i < max; i++) {
        if (searchIn[i] == toBeFound[0]) {
          for (var j = 1; j < toBeFoundLength; j++) {
            if (searchIn[i + j] != toBeFound[j]) {
              continue arrayLoop;
            }
          }
          return i;
        }
      }
    }
    else {
      arrayLoop: for (int i = start, max = end - toBeFoundLength + 1; i < max; i++) {
        if (Character.toLowerCase(searchIn[i]) == Character.toLowerCase(toBeFound[0])) {
          for (var j = 1; j < toBeFoundLength; j++) {
            if (Character.toLowerCase(searchIn[i + j]) != Character.toLowerCase(toBeFound[j])) {
              continue arrayLoop;
            }
          }
          return i;
        }
      }
    }
    return INDEX_NOT_FOUND;
  }

  /**
   * Searches for the last index in the {@link CharSequence} which has the character given.
   *
   * @param toBeFound
   *          The character to find
   * @param searchIn
   *          The {@link CharSequence} to search in.
   * @return The last zero based index or -1 if it could not be found.
   * @throws NullPointerException
   *           if the sequence is {@code null}.
   */
  public static int lastIndexOf(char toBeFound, CharSequence searchIn) {
    return lastIndexOf(toBeFound, searchIn, 0);
  }

  /**
   * Searches for the last index after the given startIndex which has the character specified.
   *
   * @param toBeFound
   *          The character to find.
   * @param searchIn
   *          The {@link CharSequence} to search in.
   * @param startIndex
   *          The index to start.
   * @return The last zero based index after the startIndex or -1 if it could not be found.
   * @throws NullPointerException
   *           if the sequence is {@code null}.
   */
  public static int lastIndexOf(char toBeFound, CharSequence searchIn, int startIndex) {
    return lastIndexOf(toBeFound, searchIn, startIndex, searchIn.length());
  }

  /**
   * Searches for the last index between the startIndex and the endIndex having the given character.
   *
   * @param toBeFound
   *          The character to find.
   * @param searchIn
   *          The {@link CharSequence} to search in.
   * @param startIndex
   *          The index where to start the search.
   * @param endIndex
   *          The index where to end the search.
   * @returnThe last zero based index between the startIndex and the endIndex or -1 if it could not be found in this
   *            section.
   * @throws NullPointerException
   *           if the sequence is {@code null}.
   */
  @SuppressWarnings("squid:S881")
  public static int lastIndexOf(char toBeFound, CharSequence searchIn, int startIndex, int endIndex) {
    for (var i = endIndex; --i >= startIndex;) {
      if (toBeFound == searchIn.charAt(i)) {
        return i;
      }
    }
    return INDEX_NOT_FOUND;
  }

  /**
   * Gets the next index after the given offset at which the current line ends. If invoked for the last line, the
   * {@link CharSequence} length (end) is returned.
   *
   * @param searchIn
   *          The {@link CharSequence} to search in.
   * @param offset
   *          The offset within the {@link CharSequence} where to start the search.
   * @return The next line end character after the given offset. If no one can be found the {@link CharSequence} length
   *         is returned.
   */
  @SuppressWarnings("HardcodedLineSeparator")
  public static int nextLineEnd(CharSequence searchIn, int offset) {
    var nlPos = indexOf('\n', searchIn, offset);
    if (nlPos < 0) {
      return searchIn.length(); // no more newline found: search to the end of searchIn
    }
    if (nlPos > 0 && searchIn.charAt(nlPos - 1) == '\r') {
      nlPos--;
    }
    return nlPos;
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
   * @return A {@link CharSequence} with the new content or {@code null} if the input text is {@code null}.
   */
  public static CharSequence replace(CharSequence text, char search, char replacement) {
    if (text == null) {
      return null;
    }
    if (text.length() < 1) {
      return "";
    }

    var result = new StringBuilder(text.length());
    for (var i = 0; i < text.length(); i++) {
      var c = text.charAt(i);
      if (c == search) {
        result.append(replacement);
      }
      else {
        result.append(c);
      }
    }
    return result;
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
    var buf = new char[s.length()];
    s.getChars(0, buf.length, buf, 0);
    return buf;
  }

  /**
   * <p>
   * Repeat a {@link CharSequence} n times to form a new {@link CharSequence}.
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
   *          the {@link CharSequence} to repeat, may be {@code null}.
   * @param n
   *          number of times to repeat, negative treated as zero.
   * @return a new {@link CharSequence} consisting of the original CharSequence repeated
   */
  public static CharSequence repeat(CharSequence str, int n) {
    if (str == null) {
      return null;
    }
    return str.toString().repeat(n);
  }

  /**
   * <p>
   * Replaces a {@link CharSequence} with another {@link CharSequence} inside a larger {@link CharSequence}
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
   *          the {@link CharSequence} to search for, may be {@code null}.
   * @param replacement
   *          the {@link CharSequence} to replace it with, may be {@code null}.
   * @return the text with any replacements processed, {@code null} if {@code null} input.
   */
  public static CharSequence replace(CharSequence text, CharSequence searchString, CharSequence replacement) {
    if (isEmpty(text) || isEmpty(searchString) || replacement == null || Objects.equals(searchString, replacement)) {
      return text;
    }
    var start = 0;
    var end = indexOf(searchString, text, start);
    if (end == INDEX_NOT_FOUND) {
      return text;
    }
    var replLength = searchString.length();
    var increase = replacement.length() - replLength;
    increase = Math.max(increase, 0);
    increase *= 16;
    var buf = new StringBuilder(text.length() + increase);
    while (end != INDEX_NOT_FOUND) {
      buf.append(text, start, end).append(replacement);
      start = end + replLength;
      end = indexOf(searchString, text, start);
    }
    buf.append(text.subSequence(start, text.length()));
    return buf;
  }

  /**
   * <p>
   * Counts how many times the substring appears in the larger {@link CharSequence}.
   * </p>
   * <p>
   * A {@code null} or empty ("") {@link CharSequence} input returns {@code 0}.
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
   *          the {@link CharSequence} to check, may be {@code null}.
   * @param sub
   *          the substring to count, may be {@code null}.
   * @return the number of occurrences, 0 if either {@link CharSequence} is {@code null}.
   */
  public static int countMatches(CharSequence str, CharSequence sub) {
    if (isEmpty(str) || isEmpty(sub)) {
      return 0;
    }
    var count = 0;
    var idx = 0;
    while ((idx = indexOf(sub, str, idx)) != INDEX_NOT_FOUND) {
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
   * Creates a {@link StringBuilder} holding the content of the file specified.
   *
   * @param file
   *          The file to load. Must not be {@code null}.
   * @param charset
   *          The {@link Charset} to use to transform the bytes in the file into characters. Consider using one of the
   *          {@link StandardCharsets} constants. Must not be {@code null}.
   * @return A {@link StringBuilder} holding the content.
   * @apiNote This method is intended for simple cases where it is convenient to read all chars. It is not intended for
   *          reading large files.
   * @throws IOException
   *           If {@link Path} does not point to a readable file or there was an error during read.
   */
  public static StringBuilder fromFile(Path file, Charset charset) throws IOException {
    Ensure.notNull(file);
    Ensure.notNull(charset);
    //noinspection NumericCastThatLosesPrecision
    var size = (int) Files.size(file);
    try (Reader reader = Files.newBufferedReader(file, charset)) {
      return read(reader, size);
    }
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
   *           While reading data from the stream.
   * @throws IllegalCharsetNameException
   *           If the given charset name is illegal.
   * @throws IllegalArgumentException
   *           If the given charset name is null.
   * @throws UnsupportedCharsetException
   *           If no support for the named charset is available in this instance of the Java virtual machine.
   * @see Charset#isSupported(String)
   * @see Charset#forName(String)
   */
  public static StringBuilder fromInputStream(InputStream is, String charsetName) throws IOException {
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
    return fromInputStream(is, charset, -1);
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
   * @param estimatedSize
   *          The estimated number of bytes returned by the {@link InputStream} or -1 if unknown.
   * @return A {@link StringBuilder} holding the contents.
   * @throws IOException
   *           While reading data from the stream.
   */
  public static StringBuilder fromInputStream(InputStream is, Charset charset, int estimatedSize) throws IOException {
    return read(new BufferedReader(new InputStreamReader(is, charset)), estimatedSize);
  }

  private static StringBuilder read(Reader in, int estimatedSize) throws IOException {
    int length;
    var buffer = new char[8192];
    var out = new StringBuilder(estimatedSize > 0 ? estimatedSize : buffer.length);
    while ((length = in.read(buffer)) != INDEX_NOT_FOUND) {
      out.append(buffer, 0, length);
    }
    return out;
  }

  /**
   * Converts the stack trace of the given {@link Throwable} into a {@link String}.
   * <p>
   * The resulting {@link String} contains no leading or trailing line separators.
   *
   * @param t
   *          The {@link Throwable}. Must not be {@code null}.
   * @return The {@link String} describing the given {@link Throwable}.
   */
  @SuppressWarnings({"squid:S1148", "squid:S1166"})
  public static String fromThrowable(Throwable t) {
    try (var w = new StringWriter(); var p = new PrintWriter(w)) {
      t.printStackTrace(p);
      var buffer = w.getBuffer();
      buffer.delete(buffer.length() - lineSeparator().length(), buffer.length());
      return w.toString();
    }
    catch (IOException e) {
      return '[' + e.toString() + ']' + t;
    }
  }

  /**
   * Converts the given input string literal into the representing original string.<br>
   * This is the inverse function of {@link #toStringLiteral(CharSequence)}.
   *
   * @param s
   *          The literal with leading and ending double-quotes
   * @return the original (un-escaped) string. if it is no valid literal string, {@code null} is returned.
   */
  public static CharSequence fromStringLiteral(CharSequence s) {
    if (s == null) {
      return null;
    }
    return replaceLiterals(withoutQuotes(s), true);
  }

  private static CharSequence replaceLiterals(CharSequence result, boolean fromLiteral) {
    //noinspection HardcodedLineSeparator
    CharSequence[] a = {"\b", "\t", "\n", "\f", "\r", "\"", "\\", "\0", "\1", "\2", "\3", "\4", "\5", "\6", "\7"};
    //noinspection HardcodedLineSeparator
    CharSequence[] b = {"\\b", "\\t", "\\n", "\\f", "\\r", "\\\"", "\\\\", "\\0", "\\1", "\\2", "\\3", "\\4", "\\5", "\\6", "\\7"};

    if (fromLiteral) {
      return replaceEach(result, b, a);
    }
    return replaceEach(result, a, b);
  }

  /**
   * Converts the given {@link CharSequence} into a string literal with leading and ending double-quotes including
   * escaping of the given string.<br>
   * This is the inverse function of {@link #fromStringLiteral(CharSequence)}.
   *
   * @param s
   *          the string to convert.
   * @return the literal string ready to be directly inserted into java source or null if the input string is null.
   */
  public static CharSequence toStringLiteral(CharSequence s) {
    if (s == null) {
      return null;
    }

    var b = new StringBuilder(s.length() * 2);
    b.append('"'); // opening delimiter
    b.append(replaceLiterals(s, false));
    b.append('"'); // closing delimiter
    return b;
  }

  /**
   * Removes leading or trailing double quotes ("), single quotes (') or back ticks (`) from the input.
   *
   * @param literal
   *          The literal from which the quotes should be removed.
   * @return The input with removed quotes.
   */
  public static CharSequence withoutQuotes(CharSequence literal) {
    return withoutQuotes(literal, true, true, true);
  }

  /**
   * Removes leading and trailing quotes (if existing) from the literal given.<br>
   * Only the first quotes are removed. If there are nested quotes, the are part of the result.
   *
   * @param literal
   *          The literal from which the quotes should be removed.
   * @param removeDouble
   *          {@code true} if double quotes (") should be removed if found.
   * @param removeSingle
   *          {@code true} if single quotes (') should be removed if found.
   * @param removeBackTick
   *          {@code true} if back ticks (`) should be removed if found.
   * @return The input with removed leading and trailing quotes respecting the enabled quote types.
   */
  public static CharSequence withoutQuotes(CharSequence literal, boolean removeDouble, boolean removeSingle, boolean removeBackTick) {
    var needsProcessing = removeDouble || removeSingle || removeBackTick;
    if (literal == null || literal.length() < 2 || !needsProcessing) {
      return literal;
    }

    var enabled = new boolean[]{removeDouble, removeSingle, removeBackTick};
    var toRemove = new char[]{'"', '\'', '`'};
    for (var i = 0; i < toRemove.length; i++) {
      if (enabled[i] && literal.charAt(0) == toRemove[i] && literal.charAt(literal.length() - 1) == toRemove[i]) {
        return literal.subSequence(1, literal.length() - 1);
      }
    }
    return literal;
  }

  /**
   * Ensures the given name starts with an upper case character.<br>
   * <br>
   * <b>Note:</b><br>
   * To ensure the first char starts with a lower case letter use {@link Introspector#decapitalize(String)}
   *
   * @param name
   *          The name to handle.
   * @return null if the input is null, an empty string if the given string is empty or only contains white spaces.
   *         Otherwise the input string is returned with the first character modified to upper case.
   */
  public static CharSequence ensureStartWithUpperCase(CharSequence name) {
    if (isEmpty(name) || Character.isUpperCase(name.charAt(0))) {
      return name;
    }
    return new StringBuilder(name.length())
        .append(Character.toUpperCase(name.charAt(0)))
        .append(name, 1, name.length());
  }

  /**
   * Returns the given input HTML with all necessary characters escaped.
   *
   * @param html
   *          The input HTML.
   * @return The escaped version.
   */
  public static CharSequence escapeHtml(CharSequence html) {
    return replaceEach(html,
        new CharSequence[]{"\"", "&", "<", ">", "'", "/"},
        new CharSequence[]{"&#34;", "&#38;", "&#60;", "&#62;", "&#39;", "&#47;"});
  }

  /**
   * <p>
   * Replaces all occurrences of strings within another string.
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
   *          the strings to search for, no-op if {@code null}.
   * @param replacementList
   *          the strings to replace them with, no-op if {@code null}.
   * @return the text with any replacements processed, {@code null} if {@code null} input.
   * @throws IllegalArgumentException
   *           if the lengths of the arrays are not the same ({@code null} is ok, and/or size 0)
   */
  @SuppressWarnings("pmd:NPathComplexity")
  public static CharSequence replaceEach(CharSequence text, CharSequence[] searchList, CharSequence[] replacementList) {
    if (text == null || text.length() == 0) {
      return text;
    }
    if (searchList == null || searchList.length == 0) {
      return text;
    }
    if (replacementList == null || replacementList.length == 0) {
      return text;
    }

    var searchLength = searchList.length;
    var replacementLength = replacementList.length;
    if (searchLength != replacementLength) { // make sure lengths are ok, these need to be equal
      throw newFail("Search and Replace array lengths don't match: {} vs {}", searchLength, replacementLength);
    }

    // keep track of which still have matches
    var noMoreMatchesForReplIndex = new boolean[searchLength];

    // index on index that the match was found
    var textIndex = INDEX_NOT_FOUND;
    var replaceIndex = INDEX_NOT_FOUND;
    int tempIndex;

    // index of replace array that will replace the search string found
    for (var i = 0; i < searchLength; i++) {
      if (noMoreMatchesForReplIndex[i] || searchList[i] == null || searchList[i].length() == 0 || replacementList[i] == null) {
        continue;
      }
      tempIndex = indexOf(searchList[i], text);

      // see if we need to keep searching for this
      if (tempIndex == INDEX_NOT_FOUND) {
        noMoreMatchesForReplIndex[i] = true;
      }
      else {
        if (textIndex == INDEX_NOT_FOUND || tempIndex < textIndex) {
          textIndex = tempIndex;
          replaceIndex = i;
        }
      }
    }

    // no search strings found, we are done
    if (textIndex == INDEX_NOT_FOUND) {
      return text;
    }

    var start = 0;

    // get a good guess on the size of the result buffer so it doesn't have to double if it goes over a bit
    var increase = getLengthIncreaseGuess(text, searchList, replacementList);
    var result = new StringBuilder(text.length() + increase);
    while (textIndex != INDEX_NOT_FOUND) {

      for (var i = start; i < textIndex; i++) {
        result.append(text.charAt(i));
      }
      result.append(replacementList[replaceIndex]);

      start = textIndex + searchList[replaceIndex].length();

      textIndex = INDEX_NOT_FOUND;
      replaceIndex = INDEX_NOT_FOUND;
      // find the next earliest match
      for (var i = 0; i < searchLength; i++) {
        if (noMoreMatchesForReplIndex[i] || searchList[i] == null || searchList[i].length() == 0 || replacementList[i] == null) {
          continue;
        }
        tempIndex = indexOf(searchList[i], text, start);

        // see if we need to keep searching for this
        if (tempIndex == INDEX_NOT_FOUND) {
          noMoreMatchesForReplIndex[i] = true;
        }
        else {
          if (textIndex == INDEX_NOT_FOUND || tempIndex < textIndex) {
            textIndex = tempIndex;
            replaceIndex = i;
          }
        }
      }
    }
    var textLength = text.length();
    for (var i = start; i < textLength; i++) {
      result.append(text.charAt(i));
    }
    return result;
  }

  private static int getLengthIncreaseGuess(CharSequence text, CharSequence[] searchList, CharSequence[] replacementList) {
    var increase = 0;
    // count the replacement text elements that are larger than their corresponding text being replaced
    //noinspection Convert2streamapi
    for (var i = 0; i < searchList.length; i++) {
      if (searchList[i] == null || replacementList[i] == null) {
        continue;
      }
      var longer = replacementList[i].length() - searchList[i].length();
      if (longer > 0) {
        increase += 3 * longer; // assume 3 matches
      }
    }
    // have upper-bound at 20% increase, then let Java take over
    return Math.min(increase, text.length() / 5);
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
    //noinspection Convert2streamapi
    for (var i = 0; i < strLen; i++) {
      if (!Character.isWhitespace(cs.charAt(i))) {
        return false;
      }
    }
    return true;
  }

  /**
   * Case-sensitively checks if str starts with the given prefix
   * 
   * @param str
   *          The {@link CharSequence} to check, may be {@code null}.
   * @param prefix
   *          The prefix, may be {@code null}.
   * @return {@code true} if both sequences are not {@code null} and str case-sensitively starts with the given prefix
   *         or the prefix has length 0.
   */
  public static boolean startsWith(CharSequence str, CharSequence prefix) {
    return startsWith(str, prefix, true);
  }

  /**
   * Checks if str starts with the given prefix
   *
   * @param str
   *          The {@link CharSequence} to check, may be {@code null}.
   * @param prefix
   *          The prefix, may be {@code null}.
   * @param isCaseSensitive
   *          Specifies if a case-sensitive check should be performed ({@code true}) or not ({@code false}).
   * @return {@code true} if both sequences are not {@code null} and str starts with the given prefix or the prefix has
   *         length 0.
   */
  @SuppressWarnings("DuplicatedCode")
  public static boolean startsWith(CharSequence str, CharSequence prefix, boolean isCaseSensitive) {
    if (str == null || prefix == null) {
      return false;
    }
    var strLength = str.length();
    var prefixLength = prefix.length();
    if (strLength < prefixLength) {
      return false;
    }
    if (prefixLength == 0) {
      return true;
    }

    var prefixIter = prefix.codePoints().iterator();
    var strIter = str.codePoints().iterator();
    if (isCaseSensitive) {
      while (prefixIter.hasNext() && strIter.hasNext()) {
        if (prefixIter.nextInt() != strIter.nextInt()) {
          return false;
        }
      }
    }
    else {
      while (prefixIter.hasNext() && strIter.hasNext()) {
        if (Character.toLowerCase(prefixIter.nextInt()) != Character.toLowerCase(strIter.nextInt())) {
          return false;
        }
      }
    }
    return true;
  }

  /**
   * Case-sensitively checks if str starts with the given prefix
   *
   * @param str
   *          The {@link String} to check, may be {@code null}.
   * @param prefix
   *          The prefix, may be {@code null}.
   * @return {@code true} if both strings are not {@code null} and str case-sensitively starts with the given prefix or
   *         the prefix has length 0.
   */
  public static boolean startsWith(String str, String prefix) {
    return startsWith(str, prefix, true);
  }

  /**
   * Checks if str starts with the given prefix
   *
   * @param str
   *          The {@link String} to check, may be {@code null}.
   * @param prefix
   *          The prefix, may be {@code null}.
   * @param isCaseSensitive
   *          Specifies if a case-sensitive check should be performed ({@code true}) or not ({@code false}).
   * @return {@code true} if both sequences are not {@code null} and str starts with the given prefix or the prefix has
   *         length 0.
   */
  public static boolean startsWith(String str, String prefix, boolean isCaseSensitive) {
    if (str == null || prefix == null) {
      return false;
    }
    var stringLength = str.length();
    var prefixLength = prefix.length();
    return stringLength >= prefixLength
        && str.regionMatches(!isCaseSensitive, 0, prefix, 0, prefixLength);
  }

  /**
   * Tests if the string specified ends with the specified suffix.
   *
   * @param string
   *          The {@link CharSequence}, may be {@code null}.
   * @param suffix
   *          The suffix to search, may be {@code null}.
   * @return {@code true} if both sequences are not {@code null} and the string ends with the specified suffix or the
   *         suffix has length 0.
   */
  public static boolean endsWith(CharSequence string, CharSequence suffix) {
    return endsWith(string, suffix, true);
  }

  /**
   * Tests if the string specified ends with the specified suffix.
   *
   * @param string
   *          The {@link CharSequence} to test, or
   * @param suffix
   *          The suffix to search
   * @param isCaseSensitive
   *          Specifies if a case-sensitive check should be performed ({@code true}) or not ({@code false}).
   * @return {@code true} if both sequences are not {@code null} and the string ends with the specified suffix or the
   *         suffix has length 0.
   */
  @SuppressWarnings("DuplicatedCode")
  public static boolean endsWith(CharSequence string, CharSequence suffix, boolean isCaseSensitive) {
    if (string == null || suffix == null) {
      return false;
    }
    var stringLength = string.length();
    var suffixLength = suffix.length();
    if (stringLength < suffixLength) {
      return false;
    }
    if (suffixLength == 0) {
      return true;
    }
    if (isCaseSensitive) {
      for (int i = stringLength - 1, j = suffixLength - 1; j >= 0; i--, j--) {
        if (string.charAt(i) != suffix.charAt(j)) {
          return false;
        }
      }
    }
    else {
      for (int i = stringLength - 1, j = suffixLength - 1; j >= 0; i--, j--) {
        if (Character.toLowerCase(string.charAt(i)) != Character.toLowerCase(suffix.charAt(j))) {
          return false;
        }
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
    if (a == null && b == null) {
      return 0;
    }
    if (a == null) {
      return INDEX_NOT_FOUND;
    }
    if (b == null) {
      return 1;
    }
    var limit = Math.min(a.length(), b.length());
    for (var i = 0; i < limit; i++) {
      var x = a.charAt(i);
      var y = b.charAt(i);
      //noinspection CharUsedInArithmeticContext
      var diff = x - y;
      if (diff != 0) {
        return diff;
      }
    }
    return a.length() - b.length();
  }
}
