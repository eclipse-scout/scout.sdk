/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.model.api;

import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.transformer.IWorkingCopyTransformer;
import org.eclipse.scout.sdk.core.generator.typeparam.ITypeParameterGenerator;
import org.eclipse.scout.sdk.core.model.spi.TypeParameterSpi;

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
