/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.generator.method;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.apidef.IApiSpecification;
import org.eclipse.scout.sdk.core.apidef.ITypeNameSupplier;
import org.eclipse.scout.sdk.core.builder.java.IJavaBuilderContext;
import org.eclipse.scout.sdk.core.builder.java.JavaBuilderContextFunction;
import org.eclipse.scout.sdk.core.builder.java.body.IMethodBodyBuilder;
import org.eclipse.scout.sdk.core.generator.IJavaElementGenerator;
import org.eclipse.scout.sdk.core.generator.ISourceGenerator;
import org.eclipse.scout.sdk.core.generator.member.IMemberGenerator;
import org.eclipse.scout.sdk.core.generator.methodparam.IMethodParameterGenerator;
import org.eclipse.scout.sdk.core.generator.methodparam.MethodParameterGenerator;
import org.eclipse.scout.sdk.core.generator.type.ITypeGenerator;
import org.eclipse.scout.sdk.core.generator.typeparam.ITypeParameterGenerator;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.model.api.IMethod;
import org.eclipse.scout.sdk.core.model.api.IType;
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
   * @return The return type reference of this {@link IMethodGenerator} if it can be computed without context. An empty
   *         {@link Optional} otherwise.
   */
  Optional<String> returnType();

  /**
   * Gets the return type reference of this {@link IMethodGenerator} or an empty {@link Optional} if it is a
   * constructor.
   * 
   * @param context
   *          The {@link IJavaBuilderContext} to compute the return or {@code null}.
   * @return The return type reference or an empty {@link Optional}.
   */
  Optional<String> returnType(IJavaBuilderContext context);

  /**
   * @return The function creating the return type reference or an empty {@link Optional} if this is a constructor.
   */
  Optional<JavaBuilderContextFunction<String>> returnTypeFunc();

  /**
   * Sets the return type of this {@link IMethodGenerator}.
   *
   * @param returnType
   *          The return type (e.g. {@link JavaTypes#_void}) or {@code "java.lang.String"} or {@code null} if this
   *          {@link IMethodGenerator} is a constructor.
   * @return This generator.
   * @see #withReturnTypeFrom(Class, Function)
   * @see #withReturnTypeFunc(Function)
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
   * @see #withReturnTypeFunc(Function)
   */
  <A extends IApiSpecification> TYPE withReturnTypeFrom(Class<A> apiDefinition, Function<A, String> returnTypeSupplier);

  /**
   * Sets the method return type to the result of the given returnTypeSupplier.
   * <p>
   * This method may be handy in case the return type is context dependent.
   * </p>
   * 
   * @param returnTypeSupplier
   *          A {@link Function} returning the data type reference or {@code null} if this is a constructor.
   * @return This generator.
   */
  TYPE withReturnTypeFunc(Function<IJavaBuilderContext, String> returnTypeSupplier);

  /**
   * @return A {@link Stream} with all {@link Throwable} types of the {@code throws} clause of this
   *         {@link IMethodGenerator}.
   */
  Stream<JavaBuilderContextFunction<ITypeNameSupplier>> throwablesFunc();

  /**
   * @return A {@link Stream} with all {@link Throwable} types of the {@code throws} clause of this
   *         {@link IMethodGenerator} that can be computed without context.
   * @see #throwablesFunc()
   */
  Stream<String> throwables();

  /**
   * Adds the given reference to the {@link Throwable throwables} of this {@link IMethodGenerator}. They will be added
   * to the {@code throws} declaration of the method.
   * <p>
   * If the same reference is already added or the given throwable is blank or {@code null}, this method does nothing.
   *
   * @param throwableFqn
   *          The fully qualified name of the throwable to add. This method does nothing if it is blank or {@code null}.
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
  <A extends IApiSpecification> TYPE withThrowableFrom(Class<A> apiDefinition, Function<A, ITypeNameSupplier> throwableSupplier);

  /**
   * Adds the result of the throwableSupplier to the list of throwables.
   * <p>
   * This method may be handy if the throwable is context dependent.
   * </p>
   * 
   * @param throwableSupplier
   *          A function returning the {@link ITypeNameSupplier} of the throwable. If the function is {@code null}, this
   *          method does nothing. The function must not return a {@code null} supplier and the supplier must not return
   *          {@code null} itself.
   * @return This generator.
   * @see #withThrowable(String)
   * @see #withThrowableFrom(Class, Function)
   */
  TYPE withThrowableFunc(Function<IJavaBuilderContext, ITypeNameSupplier> throwableSupplier);

  /**
   * Removes the throwable with the given name. This method is only successful if the name of the throwable to remove
   * can be computed without context.
   * 
   * @param fqn
   *          The throwable to remove.
   * @return This generator.
   */
  TYPE withoutThrowable(CharSequence fqn);

  /**
   * Removes the throwable with the given name. This method is only successful if the name of the throwable to remove
   * can be computed without context.
   * 
   * @param cns
   *          The throwable to remove.
   * @return This generator.
   */
  TYPE withoutThrowable(ITypeNameSupplier cns);

  /**
   * Removes all throwables for which the given {@link Predicate} returns {@code true}.
   *
   * @param toRemoveFilter
   *          A {@link Predicate} that decides if a throwable should be removed. May be {@code null}. In that case all
   *          throwables are removed.
   * @return This generator.
   */
  TYPE withoutThrowable(Predicate<JavaBuilderContextFunction<ITypeNameSupplier>> toRemoveFilter);

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
   * Appends an {@link Override} annotation if there is a method with the same signature in the super type hierarchy of
   * the declaring {@link ITypeGenerator}.<br>
   * For this to work this {@link IMethodGenerator} must be added to a {@link ITypeGenerator} by using
   * {@link ITypeGenerator#withMethod(IMethodGenerator, Object...)}.
   * <p>
   * If there is no declaring generator use {@link #withOverrideIfNecessary(boolean, IType)} to explicitly specify the
   * declaring type.
   * 
   * @return This generator.
   */
  TYPE withOverrideIfNecessary();

  /**
   * Does no longer append an {@link Override} annotation if necessary.
   * 
   * @return This generator.
   */
  TYPE withoutOverrideIfNecessary();

  /**
   * Appends an {@link Override} annotation if there is a method with the same signature in the super type hierarchy of
   * the given super types.
   * 
   * @param withOverrideIfNecessary
   *          If {@code true} an {@link Override} annotation is added as required based on the super hierarchies of the
   *          given super types.
   * @param declaringType
   *          An {@link IType} to use to calculate if an {@link Override} annotation is necessary. This type is
   *          considered to be the declaring type of this {@link IMethodGenerator}. Therefore only the super types of it
   *          are searched to check if an override annotation is necessary.
   * @return This generator.
   */
  TYPE withOverrideIfNecessary(boolean withOverrideIfNecessary, IType declaringType);

  /**
   * @return {@code true} if this {@link IMethodGenerator} is configured to automatically add an {@link Override}
   *         annotation if required based on super types.
   */
  boolean isWithOverrideIfNecessary();

  /**
   * Returns a unique identifier for this {@link IMethod}. The identifier looks like
   * 'methodName(dataTypeParam1,dataTypeParam2)'. Only the type erasure is used (no type arguments).
   *
   * @param context
   *          The context {@link IJavaEnvironment} for which the identifier should be computed. This is required because
   *          method parameter data types may be API dependent (see
   *          {@link MethodParameterGenerator#withDataTypeFrom(Class, Function)}).
   * @return The created identifier using the type erasure only.
   * @see #identifier(IJavaBuilderContext, boolean)
   * @see JavaTypes#createMethodIdentifier(CharSequence, java.util.Collection)
   * @see IMethod#identifier()
   */
  String identifier(IJavaBuilderContext context);

  /**
   * Returns the unique identifier for this {@link IMethodGenerator}. The identifier looks like
   * 'methodName(dataTypeParam1,dataTypeParam2)'.
   *
   * @param context
   *          The context {@link IJavaEnvironment} for which the identifier should be computed. This is required because
   *          method parameter data types may be API dependent (see
   *          {@link MethodParameterGenerator#withDataTypeFrom(Class, Function)}).
   * @param includeTypeArguments
   *          If {@code true} the type arguments of all parameter types are included. If {@code false} only the type
   *          erasure is used.
   * @return The created identifier
   * @see #identifier(IJavaBuilderContext)
   * @see JavaTypes#createMethodIdentifier(CharSequence, java.util.Collection)
   * @see IMethod#identifier(boolean)
   */
  String identifier(IJavaBuilderContext context, boolean includeTypeArguments);

  /**
   * @return {@code true} if this {@link IMethodGenerator} is a constructor (has no {@link #returnTypeFunc()}).
   */
  boolean isConstructor();

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
