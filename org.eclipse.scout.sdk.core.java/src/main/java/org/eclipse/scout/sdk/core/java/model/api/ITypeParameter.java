/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.java.model.api;

import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.java.generator.typeparam.ITypeParameterGenerator;
import org.eclipse.scout.sdk.core.java.model.spi.TypeParameterSpi;
import org.eclipse.scout.sdk.core.java.transformer.IWorkingCopyTransformer;

/**
 * <h3>{@link ITypeParameter}</h3> Represents a type parameter.
 *
 * @since 5.1.0
 */
public interface ITypeParameter extends IJavaElement {

  /**
   * Gets all bounds of this {@link ITypeParameter}. The first bound will be the class parameter (if existing) followed
   * by all interface bounds in the order as it is defined in the source or class file.<br>
   * <br>
   * <b>Example: </b> {@code ChildClass<X extends AbstractList<String> & Runnable & Serializable>: .getBounds() =
   * {AbstractList<String>, Runnable, Serializable}}
   *
   * @return A {@link Stream} containing all bounds of this {@link ITypeParameter}.
   */
  Stream<IType> bounds();

  /**
   * Gets the {@link IMember} this {@link ITypeParameter} belongs to.
   *
   * @return The {@link IMember} this {@link ITypeParameter} belongs to.
   */
  IMember declaringMember();

  @Override
  TypeParameterSpi unwrap();

  @Override
  ITypeParameterGenerator<?> toWorkingCopy();

  @Override
  ITypeParameterGenerator<?> toWorkingCopy(IWorkingCopyTransformer transformer);
}
