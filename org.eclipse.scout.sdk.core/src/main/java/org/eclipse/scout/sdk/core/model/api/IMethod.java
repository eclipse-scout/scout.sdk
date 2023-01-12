/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.model.api;

import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.builder.java.IJavaBuilderContext;
import org.eclipse.scout.sdk.core.generator.method.IMethodGenerator;
import org.eclipse.scout.sdk.core.model.api.query.MethodParameterQuery;
import org.eclipse.scout.sdk.core.model.api.query.SuperMethodQuery;
import org.eclipse.scout.sdk.core.model.spi.MethodSpi;
import org.eclipse.scout.sdk.core.transformer.IWorkingCopyTransformer;
import org.eclipse.scout.sdk.core.util.JavaTypes;

/**
 * <h3>{@link IMethod}</h3> Represents a method declaration.
 *
 * @since 5.1.0
 */
public interface IMethod extends IMember {

  /**
   * @return Gets the return data {@link IType} of this {@link IMethod}. The result may be the void type
   *         ({@link IType#isVoid()}). Returns an empty {@link Optional} if this {@link IMethod} is a constructor.
   * @see IType#isVoid()
   * @see #isConstructor()
   */
  Optional<IType> returnType();

  /**
   * Same as {@link #returnType()} but throws an {@link IllegalArgumentException} if this method is a constructor.
   *
   * @return The return data {@link IType} of this method. Never returns {@code null}.
   * @throws IllegalArgumentException
   *           if this method is a constructor and therefore has no return type.
   * @see #returnType()
   */
  IType requireReturnType();

  /**
   * Gets all exception declarations of this {@link IMethod} in the order as they appear in the source or class file.
   *
   * @return a {@link Stream} containing all thrown {@link IType}s of this {@link IMethod}.
   */
  Stream<IType> exceptionTypes();

  /**
   * Gets if this method is a constructor.
   *
   * @return {@code true} if this method is a constructor, {@code false} otherwise.
   */
  boolean isConstructor();

  /**
   * @return the method name without any brackets.
   */
  @Override
  String elementName();

  /**
   * Gets the method body source.
   *
   * @return The source of the method body.
   */
  Optional<ISourceRange> sourceOfBody();

  /**
   * Gets the {@link ISourceRange} of the method declaration.<br>
   * This is the range from the method name to the end of the {@code throws} declaration.
   * <p>
   * Note: Preceding modifiers (e.g. like {@code public}), the return type, annotations or comments (like javadoc) are
   * not part of the source range!
   * <p>
   * <br>
   * <b>Example:</b><br>
   * For the following method:
   * 
   * <pre>
   * &#64;Override
   * public static void myMethodName(int firstArg, String secondArg) throws IOException {
   * }
   * </pre>
   * 
   * The declaration source is
   * 
   * <pre>
   * myMethodName(int firstArg, String secondArg) throws IOException
   * </pre>
   * 
   * @return An Optional holding the source range of the method declaration section.
   */
  Optional<ISourceRange> sourceOfDeclaration();

  @Override
  MethodSpi unwrap();

  /**
   * Gets a {@link SuperMethodQuery} to access methods overridden by this {@link IMethod}.<br>
   * By default the query returns all {@link IMethod}s of all super {@link IType}s including this starting
   * {@link IMethod} itself.
   *
   * @return The new {@link SuperMethodQuery} for this {@link IMethod}.
   */
  SuperMethodQuery superMethods();

  /**
   * Gets a {@link MethodParameterQuery} to access the parameters of this {@link IMethod}.<br>
   * By default the query returns all {@link IMethodParameter}s of this {@link IMethod}.
   *
   * @return The new {@link MethodParameterQuery} for this {@link IMethod}.
   */
  MethodParameterQuery parameters();

  /**
   * @return The {@link IType} this {@link IMethod} is declared in.
   */
  IType requireDeclaringType();

  /**
   * Returns the unique identifier for this {@link IMethod}. The identifier looks like
   * 'methodName(dataTypeParam1,dataTypeParam2)' optionally including the type arguments.
   *
   * @param includeTypeArguments
   *          {@code true} to include the type arguments or {@code false} to use type erasure for all method parameter
   *          types.
   * @return The created identifier
   * @see #identifier()
   * @see JavaTypes#createMethodIdentifier(CharSequence, java.util.Collection)
   * @see IMethodGenerator#identifier(IJavaBuilderContext, boolean)
   */
  String identifier(boolean includeTypeArguments);

  /**
   * Returns a unique identifier for this {@link IMethod}. The identifier looks like
   * 'methodName(dataTypeParam1,dataTypeParam2)' and only includes the type erasure (no type arguments).
   *
   * @return The created identifier using the type erasure only.
   * @see #identifier(boolean)
   * @see JavaTypes#createMethodIdentifier(CharSequence, java.util.Collection)
   * @see IMethodGenerator#identifier(IJavaBuilderContext)
   */
  String identifier();

  @Override
  IMethodGenerator<?, ?> toWorkingCopy();

  @Override
  IMethodGenerator<?, ?> toWorkingCopy(IWorkingCopyTransformer transformer);
}
