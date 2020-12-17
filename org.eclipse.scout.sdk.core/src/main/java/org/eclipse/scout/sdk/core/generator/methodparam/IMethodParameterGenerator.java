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
package org.eclipse.scout.sdk.core.generator.methodparam;

import java.util.Optional;
import java.util.function.Function;

import org.eclipse.scout.sdk.core.apidef.ApiFunction;
import org.eclipse.scout.sdk.core.apidef.IApiSpecification;
import org.eclipse.scout.sdk.core.builder.java.IJavaBuilderContext;
import org.eclipse.scout.sdk.core.generator.IAnnotatableGenerator;
import org.eclipse.scout.sdk.core.generator.IJavaElementGenerator;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;

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
   * @return An {@link ApiFunction} that describes the data type of this {@link IMethodParameterGenerator} or an empty
   *         {@link Optional} if this {@link IMethodParameterGenerator} has no data type.
   */
  Optional<ApiFunction<?, String>> dataType();

  /**
   * Sets the data type of this {@link IMethodParameterGenerator}.
   *
   * @param dataType
   *          The new data type
   * @return This generator.
   * @see #withDataTypeFrom(Class, Function)
   */
  TYPE withDataType(String dataType);

  /**
   * Sets the method parameter data type to the result of the given dataTypeSupplier.
   * <p>
   * This method may be handy if the data type of a method parameter changes between different versions of an API. The
   * builder then decides which API to use based on the version found in the {@link IJavaEnvironment} of the
   * {@link IJavaBuilderContext}.
   * </p>
   * <b>Example:</b> {@code methodParameterGenerator.withDataTypeFrom(IJavaApi.class, IJavaApi::Long)}.
   * 
   * @param apiDefinition
   *          The api type that defines the method parameter data type. An instance of this API is passed to the
   *          dataTypeSupplier. May be {@code null} in case the given dataTypeSupplier can handle a {@code null} input.
   * @param dataTypeSupplier
   *          A {@link Function} to be called to obtain the method parameter data type of this
   *          {@link IMethodParameterGenerator}.
   * @param <A>
   *          The API type that contains the class name
   * @return This generator.
   * @see #withDataType(String)
   */
  <A extends IApiSpecification> TYPE withDataTypeFrom(Class<A> apiDefinition, Function<A, String> dataTypeSupplier);
}
