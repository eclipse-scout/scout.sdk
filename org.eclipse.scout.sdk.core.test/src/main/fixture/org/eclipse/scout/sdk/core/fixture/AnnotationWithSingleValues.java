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
public @interface AnnotationWithSingleValues {
  public int num();

  public RoundingMode enumValue();

  public String string();

  public Class<?>type();

  public Generated anno();
}
