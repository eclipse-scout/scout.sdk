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

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.LOCAL_VARIABLE;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.List;

/**
 *
 */
@Target({TYPE, ANNOTATION_TYPE, METHOD, FIELD, LOCAL_VARIABLE, PARAMETER})
@Retention(RUNTIME)
@MarkerAnnotation
public @interface TestAnnotation {

  enum TestEnum {
    A,
    B
  }

  Class<?>[] values() default {List.class};

  TestEnum en() default TestEnum.A;

  ValueAnnot[] inner() default {};
}
