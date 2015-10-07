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

import java.util.List;

import org.eclipse.scout.sdk.core.model.spi.MemberSpi;

/**
 * <h3>{@link IMember}</h3> Represents Java elements that are members.
 *
 * @author Matthias Villiger
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
   * Gets the {@link IType} this {@link IMember} is defined in.
   *
   * @return The declaring {@link IType}. Never returns <code>null</code> for {@link IMethod} and {@link IField}. For
   *         {@link IType} this is the enclosing type that may be null for primary types.
   */
  IType declaringType();

  /**
   * Gets all {@link ITypeParameter}s defined by this {@link IType} in the order as they appear in the source or class
   * file.<br>
   * <br>
   * Type parameters are declarations as defined by the hosting {@link IType}. They may have minimal bounds defined.<br>
   * The difference to {@link #typeArguments()} is that {@link #typeParameters()} returns the parameter as they are
   * declared by the class file while {@link #typeArguments()} holds the currently bound real {@link IType}s.<br>
   * <br>
   * <b>Example: </b><br>
   * <code>public class NumberList&lt;T extends java.lang.Number&gt; {}</code><br>
   * <code>public static NumberList&lt;java.lang.Double&gt; getDoubleValues() {}</code><br>
   * <br>
   * The return {@link IType} of the {@link IMethod} "getDoubleValues" would return the following values:<br>
   * <code>getDoubleValues.getReturnType().getTypeParameters().getBounds() = java.lang.Number</code><br>
   * <code>getDoubleValues.getReturnType().getTypeArguments() = java.lang.Double</code>
   *
   * @return
   */
  List<ITypeParameter> typeParameters();

  /**
   * Specifies if this {@link IType} has {@link ITypeParameter}s.
   *
   * @return <code>true</code> if this is a parameterized {@link IType} (using generics), <code>false</code> otherwise.
   */
  boolean hasTypeParameters();

  /**
   * Gets the java doc source for this {@link IMember}.
   * 
   * @return The {@link ISourceRange} for the java doc of this {@link IMember} or <code>null</code> if no source is
   *         attached.
   */
  ISourceRange javaDoc();

  @Override
  MemberSpi unwrap();
}
