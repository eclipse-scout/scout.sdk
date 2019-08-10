/*
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.fixture;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.List;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

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
