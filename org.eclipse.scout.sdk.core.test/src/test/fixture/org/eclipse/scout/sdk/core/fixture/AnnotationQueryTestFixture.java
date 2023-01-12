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

/**
 * <h3>{@link AnnotationQueryTestFixture}</h3> Fixture used in Test AnnotationQueryTest.
 *
 * @since 5.2.0
 */
public class AnnotationQueryTestFixture {

  @AnnotationWithDefaultValues
  final Object m_obj = new Object();

  public interface ITestIfc {
    @MarkerAnnotation
    void method();

    @MarkerAnnotation
    void method(String firstParam);
  }

  public static class TestClass implements ITestIfc {

    @Override
    @ValueAnnot
    public void method(String firstParam) {
      // nop
    }

    @Override
    public void method() {
      // nop
    }
  }

  public static class TestChildClass extends TestClass {
    @Override
    @AnnotationWithDefaultValues
    public void method() {
      // nop
    }
  }
}
