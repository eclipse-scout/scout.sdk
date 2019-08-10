/*
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.model.api;

import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.generator.member.IMemberGenerator;
import org.eclipse.scout.sdk.core.generator.transformer.IWorkingCopyTransformer;
import org.eclipse.scout.sdk.core.model.spi.MemberSpi;

/**
 * <h3>{@link IMember}</h3> Represents Java elements that are members.
 *
 * @since 5.1.0
 */
public interface IMember extends IAnnotatable {

  /**
   * Gets the flags of this {@link IMember}. Use the {@link Flags} to access the value in this {@link Integer}.
   *
   * @return The flags of this {@link IMember}.
   * @see Flags
   */
  int flags();

  /**
   * Gets all {@link ITypeParameter}s defined by this {@link IMember} in the order as they appear in the source or class
   * file.<br>
   * <br>
   * Type parameters are declarations as defined by the hosting {@link IMember}. They may have minimal bounds
   * defined.<br>
   * The difference to {@link IType#typeArguments()} is that {@link #typeParameters()} returns the parameter as they are
   * declared by the class file while {@link IType#typeArguments()} holds the currently bound real {@link IType}s.<br>
   * <br>
   * <b>Example: </b><br>
   * {@code public class NumberList<T extends java.lang.Number>}<br>
   * {@code public static NumberList<java.lang.Double> getDoubleValues()}<br>
   * <br>
   * The return {@link IType} of the {@link IMethod} "getDoubleValues" would return the following values:<br>
   * {@code getDoubleValues.requireReturnType().typeParameters().bounds() = java.lang.Number}<br>
   * {@code getDoubleValues.requireReturnType().typeArguments() = java.lang.Double}
   *
   * @return A {@link Stream} holding the type parameters of this {@link IType}.
   */
  Stream<ITypeParameter> typeParameters();

  /**
   * Specifies if this {@link IType} has {@link ITypeParameter}s.
   *
   * @return {@code true} if this is a parameterized {@link IType} (using generics), {@code false} otherwise.
   */
  boolean hasTypeParameters();

  /**
   * Gets the java doc source for this {@link IMember}.
   *
   * @return The {@link ISourceRange} for the java doc of this {@link IMember}.
   */
  Optional<ISourceRange> javaDoc();

  @Override
  MemberSpi unwrap();

  @Override
  IMemberGenerator<?> toWorkingCopy();

  @Override
  IMemberGenerator<?> toWorkingCopy(IWorkingCopyTransformer transformer);
}
