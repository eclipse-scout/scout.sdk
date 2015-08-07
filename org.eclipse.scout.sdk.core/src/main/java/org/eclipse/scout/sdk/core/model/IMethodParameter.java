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
package org.eclipse.scout.sdk.core.model;

/**
 * <h3>{@link IMethodParameter}</h3> Represents a parameter of an {@link IMethod}.
 *
 * @author Matthias Villiger
 * @since 5.1.0
 */
public interface IMethodParameter {

  /**
   * Gets the name of this method parameter (without any brackets).<br>
   * <b>Note: </b>If the surrounding {@link IType} is a binary type, the underlying class file may not contain the
   * original parameter names. In that case this method returns a synthetic parameter name of the form "arg" +
   * {parameterIndex}.
   *
   * @return The name of this parameter
   */
  String getName();

  /**
   * Gets the data type of this parameter.
   *
   * @return The {@link IType} that represents the data type of this parameter.
   */
  IType getType();

  /**
   * Gets the flags of this method parameter (e.g. final). Use the class {@link Flags} to decode the resulting
   * {@link Integer}.<br>
   * <b>Note: </b>If the surrounding {@link IType} is a binary type, the underlying class file may not contain all flags
   * as defined in the original source code because of reorganizations and optimizations of the compiler.
   *
   * @return An {@link Integer} holding all the flags of this method.
   * @see Flags
   */
  int getFlags();

  /**
   * Gets the {@link IMethod} this parameter belongs to
   * 
   * @return The {@link IMethod} this parameter belongs to. Never returns <code>null</code>.
   */
  IMethod getOwnerMethod();

}
