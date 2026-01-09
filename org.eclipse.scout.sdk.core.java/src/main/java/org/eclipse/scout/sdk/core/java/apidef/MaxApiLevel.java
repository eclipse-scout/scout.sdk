/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.java.apidef;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies the maximum API level for an {@link IApiSpecification}.<p>
 * This means the API spec is valid up to the specified version. Missing segments in the given value are considered to be {@link Integer#MAX_VALUE} and are therefore always included.
 * <p>
 * Example:<br>
 * Specifying a version as {@code @MaxApiLevel(9)} therefore means it is valid up to e.g. {@code 9.2147483647.2147483647}. This includes version 7.x, 8.x and e.g. 9.4.4 which are all smaller.
 *
 * @see IApiSpecification#maxLevel()
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface MaxApiLevel {
  /**
   * @return The version segments
   */
  int[] value();
}
