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
package org.eclipse.scout.sdk.core.generator.method;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.apidef.ApiFunction;
import org.eclipse.scout.sdk.core.apidef.IApiSpecification;
import org.eclipse.scout.sdk.core.apidef.IClassNameSupplier;
import org.eclipse.scout.sdk.core.builder.java.IJavaBuilderContext;
import org.eclipse.scout.sdk.core.builder.java.body.IMethodBodyBuilder;
import org.eclipse.scout.sdk.core.generator.IJavaElementGenerator;
import org.eclipse.scout.sdk.core.generator.ISourceGenerator;
import org.eclipse.scout.sdk.core.generator.member.IMemberGenerator;
import org.eclipse.scout.sdk.core.generator.methodparam.IMethodParameterGenerator;
import org.eclipse.scout.sdk.core.generator.methodparam.MethodParameterGenerator;
import org.eclipse.scout.sdk.core.generator.typeparam.ITypeParameterGenerator;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.model.api.IMethod;
import org.eclipse.scout.sdk.core.util.JavaTypes;
import org.eclipse.scout.sdk.core.util.Strings;

/**
 * <h3>{@link IMethodGenerator}</h3>
 * <p>
 * An {@link IJavaElementGenerator} that creates Java methods.
 *
 * @since 6.1.0
 */
public interface IMethodGenerator<TYPE extends IMethodGenerator<TYPE, BODY>, BODY extends IMethodBodyBuilder<?>> extends IMemberGenerator<TYPE> {

  /**
   * Returns a unique identifier for this {@link IMethod}. The identifier looks like
   * 'methodName(dataTypeParam1,dataTypeParam2)'.
   *
   * @param context
   *          The context {@link IJavaEnvironment} for which the identifier should be computed. This is required because
   *          method parameter data types may be API dependent (see
   *          {@link MethodParameterGenerator#withDataTypeFrom(Class, Function)}).
   * @return The created identifier
   * @see JavaTypes#createMethodIdentifier(CharSequence, java.util.Collection)
   * @see IMethod#identifier()
   */
  String identifier(IJavaEnvironment context);

  /**
   * Returns the unique identifier for this {@link IMethodGenerator}. The identifier looks like
   * 'methodName(dataTypeParam1,dataTypeParam2)'.
   *
   * @param context
   *          The context {@link IJavaEnvironment} for which the identifier should be computed. This is required because
   *          method parameter data types may be API dependent (see
   *          {@link MethodParameterGenerator#withDataTypeFrom(Class, Function)}).
   * @param useErasureOnly
   *          If {@code true} only the type erasure is used for all method parameter types.
   * @return The created identifier
   * @see JavaTypes#createMethodIdentifier(CharSequence, java.util.Collection)
   * @see IMethod#identifier(boolean)
   */
  String identifier(IJavaEnvironment context, boolean useErasureOnly);

  /**
   * @return An {@link ApiFunction} that describes the return type of this {@link IMethodGenerator} or an empty
   *         {@link Optional} if this {@link IMethodGenerator} describes a constructor.
   */
  Optional<ApiFunction<?, String>> returnType();

  /**
   * Sets the method name to the result of the given nameSupplier.
   * <p>
   * This method may be handy if the name of a method changes between different versions of an API. The builder then
   * decides which API to use based on the version found in the {@link IJavaEnvironment} of the
   * {@link IJavaBuilderContext}.
   * </p>
   * <b>Example:</b> {@code methodGenerator.withElementNameFrom(IJavaApi.class, api -> api.Long().valueOfMethodName())}.
   * 
   * @param apiDefinition
   *          The api type that defines the method name. An instance of this API is passed to the nameSupplier. May be
   *          {@code null} in case the given nameSupplier can handle a {@code null} input.
   * @param nameSupplier
   *          A {@link Function} to be called to obtain the method name of this {@link IMethodGenerator}.
   * @param <A>
   *          The API type that contains the class name
   * @return This generator.
   * @see #withElementName(String)
   */
  <A extends IApiSpecification> TYPE withElementNameFrom(Class<A> apiDefinition, Function<A, String> nameSupplier);

  /**
   * Gets the method name of this generator.
   * 
   * @param context
   *          The context {@link IJavaBuilderContext} for which the method name should be computed. This is required
   *          because the method name may be API dependent (see
   *          {@link IMethodGenerator#withElementNameFrom(Class, Function)}).
   * @return The method name or an empty {@link Optional} if this method has no name.
   * @see #withElementNameFrom(Class, Function)
   * @see #withElementName(String)
   */
  Optional<String> elementName(IJavaBuilderContext context);

  /**
   * Gets the method name of this generator.
   * 
   * @param context
   *          The context {@link IJavaEnvironment} for which the method name should be computed. This is required
   *          because the method name may be API dependent (see
   *          {@link IMethodGenerator#withElementNameFrom(Class, Function)}).
   * @return The method name or an empty {@link Optional} if this method has no name.
   * @see #withElementNameFrom(Class, Function)
   * @see #withElementName(String)
   */
  Optional<String> elementName(IJavaEnvironment context);

  /**
   * Sets the return type of this {@link IMethodGenerator}.
   *
   * @param returnType
   *          The return type (e.g. {@link JavaTypes#_void}) or {@code null} if this {@link IMethodGenerator} is a
   *          constructor.
   * @return This generator.
   * @see #withReturnTypeFrom(Class, Function)
   */
  TYPE withReturnType(String returnType);

  /**
   * Sets the method return type to the result of the given returnTypeSupplier.
   * <p>
   * This method may be handy if the return type of a method changes between different versions of an API. The builder
   * then decides which API to use based on the version found in the {@link IJavaEnvironment} of the
   * {@link IJavaBuilderContext}.
   * </p>
   * <b>Example:</b> {@code methodGenerator.withReturnTypeFrom(IJavaApi.class, IJavaApi::Long)}.
   * 
   * @param apiDefinition
   *          The api type that defines the method return type. An instance of this API is passed to the
   *          returnTypeSupplier. May be {@code null} in case the given returnTypeSupplier can handle a {@code null}
   *          input.
   * @param returnTypeSupplier
   *          A {@link Function} to be called to obtain the method return type of this {@link IMethodGenerator}.
   * @param <A>
   *          The API type that contains the class name
   * @return This generator.
   * @see #withReturnType(String)
   */
  <A extends IApiSpecification> TYPE withReturnTypeFrom(Class<A> apiDefinition, Function<A, String> returnTypeSupplier);

  /**
   * @return A {@link Stream} with all {@link Throwable} types of the {@code throws} clause of this
   *         {@link IMethodGenerator}.
   */
  Stream<ApiFunction<?, IClassNameSupplier>> throwables();

  /**
   * Adds the given reference to the {@link Throwable Throwables} of this {@link IMethodGenerator}. They will be printed
   * in the {@code throws} declaration of the method.
   * <p>
   * If the same reference is already added, this method does nothing.
   *
   * @param throwableFqn
   *          Must not be blank (see {@link Strings#isBlank(CharSequence)}).
   * @return This generator.
   * @see #withThrowableFrom(Class, Function)
   */
  TYPE withThrowable(String throwableFqn);

  /**
   * Adds the result of the throwableSupplier to the list of thrown throwables.
   * <p>
   * This method may be handy if the throwable type changes between different versions of an API. The builder then
   * decides which API to use based on the version found in the {@link IJavaEnvironment} of the
   * {@link IJavaBuilderContext}.
   * </p>
   * <b>Example:</b> {@code methodGenerator.withThrowableFrom(IJavaApi.class, IJavaApi::IOException)}.
   * 
   * @param apiDefinition
   *          The api type that defines the throwable type. An instance of this API is passed to the throwableSupplier.
   *          May be {@code null} in case the given throwableSupplier can handle a {@code null} input.
   * @param throwableSupplier
   *          A {@link Function} to be called to obtain the throwable type to add to this {@link IMethodGenerator}.
   * @param <A>
   *          The API type that contains the class name
   * @return This generator.
   * @see #withThrowable(String)
   */
  <A extends IApiSpecification> TYPE withThrowableFrom(Class<A> apiDefinition, Function<A, IClassNameSupplier> throwableSupplier);

  /**
   * Removes all throwables for which the given {@link Predicate} returns {@code true}.
   *
   * @param toRemoveFilter
   *          A {@link Predicate} that decides if a throwable should be removed. May be {@code null}. In that case all
   *          throwables are removed.
   * @return This generator.
   */
  TYPE withoutThrowable(Predicate<ApiFunction<?, IClassNameSupplier>> toRemoveFilter);

  /**
   * @return The {@link ISourceGenerator} that creates the method body content.
   */
  Optional<ISourceGenerator<BODY>> body();

  /**
   * Sets the {@link ISourceGenerator} that creates the method body content.
   *
   * @param body
   *          The body {@link ISourceGenerator} or {@code null} if the method should not have a body.
   * @return This generator.
   */
  TYPE withBody(ISourceGenerator<BODY> body);

  /**
   * @return A {@link Stream} returning all {@link IMethodParameterGenerator}s of this {@link IMethodGenerator}.
   */
  Stream<IMethodParameterGenerator<?>> parameters();

  /**
   * Adds the specified {@link IMethodParameterGenerator} to the list of parameters of this {@link IMethodGenerator}.
   *
   * @param parameter
   *          The parameter generator to add. This method does nothing if the specified parameter is {@code null}.
   * @return This generator.
   */
  TYPE withParameter(IMethodParameterGenerator<?> parameter);

  /**
   * Removes the first {@link IMethodParameterGenerator} with the specified parameter name.
   *
   * @param parameterName
   *          The name of the parameter to remove. Must not be blank (see {@link Strings#isBlank(CharSequence)}).
   * @return This generator.
   */
  TYPE withoutParameter(String parameterName);

  /**
   * Adds the specified {@link ITypeParameterGenerator} to the type parameters of this {@link IMethodGenerator}.
   *
   * @param typeParameter
   *          The {@link ITypeParameterGenerator} to add. This method has no effect if the parameter is {@code null}.
   * @return This generator.
   */
  TYPE withTypeParameter(ITypeParameterGenerator<?> typeParameter);

  /**
   * @return A {@link Stream} returning all {@link ITypeParameterGenerator}s of this {@link IMethodGenerator}.
   */
  Stream<ITypeParameterGenerator<?>> typeParameters();

  /**
   * Removes the first {@link ITypeParameterGenerator} with the specified type parameter name.
   *
   * @param elementName
   *          The name of the type parameter to remove. Must not be blank (see {@link Strings#isBlank(CharSequence)}).
   * @return This generator.
   */
  TYPE withoutTypeParameter(String elementName);

  /**
   * Marks this {@link IMethodGenerator} to be {@code abstract}.
   *
   * @return This generator.
   */
  TYPE asAbstract();

  /**
   * Marks this {@link IMethodGenerator} to be {@code synchronized}.
   *
   * @return This generator.
   */
  TYPE asSynchronized();

  /**
   * Marks this {@link IMethodGenerator} as {@code default} method (to be used in interfaces).
   * 
   * @return This generator.
   */
  TYPE asDefaultMethod();
}
