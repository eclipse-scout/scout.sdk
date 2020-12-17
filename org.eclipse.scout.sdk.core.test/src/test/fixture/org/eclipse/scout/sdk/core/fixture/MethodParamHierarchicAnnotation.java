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
