/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.model.api;

import org.eclipse.scout.sdk.core.generator.methodparam.IMethodParameterGenerator;
import org.eclipse.scout.sdk.core.transformer.IWorkingCopyTransformer;
import org.eclipse.scout.sdk.core.model.spi.MethodParameterSpi;

/**
 * <h3>{@link IMethodParameter}</h3> Represents a parameter of an {@link IMethod}.
 *
 * @since 5.1.0
 */
public interface IMethodParameter extends IAnnotatable {

  /**
   * Gets the data type of this parameter.
   *
   * @return The {@link IType} that represents the data type of this parameter.
   */
  IType dataType();

  /**
   * Gets the flags of this method parameter (e.g. final). Use the class {@link Flags} to decode the resulting
   * {@link Integer}.<br>
   * <b>Note: </b>If the surrounding {@link IType} is a binary type, the underlying class file may not contain all flags
   * as defined in the original source code because of reorganizations and optimizations of the compiler.
   *
   * @return An {@link Integer} holding all the flags of this method.
   * @see Flags
   */
  int flags();

  /**
   * @return The position of this {@link IMethodParameter} in the parameter list of the declaring {@link IMethod}.
   */
  int index();

  /**
   * Gets the {@link IMethod} this parameter belongs to
   *
   * @return The {@link IMethod} this parameter belongs to.
   */
  IMethod declaringMethod();

  @Override
  MethodParameterSpi unwrap();

  @Override
  IMethodParameterGenerator<?> toWorkingCopy();

  @Override
  IMethodParameterGenerator<?> toWorkingCopy(IWorkingCopyTransformer transformer);
}
