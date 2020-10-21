/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.generator.annotation;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import org.eclipse.scout.sdk.core.apidef.ApiFunction;
import org.eclipse.scout.sdk.core.apidef.IApiSpecification;
import org.eclipse.scout.sdk.core.apidef.IClassNameSupplier;
import org.eclipse.scout.sdk.core.builder.java.IJavaBuilderContext;
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
   * Gets the fully qualified name of the annotation type.
   * <p>
   * <b>Note</b>: If the annotation name is specified using {@link #withElementNameFrom(Class, Function)} it may be API
   * dependent and this method may therefore return an empty {@link Optional} for such cases even though an element name
   * is set. To be sure use {@link #elementName(IJavaEnvironment)} whenever possible.
   * </p>
   * E.g. {@code java.lang.Override}
   *
   * @return the fully qualified name of the annotation or an empty {@link Optional} if it is not yet set or is API
   *         dependent.
   */
  @Override
  Optional<String> elementName();

  /**
   * Gets the fully qualified name of the annotation type.<br>
   * E.g. {@code java.lang.Override}
   * 
   * @param context
   *          The {@link IJavaEnvironment} to use in case the element name is API dependent.
   * @return the fully qualified name of the annotation or an empty {@link Optional} if it is not yet set.
   */
  Optional<String> elementName(IJavaEnvironment context);

  /**
   * Sets the fully qualified name of the annotation type (e.g. java.lang.Override).
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
   * This method may be handy if the annotation name changes between different versions of an API. The builder then
   * decides which API to use based on the version found in the {@link IJavaEnvironment} of the
   * {@link IJavaBuilderContext}.
   * </p>
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
   */
  <A extends IApiSpecification> TYPE withElementNameFrom(Class<A> apiDefinition, Function<A, IClassNameSupplier> nameSupplier);

  /**
   * Adds a new element to this {@link IAnnotationGenerator} using the specified name and raw value. If there exists a
   * value with given name already, the existing one is replaced.
   *
   * @param name
   *          The name of the annotation element. Must not be blank (see {@link Strings#isBlank(CharSequence)}.
   * @param rawSrc
   *          The raw value as Java source. Must not be {@code null}.
   * @return This generator
   */
  TYPE withElement(String name, CharSequence rawSrc);

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
   *          A {@link Function} to be called to obtain the element name.
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
   * Adds a new element to this annotation having the name returned by the given elementNameSupplier.
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
   *          A {@link Function} to be called to obtain the element name.
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
   * Adds a new element to this {@link IAnnotationGenerator} using the specified name and value generator. If there
   * exists a value with given name already, the existing one is replaced.
   *
   * @param name
   *          The name of the annotation element. Must not be blank (see {@link Strings#isBlank(CharSequence)}.
   * @param value
   *          The {@link ISourceGenerator} that creates the element value. If the generator is {@code null}, this method
   *          does nothing.
   * @return This generator
   */
  TYPE withElement(String name, ISourceGenerator<IExpressionBuilder<?>> value);

  /**
   * Gets the last {@link ISourceGenerator} for which the specified {@link Predicate} returns {@code true}.
   *
   * @param selector
   *          A {@link Predicate} that decides which element to return.
   * @return An {@link Optional} holding the {@link ISourceGenerator} for this element name.
   */
  Optional<ISourceGenerator<IExpressionBuilder<?>>> element(Predicate<ApiFunction<?, String>> selector);

  /**
   * Gets a {@link Map} with all elements of this {@link IAnnotationGenerator}.
   * <p>
   * The key is the {@link ApiFunction} defining the element name. The value is the {@link ISourceGenerator} generating the value source for the
   * corresponding element.
   * <p>
   * The {@link Map} iterates through the elements in the order in which they have been added.
   *
   * @return An unmodifiable {@link Map} with the elements and the associated {@link ISourceGenerator}s.
   */
  Map<ApiFunction<?, String>, ISourceGenerator<IExpressionBuilder<?>>> elements();

  /**
   * Removes all elements for which the given {@link Predicate} returns {@code true}.
   * 
   * @param toRemove
   *          A {@link Predicate} that decides if an element should be removed. May be {@code null}. In that case all
   *          elements are removed.
   * @return This generator.
   */
  TYPE withoutElement(Predicate<ApiFunction<?, String>> toRemove);
}
