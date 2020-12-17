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
package org.eclipse.scout.sdk.core.model.api;

import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.builder.java.body.IMethodBodyBuilder;
import org.eclipse.scout.sdk.core.generator.method.IMethodGenerator;
import org.eclipse.scout.sdk.core.transformer.IWorkingCopyTransformer;
import org.eclipse.scout.sdk.core.model.api.query.MethodParameterQuery;
import org.eclipse.scout.sdk.core.model.api.query.SuperMethodQuery;
import org.eclipse.scout.sdk.core.model.spi.MethodSpi;
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
   * 'methodName(dataTypeParam1,dataTypeParam2)'.
   *
   * @param useErasureOnly
   *          If {@code true} only the type erasure is used for all method parameter types.
   * @return The created identifier
   * @see JavaTypes#createMethodIdentifier(CharSequence, java.util.Collection)
   * @see IMethodGenerator#identifier(IJavaEnvironment, boolean)
   */
  String identifier(boolean useErasureOnly);

  /**
   * Returns a unique identifier for this {@link IMethod}. The identifier looks like
   * 'methodName(dataTypeParam1,dataTypeParam2)'.
   *
   * @return The created identifier
   * @see JavaTypes#createMethodIdentifier(CharSequence, java.util.Collection)
   * @see IMethodGenerator#identifier(IJavaEnvironment)
   */
  String identifier();

  @Override
  IMethodGenerator<?, ? extends IMethodBodyBuilder<?>> toWorkingCopy();

  @Override
  IMethodGenerator<?, ? extends IMethodBodyBuilder<?>> toWorkingCopy(IWorkingCopyTransformer transformer);
}
