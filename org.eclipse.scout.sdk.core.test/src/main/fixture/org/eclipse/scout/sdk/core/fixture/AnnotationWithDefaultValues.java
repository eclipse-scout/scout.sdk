/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.sdk.core.fixture;

import java.math.RoundingMode;

import javax.annotation.Generated;

/**
 *
 */
public @interface AnnotationWithDefaultValues {
  public int num() default 1;

  public RoundingMode enumValue() default RoundingMode.HALF_UP;

  public String string() default "one";

  public Class<?>type() default String.class;

  public Generated anno() default @Generated("g") ;
}
