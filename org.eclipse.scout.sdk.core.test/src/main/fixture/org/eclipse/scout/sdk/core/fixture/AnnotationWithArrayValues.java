/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.sdk.core.fixture;

import java.math.RoundingMode;

/**
 *
 */
public @interface AnnotationWithArrayValues {
  public int[]nums();

  public RoundingMode[]enumValues();

  public String[]strings();

  public Class<?>[]types();

  public AnnotationWithSingleValues[]annos();
}
