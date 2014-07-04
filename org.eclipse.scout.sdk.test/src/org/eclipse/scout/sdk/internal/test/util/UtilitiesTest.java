/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.internal.test.util;

import org.eclipse.scout.sdk.util.NamingUtility;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;
import org.junit.Assert;
import org.junit.Test;

/**
 * <h3>{@link UtilitiesTest}</h3>
 *
 * @author Matthias Villiger
 * @since 4.0.0 03.04.2014
 */
public class UtilitiesTest {
  @Test
  public void testCamelCase1() throws Exception {
    Assert.assertEquals("OneTwoThree", NamingUtility.toJavaCamelCase("one two three", false));
  }

  @Test
  public void testCamelCase2() throws Exception {
    Assert.assertEquals("oneTwoThree", NamingUtility.toJavaCamelCase("one two three", true));
  }

  @Test
  public void testCamelCase3() throws Exception {
    Assert.assertEquals("OneTwoThree", NamingUtility.toJavaCamelCase("OneTwoThree", false));
  }

  @Test
  public void testCamelCase4() throws Exception {
    Assert.assertEquals("oneTwoThree", NamingUtility.toJavaCamelCase("OneTwoThree", true));
  }

  @Test
  public void testCamelCase5() throws Exception {
    Assert.assertEquals("OneTwoThree", NamingUtility.toJavaCamelCase("one-two.Three", false));
  }

  @Test
  public void testValueInBetween() throws Exception {
    final double DELTA = 0.00000000001;
    Assert.assertEquals(1500, ScoutTypeUtility.getOrderValueInBetween(1000, 2000), DELTA);
    Assert.assertEquals(1000, ScoutTypeUtility.getOrderValueInBetween(0, 2000), DELTA);
    Assert.assertEquals(50, ScoutTypeUtility.getOrderValueInBetween(100, 0), DELTA);
    Assert.assertEquals(1.5, ScoutTypeUtility.getOrderValueInBetween(1, 2), DELTA);
    Assert.assertEquals(0.5, ScoutTypeUtility.getOrderValueInBetween(0, 1), DELTA);
    Assert.assertEquals(2, ScoutTypeUtility.getOrderValueInBetween(1, 3), DELTA);
    Assert.assertEquals(2.7, ScoutTypeUtility.getOrderValueInBetween(2.4, 3), DELTA);
    Assert.assertEquals(3, ScoutTypeUtility.getOrderValueInBetween(2, 3.1), DELTA);
    Assert.assertEquals(2.05, ScoutTypeUtility.getOrderValueInBetween(2, 2.1), DELTA);
    Assert.assertEquals(2.2, ScoutTypeUtility.getOrderValueInBetween(2.1, 2.3), DELTA);
    Assert.assertEquals(4, ScoutTypeUtility.getOrderValueInBetween(2.1, 5.7), DELTA);
    Assert.assertEquals(4, ScoutTypeUtility.getOrderValueInBetween(2.1, 6.7), DELTA);
    Assert.assertEquals(5, ScoutTypeUtility.getOrderValueInBetween(2.1, 7.7), DELTA);
    Assert.assertEquals(3, ScoutTypeUtility.getOrderValueInBetween(2.6, 3.7), DELTA);
    Assert.assertEquals(187, ScoutTypeUtility.getOrderValueInBetween(125, 250), DELTA);
    Assert.assertEquals(2000, ScoutTypeUtility.getOrderValueInBetween(1000, 100000), DELTA);
  }
}
