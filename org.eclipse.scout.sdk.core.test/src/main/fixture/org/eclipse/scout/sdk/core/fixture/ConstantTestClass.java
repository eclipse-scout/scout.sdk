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
package org.eclipse.scout.sdk.core.fixture;

/**
 *
 */
public class ConstantTestClass {
  private final String NoInit;

  public final static String DirectInit = "value";
  public final static String NullInit = null;
  public final static Runnable ComplexInit = new Runnable() {
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
