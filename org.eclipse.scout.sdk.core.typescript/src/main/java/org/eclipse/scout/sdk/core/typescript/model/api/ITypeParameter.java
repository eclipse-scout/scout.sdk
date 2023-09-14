/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.typescript.model.api;

import java.util.Optional;

import org.eclipse.scout.sdk.core.typescript.model.spi.TypeParameterSpi;

/**
 * Represents a type parameter of an {@link IES6Class}.<br>
 * <br>
 * Example:
 * 
 * <pre>
 *   export interface MyClass&lt;TTypeParam extends ConstraintClass = DefaultConstraint&gt; { }
 * </pre>
 */
public interface ITypeParameter extends INodeElement {
  @Override
  TypeParameterSpi spi();

  /**
   * @return The class this {@link ITypeParameter} belongs to.
   */
  IES6Class declaringClass();

  /**
   * @return The data type constraint of the type parameter (if existing). This is the {@link IDataType} after the
   *         {@code extends} keyword
   */
  Optional<IDataType> constraint();

  /**
   * @return The default constraint data type. This is the {@link IDataType} specified after the {@code =} (equals)
   *         sign.
   */
  Optional<IDataType> defaultConstraint();
}
