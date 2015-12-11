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

import org.eclipse.scout.sdk.core.model.spi.MethodSpi;
import org.eclipse.scout.sdk.core.model.sugar.MethodParameterQuery;
import org.eclipse.scout.sdk.core.model.sugar.SuperMethodQuery;

/**
 * <h3>{@link IMethod}</h3> Represents a method declaration.
 *
 * @author Matthias Villiger
 * @since 5.1.0
 */
public interface IMethod extends IMember {

  /**
   * @return Gets the return data {@link IType} of this {@link IMethod}. The result may be the void type. Never returns
   *         <code>null</code>.
   * @see IType#isVoid()
   */
  IType returnType();

  /**
   * Gets all exception declarations of this {@link IMethod} in the order as they appear in the source or class file.
   *
   * @return a {@link List} containing all thrown {@link IType}s of this {@link IMethod}.
   */
  List<IType> exceptionTypes();

  /**
   * Gets if this method is a constructor.
   *
   * @return <code>true</code> if this method is a constructor, <code>false</code> otherwise.
   */
  boolean isConstructor();

  /**
   * @return the method name without any brackets.
   */
  @Override
  String elementName();

  /**
   * If this {@link IMethod} is a synthetic parameterized method (for example the super class of a parameterized type
   * with applied type arguments) then this method returns the original method without the type arguments applied.
   * <p>
   * Otherwise this is returned
   */
  IMethod originalMethod();

  /**
   * Gets the method body source.
   *
   * @return The source of the method body. Never returns <code>null</code>. Use {@link ISourceRange#isAvailable()} to
   *         check if source is actually available for this element.
   */
  ISourceRange sourceOfBody();

  @Override
  MethodSpi unwrap();

  /**
   * Gets a {@link SuperMethodQuery} to access methods overridden by this {@link IMethod}.
   *
   * @return The new {@link SuperMethodQuery} for this {@link IMethod}.
   */
  SuperMethodQuery superMethods();

  /**
   * Gets a {@link MethodParameterQuery} to access the parameters of this {@link IMethod}.
   *
   * @return The new {@link MethodParameterQuery} for this {@link IMethod}.
   */
  MethodParameterQuery parameters();
}
