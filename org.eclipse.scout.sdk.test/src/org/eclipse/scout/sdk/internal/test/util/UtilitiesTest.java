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
}
