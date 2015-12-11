/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.core.model.api;

import org.eclipse.scout.sdk.core.model.spi.TypeSpi;

/**
 * <h3>{@link IUnresolvedType}</h3> Represents a java data type which may not yet exist on the classpath.<br>
 * Use {@link IJavaEnvironment#findUnresolvedType(String)} to retrieve {@link IUnresolvedType}s.
 *
 * @author Ivan Motsch
 * @since 5.2.0
 */
public interface IUnresolvedType extends IJavaElement {

  /**
   * Gets the {@link IPackage} of this {@link IUnresolvedType}.
   *
   * @return The {@link IPackage} of this {@link IUnresolvedType}. Never returns <code>null</code>.
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
   * <b>Example: </b><code>org.eclipse.scout.hello.world.MainClass$InnerClass$AnotherInnerClass</code>.<br>
   *
   * @return The fully qualified name of this {@link IUnresolvedType}.
   */
  String name();

  /**
   * @return The type signature of this {@link IUnresolvedType}.
   */
  String signature();

  /**
   * @return <code>true</code> if this {@link IUnresolvedType} actually exists. <code>false</code> otherwise.
   */
  boolean exists();

  /**
   * @return the existing type or <code>null</code> if it not {@link #exists()}.
   */
  IType type();

  /**
   * Unwraps this {@link IUnresolvedType} into its underlying {@link TypeSpi}.
   *
   * @return The service provider interface for this {@link IUnresolvedType} if it {@link #exists()}. <code>null</code>
   *         otherwise.
   */
  @Override
  TypeSpi unwrap();

  /**
   * @return The {@link ISourceRange} of this {@link IUnresolvedType} if it {@link #exists()} and the underlying
   *         {@link #type()} has source attached. Never returns <code>null</code>. Use
   *         {@link ISourceRange#isAvailable()} to check if source is actually available for this element.
   */
  @Override
  ISourceRange source();
}
