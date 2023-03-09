/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.typescript.model.api.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.eclipse.scout.sdk.core.typescript.testing.spi.TestingNodeModuleSpi;
import org.junit.jupiter.api.Test;

public class NodeModuleTest {
  @Test
  public void testNodeModule() {
    var spi = new TestingNodeModuleSpi(FixtureHelper.MINIMAL_MODULE_DIR);
    var module = spi.api();

    assertSame(spi.api(), module);
    assertSame(spi.containingModule(), spi);
    assertSame(spi.api().packageJson().directory(), FixtureHelper.MINIMAL_MODULE_DIR);

    assertSame(module.spi(), spi);
    assertSame(module.name(), module.packageJson().name());
    assertEquals("@eclipse-scout/sdk-minimal-module", module.packageJson().name());
    assertEquals("23.1.0-snapshot", module.packageJson().version());
    assertEquals("@eclipse-scout/sdk-minimal-module@23.1.0-snapshot", module.toString());
  }
}
