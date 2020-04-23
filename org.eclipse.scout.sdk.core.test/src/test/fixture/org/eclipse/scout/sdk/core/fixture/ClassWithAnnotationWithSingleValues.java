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
 * Example with single values
 */
@AnnotationWithSingleValues(type = String.class, enumValue = RoundingMode.HALF_UP, num = Integer.MIN_VALUE, string = "alpha", anno = @ValueAnnot("g1"))
public class ClassWithAnnotationWithSingleValues {

  @AnnotationWithSingleValues(type = Integer.class, enumValue = RoundingMode.HALF_DOWN, num = Integer.MAX_VALUE, string = ClassWithAnnotationConstants.ALPHA, anno = @ValueAnnot("g2"))
  public void run(String a) {
    System.out.println(a);
  }

}
