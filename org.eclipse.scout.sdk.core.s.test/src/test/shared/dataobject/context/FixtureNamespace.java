/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
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
