/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.builder.java;

import java.util.Optional;
import java.util.function.Function;

import org.eclipse.scout.sdk.core.util.Ensure;

/**
 * Represents a {@link Function} that returns an {@link IJavaBuilderContext} dependent value
 * 
 * @param <R>
 *          The type of the return value
 */
public class JavaBuilderContextFunction<R> implements Function<IJavaBuilderContext, R> {

  private final boolean m_requireContext;
  private final Function<IJavaBuilderContext, R> m_function;

  protected JavaBuilderContextFunction(R constValue) {
    this(c -> constValue, false);
  }

  protected JavaBuilderContextFunction(Function<IJavaBuilderContext, R> func) {
    this(func, true);
  }

  protected JavaBuilderContextFunction(Function<IJavaBuilderContext, R> func, boolean requireContext) {
    m_function = Ensure.notNull(func);
    m_requireContext = requireContext;
  }

  /**
   * Creates a {@link JavaBuilderContextFunction} for the given constant value. The returned function does not require a
   * context to be executed (see {@link #isContextRequired()}).
   * 
   * @param constValue
   *          The value or {@code null}.
   * @return A {@link JavaBuilderContextFunction} holding the value given or {@code null} if the value is {@code null}.
   */
  public static <R> JavaBuilderContextFunction<R> orNull(R constValue) {
    if (constValue == null) {
      return null;
    }
    return create(constValue);
  }

  /**
   * Creates a {@link JavaBuilderContextFunction} for the given function.
   * 
   * @param function
   *          The {@link Function} or {@code null}.
   * @return A {@link JavaBuilderContextFunction} holding the function given or {@code null} if the function is
   *         {@code null}.
   */
  public static <R> JavaBuilderContextFunction<R> orNull(Function<IJavaBuilderContext, R> function) {
    if (function == null) {
      return null;
    }
    return create(function);
  }

  /**
   * Creates a {@link JavaBuilderContextFunction} for the given constant value. The returned function does not require a
   * context to be executed (see {@link #isContextRequired()}).
   *
   * @param constValue
   *          The value. Must not be {@code null}.
   * @return A {@link JavaBuilderContextFunction} holding the value given.
   */
  public static <R> JavaBuilderContextFunction<R> create(R constValue) {
    return new JavaBuilderContextFunction<>(Ensure.notNull(constValue));
  }

  /**
   * Creates a {@link JavaBuilderContextFunction} for the given function.
   * 
   * @param function
   *          The {@link Function}. Must not be {@code null}.
   * @return A {@link JavaBuilderContextFunction} holding the function given.
   */
  public static <R> JavaBuilderContextFunction<R> create(Function<IJavaBuilderContext, R> function) {
    if (function instanceof JavaBuilderContextFunction<?>) {
      return (JavaBuilderContextFunction<R>) function;
    }
    return new JavaBuilderContextFunction<>(function);
  }

  @Override
  public R apply(IJavaBuilderContext context) {
    return m_function.apply(context);
  }

  /**
   * Tries to apply this function without context. This might succeed in case {@link #isContextRequired()} returns
   * false. In that case the resulting {@link Optional} holds the return value of this function. Otherwise the
   * {@link Optional} is empty.
   * 
   * @return An {@link Optional} holding the value of this function if it can be executed without a
   *         {@link IJavaBuilderContext}.
   */
  public Optional<R> apply() {
    if (isContextRequired()) {
      return Optional.empty();
    }
    return Optional.ofNullable(apply(null));
  }

  /**
   * @return {@code true} if an {@link IJavaBuilderContext} is required to execute this function (context dependent
   *         return value). {@code false} if the value is constant.
   */
  public boolean isContextRequired() {
    return m_requireContext;
  }

  /**
   * @return The nested {@link Function}. Never returns {@code null}.
   */
  public Function<IJavaBuilderContext, R> contextFunction() {
    return m_function;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    var that = (JavaBuilderContextFunction<?>) o;
    return m_function.equals(that.m_function);
  }

  @Override
  public int hashCode() {
    return m_function.hashCode();
  }
}
