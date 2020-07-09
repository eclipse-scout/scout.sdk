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

import static org.eclipse.scout.sdk.core.util.StreamUtils.allMatchResults;
import static org.eclipse.scout.sdk.core.util.StreamUtils.allMatches;
import static org.eclipse.scout.sdk.core.util.StreamUtils.toStream;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.StringTokenizer;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

public class StreamUtilsTest {
  @Test
  public void testAllMatches() {
    assertArrayEquals(new String[]{"abc", "axc"}, allMatches(Pattern.compile("a.c"), "abcdefaxc").toArray(String[]::new));
  }

  @Test
  public void testAllMatchResults() {
    String input = "abcdefab";
    Pattern pattern = Pattern.compile("ab");
    assertEquals(2, allMatchResults(pattern, input).count());
    assertTrue(allMatchResults(pattern, input).anyMatch(candidate -> candidate.group().length() == 2));
    assertFalse(allMatchResults(pattern, input).anyMatch(candidate -> candidate.group().length() == 6));
  }

  @Test
  public void testToStreamWithEnumeration() {
    String elements = "asdfx.1234.hjkl.9876";
    String delim = ".";
    assertEquals(4, toStream(new StringTokenizer(elements, delim)).count());
    assertTrue(toStream(new StringTokenizer(elements, delim)).anyMatch(candidate -> candidate.toString().length() == 4));
    assertFalse(toStream(new StringTokenizer(elements, delim)).anyMatch(candidate -> candidate.toString().length() == 6));
  }
}
