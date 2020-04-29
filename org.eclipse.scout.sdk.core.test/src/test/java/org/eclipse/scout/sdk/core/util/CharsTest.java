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

import static org.eclipse.scout.sdk.core.util.Chars.indexOf;
import static org.eclipse.scout.sdk.core.util.Chars.lastIndexOf;
import static org.eclipse.scout.sdk.core.util.Chars.nextLineEnd;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.CharBuffer;

import org.junit.jupiter.api.Test;

public class CharsTest {
  @Test
  public void testEqualsCharArray() {
    char[] a = "".toCharArray();
    char[] b = "xx".toCharArray();
    char[] c = "xx".toCharArray();
    char[] d = "yy".toCharArray();
    char[] e = "xxx".toCharArray();
    assertTrue(Chars.equals(a, a));
    assertTrue(Chars.equals(c, b));
    assertFalse(Chars.equals(null, b));
    assertFalse(Chars.equals(a, null));
    assertFalse(Chars.equals(c, d));
    assertFalse(Chars.equals(c, e));
    assertTrue(Chars.equals(null, (char[]) null));
  }

  @Test
  public void testEqualsCharArrayIgnoreCase() {
    char[] a = "".toCharArray();
    char[] b = "xx".toCharArray();
    char[] c = "xx".toCharArray();
    char[] d = "yy".toCharArray();
    char[] e = "xxx".toCharArray();
    char[] f = "xXx".toCharArray();
    assertTrue(Chars.equals(a, a, true));
    assertTrue(Chars.equals(c, b, true));
    assertFalse(Chars.equals(null, b, true));
    assertFalse(Chars.equals(a, null, true));
    assertFalse(Chars.equals(c, d, true));
    assertFalse(Chars.equals(c, e, true));
    assertTrue(Chars.equals(null, (char[]) null, true));

    assertTrue(Chars.equals(a, a, false));
    assertTrue(Chars.equals(c, b, false));
    assertFalse(Chars.equals(null, b, false));
    assertFalse(Chars.equals(a, null, false));
    assertFalse(Chars.equals(c, d, false));
    assertFalse(Chars.equals(c, e, false));
    assertTrue(Chars.equals(null, (char[]) null, false));
    assertTrue(Chars.equals(e, f, false));
  }

  @Test
  public void testEqualsCharSequence() {
    CharSequence a = "";
    CharSequence b = CharBuffer.wrap("xx".toCharArray());
    CharSequence c = "xx";
    CharSequence d = "yy";
    CharSequence e = "xxx";
    assertTrue(Chars.equals(a, a));
    assertTrue(Chars.equals(c, b));
    assertFalse(Chars.equals(null, b));
    assertFalse(Chars.equals(a, null));
    assertFalse(Chars.equals(c, d));
    assertFalse(Chars.equals(c, e));
    assertTrue(Chars.equals(null, (CharSequence) null));
  }

  @Test
  public void testEqualsCharSequenceIgnoreCase() {
    CharSequence a = "";
    CharSequence b = CharBuffer.wrap("xx".toCharArray());
    CharSequence c = "xx";
    CharSequence d = "yy";
    CharSequence e = "xxx";
    CharSequence f = "xXx";
    assertTrue(Chars.equals(a, a, true));
    assertTrue(Chars.equals(c, b, true));
    assertFalse(Chars.equals(null, b, true));
    assertFalse(Chars.equals(a, null, true));
    assertFalse(Chars.equals(c, d, true));
    assertFalse(Chars.equals(c, e, true));
    assertTrue(Chars.equals(null, (CharSequence) null, true));

    assertTrue(Chars.equals(a, a, false));
    assertTrue(Chars.equals(c, b, false));
    assertFalse(Chars.equals(null, b, false));
    assertFalse(Chars.equals(a, null, false));
    assertFalse(Chars.equals(c, d, false));
    assertFalse(Chars.equals(c, e, false));
    assertTrue(Chars.equals(null, (CharSequence) null, false));
    assertTrue(Chars.equals(e, f, false));
  }

  @Test
  public void testIndexOfCharArray() {
    assertEquals(3, indexOf('d', "abcdraqrd".toCharArray(), 0, 4));
    assertEquals(-1, indexOf('d', "abcdgaerd".toCharArray(), 0, 3));
    assertEquals(3, indexOf('d', "abcdw4easd".toCharArray(), 0, 100));
    assertEquals(-1, indexOf('x', "abcd".toCharArray(), 0, 100));
    assertEquals(-1, indexOf('a', "abcdrgtd".toCharArray(), 1));
    assertEquals(-1, indexOf('d', "abcd".toCharArray(), 1, 3));
    assertEquals(-1, indexOf('x', "abcd".toCharArray()));
    assertEquals(3, indexOf('d', "abcdasdfd".toCharArray()));
  }

  @Test
  public void testIndexOfCharSequence() {
    assertEquals(3, indexOf('d', "abcdraqrd", 0, 4));
    assertEquals(-1, indexOf('d', "abcdgaerd", 0, 3));
    assertEquals(3, indexOf('d', "abcdw4easd", 0, 100));
    assertEquals(-1, indexOf('x', "abcd", 0, 100));
    assertEquals(-1, indexOf('a', "abcdrgtd", 1));
    assertEquals(-1, indexOf('d', "abcd", 1, 3));
    assertEquals(-1, indexOf('x', "abcd"));
    assertEquals(3, indexOf('d', "abcdasdfd"));
  }

  @Test
  public void testIndexOfCharsInChars() {
    assertEquals(-1, indexOf("abcdd".toCharArray(), "abc".toCharArray()));
    assertEquals(-1, indexOf("abcdd".toCharArray(), "abc".toCharArray(), -1));
    assertEquals(-1, indexOf("abcdd".toCharArray(), "abc".toCharArray(), -1, 3));
    assertEquals(-1, indexOf("x".toCharArray(), "abc".toCharArray(), -1));
    assertEquals(0, indexOf("".toCharArray(), "abcdd".toCharArray()));

    assertEquals(-1, indexOf("abc".toCharArray(), "xxabcxx".toCharArray(), 0, 3));
    assertEquals(-1, indexOf("abc".toCharArray(), "xxabcxx".toCharArray(), 3, 6));
    assertEquals(2, indexOf("abc".toCharArray(), "xxabcxx".toCharArray(), 0, 5));
    assertEquals(0, indexOf("abc".toCharArray(), "abc".toCharArray()));
    assertEquals(-1, indexOf("abx".toCharArray(), "abc".toCharArray()));
    assertEquals(-1, indexOf("abx".toCharArray(), "abcddd".toCharArray()));

    assertEquals(-1, indexOf("abc".toCharArray(), "xxabcxx".toCharArray(), 0, 3, false));
    assertEquals(-1, indexOf("abc".toCharArray(), "xxabcxx".toCharArray(), 3, 6, false));
    assertEquals(2, indexOf("abc".toCharArray(), "xxabcxx".toCharArray(), 0, 5, false));
    assertEquals(0, indexOf("abc".toCharArray(), "abc".toCharArray(), 0, 3, false));
    assertEquals(-1, indexOf("abx".toCharArray(), "abc".toCharArray(), 0, 3, false));
    assertEquals(-1, indexOf("abx".toCharArray(), "abcddd".toCharArray(), 0, 6, false));

    assertEquals(-1, indexOf("abc".toCharArray(), "abc".toCharArray(), 3, 3, false));
  }

  @Test
  public void testIndexOfCharsInChars2() {
    char[] array = new char[]{'a', 'b', 'c'};
    char[] array2 = new char[]{'a', 'b', 'c', 'a', 'a'};
    assertTrue(indexOf(array, array2, -1, array2.length, true) < 0);
  }

  @Test
  public void testIndexOfCharsInChars3() {
    char[] array = new char[]{'a', 'b', 'c'};
    char[] array2 = new char[]{'a', 'b', 'c', 'a', 'a'};
    assertTrue(indexOf(array, array2, -1, array2.length, false) < 0);
  }

  @Test
  public void testNextLineEnd() {
    assertEquals(3, nextLineEnd("abc".toCharArray(), 0));
    assertEquals(5, nextLineEnd("first\nsecond\nthird".toCharArray(), 0));
    assertEquals(12, nextLineEnd("first\nsecond\nthird".toCharArray(), 6));
    assertEquals(12, nextLineEnd("first\nsecond\r\nthird".toCharArray(), 6));
    assertEquals(19, nextLineEnd("first\nsecond\r\nthird".toCharArray(), 14));
    assertEquals(19, nextLineEnd("first\nsecond\r\nthird".toCharArray(), 100));
    assertEquals(3, nextLineEnd("abc\n".toCharArray(), 0));
    assertEquals(3, nextLineEnd("abc\r\n".toCharArray(), 0));
    assertEquals(3, nextLineEnd("abc\r\n".toCharArray(), 3));
    assertEquals(3, nextLineEnd("abc\r\n".toCharArray(), 4));
    assertEquals(0, nextLineEnd("\nsecond".toCharArray(), 0));
  }

  @Test
  public void testLastIndexOf() {
    assertEquals(-1, lastIndexOf('x', "abc"));
    assertEquals(1, lastIndexOf('x', "axc"));
    assertEquals(3, lastIndexOf('x', "axcxd"));

    assertEquals(-1, lastIndexOf('x', "abcxd", 0, 3));
    assertEquals(1, lastIndexOf('x', "axcxd", 0, 3));
    assertEquals(3, lastIndexOf('x', "axcxd", 0, 4));
    assertEquals(5, lastIndexOf('x', "axcbbxd", 2));
    assertEquals(-1, lastIndexOf('x', "axcbbxd", 2, 4));
  }
}
