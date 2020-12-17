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
package org.eclipse.scout.sdk.core.builder.java.expression;

import java.util.function.Function;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.apidef.IApiSpecification;
import org.eclipse.scout.sdk.core.apidef.IClassNameSupplier;
import org.eclipse.scout.sdk.core.builder.ISourceBuilder;
import org.eclipse.scout.sdk.core.builder.java.IJavaBuilderContext;
import org.eclipse.scout.sdk.core.builder.java.IJavaSourceBuilder;
import org.eclipse.scout.sdk.core.generator.ISourceGenerator;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;

/**
 * <h3>{@link IExpressionBuilder}</h3>
 * <p>
 * An {@link ISourceBuilder} that provides methods to create java expressions.
 *
 * @since 6.1.0
 */
public interface IExpressionBuilder<TYPE extends IExpressionBuilder<TYPE>> extends IJavaSourceBuilder<TYPE> {

  /**
   * Appends the specified elements as array.
   * <p>
   * <b>Example:</b> <code>{ element0, element1, element2 }</code>
   *
   * @param elements
   *          The {@link ISourceGenerator}s generating the source of the array elements. Must not be {@code null}.
   * @param formatWithNewlines
   *          Specifies if a new line should be created for each array element.
   * @return This builder
   */
  @SuppressWarnings("HtmlTagCanBeJavadocTag")
  TYPE array(Stream<? extends ISourceGenerator<ISourceBuilder<?>>> elements, boolean formatWithNewlines);

  /**
   * Appends a reference to an {@code enum} value.
   *
   * @param enumType
   *          The fully qualified name of the enum class. Must not be {@code null}.
   * @param enumField
   *          The name of the enum field to reference. Must not be {@code null}.
   * @return This builder
   */
  TYPE enumValue(CharSequence enumType, CharSequence enumField);

  /**
   * Appends a class literal for the given type.
   * <p>
   * <b>Example:</b> {@code Long.class}
   *
   * @param reference
   *          The fully qualified name of the class to reference.
   * @return This builder
   */
  TYPE classLiteral(CharSequence reference);

  /**
   * Appends a class literal obtained from an {@link IApiSpecification}. The fully qualified name to use is obtained by
   * using the {@link IClassNameSupplier} returned by invoking the given nameSupplier.
   * <p>
   * This method may be handy if the name of a class changes between different versions of an API. The builder then
   * decides which API to use based on the version found in the {@link IJavaEnvironment} of the
   * {@link IJavaBuilderContext}.
   * </p>
   * <b>Example:</b> {@code expressionBuilder.classLiteralFrom(IJavaApi.class, IJavaApi::Long)} creates
   * {@code Long.class}.
   * 
   * @param apiClass
   *          The api type that contains the class name. An instance of this type is passed to the nameSupplier. May be
   *          {@code null} in case the given nameSupplier can handle a {@code null} input.
   * @param nameSupplier
   *          A {@link Function} to be called to obtain the {@link IClassNameSupplier} whose fully qualified name should be
   *          added as class literal.
   * @param <T>
   *          The API type that contains the class name
   * @return
   */
  <T extends IApiSpecification> TYPE classLiteralFrom(Class<T> apiClass, Function<T, IClassNameSupplier> nameSupplier);

  /**
   * Appends a string literal with given value. The value is automatically escaped as necessary and surrounded with
   * double quotes.<br>
   * If the specified value is {@code null}, a null literal is appended instead (see {@link #nullLiteral()}).
   * <p>
   * <b>Example:</b> {@code abc"def} -> {@code "abc\"def"}
   *
   * @param literalValue
   *          The literal value without leading and trailing quotes or {@code null}.
   * @return This builder
   */
  TYPE stringLiteral(CharSequence literalValue);

  /**
   * Appends {@code null}.
   *
   * @return This builder
   */
  TYPE nullLiteral();

  /**
   * Appends a string array with the elements from the specified {@link Stream}. The values are automatically escaped as
   * necessary and surrounded with quotes.
   *
   * @param elements
   *          The raw elements of the array. Must not be {@code null}. If a value in the {@link Stream} is {@code null},
   *          a null literal is appended (see {@link #nullLiteral()}).
   * @param formatWithNewlines
   *          Specifies if a new line should be created for each array element.
   * @return This builder
   * @see #stringLiteral(CharSequence)
   * @see #stringLiteralArray(CharSequence...)
   * @see #stringLiteralArray(CharSequence[], boolean)
   * @see #nullLiteral()
   */
  TYPE stringLiteralArray(Stream<? extends CharSequence> elements, boolean formatWithNewlines);

  /**
   * Appends a string array with the elements specified. The values are automatically escaped as necessary and
   * surrounded with quotes. All values are placed on a single line.
   * 
   * @param elements
   *          The elements of the array. If a value in the array is {@code null}, a null literal is appended (see
   *          {@link #nullLiteral()}).
   * @return This builder
   * @see #stringLiteral(CharSequence)
   * @see #stringLiteralArray(CharSequence[], boolean)
   * @see #stringLiteralArray(Stream, boolean)
   */
  TYPE stringLiteralArray(CharSequence... elements);

  /**
   * Appends a string array with the elements from the specified array. The values are automatically escaped as
   * necessary and surrounded with quotes.
   *
   * @param elements
   *          The raw elements of the array. Must not be {@code null}. If a value in the array is {@code null}, a null
   *          literal is appended (see {@link #nullLiteral()}).
   * @param formatWithNewlines
   *          Specifies if a new line should be created for each array element.
   * @return This builder
   * @see #stringLiteral(CharSequence)
   * @see #stringLiteralArray(CharSequence...)
   * @see #stringLiteralArray(Stream, boolean)
   * @see #nullLiteral()
   */
  TYPE stringLiteralArray(CharSequence[] elements, boolean formatWithNewlines);

  /**
   * Appends a string array with the elements from the specified array. The values are automatically escaped as
   * necessary and surrounded with quotes.
   *
   * @param elements
   *          The raw elements of the array. Must not be {@code null}. If a value in the array is {@code null}, a null
   *          literal is appended (see {@link #nullLiteral()}).
   * @param formatWithNewlines
   *          Specifies if a new line should be created for each array element.
   * @param stringLiteralOnSingleElementArray
   *          If {@code true} and the given element array exactly contains one element, a string literal is appended
   *          instead of an array with one string literal. This may be useful e.g. for annotation element values: An
   *          element of type string array may also be filled with a string literal.
   * @return This builder
   * @see #stringLiteral(CharSequence)
   * @see #nullLiteral()
   */
  TYPE stringLiteralArray(CharSequence[] elements, boolean formatWithNewlines, boolean stringLiteralOnSingleElementArray);

  /**
   * Appends the default value for the given data type. This method has no effect if the given data type has no default
   * value (e.g. the {@code void} type) or is {@code null}.
   *
   * @param dataTypeFqn
   *          The fully qualified data type.
   * @return This builder
   */
  TYPE appendDefaultValueOf(CharSequence dataTypeFqn);

  /**
   * Appends a {@code new} clause including a trailing space.
   *
   * @return This builder
   */
  TYPE appendNew();

  /**
   * Appends a {@code throw} clause including a trailing space.
   *
   * @return This builder
   */
  TYPE appendThrow();

  /**
   * Appends an {@code if} expression with a trailing space.
   *
   * @return This builder
   */
  TYPE appendIf();

  /**
   * Appends a {@code !}.
   *
   * @return This builder
   */
  TYPE appendNot();
}
