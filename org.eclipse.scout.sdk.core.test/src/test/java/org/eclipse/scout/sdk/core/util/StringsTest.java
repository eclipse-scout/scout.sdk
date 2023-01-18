/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.util;

import static java.lang.System.lineSeparator;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.eclipse.scout.sdk.core.util.Strings.camelCaseToScreamingSnakeCase;
import static org.eclipse.scout.sdk.core.util.Strings.capitalize;
import static org.eclipse.scout.sdk.core.util.Strings.compareTo;
import static org.eclipse.scout.sdk.core.util.Strings.countMatches;
import static org.eclipse.scout.sdk.core.util.Strings.decapitalize;
import static org.eclipse.scout.sdk.core.util.Strings.endsWith;
import static org.eclipse.scout.sdk.core.util.Strings.escapeHtml;
import static org.eclipse.scout.sdk.core.util.Strings.fromFile;
import static org.eclipse.scout.sdk.core.util.Strings.fromInputStream;
import static org.eclipse.scout.sdk.core.util.Strings.fromThrowable;
import static org.eclipse.scout.sdk.core.util.Strings.hasText;
import static org.eclipse.scout.sdk.core.util.Strings.indexOf;
import static org.eclipse.scout.sdk.core.util.Strings.isBlank;
import static org.eclipse.scout.sdk.core.util.Strings.isEmpty;
import static org.eclipse.scout.sdk.core.util.Strings.lastIndexOf;
import static org.eclipse.scout.sdk.core.util.Strings.levenshteinDistance;
import static org.eclipse.scout.sdk.core.util.Strings.nextLineEnd;
import static org.eclipse.scout.sdk.core.util.Strings.notBlank;
import static org.eclipse.scout.sdk.core.util.Strings.notEmpty;
import static org.eclipse.scout.sdk.core.util.Strings.removePrefix;
import static org.eclipse.scout.sdk.core.util.Strings.removeSuffix;
import static org.eclipse.scout.sdk.core.util.Strings.repeat;
import static org.eclipse.scout.sdk.core.util.Strings.replace;
import static org.eclipse.scout.sdk.core.util.Strings.replaceEach;
import static org.eclipse.scout.sdk.core.util.Strings.screamingSnakeCaseToCamelCase;
import static org.eclipse.scout.sdk.core.util.Strings.toCharArray;
import static org.eclipse.scout.sdk.core.util.Strings.trim;
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
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.stream.IntStream;

import javax.swing.text.Segment;

import org.junit.jupiter.api.Test;

/**
 * <h3>{@link StringsTest}</h3>
 *
 * @since 6.1.0
 */
public class StringsTest {

  @Test
  public void testIsEmpty() {
    assertTrue(isEmpty(null));
    assertTrue(isEmpty(""));
    assertFalse(isEmpty(" "));
    assertFalse(isEmpty("bob"));
    assertFalse(isEmpty("  bob  "));
  }

  @Test
  public void testInputStreamToString() throws IOException {
    var testData = "my test data";
    assertEquals(testData, fromInputStream(new ByteArrayInputStream(testData.getBytes(StandardCharsets.UTF_16LE)), StandardCharsets.UTF_16LE).toString());
    assertEquals(testData, fromInputStream(new ByteArrayInputStream(testData.getBytes(StandardCharsets.UTF_16BE)), StandardCharsets.UTF_16BE.name()).toString());
  }

  @Test
  public void testToCharArray() {
    assertArrayEquals("".toCharArray(), toCharArray(new StringBuilder()));
    assertArrayEquals("abc".toCharArray(), toCharArray(new StringBuilder("abc")));
    assertArrayEquals("abc".toCharArray(), toCharArray(new StringBuffer("abc")));
    assertArrayEquals("abc".toCharArray(), toCharArray("abc"));
    assertArrayEquals("abc".toCharArray(), toCharArray(CharBuffer.wrap("abc".toCharArray())));
    assertArrayEquals("abc".toCharArray(), toCharArray(new Segment("abc".toCharArray(), 0, 3)));
    assertThrows(NullPointerException.class, () -> toCharArray(null));
  }

  @Test
  public void testFromThrowable() {
    var s = fromThrowable(new Exception());
    assertFalse(s.startsWith(lineSeparator()));
    assertFalse(s.endsWith(lineSeparator()));
  }

  @Test
  public void testFromFile() throws IOException {
    assertThrows(IOException.class, () -> fromFile(Paths.get("not.existing.file"), StandardCharsets.UTF_8));
    assertThrows(IllegalArgumentException.class, () -> fromFile(Paths.get("not.existing.file"), null));
    assertThrows(IllegalArgumentException.class, () -> fromFile(null, StandardCharsets.UTF_8));

    var testFileContent = "testcontent\nnewline\n👌👌";
    var testFile = Files.createTempFile("scoutSdkTestFile", ".txt");
    try {
      // write test content
      Files.writeString(testFile, testFileContent, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);

      assertEquals(testFileContent, fromFile(testFile, StandardCharsets.UTF_8).toString());
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
    assertEquals("aaa", repeat("a", 3).toString());
    assertEquals("abab", repeat("ab", 2).toString());
    assertThrows(IllegalArgumentException.class, () -> repeat("asdf", -1));
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
    assertTrue(endsWith("ABcd", "cd"));

    assertTrue(endsWith("", "", false));
    assertTrue(endsWith("abc", "", false));
    assertFalse(endsWith(null, "", false));
    assertFalse(endsWith("abc", null, false));
    assertFalse(endsWith("", null, false));
    assertFalse(endsWith(null, null, false));
    assertFalse(endsWith(null, "abc", false));
    assertFalse(endsWith("abc", "de", false));
    assertFalse(endsWith("abc", "abcde", false));
    assertTrue(endsWith("aabc", "bc", false));
    assertTrue(endsWith("aa  ", " ", false));
    assertTrue(endsWith("", "", false));
    assertTrue(endsWith("abcd", "abcd", false));
    assertTrue(endsWith("abcd", "abCd", false));
    assertTrue(endsWith("abcd", "abCD", false));
    assertTrue(endsWith("abcd", "CD", false));
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
    assertThrows(UnsupportedCharsetException.class, () -> fromInputStream(new ByteArrayInputStream("test".getBytes(StandardCharsets.UTF_16LE)), "not-existing"));
    assertThrows(IllegalArgumentException.class, () -> fromInputStream(new ByteArrayInputStream("test".getBytes(StandardCharsets.UTF_16LE)), (String) null));
    assertThrows(IllegalCharsetNameException.class, () -> fromInputStream(new ByteArrayInputStream("test".getBytes(StandardCharsets.UTF_16LE)), "##"));
  }

  @Test
  public void testEscapeHtml() {
    assertEquals("", escapeHtml(""));
    assertEquals("a&#38;&#60;&#62;&#34;&#47;&#39;&#39;b", escapeHtml("a&<>\"/''b").toString());
    assertNull(escapeHtml(null));
  }

  @Test
  public void testTrim() {
    assertNull(trim(null));
    assertEquals("", trim("").toString());
    assertEquals("", trim("    ").toString());
    assertEquals("abc", trim("  abc  ").toString());
    assertEquals("y\rx", trim("  \ty\rx\n  ").toString());
    assertEquals("👌👌", trim("  👌👌\u3000  ").toString());
    assertEquals("ab\2c", trim("\0\1ab\2c \0\1").toString());
  }

  @Test
  public void testIsBlankHasText() {
    assertTrue(isBlank(null));
    assertTrue(isBlank(""));
    assertTrue(isBlank(" "));
    assertFalse(isBlank("bob"));
    assertFalse(isBlank("  bob  "));
    assertTrue(isBlank("  \t\r\n  "));
    assertFalse(isBlank("👌👌"));

    assertFalse(hasText(null));
    assertFalse(hasText(""));
    assertFalse(hasText(" "));
    assertTrue(hasText("bob"));
    assertTrue(hasText("  bob  "));
    assertFalse(hasText("  \t\r\n  "));
    assertTrue(hasText("👌👌"));

    for (char c = 0; c <= 8; c++) {
      assertTrue(isBlank(String.valueOf(c)));
      assertFalse(hasText(String.valueOf(c)));
    }
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
  public void testCapitalize() {
    assertNull(capitalize((CharSequence) null));
    assertEquals("", capitalize(""));
    assertEquals("  ", capitalize("  ").toString());
    assertEquals("A", capitalize("a").toString());
    assertEquals("Ab", capitalize("ab").toString());
    assertEquals("A", capitalize("A").toString());
    assertEquals("Ab", capitalize("Ab").toString());
    assertEquals("ABC", capitalize("ABC").toString());
    assertEquals("Abc", capitalize("abc").toString());
    assertEquals("ABC", capitalize("aBC").toString());

    assertEquals(emptyList(), capitalize((Collection<CharSequence>) null));
    assertTrue(arrayToStringEquals(new String[]{null, "", "  ", "A", "Ab", "Abc"}, capitalize(asList(null, "", "  ", "a", "ab", "abc")).toArray()));
    assertTrue(arrayToStringEquals(new String[]{null, "", "  ", "A", "Ab", "Abc"}, capitalize(asList(null, "", "  ", "A", "Ab", "Abc")).toArray()));
  }

  @Test
  public void testDecapitalize() {
    assertEquals(emptyList(), decapitalize(null));
    assertTrue(arrayToStringEquals(new String[]{null, "", "  ", "a", "ab", "abc"}, decapitalize(asList(null, "", "  ", "A", "Ab", "Abc")).toArray()));
    assertTrue(arrayToStringEquals(new String[]{null, "", "  ", "a", "ab", "abc"}, decapitalize(asList(null, "", "  ", "a", "ab", "abc")).toArray()));
  }

  private static boolean arrayToStringEquals(String[] expected, Object[] actual) {
    if (expected == null && actual == null) {
      return true;
    }
    if (expected == null || actual == null) {
      return false;
    }
    if (expected.length != actual.length) {
      return false;
    }
    return IntStream.range(0, expected.length)
        .allMatch(i -> {
          var e = expected[i];
          var a = actual[i];
          if (e == null && a == null) {
            return true;
          }
          if (e == null || a == null) {
            return false;
          }
          return e.equals(a.toString());
        });
  }

  @Test
  public void testEqualsCharArray() {
    var a = "".toCharArray();
    var b = "xx".toCharArray();
    var c = "xx".toCharArray();
    var d = "yy".toCharArray();
    var e = "xxx".toCharArray();
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
    var a = "".toCharArray();
    var b = "xx".toCharArray();
    var c = "xx".toCharArray();
    var d = "yy".toCharArray();
    var e = "xxx".toCharArray();
    var f = "xXx".toCharArray();
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
    var array = new char[]{'a', 'b', 'c'};
    var array2 = new char[]{'a', 'b', 'c', 'a', 'a'};
    assertTrue(indexOf(array, array2, -1, array2.length, true) < 0);
  }

  @Test
  public void testIndexOfCharsInChars3() {
    var array = new char[]{'a', 'b', 'c'};
    var array2 = new char[]{'a', 'b', 'c', 'a', 'a'};
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
    assertEquals(3, nextLineEnd("abc", 0));
    assertEquals(5, nextLineEnd("first\nsecond\nthird", 0));
    assertEquals(12, nextLineEnd("first\nsecond\nthird", 6));
    assertEquals(12, nextLineEnd("first\nsecond\r\nthird", 6));
    assertEquals(19, nextLineEnd("first\nsecond\r\nthird", 14));
    assertEquals(19, nextLineEnd("first\nsecond\r\nthird", 100));
    assertEquals(3, nextLineEnd("abc\n", 0));
    assertEquals(3, nextLineEnd("abc\r\n", 0));
    assertEquals(3, nextLineEnd("abc\r\n", 3));
    assertEquals(3, nextLineEnd("abc\r\n", 4));
    assertEquals(0, nextLineEnd("\nsecond", 0));
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
  public void testStartsWithString() {
    assertFalse(Strings.startsWith(null, null));
    assertFalse(Strings.startsWith("a", null));
    assertFalse(Strings.startsWith(null, "b"));
    assertFalse(Strings.startsWith("abc", "abcd"));
    assertFalse(Strings.startsWith("a👌b", "abc"));
    assertTrue(Strings.startsWith("a👌b", "a👌b"));
    assertTrue(Strings.startsWith("abc", "ab"));
    assertFalse(Strings.startsWith("abc", "aB"));
    assertFalse(Strings.startsWith("ab", "👌👌"));

    assertFalse(Strings.startsWith("abc", "abcd", false));
    assertFalse(Strings.startsWith("a👌b", "abc", false));
    assertTrue(Strings.startsWith("a👌b", "A👌B", false));
    assertTrue(Strings.startsWith("abc", "AB", false));
  }

  @Test
  public void testStartsWithCharSequence() {
    assertFalse(Strings.startsWith(null, null));
    assertFalse(Strings.startsWith("a", null));
    assertFalse(Strings.startsWith(null, "b"));
    assertFalse(Strings.startsWith(CharBuffer.wrap("abc"), CharBuffer.wrap("abcd")));
    assertFalse(Strings.startsWith(CharBuffer.wrap("a👌b"), CharBuffer.wrap("abc")));
    assertTrue(Strings.startsWith(CharBuffer.wrap("a👌b"), CharBuffer.wrap("a👌b")));
    assertTrue(Strings.startsWith(CharBuffer.wrap("abc"), CharBuffer.wrap("ab")));
    assertFalse(Strings.startsWith(CharBuffer.wrap("abc"), CharBuffer.wrap("aB")));
    assertFalse(Strings.startsWith(CharBuffer.wrap("ab"), CharBuffer.wrap("👌👌")));

    assertFalse(Strings.startsWith(CharBuffer.wrap("abc"), CharBuffer.wrap("abcd"), false));
    assertFalse(Strings.startsWith(CharBuffer.wrap("a👌b"), CharBuffer.wrap("abc"), false));
    assertTrue(Strings.startsWith(CharBuffer.wrap("a👌b"), CharBuffer.wrap("A👌B"), false));
    assertTrue(Strings.startsWith(CharBuffer.wrap("abc"), CharBuffer.wrap("AB"), false));
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

  @Test
  public void testLevenshteinDistance() {
    assertEquals(0, levenshteinDistance(null, null));
    assertEquals(0, levenshteinDistance(null, "something"));
    assertEquals(0, levenshteinDistance("something", null));
    assertEquals(0, levenshteinDistance("something", "something"));
    assertEquals(0, levenshteinDistance("", ""));

    assertEquals(9, levenshteinDistance("", "something"));
    assertEquals(9, levenshteinDistance("something", ""));

    assertEquals(8, levenshteinDistance("else", "something"));
    assertEquals(8, levenshteinDistance("something", "else"));

    assertEquals(5, levenshteinDistance("some", "something"));
    assertEquals(5, levenshteinDistance("something", "some"));

    assertEquals(4, levenshteinDistance("thing", "something"));
    assertEquals(4, levenshteinDistance("something", "thing"));
  }

  @Test
  public void testRemoveSuffix() {
    assertNull(removeSuffix(null, ""));
    assertEquals("", removeSuffix("", ""));
    assertEquals("ab", removeSuffix("ab", ""));
    assertEquals("ab", removeSuffix("ab", null));
    assertEquals("", removeSuffix("", "ab"));
    assertEquals("ab", removeSuffix("abc", "c"));
    assertEquals("ab", removeSuffix("abcdef", "cdef"));
    assertEquals("abcDef", removeSuffix("abcDef", "cdef"));
    assertEquals("ab", removeSuffix("abcDef", "cdef", false));
  }

  @Test
  public void testRemovePrefixSuffix() {
    assertNull(removePrefix(null, ""));
    assertEquals("", removePrefix("", ""));
    assertEquals("ab", removePrefix("ab", ""));
    assertEquals("ab", removePrefix("ab", null));
    assertEquals("", removePrefix("", "ab"));
    assertEquals("bc", removePrefix("abc", "a"));
    assertEquals("ef", removePrefix("abcdef", "abcd"));
    assertEquals("abcDef", removePrefix("abcDef", "abcd"));
    assertEquals("ef", removePrefix("abcDef", "abcd", false));
  }

  @Test
  public void testCamelCaseToScreamingSnakeCase() {
    assertNull(camelCaseToScreamingSnakeCase(null));
    assertEquals("", camelCaseToScreamingSnakeCase(""));
    assertEquals(" ", camelCaseToScreamingSnakeCase(" "));
    assertEquals("FOO", camelCaseToScreamingSnakeCase("foo"));
    assertEquals("FOO", camelCaseToScreamingSnakeCase("FOO"));
    assertEquals("FOO_BAR", camelCaseToScreamingSnakeCase("fooBar"));
    assertEquals("FOO_BAR", camelCaseToScreamingSnakeCase("fooBAR"));
    assertEquals("FOO_BAR_SEE", camelCaseToScreamingSnakeCase("fooBarSee"));
    assertEquals("FOO_BAR_SEE", camelCaseToScreamingSnakeCase("fooBARSee"));
    assertEquals("FOO BAR SEE", camelCaseToScreamingSnakeCase("foo bar see"));
  }

  @Test
  public void testScreamingSnakeCaseToCamelCase() {
    assertNull(screamingSnakeCaseToCamelCase(null));
    assertEquals("", screamingSnakeCaseToCamelCase(""));
    assertEquals(" ", screamingSnakeCaseToCamelCase(" "));
    assertEquals("Foo", screamingSnakeCaseToCamelCase("FOO"));
    assertEquals("FooBar", screamingSnakeCaseToCamelCase("FOO_BAR"));
    assertEquals("FooBarSee", screamingSnakeCaseToCamelCase("FOO_BAR_SEE"));
    assertEquals("Foo bar see", screamingSnakeCaseToCamelCase("FOO BAR SEE"));
  }
}
