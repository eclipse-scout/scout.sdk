/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.model.spi;

import java.util.List;

import org.eclipse.scout.sdk.core.model.api.Flags;
import org.eclipse.scout.sdk.core.model.api.IMember;
import org.eclipse.scout.sdk.core.model.api.ISourceRange;

/**
 * <h3>{@link MemberSpi}</h3> Represents Java elements that are members.
 *
 * @since 5.1.0
 */
public interface MemberSpi extends AnnotatableSpi {

  /**
   * Gets the flags of this {@link MemberSpi}. Use the {@link Flags} to access the value in this {@link Integer}.
   *
   * @return The flags of this {@link MemberSpi}.
   * @see Flags
   */
  int getFlags();

  /**
   * @return The {@link TypeSpi} this member is defined in. Never returns {@code null} for {@link MethodSpi} and
   *         {@link FieldSpi}. For {@link TypeSpi} this is the enclosing type that may be null.
   */
  @SuppressWarnings("ClassReferencesSubclass")
  TypeSpi getDeclaringType();

  /**
   * Gets all {@link TypeParameterSpi}s defined by this {@link MemberSpi} in the order as they appear in the source or
   * class file.<br>
   * <br>
   * Type parameters are declarations as defined by the hosting {@link MemberSpi}. They may have minimal bounds defined.
   * <br>
   * The difference to {@link TypeSpi#getTypeArguments()} is that {@link #getTypeParameters()} returns the parameter as
   * they are declared by the class file while {@link TypeSpi#getTypeArguments()} } holds the currently bound real
   * {@link TypeSpi}s. <br>
   * <br>
   * <b>Example: </b><br>
   * {@code public class NumberList<T extends java.lang.Number> {}}<br>
   * {@code public static NumberList<java.lang.Double> getDoubleValues() {}}<br>
   * <br>
   * The return {@link TypeSpi} of the {@link MethodSpi} "getDoubleValues" would return the following values:<br>
   * {@code getDoubleValues.getReturnType().getTypeParameters().getBounds() = java.lang.Number}<br>
   * {@code getDoubleValues.getReturnType().getTypeArguments() = java.lang.Double}
   *
   * @return A {@link List} containing all {@link TypeParameterSpi}s of this {@link MemberSpi}. Never returns
   *         {@code null}.
   */
  List<TypeParameterSpi> getTypeParameters();

  /**
   * Specifies if this {@link MemberSpi} has {@link TypeParameterSpi}s.
   *
   * @return {@code true} if this is a parameterized {@link MemberSpi} (using generics), {@code false} otherwise.
   */
  boolean hasTypeParameters();

  ISourceRange getJavaDoc();

  @Override
  IMember wrap();
}
