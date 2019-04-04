/*******************************************************************************
 * Copyright (c) 2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
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
  @SuppressWarnings("unlikely-arg-type")
  public void testMavenBuild() {
    MavenBuild a = new MavenBuild()
        .withGoal("goal1")
        .withOption("option1")
        .withProperty('x');

    MavenBuild b = new MavenBuild()
        .withGoal("goal2")
        .withOption("option2")
        .withProperty("prop2");

    MavenBuild bb = new MavenBuild()
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
