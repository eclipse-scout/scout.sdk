/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.java.generator.field;

import java.util.Optional;
import java.util.function.Function;

import org.eclipse.scout.sdk.core.generator.ISourceGenerator;
import org.eclipse.scout.sdk.core.java.apidef.IApiSpecification;
import org.eclipse.scout.sdk.core.java.builder.IJavaBuilderContext;
import org.eclipse.scout.sdk.core.java.builder.JavaBuilderContextFunction;
import org.eclipse.scout.sdk.core.java.builder.expression.IExpressionBuilder;
import org.eclipse.scout.sdk.core.java.generator.member.IMemberGenerator;
import org.eclipse.scout.sdk.core.java.model.api.IJavaEnvironment;

/**
 * <h3>{@link IFieldGenerator}</h3>
 * <p>
 * An {@link ISourceGenerator} that creates Java fields and static blocks.
 *
 * @since 6.1.0
 */
public interface IFieldGenerator<TYPE extends IFieldGenerator<TYPE>> extends IMemberGenerator<TYPE> {

  /**
   * @return The data type reference of this {@link IFieldGenerator} if it is context independent or an empty
   *         {@link Optional} otherwise.
   */
  Optional<String> dataType();

  /**
   * Gets the data type of this {@link IFieldGenerator}.
   * 
   * @param context
   *          The {@link IJavaBuilderContext} to use to compute the data type in case it is context dependent or
   *          {@code null}.
   * @return The data type reference of this {@link IFieldGenerator} or an empty {@link Optional} if this field has no
   *         data type.
   */
  Optional<String> dataType(IJavaBuilderContext context);

  /**
   * @return A {@link JavaBuilderContextFunction} that describes the data type of this {@link IFieldGenerator} or an
   *         empty {@link Optional} if this field has no data type.
   */
  Optional<JavaBuilderContextFunction<String>> dataTypeFunc();

  /**
   * Sets the data type of this {@link IFieldGenerator}.
   *
   * @param reference
   *          The data type reference or {@code null}. E.g. {@code java.util.List<java.lang.String>}
   * @return This generator.
   * @see #withDataTypeFrom(Class, Function)
   */
  TYPE withDataType(String reference);

  /**
   * Uses the result of the given dataTypeSupplier as data type of this {@link IFieldGenerator}.
   * <p>
   * This method may be handy if the name of a class changes between different versions of an API. The builder then
   * decides which API to use based on the version found in the {@link IJavaEnvironment} of the
   * {@link IJavaBuilderContext}.
   * </p>
   * <b>Example:</b> {@code fieldGenerator.withDataTypeFrom(IJavaApi.class, IJavaApi::Long)}.
   * 
   * @param apiDefinition
   *          The api type that defines the data type. An instance of this API is passed to the dataTypeSupplier. May be
   *          {@code null} in case the given dataTypeSupplier can handle a {@code null} input.
   * @param dataTypeSupplier
   *          A {@link Function} to be called to obtain the data type of this {@link IFieldGenerator}. Must not be
   *          {@code null}.
   * @param <A>
   *          The API type that contains the class name
   * @return This generator.
   * @see #withDataType(String)
   */
  <A extends IApiSpecification> TYPE withDataTypeFrom(Class<A> apiDefinition, Function<A, String> dataTypeSupplier);

  /**
   * Uses the result of the given {@link Function} as data type for this {@link IFieldGenerator}.
   * <p>
   * This method may be handy if the data type is context dependent.
   * </p>
   * 
   * @param dataTypeSupplier
   *          A {@link Function} to be called to obtain the data type of this {@link IFieldGenerator} or {@code null}.
   * @return This generator.
   */
  TYPE withDataTypeFunc(Function<IJavaBuilderContext, String> dataTypeSupplier);

  /**
   * @return The value {@link ISourceGenerator} of this {@link IFieldGenerator}.
   */
  Optional<ISourceGenerator<IExpressionBuilder<?>>> value();

  /**
   * Sets the value of this {@link IFieldGenerator}.
   * <p>
   * If this {@link IFieldGenerator} has an {@link #elementName()}, this specifies the initial value expression of the
   * field (in this case {@link #dataTypeFunc()} is required).<br>
   * Otherwise, the value is printed directly. This allows to create static constructors or blocks.
   *
   * @param valueGenerator
   *          The {@link ISourceGenerator} that creates the value of this {@link IFieldGenerator} or {@code null} for no
   *          initializer.
   * @return This generator.
   */
  TYPE withValue(ISourceGenerator<IExpressionBuilder<?>> valueGenerator);

  /**
   * Marks this {@link IFieldGenerator} to create a {@code transient} field.
   *
   * @return This generator.
   */
  TYPE asTransient();

  /**
   * Marks this {@link IFieldGenerator} to create a {@code volatile} field.
   *
   * @return This generator.
   */
  TYPE asVolatile();
}
