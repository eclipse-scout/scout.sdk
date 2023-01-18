/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.java.generator.member;

import java.util.Optional;

import org.eclipse.scout.sdk.core.java.generator.IAnnotatableGenerator;
import org.eclipse.scout.sdk.core.java.generator.IJavaElementGenerator;
import org.eclipse.scout.sdk.core.java.generator.compilationunit.ICompilationUnitGenerator;
import org.eclipse.scout.sdk.core.java.generator.type.ITypeGenerator;
import org.eclipse.scout.sdk.core.java.model.api.Flags;

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

  /**
   * @return The {@link IJavaElementGenerator} this {@link IMemberGenerator} will be created in. Typically, this is
   *         either an {@link ICompilationUnitGenerator} if this is a primary {@link ITypeGenerator}, another
   *         {@link ITypeGenerator} (e.g. for fields or methods) or nothing if not connected to a declaring generator.
   */
  Optional<IJavaElementGenerator<?>> declaringGenerator();
}
