/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.testing;

import static org.eclipse.scout.sdk.core.s.testing.CoreScoutTestingUtils.rtToArchetypeVersion;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class CoreScoutTestingUtilsTest {

  @Test
  @SuppressWarnings("AccessOfSystemProperties")
  public void testScoutVersionWithProperty() {
    var oldValue = System.getProperty(CoreScoutTestingUtils.SCOUT_VERSION_KEY);
    try {
      var expected = "3.4.5";
      System.setProperty(CoreScoutTestingUtils.SCOUT_VERSION_KEY, expected);
      assertEquals(expected, CoreScoutTestingUtils.currentScoutVersion());
    }
    finally {
      if (oldValue == null) {
        System.clearProperty(CoreScoutTestingUtils.SCOUT_VERSION_KEY);
      }
      else {
        System.setProperty(CoreScoutTestingUtils.SCOUT_VERSION_KEY, oldValue);
      }
    }
  }

  @Test
  public void testRtToArchetypeVersion() {
    // Version <= 11 uses Scout SDK Version schema (at least three digits to comply with OSGi requirements)
    assertEquals("10.0.0", rtToArchetypeVersion("10"));
    assertEquals("10.1.0", rtToArchetypeVersion("10.1"));
    assertEquals("10.0.4", rtToArchetypeVersion("10.0.4"));
    assertEquals("10.0.0-SNAPSHOT", rtToArchetypeVersion("10.0.0-SNAPSHOT"));
    assertEquals("11.0.0-SNAPSHOT", rtToArchetypeVersion("11.0-SNAPSHOT"));
    assertEquals("11.0.0-alpha.5", rtToArchetypeVersion("11.0.0-alpha.5"));
    assertEquals("11.0.0-alpha.5", rtToArchetypeVersion("11.0-alpha.5"));

    // Version > 11 uses Scout RT Version schema (only two digits for snapshots)
    assertEquals("12.0-SNAPSHOT", rtToArchetypeVersion("12.0-SNAPSHOT"));
    assertEquals("22.0-SNAPSHOT", rtToArchetypeVersion("22.0-SNAPSHOT"));
    assertEquals("22.0.5", rtToArchetypeVersion("22.0.5"));
  }

  @Test
  public void testScoutVersionRunningClasspath() {
    var scoutVersion = CoreScoutTestingUtils.currentScoutVersion();
    assertTrue(scoutVersion.endsWith("-SNAPSHOT"));
  }
}
