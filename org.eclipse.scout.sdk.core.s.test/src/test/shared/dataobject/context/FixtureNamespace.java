/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dataobject.context;

// cannot implement INamespace here as this class is not available in Scout < 22
@SuppressWarnings("MethodMayBeStatic")
public class FixtureNamespace {
  public static final String ID = "sdk";
  public static final double ORDER = 5100;

  public String getId() {
    return ID;
  }

  public double getOrder() {
    return ORDER;
  }
}
