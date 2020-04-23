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

import java.math.RoundingMode;

/**
 * Example with array values
 */
@AnnotationWithArrayValues(
    nums = {1, 2},
    enumValues = {RoundingMode.HALF_UP, RoundingMode.HALF_DOWN},
    strings = {"alpha", ClassWithAnnotationConstants.ALPHA},
    types = {String.class, String.class},
    annos = {
        @AnnotationWithSingleValues(type = Integer.class, enumValue = RoundingMode.HALF_UP, num = 11, string = "beta", anno = @ValueAnnot("g1")),
        @AnnotationWithSingleValues(type = Integer.class, enumValue = RoundingMode.HALF_DOWN, num = 12, string = ClassWithAnnotationConstants.BETA, anno = @ValueAnnot("g2")),
    })
public class ClassWithAnnotationWithArrayValues {

  @AnnotationWithArrayValues(
      nums = {21, 22},
      enumValues = {RoundingMode.HALF_EVEN, RoundingMode.HALF_EVEN},
      strings = {"gamma", ClassWithAnnotationConstants.GAMMA},
      types = {Float.class, Float.class},
      annos = {
          @AnnotationWithSingleValues(type = Double.class, enumValue = RoundingMode.HALF_EVEN, num = 31, string = "delta", anno = @ValueAnnot("g3")),
          @AnnotationWithSingleValues(type = Double.class, enumValue = RoundingMode.HALF_EVEN, num = 32, string = ClassWithAnnotationConstants.DELTA, anno = @ValueAnnot("g4")),
      })
  public void run(String a) {
    System.out.println(a);
  }

}
