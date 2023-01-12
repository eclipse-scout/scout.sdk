/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.fixture;

import java.io.Serializable;

public class ClassWithDefaultMethods<T extends Runnable & Serializable> implements InterfaceWithDefaultMethods<T> {
  @Override
  public void defMethod2(String param) {
    System.out.println(param);
  }

  @Override
  public T getElement(int counter) {
    return null;
  }

  @Override
  public <R extends CharSequence> R getString(int... elements) {
    return null;
  }
}
