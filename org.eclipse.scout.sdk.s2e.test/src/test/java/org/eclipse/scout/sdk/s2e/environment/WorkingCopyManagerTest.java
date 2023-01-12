/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2e.environment;

import static org.eclipse.scout.sdk.s2e.environment.WorkingCopyManager.currentWorkingCopyManager;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

/**
 * <h3>{@link WorkingCopyManagerTest}</h3>
 *
 * @since 9.0.0
 */
public class WorkingCopyManagerTest {

  @Test
  public void testLifecycle() {
    var manager = new AtomicReference<WorkingCopyManager>();
    WorkingCopyManager.runWithWorkingCopyManager(() -> {
      var currentWorkingCopyManager = (WorkingCopyManager) currentWorkingCopyManager();
      manager.set(currentWorkingCopyManager);
      assertTrue(currentWorkingCopyManager.isOpen());
      assertEquals(0, currentWorkingCopyManager.size());
      assertTrue(currentWorkingCopyManager.checkpoint(null));
      assertTrue(currentWorkingCopyManager.isOpen());
    }, () -> null);

    assertFalse(manager.get().isOpen());
    assertThrows(IllegalArgumentException.class, () -> manager.get().checkpoint(null));
    assertThrows(IllegalArgumentException.class, () -> manager.get().register(null, null));
    assertThrows(IllegalArgumentException.class, () -> manager.get().reconcile(null, null));
    assertThrows(IllegalArgumentException.class, () -> manager.get().unregisterAll(false, null));
  }
}
