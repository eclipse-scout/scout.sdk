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
package org.eclipse.scout.sdk.core.model.spi;

import java.util.List;

import org.eclipse.scout.sdk.core.model.api.Flags;
import org.eclipse.scout.sdk.core.model.api.IMember;
import org.eclipse.scout.sdk.core.model.api.ISourceRange;

/**
 * <h3>{@link MemberSpi}</h3> Represents Java elements that are members.
 *
 * @author Matthias Villiger
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
   * @return The {@link TypeSpi} this member is defined in. Never returns <code>null</code> for {@link MethodSpi} and
   *         {@link FieldSpi}. For {@link TypeSpi} this is the enclosing type that may be null.
   */
  TypeSpi getDeclaringType();

  /**
   * Gets all {@link TypeParameterSpi}s defined by this {@link MemberSpi} in the order as they appear in the source or
   * class file.<br>
   * <br>
   * Type parameters are declarations as defined by the hosting {@link MemberSpi}. They may have minimal bounds defined.
   * <br>
   * The difference to {@link TypeSpi#getTypeArguments()} is that {@link #getTypeParameters()} returns the parameter as
   * they are declared by the class file while {@link #typeArguments()} holds the currently bound real {@link TypeSpi}s.
   * <br>
   * <br>
   * <b>Example: </b><br>
   * <code>public class NumberList&lt;T extends java.lang.Number&gt; {}</code><br>
   * <code>public static NumberList&lt;java.lang.Double&gt; getDoubleValues() {}</code><br>
   * <br>
   * The return {@link TypeSpi} of the {@link MethodSpi} "getDoubleValues" would return the following values:<br>
   * <code>getDoubleValues.getReturnType().getTypeParameters().getBounds() = java.lang.Number</code><br>
   * <code>getDoubleValues.getReturnType().getTypeArguments() = java.lang.Double</code>
   *
   * @return A {@link List} containing all {@link TypeParameterSpi}s of this {@link MemberSpi}. Never returns
   *         <code>null</code>.
   */
  List<TypeParameterSpi> getTypeParameters();

  /**
   * Specifies if this {@link MemberSpi} has {@link TypeParameterSpi}s.
   *
   * @return <code>true</code> if this is a parameterized {@link MemberSpi} (using generics), <code>false</code>
   *         otherwise.
   */
  boolean hasTypeParameters();

  ISourceRange getJavaDoc();

  @Override
  IMember wrap();
}
