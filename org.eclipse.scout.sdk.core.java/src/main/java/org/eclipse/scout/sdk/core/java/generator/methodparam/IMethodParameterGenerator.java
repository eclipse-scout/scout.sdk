/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.java.generator.methodparam;

import java.util.Optional;
import java.util.function.Function;

import org.eclipse.scout.sdk.core.java.apidef.IApiSpecification;
import org.eclipse.scout.sdk.core.java.builder.IJavaBuilderContext;
import org.eclipse.scout.sdk.core.java.builder.JavaBuilderContextFunction;
import org.eclipse.scout.sdk.core.java.generator.IAnnotatableGenerator;
import org.eclipse.scout.sdk.core.java.generator.IJavaElementGenerator;
import org.eclipse.scout.sdk.core.java.model.api.IJavaEnvironment;

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
   * Gets the full reference of the data type of this {@link IMethodParameterGenerator} including all type arguments.
   * 
   * @param context
   *          The {@link IJavaEnvironment} to use to compute the data types in case they are context dependent (e.g.
   *          using an {@link IApiSpecification}). May be {@code null}.
   * @return The reference of the data type of this {@link IMethodParameterGenerator}.
   * @see #withDataType(String)
   * @see #isVarargs()
   */
  String reference(IJavaBuilderContext context);

  /**
   * Gets the data type reference of this {@link IMethodParameterGenerator}.
   * 
   * @param context
   *          The {@link IJavaBuilderContext} to use to compute the data types in case they are context dependent (e.g.
   *          using an {@link IApiSpecification}). May be {@code null}.
   * @param useErasureOnly
   *          If {@code true}, no type arguments are included. If {@code false}, all type arguments are part of the
   *          reference.
   * @return The reference of the data type of this {@link IMethodParameterGenerator}
   * @see #withDataType(String)
   * @see #isVarargs()
   */
  String reference(IJavaBuilderContext context, boolean useErasureOnly);

  /**
   * @return The data type of the method parameter if it can be computed without context. Otherwise, an empty
   *         {@link Optional}.
   */
  Optional<String> dataType();

  /**
   * @param context
   *          The context to compute the data type.
   * @return The data type of the method parameter or an empty {@link Optional} if it has no data type yet.
   */
  Optional<String> dataType(IJavaBuilderContext context);

  /**
   * @return An {@link JavaBuilderContextFunction} that describes the data type of this
   *         {@link IMethodParameterGenerator} or an empty {@link Optional} if this {@link IMethodParameterGenerator}
   *         has no data type.
   */
  Optional<JavaBuilderContextFunction<String>> dataTypeFunc();

  /**
   * Sets the data type of this {@link IMethodParameterGenerator}.
   *
   * @param dataType
   *          The new data type or {@code null}.
   * @return This generator.
   * @see #withDataTypeFrom(Class, Function)
   * @see #withDataTypeFunc(Function)
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
   *          {@link IMethodParameterGenerator}. Must not be {@code null}.
   * @param <A>
   *          The API type that contains the class name
   * @return This generator.
   * @see #withDataType(String)
   * @see #withDataTypeFunc(Function)
   */
  <A extends IApiSpecification> TYPE withDataTypeFrom(Class<A> apiDefinition, Function<A, String> dataTypeSupplier);

  /**
   * Sets the method parameter data type to the result of the given dataTypeSupplier.
   * <p>
   * This method may be handy if the data type is context dependent.
   * </p>
   *
   * @param dataTypeSupplier
   *          A {@link Function} to be called to obtain the method parameter data type of this
   *          {@link IMethodParameterGenerator} or {@code null}.
   * @return This generator.
   * @see #withDataType(String)
   * @see #withDataTypeFrom(Class, Function)
   */
  TYPE withDataTypeFunc(Function<IJavaBuilderContext, String> dataTypeSupplier);
}
