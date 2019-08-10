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
package org.eclipse.scout.sdk.core.generator.method;

import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.builder.java.body.IMethodBodyBuilder;
import org.eclipse.scout.sdk.core.generator.IJavaElementGenerator;
import org.eclipse.scout.sdk.core.generator.ISourceGenerator;
import org.eclipse.scout.sdk.core.generator.member.IMemberGenerator;
import org.eclipse.scout.sdk.core.generator.methodparam.IMethodParameterGenerator;
import org.eclipse.scout.sdk.core.generator.typeparam.ITypeParameterGenerator;
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
   * @return The created identifier
   * @see JavaTypes#createMethodIdentifier(CharSequence, java.util.Collection)
   * @see IMethod#identifier()
   */
  String identifier();

  /**
   * Returns the unique identifier for this {@link IMethodGenerator}. The identifier looks like
   * 'methodName(dataTypeParam1,dataTypeParam2)'.
   *
   * @param useErasureOnly
   *          If {@code true} only the type erasure is used for all method parameter types.
   * @return The created identifier
   * @see JavaTypes#createMethodIdentifier(CharSequence, java.util.Collection)
   * @see IMethod#identifier(boolean)
   */
  String identifier(boolean useErasureOnly);

  /**
   * Gets the return type of this {@link IMethodGenerator}.
   * <p>
   * The resulting {@link Optional} is empty if this is a constructor. The {@link Optional} may contain
   * {@link JavaTypes#_void}.
   *
   * @return An {@link Optional} describing the return type of this {@link IMethodGenerator}.
   */
  Optional<String> returnType();

  /**
   * Sets the return type of this {@link IMethodGenerator}.
   *
   * @param returnType
   *          The return type (e.g. {@link JavaTypes#_void}) or {@code null} if this {@link IMethodGenerator} is a
   *          constructor.
   * @return This generator.
   */
  TYPE withReturnType(String returnType);

  /**
   * @return A {@link Stream} with all {@link Exception} types of the {@code throws} clause of this
   *         {@link IMethodGenerator}.
   */
  Stream<String> exceptions();

  /**
   * Adds the given reference to the {@link Exception}s of this {@link IMethodGenerator}. They will be printed in the
   * {@code throws} declaration of the method.
   * <p>
   * If the same reference is already added, this method does nothing.
   *
   * @param exceptionReference
   *          Must not be blank (see {@link Strings#isBlank(CharSequence)}).
   * @return This generator.
   */
  TYPE withException(String exceptionReference);

  /**
   * Removes the specified reference from the exception list of this {@link IMethodGenerator}.
   *
   * @param exceptionReference
   *          The reference to remove.
   * @return This generator.
   */
  TYPE withoutException(String exceptionReference);

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
}
