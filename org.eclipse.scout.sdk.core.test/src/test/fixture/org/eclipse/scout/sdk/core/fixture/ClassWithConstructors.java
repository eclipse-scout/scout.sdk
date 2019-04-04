package org.eclipse.scout.sdk.core.fixture;

public class ClassWithConstructors {
  public ClassWithConstructors() {
    this("whatever");
  }

  public ClassWithConstructors(String other) {
    System.out.println(other);
  }
}
