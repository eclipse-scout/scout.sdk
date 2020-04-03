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

import static java.lang.System.lineSeparator;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

/**
 * <h3>{@link StringsTest}</h3>
 *
 * @since 6.1.0
 */
@SuppressWarnings({"HardcodedFileSeparator", "HardcodedLineSeparator"})
public class StringsTest {

  @Test
  public void testInputStreamToString() throws IOException {
    String testData = "my test data";
    assertEquals(testData, Strings.fromInputStream(new ByteArrayInputStream(testData.getBytes(StandardCharsets.UTF_16LE)), StandardCharsets.UTF_16LE).toString());
    assertEquals(testData, Strings.fromInputStream(new ByteArrayInputStream(testData.getBytes(StandardCharsets.UTF_16BE)), StandardCharsets.UTF_16BE.name()).toString());
  }

  @Test
  public void testToCharArray() {
    assertArrayEquals("".toCharArray(), Strings.toCharArray(new StringBuilder()));
    assertArrayEquals("abc".toCharArray(), Strings.toCharArray(new StringBuilder("abc")));
    assertThrows(IllegalArgumentException.class, () -> Strings.toCharArray(null));
  }

  @Test
  public void testFromThrowable() {
    String s = Strings.fromThrowable(new Exception());
    assertFalse(s.startsWith(lineSeparator()));
    assertFalse(s.endsWith(lineSeparator()));
  }

  @Test
  public void testRepeat() {
    assertNull(Strings.repeat(null, 1));
    assertEquals("", Strings.repeat("", 10));
    assertEquals("", Strings.repeat("asdf", 0));
    assertEquals("", Strings.repeat("asdf", -1));
    assertEquals("aaa", Strings.repeat("a", 3));
    assertEquals("abab", Strings.repeat("ab", 2));
  }

  @Test
  public void testEndsWith() {
    assertTrue(Strings.endsWith("", ""));
    assertTrue(Strings.endsWith("abc", ""));
    assertFalse(Strings.endsWith(null, ""));
    assertFalse(Strings.endsWith("abc", null));
    assertFalse(Strings.endsWith("", null));
    assertFalse(Strings.endsWith(null, null));
    assertFalse(Strings.endsWith(null, "abc"));
    assertFalse(Strings.endsWith("abc", "de"));
    assertFalse(Strings.endsWith("abc", "abcde"));
    assertTrue(Strings.endsWith("aabc", "bc"));
    assertTrue(Strings.endsWith("aa  ", " "));
    assertTrue(Strings.endsWith("", ""));
    assertTrue(Strings.endsWith("abcd", "abcd"));
  }

  @Test
  public void testReplace() {
    assertNull(Strings.replace(null, null, null));
    assertEquals("", Strings.replace("", null, null));
    assertEquals("asdf", Strings.replace("asdf", null, "ff"));
    assertEquals("asdf", Strings.replace("asdf", "f", null));
    assertEquals("asdf", Strings.replace("asdf", "", null));
    assertEquals("sdf", Strings.replace("asdf", "a", ""));
    assertEquals("gsdf", Strings.replace("asdf", "a", "g"));
    assertEquals("asdf", Strings.replace("asdf", "xx", "g"));
  }

  @Test
  public void testReplaceSequence() {
    assertNull(Strings.replace(null, 'a', 'b'));
    assertEquals("", Strings.replace("", 'a', 'b'));
    assertEquals("akdf", Strings.replace("asdf", 's', 'k'));
    assertEquals("ksdf", Strings.replace("asdf", 'a', 'k'));
    assertEquals("asdk", Strings.replace("asdf", 'f', 'k'));
  }

  @Test
  public void testCountMatches() {
    assertEquals(0, Strings.countMatches(null, "asdf"));
    assertEquals(0, Strings.countMatches("", "sss"));
    assertEquals(0, Strings.countMatches("abba", null));
    assertEquals(0, Strings.countMatches("abba", ""));
    assertEquals(2, Strings.countMatches("abba", "a"));
    assertEquals(1, Strings.countMatches("abba", "ab"));
    assertEquals(0, Strings.countMatches("abba", "xxx"));
  }

  @Test
  public void testInputStreamToStringWrongCharset() {
    assertThrows(IOException.class, () -> Strings.fromInputStream(new ByteArrayInputStream("test".getBytes(StandardCharsets.UTF_16LE)), "not-existing"));
  }

  @Test
  public void testEscapeHtml() {
    assertEquals("", Strings.escapeHtml(""));
    assertEquals("a&amp;&lt;&gt;&quot;&#47;&apos;&apos;b", Strings.escapeHtml("a&<>\"/''b"));
    assertNull(Strings.escapeHtml(null));
  }

  @Test
  public void testIsBlankHasText() {
    assertTrue(Strings.isBlank(null));
    assertTrue(Strings.isBlank(""));
    assertTrue(Strings.isBlank(" "));
    assertFalse(Strings.isBlank("bob"));
    assertFalse(Strings.isBlank("  bob  "));
    assertTrue(Strings.isBlank("  \t\r\n  "));

    assertFalse(Strings.hasText(null));
    assertFalse(Strings.hasText(""));
    assertFalse(Strings.hasText(" "));
    assertTrue(Strings.hasText("bob"));
    assertTrue(Strings.hasText("  bob  "));
    assertFalse(Strings.hasText("  \t\r\n  "));
  }

  @Test
  public void testFromStringLiteral() {
    assertNull(Strings.fromStringLiteral(null));
    assertNull(Strings.fromStringLiteral("a"));
    assertNull(Strings.fromStringLiteral("\"a\\nb"));
    assertNull(Strings.fromStringLiteral("aaa\""));
    assertEquals("a\nb", Strings.fromStringLiteral("\"a\\nb\""));
    assertEquals("", Strings.fromStringLiteral("\"\""));
    assertEquals("a\"b", Strings.fromStringLiteral("\"a\\\"b\""));
  }

  @Test
  public void testToStringLiteral() {
    assertEquals("\"a\\nb\"", Strings.toStringLiteral("a\nb"));
    assertEquals("\"a\\\"b\"", Strings.toStringLiteral("a\"b"));
    assertNull(Strings.toStringLiteral(null));
    String in = "teststring \na\"";
    assertEquals(in, Strings.fromStringLiteral(Strings.toStringLiteral(in)));
  }

  @Test
  public void testReplaceEach() {
    assertNull(Strings.replaceEach(null, new String[]{"aa"}, new String[]{"bb"}));
    assertEquals("", Strings.replaceEach("", new String[]{"aa"}, new String[]{"bb"}));
    assertEquals("aba", Strings.replaceEach("aba", null, null));
    assertEquals("aba", Strings.replaceEach("aba", new String[0], null));
    assertEquals("aba", Strings.replaceEach("aba", null, new String[0]));
    assertEquals("aba", Strings.replaceEach("aba", new String[]{"a"}, null));
    assertEquals("b", Strings.replaceEach("aba", new String[]{"a"}, new String[]{""}));
    assertEquals("aba", Strings.replaceEach("aba", new String[]{null}, new String[]{"a"}));
    assertEquals("aba", Strings.replaceEach("aba", new String[]{"b"}, new String[]{null}));
    assertEquals("wcte", Strings.replaceEach("abcde", new String[]{"ab", "d"}, new String[]{"w", "t"}));
    assertEquals("abcde", Strings.replaceEach("abcde", new String[]{"ab", "d"}, new String[]{}));
    assertThrows(IllegalArgumentException.class, () -> assertEquals("abcde", Strings.replaceEach("abcde", new String[]{"ab", "d"}, new String[]{"a"})));
  }

  @Test
  public void testEnsureStartWithUpperCase() {
    assertNull(Strings.ensureStartWithUpperCase(null));
    assertEquals("", Strings.ensureStartWithUpperCase(""));
    assertEquals("  ", Strings.ensureStartWithUpperCase("  "));
    assertEquals("A", Strings.ensureStartWithUpperCase("a"));
    assertEquals("Ab", Strings.ensureStartWithUpperCase("ab"));
    assertEquals("A", Strings.ensureStartWithUpperCase("A"));
    assertEquals("Ab", Strings.ensureStartWithUpperCase("Ab"));
    assertEquals("ABC", Strings.ensureStartWithUpperCase("ABC"));
    assertEquals("Abc", Strings.ensureStartWithUpperCase("abc"));
    assertEquals("ABC", Strings.ensureStartWithUpperCase("aBC"));
  }
}
