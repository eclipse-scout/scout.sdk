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
package org.eclipse.scout.sdk.core.util;

/**
 * Holds helper methods to deal with char arrays and {@link CharSequence}s.
 */
public final class Chars {

  private Chars() {
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
    for (int i = first.length; --i >= 0;) {
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
    for (int i = first.length; --i >= 0;) {
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
    for (int i = first.length(); --i >= 0;) {
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
    for (int i = first.length(); --i >= 0;) {
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
    int limit = Math.min(end, searchIn.length);
    for (int i = start; i < limit; ++i) {
      if (toBeFound == searchIn[i]) {
        return i;
      }
    }
    return -1;
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
    int limit = Math.max(Math.min(end, searchIn.length()), 0);
    for (int i = start; i < limit; i++) {
      if (toBeFound == searchIn.charAt(i)) {
        return i;
      }
    }
    return -1;
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
   * Answers the first index in searchIn for which toBeFound is a matching subarray following the case rule starting at
   * the index start. Answers -1 if no match is found.<br>
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
   *          the subarray to search
   * @param searchIn
   *          the array to be searched
   * @param start
   *          the starting index (inclusive)
   * @param end
   *          the end index (exclusive)
   * @param isCaseSensitive
   *          flag to know if the matching should be case sensitive
   * @return the first index in searchIn for which the toBeFound array is a matching subarray following the case rule.
   * @throws NullPointerException
   *           if searchIn is {@code null} or toBeFound is {@code null}
   */
  public static int indexOf(char[] toBeFound, char[] searchIn, int start, int end, boolean isCaseSensitive) {
    int toBeFoundLength = toBeFound.length;
    if (toBeFoundLength > end || start < 0) {
      return -1;
    }
    if (toBeFoundLength == 0) {
      return 0;
    }
    if (isCaseSensitive) {
      arrayLoop: for (int i = start, max = end - toBeFoundLength + 1; i < max; i++) {
        if (searchIn[i] == toBeFound[0]) {
          for (int j = 1; j < toBeFoundLength; j++) {
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
          for (int j = 1; j < toBeFoundLength; j++) {
            if (Character.toLowerCase(searchIn[i + j]) != Character.toLowerCase(toBeFound[j])) {
              continue arrayLoop;
            }
          }
          return i;
        }
      }
    }
    return -1;
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
    for (int i = endIndex; --i >= startIndex;) {
      if (toBeFound == searchIn.charAt(i)) {
        return i;
      }
    }
    return -1;
  }

  /**
   * Gets the next index after the given offset at which the current line ends. If invoked for the last line, the array
   * length (end) is returned.
   * 
   * @param searchIn
   *          The array to search in.
   * @param offset
   *          The offset within the array where to start the search.
   * @return The next line end character after the given offset. If no one can be found the array length is returned.
   */
  @SuppressWarnings("HardcodedLineSeparator")
  public static int nextLineEnd(char[] searchIn, int offset) {
    int nlPos = indexOf('\n', searchIn, offset);
    if (nlPos < 0) {
      return searchIn.length; // no more newline found: search to the end of searchIn
    }
    if (nlPos > 0 && searchIn[nlPos - 1] == '\r') {
      nlPos--;
    }
    return nlPos;
  }
}
