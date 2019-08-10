/*
 * Copyright (c) 2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
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
