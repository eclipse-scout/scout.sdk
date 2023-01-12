/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.testing.apidef;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class ApiExecutionConditionTest {
  @Test
  public void testBefore() {
    assertTrue(ApiRequirement.BEFORE.isFulfilled(new int[]{1}, new int[]{2}));
    assertTrue(ApiRequirement.BEFORE.isFulfilled(new int[]{1, 0}, new int[]{2}));
    assertTrue(ApiRequirement.BEFORE.isFulfilled(new int[]{1, 0}, new int[]{2, 0}));
    assertFalse(ApiRequirement.BEFORE.isFulfilled(new int[]{2}, new int[]{2}));
    assertFalse(ApiRequirement.BEFORE.isFulfilled(new int[]{3}, new int[]{2}));
    assertFalse(ApiRequirement.BEFORE.isFulfilled(new int[]{2, 1}, new int[]{2}));
    assertFalse(ApiRequirement.BEFORE.isFulfilled(new int[]{2, 0}, new int[]{2}));
    assertFalse(ApiRequirement.BEFORE.isFulfilled(new int[]{2}, new int[]{2, 0}));
  }

  @Test
  public void testAfter() {
    assertFalse(ApiRequirement.AFTER.isFulfilled(new int[]{1}, new int[]{2}));
    assertFalse(ApiRequirement.AFTER.isFulfilled(new int[]{1, 0}, new int[]{2}));
    assertFalse(ApiRequirement.AFTER.isFulfilled(new int[]{1, 0}, new int[]{2, 0}));
    assertFalse(ApiRequirement.AFTER.isFulfilled(new int[]{2}, new int[]{2}));
    assertTrue(ApiRequirement.AFTER.isFulfilled(new int[]{3}, new int[]{2}));
    assertTrue(ApiRequirement.AFTER.isFulfilled(new int[]{2, 1}, new int[]{2}));
    assertFalse(ApiRequirement.AFTER.isFulfilled(new int[]{2, 0}, new int[]{2}));
    assertFalse(ApiRequirement.AFTER.isFulfilled(new int[]{2}, new int[]{2, 0}));
  }

  @Test
  public void testMin() {
    assertFalse(ApiRequirement.MIN.isFulfilled(new int[]{1}, new int[]{2}));
    assertFalse(ApiRequirement.MIN.isFulfilled(new int[]{1, 0}, new int[]{2}));
    assertFalse(ApiRequirement.MIN.isFulfilled(new int[]{1, 0}, new int[]{2, 0}));
    assertTrue(ApiRequirement.MIN.isFulfilled(new int[]{2}, new int[]{2}));
    assertTrue(ApiRequirement.MIN.isFulfilled(new int[]{3}, new int[]{2}));
    assertTrue(ApiRequirement.MIN.isFulfilled(new int[]{2, 1}, new int[]{2}));
    assertTrue(ApiRequirement.MIN.isFulfilled(new int[]{2, 0}, new int[]{2}));
    assertTrue(ApiRequirement.MIN.isFulfilled(new int[]{2}, new int[]{2, 0}));
  }

  @Test
  public void testMax() {
    assertTrue(ApiRequirement.MAX.isFulfilled(new int[]{1}, new int[]{2}));
    assertTrue(ApiRequirement.MAX.isFulfilled(new int[]{1, 0}, new int[]{2}));
    assertTrue(ApiRequirement.MAX.isFulfilled(new int[]{1, 0}, new int[]{2, 0}));
    assertTrue(ApiRequirement.MAX.isFulfilled(new int[]{2}, new int[]{2}));
    assertFalse(ApiRequirement.MAX.isFulfilled(new int[]{3}, new int[]{2}));
    assertFalse(ApiRequirement.MAX.isFulfilled(new int[]{2, 1}, new int[]{2}));
    assertTrue(ApiRequirement.MAX.isFulfilled(new int[]{2, 0}, new int[]{2}));
    assertTrue(ApiRequirement.MAX.isFulfilled(new int[]{2}, new int[]{2, 0}));
  }
}
