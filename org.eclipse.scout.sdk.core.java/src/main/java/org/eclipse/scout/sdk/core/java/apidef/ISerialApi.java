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

public interface ISerialApi extends IApiSpecification {

  /**
   * @return {@code true} if the {@link java.io.Serial} annotation should be used.
   */
  boolean isSerialAnnotationEnabled();
}
