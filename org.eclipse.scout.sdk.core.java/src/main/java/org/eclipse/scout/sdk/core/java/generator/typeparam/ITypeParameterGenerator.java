/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.java.generator.typeparam;

import java.util.function.Function;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.java.apidef.IApiSpecification;
import org.eclipse.scout.sdk.core.java.builder.IJavaBuilderContext;
import org.eclipse.scout.sdk.core.java.builder.JavaBuilderContextFunction;
import org.eclipse.scout.sdk.core.java.generator.IJavaElementGenerator;
import org.eclipse.scout.sdk.core.java.model.api.IJavaEnvironment;

/**
 * <h3>{@link ITypeParameterGenerator}</h3>
 * <p>
 * An {@link IJavaElementGenerator} that creates type parameters.
 *
 * @since 6.1.0
 */
public interface ITypeParameterGenerator<TYPE extends ITypeParameterGenerator<TYPE>> extends IJavaElementGenerator<TYPE> {

  /**
   * Adds the specified binding to this {@link ITypeParameterGenerator}.
   *
   * @param binding
   *          The fully qualified name of the binding to add. This method does nothing if the binding is blank or
   *          {@code null}.
   * @return This generator.
   * @see #withBindingFrom(Class, Function)
   */
  TYPE withBinding(String binding);

  /**
   * Adds the result of the bindingSupplier to the list of bindings.
   * <p>
   * This method may be handy if bindings change between different versions of an API. The builder then decides which
   * API to use based on the version found in the {@link IJavaEnvironment} of the {@link IJavaBuilderContext}.
   * </p>
   * <b>Example:</b> {@code typeParameterGenerator.withBindingFrom(IJavaApi.class, IJavaApi::List)}.
   *
   * @param apiDefinition
   *          The api type that defines the binding type. An instance of this API is passed to the bindingSupplier. May
   *          be {@code null} in case the given bindingSupplier can handle a {@code null} input.
   * @param bindingSupplier
   *          A {@link Function} to be called to obtain the binding type to add to this {@link ITypeParameterGenerator}.
   *          Must not be {@code null}.
   * @param <A>
   *          The API type that contains the class name
   * @return This generator.
   * @see #withBinding(String)
   */
  <A extends IApiSpecification> TYPE withBindingFrom(Class<A> apiDefinition, Function<A, String> bindingSupplier);

  /**
   * Adds the result of the bindingSupplier to the list of bindings.
   * <p>
   * This method may be handy if bindings are context dependent.
   * </p>
   * 
   * @param bindingSupplier
   *          A {@link Function} to be called to obtain the binding type to add to this {@link ITypeParameterGenerator}.
   *          If it is {@code null}, this method does nothing.
   * @return This generator.
   * @see #withBinding(String)
   */
  TYPE withBindingFunc(Function<IJavaBuilderContext, String> bindingSupplier);

  /**
   * @return A {@link Stream} with all bounds that can be computed without context.
   */
  Stream<String> bounds();

  /**
   * @return A {@link Stream} returning all type parameter bounds.
   */
  Stream<JavaBuilderContextFunction<String>> boundsFunc();

}
