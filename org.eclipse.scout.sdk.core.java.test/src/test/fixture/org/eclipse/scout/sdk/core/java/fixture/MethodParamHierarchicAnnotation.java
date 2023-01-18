/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.java.fixture;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

public class MethodParamHierarchicAnnotation {

  @Target(ElementType.PARAMETER)
  public @interface ParamMarkerAnnotation {

  }

  @Target(ElementType.PARAMETER)
  public @interface ParamAnnotationWithValue {
    String msg();
  }

  public interface ParameterAnnotationIfc {
    void methodWithAnnotatedParams(String firstParam, @ParamMarkerAnnotation int secondParam);
  }

  public static class ParamAnnotationSuperClass implements ParameterAnnotationIfc {
    @Override
    public void methodWithAnnotatedParams(@ParamMarkerAnnotation @ParamAnnotationWithValue(msg = "test") String firstParam, int secondParam) {
      // nop
    }
  }

  public static class ParamAnnotationChildClass extends ParamAnnotationSuperClass {
    @Override
    public void methodWithAnnotatedParams(String firstParam, int secondParam) {
      // nop
    }
  }
}
