/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.sdk.core.fixture;

import java.math.RoundingMode;

import javax.annotation.Generated;

/**
 * Example with single values
 */
@AnnotationWithSingleValues(type = String.class, enumValue = RoundingMode.HALF_UP, num = Integer.MIN_VALUE, string = "alpha", anno = @Generated("g1") )
public class ClassWithAnnotationWithSingleValues {

  @AnnotationWithSingleValues(type = Integer.class, enumValue = RoundingMode.HALF_DOWN, num = Integer.MAX_VALUE, string = ClassWithAnnotationConstants.ALPHA, anno = @Generated("g2") )
  public void run(String a) {
    System.out.println(a);
  }

}
