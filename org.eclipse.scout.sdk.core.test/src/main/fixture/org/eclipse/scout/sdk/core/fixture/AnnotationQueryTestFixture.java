/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.core.fixture;

/**
 * <h3>{@link AnnotationQueryTestFixture}</h3> Fixture used in Test AnnotationQueryTest.
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
public class AnnotationQueryTestFixture {

  public static interface ITestIfc {
    @MarkerAnnotation
    void method();

    @MarkerAnnotation
    void method(String firstParam);
  }

  public static class TestClass implements ITestIfc {

    @Override
    @ValueAnnot
    public void method(String firstParam) {
    }

    @Override
    public void method() {
    }
  }

  public static class TestChildClass extends TestClass {
    @Override
    @AnnotationWithDefaultValues
    public void method() {
    }
  }
}
