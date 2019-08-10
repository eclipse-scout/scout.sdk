/*
 * Copyright (c) 2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.generator.methodparam;

import java.util.Optional;

import org.eclipse.scout.sdk.core.generator.IAnnotatableGenerator;
import org.eclipse.scout.sdk.core.generator.IJavaElementGenerator;

/**
 * <h3>{@link IMethodParameterGenerator}</h3>
 * <p>
 * An {@link IJavaElementGenerator} that creates method parameters.
 *
 * @since 6.1.0
 */
public interface IMethodParameterGenerator<TYPE extends IMethodParameterGenerator<TYPE>> extends IAnnotatableGenerator<TYPE> {

  /**
   * @return {@code true} if this {@link IMethodParameterGenerator} is marked as {@code final}.
   */
  boolean isFinal();

  /**
   * Marks this {@link IMethodParameterGenerator} as {@code final}.
   *
   * @return This generator.
   */
  TYPE asFinal();

  /**
   * Marks this {@link IMethodParameterGenerator} as not {@code final}.
   *
   * @return This generator.
   */
  TYPE notFinal();

  /**
   * Specifies if this {@link IMethodParameterGenerator} should be {@code final}.
   *
   * @param newFinalValue
   *          {@code true} if it should be {@code final}. {@code false} otherwise.
   * @return This generator.
   */
  TYPE asFinal(boolean newFinalValue);

  /**
   * @return {@code true} if this {@link IMethodParameterGenerator} is a varargs parameter.
   */
  boolean isVarargs();

  /**
   * Marks this {@link IMethodParameterGenerator} as varargs parameter.
   *
   * @return This generator.
   */
  TYPE asVarargs();

  /**
   * Marks this {@link IMethodParameterGenerator} as no varargs parameter.
   *
   * @return This generator.
   */
  TYPE notVarargs();

  /**
   * Specifies if this {@link IMethodParameterGenerator} should be a vararg parameter.
   *
   * @param newVarargsValue
   *          {@code true} if it should be a varargs parameter. {@code false} otherwise.
   * @return This generator.
   */
  TYPE asVarargs(boolean newVarargsValue);

  /**
   * @return The data type of this {@link IMethodParameterGenerator}.
   */
  Optional<String> dataType();

  /**
   * Sets the data type of this {@link IMethodParameterGenerator}.
   *
   * @param dataType
   *          The new data type
   * @return This generator.
   */
  TYPE withDataType(String dataType);
}
