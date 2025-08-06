/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.testing;

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
  public void testScoutVersionRunningClasspath() {
    var scoutVersion = CoreScoutTestingUtils.currentScoutVersion();
    assertTrue(scoutVersion.endsWith("-SNAPSHOT"));
  }
}
