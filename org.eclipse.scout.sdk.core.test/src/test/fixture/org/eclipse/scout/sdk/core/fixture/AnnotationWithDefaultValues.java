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
