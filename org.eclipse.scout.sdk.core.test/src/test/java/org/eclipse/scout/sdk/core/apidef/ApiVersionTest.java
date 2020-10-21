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
package org.eclipse.scout.sdk.core.apidef;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.scout.sdk.core.fixture.apidef.Java13Api;
import org.eclipse.scout.sdk.core.fixture.apidef.Java8Api;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ApiVersionTest {

  @Test
  public void testParse() {
    assertParseResult("43.245.1");
    assertParseResult(".245.", "245");
    assertParseResult("11.0-SNAPSHOT");
    assertParseResult("10.0.0-SNAPSHOT");
    assertParseResult("11");
    Assertions.assertFalse(ApiVersion.parse("").isPresent());
    Assertions.assertFalse(ApiVersion.parse(null).isPresent());
  }

  private static void assertParseResult(String input) {
    assertParseResult(input, null);
  }

  private static void assertParseResult(String input, String expected) {
    Assertions.assertEquals(expected == null ? input : expected, ApiVersion.parse(input).get().asString());
  }

  @Test
  public void testSegments() {
    int[] segments = {12, 4};
    var v = new ApiVersion(segments);
    assertArrayEquals(segments, v.segments());
    assertNotSame(segments, v.segments());
  }

  @Test
  public void testApiVersionOf() {
    Assertions.assertEquals("13", ApiVersion.requireApiLevelOf(Java13Api.class).asString());
    Assertions.assertEquals(ApiVersion.class.getSimpleName() + " 8", ApiVersion.requireApiLevelOf(Java8Api.class).toString());
  }

  @Test
  @SuppressWarnings({"EqualsWithItself", "SimplifiableJUnitAssertion", "ConstantConditions", "EqualsBetweenInconvertibleTypes"})
  public void testEqualsAndHashCode() {
    var a = new ApiVersion(10);
    var b = new ApiVersion(10, 0, 1);
    var c = new ApiVersion(11, 2, 3);
    var d = new ApiVersion(10, 1, 1);
    var e = new ApiVersion(10);

    assertTrue(a.equals(a));
    assertTrue(a.equals(e));

    assertFalse(a.equals(null));
    assertFalse(a.equals("whatever"));
    assertFalse(a.equals(b));
    assertFalse(d.equals(b));
    assertFalse(c.equals(a));

    assertEquals(a.hashCode(), e.hashCode());
    assertNotEquals(a.hashCode(), b.hashCode());
  }

  @Test
  @SuppressWarnings("EqualsWithItself")
  public void testCompareTo() {
    var a = new ApiVersion(10);
    var b = new ApiVersion(10, 0, 1);
    var c = new ApiVersion(11, 2, 3);
    var d = new ApiVersion(10, 1, 1);
    var e = new ApiVersion(10);

    assertEquals(0, a.compareTo(a));
    assertEquals(0, a.compareTo(e));

    assertTrue(a.compareTo(null) > 0);
    assertTrue(a.compareTo(b) < 0);
    assertTrue(d.compareTo(b) > 0);
    assertTrue(c.compareTo(a) > 0);
  }
}
