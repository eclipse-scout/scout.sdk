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

import static org.eclipse.scout.sdk.core.util.MatcherStream.all;
import static org.eclipse.scout.sdk.core.util.MatcherStream.allMatches;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

public class MatcherStreamTest {
  @Test
  public void testAll() {
    assertArrayEquals(new String[]{"abc", "axc"}, all(Pattern.compile("a.c"), "abcdefaxc").toArray(String[]::new));
  }

  @Test
  public void testAllMatches() {
    assertEquals(2, allMatches(Pattern.compile("ab"), "abcdefab").count());
  }
}
