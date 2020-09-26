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
  @SuppressWarnings({"unlikely-arg-type", "UnnecessaryBoxing", "RedundantArrayCreation", "SimplifiableJUnitAssertion", "ConstantConditions", "EqualsBetweenInconvertibleTypes", "EqualsWithItself"})
  public void testSameness() {
    Long a = Long.valueOf(300);
    Long b = Long.valueOf(300);
    assertNotSame(a, b);
    assertEquals(a, b);

    SameCompositeObject o1 = new SameCompositeObject(a, b);
    SameCompositeObject o2 = new SameCompositeObject(a, a);
    SameCompositeObject o3 = new SameCompositeObject(a, b);
    SameCompositeObject o4 = new SameCompositeObject((Object[]) null);
    Object[] arr = {a, null};
    SameCompositeObject o5 = new SameCompositeObject(arr);
    SameCompositeObject o6 = new SameCompositeObject(arr);
    SameCompositeObject o7 = new SameCompositeObject(new Object[]{a, b, null});
    SameCompositeObject o8 = new SameCompositeObject((Object[]) null);

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
