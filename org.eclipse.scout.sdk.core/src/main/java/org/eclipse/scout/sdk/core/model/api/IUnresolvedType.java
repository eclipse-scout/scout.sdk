/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.model.api;

import java.util.Optional;

import org.eclipse.scout.sdk.core.transformer.IWorkingCopyTransformer;
import org.eclipse.scout.sdk.core.generator.type.ITypeGenerator;
import org.eclipse.scout.sdk.core.model.api.internal.UnresolvedTypeImplementor.UnresolvedTypeSpi;

/**
 * <h3>{@link IUnresolvedType}</h3> Represents a java data type which may not yet exist on the classpath.<br>
 * Use {@link IJavaEnvironment#findUnresolvedType(String)} to retrieve {@link IUnresolvedType}s.
 *
 * @since 5.2.0
 */
public interface IUnresolvedType extends IJavaElement {

  /**
   * Gets the {@link IPackage} of this {@link IUnresolvedType}.
   *
   * @return The {@link IPackage} of this {@link IUnresolvedType}.
   */
  IPackage containingPackage();

  /**
   * Gets the simple name of this {@link IUnresolvedType}.
   *
   * @return The simple name of this {@link IUnresolvedType}.
   */
  @Override
  String elementName();

  /**
   * Gets the fully qualified name of this {@link IUnresolvedType}.<br>
   * Inner types are separated by '$'.<br>
   * <br>
   * <b>Example: </b>{@code org.eclipse.scout.hello.world.MainClass$InnerClass$AnotherInnerClass}.<br>
   *
   * @return The fully qualified name of this {@link IUnresolvedType}.
   */
  String name();

  /**
   * @return The type reference of this {@link IUnresolvedType}.
   */
  String reference();

  /**
   * @return {@code true} if this {@link IUnresolvedType} actually exists. {@code false} otherwise.
   */
  boolean exists();

  /**
   * @return the existing type or an empty {@link Optional} if it not {@link #exists()}.
   */
  Optional<IType> type();

  /**
   * Unwraps this {@link IUnresolvedType} into its underlying {@link UnresolvedTypeSpi}.
   *
   * @return The service provider interface for this {@link IUnresolvedType}.
   */
  @Override
  UnresolvedTypeSpi unwrap();

  /**
   * @return The {@link ISourceRange} of this {@link IUnresolvedType} if it {@link #exists()} and the underlying
   *         {@link #type()} has source attached.
   */
  @Override
  Optional<ISourceRange> source();

  @Override
  ITypeGenerator<?> toWorkingCopy();

  @Override
  ITypeGenerator<?> toWorkingCopy(IWorkingCopyTransformer transformer);
}
