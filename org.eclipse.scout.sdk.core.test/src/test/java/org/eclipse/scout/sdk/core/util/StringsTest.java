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

import static java.lang.System.lineSeparator;
import static org.eclipse.scout.sdk.core.util.Strings.compareTo;
import static org.eclipse.scout.sdk.core.util.Strings.countMatches;
import static org.eclipse.scout.sdk.core.util.Strings.endsWith;
import static org.eclipse.scout.sdk.core.util.Strings.ensureStartWithUpperCase;
import static org.eclipse.scout.sdk.core.util.Strings.escapeHtml;
import static org.eclipse.scout.sdk.core.util.Strings.fromFileAsCharSequence;
import static org.eclipse.scout.sdk.core.util.Strings.fromFileAsChars;
import static org.eclipse.scout.sdk.core.util.Strings.fromFileAsString;
import static org.eclipse.scout.sdk.core.util.Strings.fromInputStream;
import static org.eclipse.scout.sdk.core.util.Strings.fromStringLiteral;
import static org.eclipse.scout.sdk.core.util.Strings.fromThrowable;
import static org.eclipse.scout.sdk.core.util.Strings.hasText;
import static org.eclipse.scout.sdk.core.util.Strings.indexOf;
import static org.eclipse.scout.sdk.core.util.Strings.isBlank;
import static org.eclipse.scout.sdk.core.util.Strings.lastIndexOf;
import static org.eclipse.scout.sdk.core.util.Strings.nextLineEnd;
import static org.eclipse.scout.sdk.core.util.Strings.notBlank;
import static org.eclipse.scout.sdk.core.util.Strings.notEmpty;
import static org.eclipse.scout.sdk.core.util.Strings.repeat;
import static org.eclipse.scout.sdk.core.util.Strings.replace;
import static org.eclipse.scout.sdk.core.util.Strings.replaceEach;
import static org.eclipse.scout.sdk.core.util.Strings.toCharArray;
import static org.eclipse.scout.sdk.core.util.Strings.toStringLiteral;
import static org.eclipse.scout.sdk.core.util.Strings.withoutQuotes;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.junit.jupiter.api.Test;

/**
 * <h3>{@link StringsTest}</h3>
 *
 * @since 6.1.0
 */
public class StringsTest {

  @Test
  public void testInputStreamToString() throws IOException {
    String testData = "my test data";
    assertEquals(testData, fromInputStream(new ByteArrayInputStream(testData.getBytes(StandardCharsets.UTF_16LE)), StandardCharsets.UTF_16LE).toString());
    assertEquals(testData, fromInputStream(new ByteArrayInputStream(testData.getBytes(StandardCharsets.UTF_16BE)), StandardCharsets.UTF_16BE.name()).toString());
  }

  @Test
  public void testToCharArray() {
    assertArrayEquals("".toCharArray(), toCharArray(new StringBuilder()));
    assertArrayEquals("abc".toCharArray(), toCharArray(new StringBuilder("abc")));
    assertThrows(IllegalArgumentException.class, () -> toCharArray(null));
  }

  @Test
  public void testFromThrowable() {
    String s = fromThrowable(new Exception());
    assertFalse(s.startsWith(lineSeparator()));
    assertFalse(s.endsWith(lineSeparator()));
  }

  @Test
  public void testFromFile() throws IOException {
    assertThrows(IOException.class, () -> fromFileAsString(Paths.get("not.existing.file"), StandardCharsets.UTF_8));
    assertThrows(IOException.class, () -> fromFileAsChars(Paths.get("not.existing.file"), StandardCharsets.UTF_8));
    assertThrows(IOException.class, () -> fromFileAsCharSequence(Paths.get("not.existing.file"), StandardCharsets.UTF_8));
    assertThrows(IllegalArgumentException.class, () -> fromFileAsString(Paths.get("not.existing.file"), null));
    assertThrows(IllegalArgumentException.class, () -> fromFileAsChars(Paths.get("not.existing.file"), null));
    assertThrows(IllegalArgumentException.class, () -> fromFileAsCharSequence(Paths.get("not.existing.file"), null));
    assertThrows(IllegalArgumentException.class, () -> fromFileAsString(null, StandardCharsets.UTF_8));
    assertThrows(IllegalArgumentException.class, () -> fromFileAsChars(null, StandardCharsets.UTF_8));
    assertThrows(IllegalArgumentException.class, () -> fromFileAsCharSequence(null, StandardCharsets.UTF_8));

    String testFileContent = "testcontent\nnewline";
    Path testFile = Files.createTempFile("scoutSdkTestFile", ".txt");
    try {
      // write test content
      Files.write(testFile, testFileContent.getBytes(StandardCharsets.UTF_8), StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);

      assertEquals(testFileContent, fromFileAsCharSequence(testFile, StandardCharsets.UTF_8).toString());
      assertArrayEquals(testFileContent.toCharArray(), fromFileAsChars(testFile, StandardCharsets.UTF_8));
      assertEquals(testFileContent, fromFileAsString(testFile, StandardCharsets.UTF_8));
    }
    finally {
      Files.delete(testFile);
    }
  }

  @Test
  public void testRepeat() {
    assertNull(repeat(null, 1));
    assertEquals("", repeat("", 10));
    assertEquals("", repeat("asdf", 0));
    assertEquals("", repeat("asdf", -1));
    assertEquals("aaa", repeat("a", 3).toString());
    assertEquals("abab", repeat("ab", 2).toString());
  }

  @Test
  public void testEndsWith() {
    assertTrue(endsWith("", ""));
    assertTrue(endsWith("abc", ""));
    assertFalse(endsWith(null, ""));
    assertFalse(endsWith("abc", null));
    assertFalse(endsWith("", null));
    assertFalse(endsWith(null, null));
    assertFalse(endsWith(null, "abc"));
    assertFalse(endsWith("abc", "de"));
    assertFalse(endsWith("abc", "abcde"));
    assertTrue(endsWith("aabc", "bc"));
    assertTrue(endsWith("aa  ", " "));
    assertTrue(endsWith("", ""));
    assertTrue(endsWith("abcd", "abcd"));
  }

  @Test
  public void testReplace() {
    assertNull(replace(null, null, null));
    assertEquals("", replace("", null, null));
    assertEquals("asdf", replace("asdf", null, "ff"));
    assertEquals("asdf", replace("asdf", "f", null));
    assertEquals("asdf", replace("asdf", "", null));
    assertEquals("sdf", replace("asdf", "a", "").toString());
    assertEquals("gsdf", replace("asdf", "a", "g").toString());
    assertEquals("asdf", replace("asdf", "xx", "g").toString());
  }

  @Test
  public void testCountMatches() {
    assertEquals(0, countMatches(null, "asdf"));
    assertEquals(0, countMatches("", "sss"));
    assertEquals(0, countMatches("abba", null));
    assertEquals(0, countMatches("abba", ""));
    assertEquals(2, countMatches("abba", "a"));
    assertEquals(1, countMatches("abba", "ab"));
    assertEquals(0, countMatches("abba", "xxx"));
  }

  @Test
  public void testInputStreamToStringWrongCharset() {
    assertThrows(IOException.class, () -> fromInputStream(new ByteArrayInputStream("test".getBytes(StandardCharsets.UTF_16LE)), "not-existing"));
  }

  @Test
  public void testEscapeHtml() {
    assertEquals("", escapeHtml(""));
    assertEquals("a&#38;&#60;&#62;&#34;&#47;&#39;&#39;b", escapeHtml("a&<>\"/''b").toString());
    assertNull(escapeHtml(null));
  }

  @Test
  public void testIsBlankHasText() {
    assertTrue(isBlank(null));
    assertTrue(isBlank(""));
    assertTrue(isBlank(" "));
    assertFalse(isBlank("bob"));
    assertFalse(isBlank("  bob  "));
    assertTrue(isBlank("  \t\r\n  "));

    assertFalse(hasText(null));
    assertFalse(hasText(""));
    assertFalse(hasText(" "));
    assertTrue(hasText("bob"));
    assertTrue(hasText("  bob  "));
    assertFalse(hasText("  \t\r\n  "));
  }

  @Test
  public void testFromStringLiteral() {
    assertNull(fromStringLiteral(null));
    assertEquals("a", fromStringLiteral("a"));
    assertEquals("\"a\nb", fromStringLiteral("\"a\\nb").toString());
    assertEquals("aaa\"", fromStringLiteral("aaa\"").toString());
    assertEquals("a\nb", fromStringLiteral("\"a\\nb\"").toString());
    assertEquals("", fromStringLiteral("\"\"").toString());
    assertEquals("a\"b", fromStringLiteral("\"a\\\"b\"").toString());
  }

  @Test
  public void testToStringLiteral() {
    assertEquals("\"a\\nb\"", toStringLiteral("a\nb").toString());
    assertEquals("\"a\\\"b\"", toStringLiteral("a\"b").toString());
    assertNull(toStringLiteral(null));
    CharSequence in = "teststring \na\"";
    assertEquals(in, fromStringLiteral(toStringLiteral(in)).toString());
  }

  @Test
  public void testWithoutQuotes() {
    assertEquals("", withoutQuotes(""));
    assertNull(withoutQuotes(null));
    assertEquals("a", withoutQuotes("a"));
    assertEquals("aaa", withoutQuotes("aaa"));

    assertEquals("aaa", withoutQuotes("'aaa'"));
    assertEquals("'aaa'", withoutQuotes("'aaa'", true, false, true));

    assertEquals("aaa", withoutQuotes("`aaa`"));
    assertEquals("`aaa`", withoutQuotes("`aaa`", true, true, false));

    assertEquals("aaa", withoutQuotes("\"aaa\""));
    assertEquals("\"aaa\"", withoutQuotes("\"aaa\"", false, true, false));

    assertEquals("'aaa'", withoutQuotes("'aaa'", false, false, false));
    assertEquals("'`aaa`'", withoutQuotes("'`aaa`'", false, false, true));
    assertEquals("'aaa'", withoutQuotes("''aaa''", false, true, false));
  }

  @Test
  public void testReplaceEach() {
    assertNull(replaceEach(null, new String[]{"aa"}, new String[]{"bb"}));
    assertEquals("", replaceEach("", new String[]{"aa"}, new String[]{"bb"}));
    assertEquals("aba", replaceEach("aba", null, null));
    assertEquals("aba", replaceEach("aba", new String[0], null));
    assertEquals("aba", replaceEach("aba", null, new String[0]));
    assertEquals("aba", replaceEach("aba", new String[]{"a"}, null));
    assertEquals("b", replaceEach("aba", new String[]{"a"}, new String[]{""}).toString());
    assertEquals("aba", replaceEach("aba", new String[]{null}, new String[]{"a"}).toString());
    assertEquals("aba", replaceEach("aba", new String[]{"b"}, new String[]{null}).toString());
    assertEquals("wcte", replaceEach("abcde", new String[]{"ab", "d"}, new String[]{"w", "t"}).toString());
    assertEquals("abcde", replaceEach("abcde", new String[]{"ab", "d"}, new String[]{}).toString());
    assertThrows(IllegalArgumentException.class, () -> assertEquals("abcde", replaceEach("abcde", new String[]{"ab", "d"}, new String[]{"a"}).toString()));
  }

  @Test
  public void testEnsureStartWithUpperCase() {
    assertNull(ensureStartWithUpperCase(null));
    assertEquals("", ensureStartWithUpperCase(""));
    assertEquals("  ", ensureStartWithUpperCase("  ").toString());
    assertEquals("A", ensureStartWithUpperCase("a").toString());
    assertEquals("Ab", ensureStartWithUpperCase("ab").toString());
    assertEquals("A", ensureStartWithUpperCase("A").toString());
    assertEquals("Ab", ensureStartWithUpperCase("Ab").toString());
    assertEquals("ABC", ensureStartWithUpperCase("ABC").toString());
    assertEquals("Abc", ensureStartWithUpperCase("abc").toString());
    assertEquals("ABC", ensureStartWithUpperCase("aBC").toString());
  }

  @Test
  public void testEqualsCharArray() {
    char[] a = "".toCharArray();
    char[] b = "xx".toCharArray();
    char[] c = "xx".toCharArray();
    char[] d = "yy".toCharArray();
    char[] e = "xxx".toCharArray();
    assertTrue(Strings.equals(a, a));
    assertTrue(Strings.equals(c, b));
    assertFalse(Strings.equals(null, b));
    assertFalse(Strings.equals(a, null));
    assertFalse(Strings.equals(c, d));
    assertFalse(Strings.equals(c, e));
    assertTrue(Strings.equals(null, (char[]) null));
  }

  @Test
  public void testEqualsCharArrayIgnoreCase() {
    char[] a = "".toCharArray();
    char[] b = "xx".toCharArray();
    char[] c = "xx".toCharArray();
    char[] d = "yy".toCharArray();
    char[] e = "xxx".toCharArray();
    char[] f = "xXx".toCharArray();
    assertTrue(Strings.equals(a, a, true));
    assertTrue(Strings.equals(c, b, true));
    assertFalse(Strings.equals(null, b, true));
    assertFalse(Strings.equals(a, null, true));
    assertFalse(Strings.equals(c, d, true));
    assertFalse(Strings.equals(c, e, true));
    assertTrue(Strings.equals(null, (char[]) null, true));

    assertTrue(Strings.equals(a, a, false));
    assertTrue(Strings.equals(c, b, false));
    assertFalse(Strings.equals(null, b, false));
    assertFalse(Strings.equals(a, null, false));
    assertFalse(Strings.equals(c, d, false));
    assertFalse(Strings.equals(c, e, false));
    assertTrue(Strings.equals(null, (char[]) null, false));
    assertTrue(Strings.equals(e, f, false));
  }

  @Test
  public void testEqualsCharSequence() {
    CharSequence a = "";
    CharSequence b = CharBuffer.wrap("xx".toCharArray());
    CharSequence c = "xx";
    CharSequence d = "yy";
    CharSequence e = "xxx";
    assertTrue(Strings.equals(a, a));
    assertTrue(Strings.equals(c, b));
    assertFalse(Strings.equals(null, b));
    assertFalse(Strings.equals(a, null));
    assertFalse(Strings.equals(c, d));
    assertFalse(Strings.equals(c, e));
    assertTrue(Strings.equals(null, (CharSequence) null));
  }

  @Test
  public void testEqualsCharSequenceIgnoreCase() {
    CharSequence a = "";
    CharSequence b = CharBuffer.wrap("xx".toCharArray());
    CharSequence c = "xx";
    CharSequence d = "yy";
    CharSequence e = "xxx";
    CharSequence f = "xXx";
    assertTrue(Strings.equals(a, a, true));
    assertTrue(Strings.equals(c, b, true));
    assertFalse(Strings.equals(null, b, true));
    assertFalse(Strings.equals(a, null, true));
    assertFalse(Strings.equals(c, d, true));
    assertFalse(Strings.equals(c, e, true));
    assertTrue(Strings.equals(null, (CharSequence) null, true));

    assertTrue(Strings.equals(a, a, false));
    assertTrue(Strings.equals(c, b, false));
    assertFalse(Strings.equals(null, b, false));
    assertFalse(Strings.equals(a, null, false));
    assertFalse(Strings.equals(c, d, false));
    assertFalse(Strings.equals(c, e, false));
    assertTrue(Strings.equals(null, (CharSequence) null, false));
    assertTrue(Strings.equals(e, f, false));
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
  public void testIndexOfCharSequenceInCharSequence() {
    assertEquals(-1, indexOf("abcdd", "abc"));
    assertEquals(-1, indexOf("abcdd", "abc", -1));
    assertEquals(-1, indexOf("abcdd", "abc", -1, 3));
    assertEquals(-1, indexOf("x", "abc", -1));
    assertEquals(0, indexOf("", "abcdd"));

    assertEquals(-1, indexOf("abc", "xxabcxx", 0, 3));
    assertEquals(-1, indexOf("abc", "xxabcxx", 3, 6));
    assertEquals(2, indexOf("abc", "xxabcxx", 0, 5));
    assertEquals(0, indexOf("abc", "abc"));
    assertEquals(-1, indexOf("abx", "abc"));
    assertEquals(-1, indexOf("abx", "abcddd"));

    assertEquals(-1, indexOf("abc", "xxabcxx", 0, 3));
    assertEquals(-1, indexOf("abc", "xxabcxx", 3, 6));
    assertEquals(2, indexOf("abc", "xxabcxx", 0, 5));
    assertEquals(0, indexOf("abc", "abc", 0, 3));
    assertEquals(-1, indexOf("abx", "abc", 0, 3));
    assertEquals(-1, indexOf("abx", "abcddd", 0, 6));

    assertEquals(-1, indexOf("abc", "abc", 3, 3));
  }

  @Test
  public void testIndexOfCharSequenceInCharSequence2() {
    CharSequence array = "abc";
    CharSequence array2 = "abcaa";
    assertTrue(indexOf(array, array2, -1, array2.length()) < 0);
  }

  @Test
  public void testIndexOfCharSequenceInCharSequence3() {
    CharSequence array = "abc";
    CharSequence array2 = "abcaa";
    assertTrue(indexOf(array, array2, -1, array2.length()) < 0);
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

  @Test
  public void testReplaceSequence() {
    assertNull(replace(null, 'a', 'b'));
    assertEquals("", replace("", 'a', 'b'));
    assertEquals("akdf", replace("asdf", 's', 'k').toString());
    assertEquals("ksdf", replace("asdf", 'a', 'k').toString());
    assertEquals("asdk", replace("asdf", 'f', 'k').toString());
  }

  @Test
  public void testNotEmpty() {
    assertFalse(notEmpty(null).isPresent());
    assertTrue(notEmpty("a").isPresent());
    assertTrue(notEmpty(" ").isPresent());
    assertTrue(notEmpty(" a ").isPresent());
    assertFalse(notEmpty("").isPresent());
  }

  @Test
  public void testNotBlank() {
    assertFalse(notBlank(null).isPresent());
    assertTrue(notBlank("a").isPresent());
    assertFalse(notBlank(" ").isPresent());
    assertTrue(notBlank(" a ").isPresent());
    assertFalse(notBlank("").isPresent());
  }

  @Test
  public void testCompareTo() {
    assertTrue(compareTo(null, "a") < 0);
    assertTrue(compareTo("b", null) > 0);
    assertTrue(compareTo("b", "a") > 0);
    assertTrue(compareTo("a", "b") < 0);
    assertEquals(0, compareTo("a", "a"));
    assertEquals(0, compareTo(null, null));
    assertEquals(0, compareTo("", ""));
    assertTrue(compareTo("a", "ab") < 0);
  }
}
