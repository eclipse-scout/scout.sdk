/*******************************************************************************
 * Copyright (c) 2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.core.generator.member;

import org.eclipse.scout.sdk.core.generator.IAnnotatableGenerator;
import org.eclipse.scout.sdk.core.model.api.Flags;

/**
 * <h3>{@link IMemberGenerator}</h3>
 *
 * @since 6.1.0
 */
public interface IMemberGenerator<TYPE extends IMemberGenerator<TYPE>> extends IAnnotatableGenerator<TYPE> {

  /**
   * Adds the specified flags to this {@link IMemberGenerator}.
   *
   * @param flags
   *          The flags to add.
   * @return This generator.
   * @see Flags
   */
  TYPE withFlags(int flags);

  /**
   * @return The flags of this {@link IMemberGenerator}.
   */
  int flags();

  /**
   * Removes the specified flags from this {@link IMemberGenerator}.
   *
   * @param flags
   *          The flags to remove.
   * @return This generator.
   * @see Flags
   */
  TYPE withoutFlags(int flags);

  /**
   * Marks this {@link IMemberGenerator} to create a {@code public} member.
   *
   * @return This generator.
   * @see Flags
   */
  TYPE asPublic();

  /**
   * Marks this {@link IMemberGenerator} to create a {@code private} member.
   *
   * @return This generator.
   * @see Flags
   */
  TYPE asPrivate();

  /**
   * Marks this {@link IMemberGenerator} to create a {@code protected} member.
   *
   * @return This generator.
   * @see Flags
   */
  TYPE asProtected();

  /**
   * Marks this {@link IMemberGenerator} to create a package private (default visibility) member.
   *
   * @return This generator.
   * @see Flags
   */
  TYPE asPackagePrivate();

  /**
   * Marks this {@link IMemberGenerator} to create a {@code static} member.
   *
   * @return This generator.
   * @see Flags
   */
  TYPE asStatic();

  /**
   * Marks this {@link IMemberGenerator} to create a {@code final} member.
   *
   * @return This generator.
   * @see Flags
   */
  TYPE asFinal();

}
