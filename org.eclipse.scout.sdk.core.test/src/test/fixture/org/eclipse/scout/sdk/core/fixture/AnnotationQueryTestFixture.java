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
