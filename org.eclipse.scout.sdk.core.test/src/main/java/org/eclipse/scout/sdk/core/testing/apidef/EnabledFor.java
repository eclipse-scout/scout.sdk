/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.testing.apidef;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.eclipse.scout.sdk.core.apidef.IApiSpecification;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.testing.context.ExtendWithJavaEnvironmentFactory;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Specifies an API version filter. If this filter is used on a test the {@link ExtendWithJavaEnvironmentFactory} must
 * be specified as well. The API will then be searched within this {@link IJavaEnvironment}.<br>
 * If the API cannot be found in the {@link IJavaEnvironment} the test is executed.<br>
 * If the API can be found the test is only executed if the version found fulfills the {@link ApiRequirement} given.
 */
@Inherited
@Documented
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(ApiExecutionCondition.class)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface EnabledFor {

  /**
   * @return The {@link IApiSpecification} to search.
   */
  Class<? extends IApiSpecification> api();

  /**
   * @return The type of requirement
   */
  ApiRequirement require();

  /**
   * @return The version to compare the requirement against.
   */
  int[] version();

}
