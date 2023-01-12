/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.model.ecj;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * <h3>{@link SameCompositeObjectTest}</h3>
 *
 * @since 5.2.0
 */
public class SameCompositeObjectTest {

  @Test
  @SuppressWarnings({"unlikely-arg-type", "RedundantArrayCreation", "SimplifiableJUnitAssertion", "ConstantConditions", "EqualsBetweenInconvertibleTypes", "EqualsWithItself"})
  public void testSameness() {
    var a = Long.valueOf(300);
    var b = Long.valueOf(300);
    assertNotSame(a, b);
    assertEquals(a, b);

    var o1 = new SameCompositeObject(a, b);
    var o2 = new SameCompositeObject(a, a);
    var o3 = new SameCompositeObject(a, b);
    var o4 = new SameCompositeObject((Object[]) null);
    Object[] arr = {a, null};
    var o5 = new SameCompositeObject(arr);
    var o6 = new SameCompositeObject(arr);
    var o7 = new SameCompositeObject(new Object[]{a, b, null});
    var o8 = new SameCompositeObject((Object[]) null);

    assertFalse(o1.equals(o2));
    assertFalse(o1.equals(null));
    assertFalse(o1.equals(o4));
    assertFalse(o4.equals(o1));
    assertTrue(o5.equals(o6));
    assertTrue(o6.equals(o5));
    assertFalse(o1.equals(""));
    assertFalse(o6.equals(o7));
    assertTrue(o1.equals(o3));
    assertTrue(o4.equals(o8));
    assertTrue(o1.equals(o1));
    assertEquals(o1.hashCode(), o3.hashCode());
  }
}
