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
