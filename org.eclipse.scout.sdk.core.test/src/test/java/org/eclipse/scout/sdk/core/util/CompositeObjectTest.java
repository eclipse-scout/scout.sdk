/*******************************************************************************
 * Copyright (c) 2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.core.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * <h3>{@link CompositeObjectTest}</h3>
 *
 * @since 6.1.0
 */
public class CompositeObjectTest {

  @Test
  @SuppressWarnings("unlikely-arg-type")
  public void testEquality() {
    Long a = Long.valueOf(400);
    Long b = Long.valueOf(400);
    assertNotSame(a, b);
    assertEquals(a, b);

    CompositeObject o1 = new CompositeObject(a, b);
    CompositeObject o2 = new CompositeObject(a, a);
    CompositeObject o3 = new CompositeObject(a, b);
    CompositeObject o4 = new CompositeObject((Object[]) null);
    Object[] arr = {a, null};
    CompositeObject o5 = new CompositeObject(arr);
    CompositeObject o6 = new CompositeObject(arr);
    CompositeObject o7 = new CompositeObject(new Object[]{a, b, null});
    CompositeObject o8 = new CompositeObject((Object[]) null);

    assertTrue(o1.equals(o2));
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
    assertEquals(o1.hashCode(), o3.hashCode());
  }

  @Test
  public void testConcat() {
    assertEquals(new CompositeObject(), CompositeObject.concat((CompositeObject[]) null));
    assertEquals(new CompositeObject(), CompositeObject.concat(new CompositeObject[]{}));
    assertEquals(new CompositeObject(1, 2, 3), CompositeObject.concat(new CompositeObject(1, 2), new CompositeObject(3)));
    assertEquals(new CompositeObject(1, 2, 3), CompositeObject.concat(new CompositeObject(1, 2), null, new CompositeObject(3)));
    assertEquals(new CompositeObject(1, 2, null, 3), CompositeObject.concat(new CompositeObject(1, 2, null), null, new CompositeObject(3)));
    assertEquals(new CompositeObject(1, 2, null, 3), CompositeObject.concat(new CompositeObject(1, 2, null), new CompositeObject((Object[]) null), new CompositeObject(3)));
    assertEquals(new CompositeObject(1, 2, null, 3), CompositeObject.concat(new CompositeObject(1, 2, null), new CompositeObject(new Object[]{}), new CompositeObject(3)));
  }

  @Test
  public void testCompareTo() {
    CompositeObject o1 = new CompositeObject(1, 2, 3);
    CompositeObject o2 = new CompositeObject(1, 2, 3);
    CompositeObject o3 = new CompositeObject(4, 5, 6);
    CompositeObject o4 = new CompositeObject((Object[]) null);
    CompositeObject o5 = new CompositeObject((Object[]) null);
    CompositeObject o6 = new CompositeObject(1, 2, 3, 4);
    CompositeObject o7 = new CompositeObject(new Object[]{null});
    CompositeObject o8 = new CompositeObject(new Object[]{null});
    CompositeObject o9 = new CompositeObject(new Object());

    assertEquals(0, o1.compareTo(o2));
    assertEquals(-1, o1.compareTo(o3));
    assertEquals(1, o3.compareTo(o1));
    assertEquals(-1, o4.compareTo(o1));
    assertEquals(0, o4.compareTo(o4));
    assertEquals(1, o1.compareTo(o4));
    assertEquals(0, o5.compareTo(o4));
    assertEquals(1, o6.compareTo(o1));
    assertEquals(-1, o1.compareTo(o6));
    assertEquals(1, o1.compareTo(o7));
    assertEquals(-1, o7.compareTo(o1));
    assertEquals(0, o7.compareTo(o8));
    assertEquals(0, o8.compareTo(o7));

    assertTrue(o9.compareTo(o1) > 0);
    assertTrue(o1.compareTo(o9) < 0);

    assertThrows(ClassCastException.class, () -> o1.compareTo(new CompositeObject("1", "2", "3")), "compare of different data types is not allowed");
  }

  @Test
  public void testToString() {
    CompositeObject a = new CompositeObject("first", 2, 3.3f);
    assertEquals("[first, 2, 3.3]", a.toString());

    CompositeObject b = new CompositeObject((Object[]) null);
    assertEquals("[]", b.toString());

    CompositeObject c = new CompositeObject(new Object[]{});
    assertEquals("[]", c.toString());
  }
}