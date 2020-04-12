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

import javax.annotation.Generated;

/**
 *
 */
public @interface AnnotationWithDefaultValues {
  int num() default 1;

  RoundingMode enumValue() default RoundingMode.HALF_UP;

  String string() default "one";

  Class<?> type() default String.class;

  Generated anno() default @Generated("g");
}
