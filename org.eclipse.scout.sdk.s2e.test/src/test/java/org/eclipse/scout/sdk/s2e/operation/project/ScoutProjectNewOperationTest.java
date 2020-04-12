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
package org.eclipse.scout.sdk.s2e.operation.project;

import static org.eclipse.scout.sdk.s2e.operation.project.ScoutProjectNewOperation.execEnvironmentToVersion;
import static org.eclipse.scout.sdk.s2e.operation.project.ScoutProjectNewOperation.versionToString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.osgi.framework.Version.parseVersion;

import org.junit.jupiter.api.Test;

/**
 * <h3>{@link ScoutProjectNewOperationTest}</h3>
 *
 * @since 7.1.0
 */
public class ScoutProjectNewOperationTest {

  @Test
  public void testExecEnvironmentToVersion() {
    assertEquals(parseVersion("1.7"), execEnvironmentToVersion(ScoutProjectNewOperation.EXEC_ENV_PREFIX + "1.7"));
    assertEquals(parseVersion("1.8"), execEnvironmentToVersion(ScoutProjectNewOperation.EXEC_ENV_PREFIX + "1.8"));
    assertEquals(parseVersion("1.9"), execEnvironmentToVersion(ScoutProjectNewOperation.EXEC_ENV_PREFIX + "1.9"));
    assertEquals(parseVersion("9"), execEnvironmentToVersion(ScoutProjectNewOperation.EXEC_ENV_PREFIX + '9'));
    assertEquals(parseVersion("10"), execEnvironmentToVersion(ScoutProjectNewOperation.EXEC_ENV_PREFIX + "10"));
    assertEquals(parseVersion(ScoutProjectNewOperation.MIN_JVM_VERSION), execEnvironmentToVersion(ScoutProjectNewOperation.EXEC_ENV_PREFIX));
    assertEquals(parseVersion(ScoutProjectNewOperation.MIN_JVM_VERSION), execEnvironmentToVersion(""));
    assertEquals(parseVersion(ScoutProjectNewOperation.MIN_JVM_VERSION), execEnvironmentToVersion(null));
    assertEquals(parseVersion(ScoutProjectNewOperation.MIN_JVM_VERSION), execEnvironmentToVersion(ScoutProjectNewOperation.EXEC_ENV_PREFIX + "abc"));
    assertTrue(execEnvironmentToVersion(ScoutProjectNewOperation.EXEC_ENV_PREFIX + "10").compareTo(execEnvironmentToVersion(ScoutProjectNewOperation.EXEC_ENV_PREFIX + '9')) > 0);
  }

  @Test
  public void testVersionToString() {
    assertEquals("1.7", versionToString(parseVersion("1.7")));
    assertEquals("1.8", versionToString(parseVersion("1.8")));
    assertEquals("1.9", versionToString(parseVersion("1.9")));
    assertEquals("9", versionToString(parseVersion("9.0.0")));
    assertEquals("9", versionToString(parseVersion("9.0")));
    assertEquals("10", versionToString(parseVersion("10.0.0")));
    assertEquals("10.1", versionToString(parseVersion("10.1.0")));
  }
}
