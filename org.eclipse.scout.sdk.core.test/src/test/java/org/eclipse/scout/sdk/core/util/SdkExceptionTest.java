/*******************************************************************************
 * Copyright (c) 2017 BSI Business Systems Integration AG.
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

import org.junit.jupiter.api.Test;

/**
 * <h3>{@link SdkExceptionTest}</h3>
 *
 * @since 7.0.0
 */
public class SdkExceptionTest {

  @Test
  public void testMessageOnly() {
    try {
      throw new SdkException("msg");
    }
    catch (SdkException e) {
      assertEquals("msg", e.getMessage());
    }
  }

  @Test
  public void testMessageWithArgs() {
    try {
      throw new SdkException("msg{}{}", "1", 2);
    }
    catch (SdkException e) {
      assertEquals("msg12", e.getMessage());
    }

    try {
      throw new SdkException("msg{}{}", null, 22, new Exception("inner"));
    }
    catch (SdkException e) {
      assertEquals("msgnull22", e.getMessage());
      assertEquals("inner", e.getCause().getMessage());
    }
  }

  @Test
  public void testExceptionOnly() {
    try {
      throw new SdkException(new Exception("inner"));
    }
    catch (SdkException e) {
      assertEquals("", e.getMessage());
      assertEquals("inner", e.getCause().getMessage());
    }
  }
}
