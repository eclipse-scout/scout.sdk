/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.builder.java.member;

import org.eclipse.scout.sdk.core.builder.java.IJavaSourceBuilder;
import org.eclipse.scout.sdk.core.model.api.Flags;

/**
 * <h3>{@link IMemberBuilder}</h3>
 *
 * @since 6.1.0
 */
public interface IMemberBuilder<TYPE extends IMemberBuilder<TYPE>> extends IJavaSourceBuilder<TYPE> {

  /**
   * Appends the source representation of the given flags including a trailing space. The flags are appended in the
   * order as specified by the Java Language Specification.
   *
   * @param flags
   *          The flags to append.
   * @return This builder
   * @see Flags#toString(int)
   */
  TYPE appendFlags(int flags);

}
