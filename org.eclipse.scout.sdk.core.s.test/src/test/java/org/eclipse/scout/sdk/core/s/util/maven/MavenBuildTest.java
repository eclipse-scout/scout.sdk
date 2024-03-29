/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.util.maven;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * <h3>{@link MavenBuildTest}</h3>
 *
 * @since 7.0.0
 */
public class MavenBuildTest {
  @Test
  @SuppressWarnings({"unlikely-arg-type", "SimplifiableJUnitAssertion", "EqualsWithItself", "ConstantConditions", "EqualsBetweenInconvertibleTypes"})
  public void testMavenBuild() {
    var a = new MavenBuild()
        .withGoal("goal1")
        .withOption("option1")
        .withProperty('x');

    var b = new MavenBuild()
        .withGoal("goal2")
        .withOption("option2")
        .withProperty("prop2");

    var bb = new MavenBuild()
        .withGoal("goal2")
        .withOption("option2")
        .withProperty("prop2");

    assertTrue(a.hasOption("option1"));
    assertFalse(a.hasOption('c'));
    assertEquals(b.hashCode(), bb.hashCode());
    assertEquals(b, bb);
    assertNotEquals(a, b);
    assertTrue(a.equals(a));
    assertFalse(a.equals(null));
    assertFalse(a.equals(""));

    a.clearProperties();
    assertEquals("Maven build in dir 'null': goal1 -option1 ", a.toString());

    a.clearOptions();
    a.clearGoals();
    assertEquals("Maven build in dir 'null': ", a.toString());
  }
}
