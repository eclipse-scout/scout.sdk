/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.generator.annotation;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import org.eclipse.scout.sdk.core.apidef.IApiSpecification;
import org.eclipse.scout.sdk.core.apidef.ITypeNameSupplier;
import org.eclipse.scout.sdk.core.builder.java.IJavaBuilderContext;
import org.eclipse.scout.sdk.core.builder.java.JavaBuilderContextFunction;
import org.eclipse.scout.sdk.core.builder.java.expression.IExpressionBuilder;
import org.eclipse.scout.sdk.core.generator.IJavaElementGenerator;
import org.eclipse.scout.sdk.core.generator.ISourceGenerator;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.util.Strings;

/**
 * <h3>{@link IAnnotationGenerator}</h3>
 *
 * @since 6.1.0
 */
public interface IAnnotationGenerator<TYPE extends IAnnotationGenerator<TYPE>> extends IJavaElementGenerator<TYPE> {

  /**
   * Gets the fully qualified name of the annotation type.<br>
   * E.g. "{@code java.lang.Override}".<br>
   * <br>
   * <p>
   * <b>Note</b>: If the annotation name is specified using {@link #withElementNameFrom(Class, Function)} or
   * {@link #withElementNameFunc(Function)} it may be context dependent and this method may therefore return an empty
   * {@link Optional} for such cases even though an element name is set. To be sure use
   * {@link #elementName(IJavaBuilderContext)} whenever possible.
   * </p>
   *
   * @return the fully qualified name of the annotation or an empty {@link Optional} if it is not yet set or is context
   *         dependent.
   */
  @Override
  Optional<String> elementName();

  /**
   * Gets the fully qualified name of the annotation type.<br>
   * E.g. {@code java.lang.Override}
   *
   * @param context
   *          The {@link IJavaBuilderContext} to use in case the element name is context dependent.
   * @return the fully qualified name of the annotation or an empty {@link Optional} if it is not yet set.
   */
  @Override
  Optional<String> elementName(IJavaBuilderContext context);

  /**
   * Sets the fully qualified name of the annotation type (e.g. "{@code java.lang.Override}").
   *
   * @param newName
   *          The new fully qualified annotation name or {@code null}.
   * @return this generator
   * @see #withElementNameFrom(Class, Function)
   */
  @Override
  TYPE withElementName(String newName);

  /**
   * Sets the result of the nameSupplier as the fully qualified name of this {@link IAnnotationGenerator}.
   * <p>
   * This method may be handy if the annotation name changes between different versions of an API. Then the name is
   * chosen based on the API available in the {@link IJavaBuilderContext}.
   * </p>
   * <br>
   * <b>Example:</b> {@code annotationGenerator.withElementNameFrom(IJavaApi.class, api -> api.Override().fqn())}.
   *
   * @param apiDefinition
   *          The api type that defines the annotation type. An instance of this API is passed to the nameSupplier. May
   *          be {@code null} in case the given nameSupplier can handle a {@code null} input.
   * @param nameSupplier
   *          A {@link Function} to be called to obtain the annotation type to set.
   * @param <A>
   *          The API type that contains the class name
   * @return This generator.
   * @see #withElementName(String)
   * @see #withAnnotationNameFrom(Class, Function)
   * @see #withElementNameFunc(Function)
   */
  @Override
  <A extends IApiSpecification> TYPE withElementNameFrom(Class<A> apiDefinition, Function<A, String> nameSupplier);

  /**
   * Sets the result of the nameSupplier as the fully qualified name of this {@link IAnnotationGenerator}.
   * <p>
   * This method may be handy if the annotation name changes between different versions of an API. Then the name is
   * chosen based on the API available in the {@link IJavaBuilderContext}.
   * </p>
   * <br>
   * <b>Example:</b> {@code annotationGenerator.withElementNameFrom(IJavaApi.class, IJavaApi::Override)}.
   *
   * @param apiDefinition
   *          The api type that defines the annotation type. An instance of this API is passed to the nameSupplier. May
   *          be {@code null} in case the given nameSupplier can handle a {@code null} input.
   * @param nameSupplier
   *          A {@link Function} to be called to obtain the annotation type to set.
   * @param <A>
   *          The API type that contains the class name
   * @return This generator.
   * @see #withElementName(String)
   * @see #withElementNameFrom(Class, Function)
   * @see #withElementNameFunc(Function)
   */
  <A extends IApiSpecification> TYPE withAnnotationNameFrom(Class<A> apiDefinition, Function<A, ITypeNameSupplier> nameSupplier);

  /**
   * Sets the result of the nameSupplier as the fully qualified name of this {@link IAnnotationGenerator}.
   * <p>
   * This method may be handy if the annotation name depends on the {@link IJavaBuilderContext}.
   * </p>
   * 
   * @param nameSupplier
   *          A {@link Function} to be called to obtain the annotation type to set.
   * @return This generator.
   * @see #withElementName(String)
   * @see #withElementNameFrom(Class, Function)
   * @see #withAnnotationNameFrom(Class, Function)
   */
  @Override
  TYPE withElementNameFunc(Function<IJavaBuilderContext, String> nameSupplier);

  /**
   * Adds a new element to this {@link IAnnotationGenerator} using the specified name and raw value. If there exists a
   * value with given name already, the existing one is replaced.
   *
   * @param name
   *          The name of the annotation element. If it is blank (see {@link Strings#isBlank(CharSequence)}, this method
   *          does nothing.
   * @param rawSrc
   *          The raw value as Java source. Must not be {@code null}.
   * @return This generator
   */
  TYPE withElement(String name, CharSequence rawSrc);

  /**
   * Adds a new element to this {@link IAnnotationGenerator} using the specified name and value generator. If there
   * exists a value with given name already, the existing one is replaced.
   *
   * @param name
   *          The name of the annotation element. If it is blank (see {@link Strings#isBlank(CharSequence)}, this method
   *          does nothing.
   * @param value
   *          The {@link ISourceGenerator} that creates the element value. If the generator is {@code null}, this method
   *          does nothing.
   * @return This generator
   */
  TYPE withElement(String name, ISourceGenerator<IExpressionBuilder<?>> value);

  /**
   * Adds a new element to this annotation having the name returned by the given elementNameSupplier.
   * <p>
   * This method may be handy if the element name changes between different versions of an API. The builder then decides
   * which API to use based on the version found in the {@link IJavaEnvironment} of the {@link IJavaBuilderContext}.
   * </p>
   * <b>Example:</b>
   * {@code annotationGenerator.withElementFrom(IJavaApi.class, api -> api.SuppressWarnings().valueElementName(), "value")}.
   * 
   * @param apiDefinition
   *          The api type that defines the element name. An instance of this API is passed to the elementNameSupplier.
   *          May be {@code null} in case the given elementNameSupplier can handle a {@code null} input.
   * @param elementNameSupplier
   *          A {@link Function} to be called to obtain the element name. If it is {@code null}, this method does
   *          nothing.
   * @param valueSrc
   *          The raw value as Java source. Must not be {@code null}.
   * @param <A>
   *          The API type that contains the class name
   * @return This generator.
   * @see #withElement(String, CharSequence)
   * @see #withElement(String, ISourceGenerator)
   * @see #withElementFrom(Class, Function, ISourceGenerator)
   */
  <A extends IApiSpecification> TYPE withElementFrom(Class<A> apiDefinition, Function<A, String> elementNameSupplier, CharSequence valueSrc);

  /**
   * Adds a new element to this annotation having the name returned by the given elementNameSupplier. This method does
   * nothing if the elementNameSupplier or the value generator is {@code null}.
   * <p>
   * This method may be handy if the element name changes between different versions of an API. The builder then decides
   * which API to use based on the version found in the {@link IJavaEnvironment} of the {@link IJavaBuilderContext}.
   * </p>
   * <b>Example:</b>
   * {@code annotationGenerator.withElementFrom(IJavaApi.class, api -> api.SuppressWarnings().valueElementName(), b -> b.stringLiteral("value"))}.
   *
   * @param apiDefinition
   *          The api type that defines the element name. An instance of this API is passed to the elementNameSupplier.
   *          May be {@code null} in case the given elementNameSupplier can handle a {@code null} input.
   * @param elementNameSupplier
   *          A {@link Function} to be called to obtain the element name. If it is {@code null}, this method does
   *          nothing.
   * @param value
   *          The {@link ISourceGenerator} that creates the element value. If the generator is {@code null}, this method
   *          does nothing.
   * @param <A>
   *          The API type that contains the class name
   * @return This generator.
   * @see #withElement(String, CharSequence)
   * @see #withElement(String, ISourceGenerator)
   * @see #withElementFrom(Class, Function, CharSequence)
   */
  <A extends IApiSpecification> TYPE withElementFrom(Class<A> apiDefinition, Function<A, String> elementNameSupplier, ISourceGenerator<IExpressionBuilder<?>> value);

  /**
   * Adds a new element to this annotation having the name returned by the given elementNameSupplier. This method may be
   * handy if the element name is context dependent. This method does nothing if one of the inputs is {@code null}.
   * 
   * @param elementNameSupplier
   *          A {@link Function} to be called to obtain the element name. If it is {@code null}, this method does
   *          nothing.
   * @param value
   *          The {@link ISourceGenerator} that creates the element value. If it is {@code null}, this method does
   *          nothing.
   * @return This generator.
   */
  TYPE withElementFunc(Function<IJavaBuilderContext, String> elementNameSupplier, ISourceGenerator<IExpressionBuilder<?>> value);

  /**
   * Adds a new element to this annotation having the name returned by the given elementNameSupplier. This method may be
   * handy if the element name is context dependent. This method does nothing if one of the inputs is {@code null}.
   *
   * @param elementNameSupplier
   *          A {@link Function} to be called to obtain the element name. If it is {@code null}, this method does
   *          nothing.
   * @param valueSrc
   *          The raw value as Java source. Must not be {@code null}.
   * @return This generator.
   */
  TYPE withElementFunc(Function<IJavaBuilderContext, String> elementNameSupplier, CharSequence valueSrc);

  /**
   * Gets the last element having the given name. This method only returns elements for which the name can be computed
   * without context (see {@link #withElementFrom(Class, Function, CharSequence)} and
   * {@link #withElementFunc(Function, CharSequence)}).
   * 
   * @param name
   *          The name to search
   * @return This generator.
   */
  Optional<ISourceGenerator<IExpressionBuilder<?>>> element(String name);

  /**
   * Gets the last {@link ISourceGenerator} for which the specified {@link Predicate} returns {@code true}.
   *
   * @param selector
   *          A {@link Predicate} that decides which element to return. Must not be {@code null}.
   * @return An {@link Optional} holding the {@link ISourceGenerator} for this element name.
   */
  Optional<ISourceGenerator<IExpressionBuilder<?>>> element(Predicate<JavaBuilderContextFunction<String>> selector);

  /**
   * Gets a {@link Map} with all elements of this {@link IAnnotationGenerator} for which the name can be computed
   * without context (see {@link #withElementFrom(Class, Function, CharSequence)} and
   * {@link #withElementFunc(Function, CharSequence)}).<br>
   * The {@link Map} iterates through the elements in the order in which they have been added.
   *
   * @return A {@link Map} with the element names and the associated {@link ISourceGenerator}s.
   */
  Map<String, ISourceGenerator<IExpressionBuilder<?>>> elements();

  /**
   * @return An unmodifiable {@link Map} with the element name function as key and the corresponding source generator as
   *         values.
   */
  Map<JavaBuilderContextFunction<String>, ISourceGenerator<IExpressionBuilder<?>>> elementsFunc();

  /**
   * Removes the element with the given name. Only elements for which the name can be computed without context (see
   * {@link #withElementFrom(Class, Function, CharSequence)} and {@link #withElementFunc(Function, CharSequence)}) can
   * be removed.
   * 
   * @param elementName
   *          The element name to remove.
   * @return This generator.
   */
  TYPE withoutElement(String elementName);

  /**
   * Removes all elements for which the given {@link Predicate} returns {@code true}.
   * 
   * @param toRemove
   *          A {@link Predicate} that decides if an element should be removed. May be {@code null}. In that case all
   *          elements are removed.
   * @return This generator.
   */
  TYPE withoutElement(Predicate<JavaBuilderContextFunction<String>> toRemove);
}
