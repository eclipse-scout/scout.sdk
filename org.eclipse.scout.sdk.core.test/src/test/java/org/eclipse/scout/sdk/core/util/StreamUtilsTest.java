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

import static org.eclipse.scout.sdk.core.util.StreamUtils.firstBy;
import static org.eclipse.scout.sdk.core.util.StreamUtils.toStream;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Arrays;
import java.util.Map.Entry;
import java.util.StringTokenizer;

import org.junit.jupiter.api.Test;

public class StreamUtilsTest {

  @Test
  public void testFirstBy() {
    var items = Arrays.asList(new SimpleImmutableEntry<>("key0", "value0"),
        new SimpleImmutableEntry<>("key1", "value1"),
        new SimpleImmutableEntry<>("key0", "value2"),
        new SimpleImmutableEntry<>("key1", "value3"));
    assertEquals(2, items.stream().filter(firstBy(Entry::getKey)).count());
    assertEquals(4, items.stream().filter(firstBy(Entry::getValue)).count());
    assertArrayEquals(new String[]{"value0", "value1"}, items.stream()
        .filter(firstBy(Entry::getKey))
        .map(Entry::getValue)
        .toArray(String[]::new));

  }

  @Test
  public void testToStreamWithEnumeration() {
    var elements = "asdfx.1234.hjkl.9876";
    var delim = ".";
    assertEquals(4, toStream(new StringTokenizer(elements, delim)).count());
    assertTrue(toStream(new StringTokenizer(elements, delim)).anyMatch(candidate -> candidate.toString().length() == 4));
    assertFalse(toStream(new StringTokenizer(elements, delim)).anyMatch(candidate -> candidate.toString().length() == 6));
  }
}
