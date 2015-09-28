/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.core.util;

import org.junit.Assert;
import org.junit.Test;

/**
 * <h3>{@link CompositeObjectTest}</h3>
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
public class CompositeObjectTest {
  @Test
  public void testSameness() {
    Long a = new Long(1);
    Long b = new Long(1);
    Assert.assertNotSame(a, b);
    Assert.assertEquals(a, b);

    SameCompositeObject o1 = new SameCompositeObject(a, b);
    SameCompositeObject o2 = new SameCompositeObject(a, a);
    SameCompositeObject o3 = new SameCompositeObject(a, b);

    Assert.assertFalse(o1.equals(o2));
    Assert.assertTrue(o1.equals(o3));
  }

  @Test
  public void testEquality() {
    Long a = new Long(1);
    Long b = new Long(1);
    Assert.assertNotSame(a, b);
    Assert.assertEquals(a, b);

    CompositeObject c1 = new CompositeObject(a, b);
    CompositeObject c2 = new CompositeObject(a, a);
    CompositeObject c3 = new CompositeObject(a, b);

    Assert.assertTrue(c1.equals(c2));
    Assert.assertTrue(c1.equals(c3));
  }
}
