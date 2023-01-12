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

import java.math.RoundingMode;

/**
 * Example with single values
 */
@AnnotationWithSingleValues(type = String.class, enumValue = RoundingMode.HALF_UP, num = Integer.MIN_VALUE, string = "alpha", anno = @ValueAnnot("g1"))
public class ClassWithAnnotationWithSingleValues {

  @AnnotationWithSingleValues(type = Integer.class, enumValue = RoundingMode.HALF_DOWN, num = Integer.MAX_VALUE, string = ClassWithAnnotationConstants.ALPHA, anno = @ValueAnnot("g2"))
  public void run(String a) {
    System.out.println(a);
  }

}
