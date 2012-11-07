/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.operation.form.formdata;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 *
 */
public class FormDataUtilityTest {

  @Test
  public void testGetConstantName() {
    runGetConstantName("VALUE", "value");
    runGetConstantName("VALUE", "Value");
    runGetConstantName("VALUE", "VALUE");
    runGetConstantName("I", "i");
    runGetConstantName("I", "I");
    runGetConstantName("SIMPLE_VALUE", "simpleValue");
    runGetConstantName("SIMPLE_VALUE", "SimpleValue");
    runGetConstantName("VALUE_RCP", "valueRCP");
    runGetConstantName("VALUE_RCP_EXT", "valueRCPExt");
    runGetConstantName("RCP_EXT", "RCPExt");
    runGetConstantName("RCP_EXT_", "RCPExt_");
    runGetConstantName("", "");
  }

  /**
   * @param expected
   *          the expected name
   * @param name
   *          input for the {@link FormDataUtility#getConstantName(String)} function.
   */
  private void runGetConstantName(String expected, String name) {
    String actual = FormDataUtility.getConstantName(name);
    assertEquals(expected, actual);
  }
}
