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