/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.fixture;

import java.io.Serializable;

public interface InterfaceWithDefaultMethods<T extends Runnable & Serializable> {
  default int defMethod() {
    return Integer.MAX_VALUE;
  }

  default void defMethod2(final String param) {
    return;
  }

  T getElement(int counter);

  <R extends CharSequence> R getString(int... elements);
}
