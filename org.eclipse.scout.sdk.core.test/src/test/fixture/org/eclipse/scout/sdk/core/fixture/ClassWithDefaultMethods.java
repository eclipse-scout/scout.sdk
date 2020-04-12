/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
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
