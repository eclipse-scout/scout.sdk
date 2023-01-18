/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.java.fixture;

public class ConstantTestClass {
  private final String NoInit;

  public static final String DirectInit = "value";
  public static final String NullInit = null;
  public static final Runnable ComplexInit = new Runnable() {
    @Override
    public void run() {
    }
  };

  private ConstantTestClass() {
    NoInit = "";
  }

  public String getNoInit() {
    return NoInit;
  }
}
