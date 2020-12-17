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
package org.eclipse.scout.sdk.core.util.visitor;

import java.util.function.Function;

import org.eclipse.scout.sdk.core.util.Ensure;

/**
 * <h3>{@link DepthFirstVisitorTypeAdapter}</h3>
 * <p>
 * IDepthFirstVisitor wrapper that narrows the elements to be visited to a specific sub-type. All non-matching elements
 * are normally visited ({@link TreeVisitResult#CONTINUE}) but not delegated to the wrapped, more-specific visitor.
 *
 * @since 8.0.0
 */
public class DepthFirstVisitorTypeAdapter<T, U extends T> implements IDepthFirstVisitor<T> {

  private final Class<U> m_type;
  private final IDepthFirstVisitor<U> m_visitor;

  /**
   * Converts the specified {@link Function} to a pre-order {@link IDepthFirstVisitor}.
   *
   * @param visitor
   *          The {@link Function} to convert. Must not be {@code null}.
   * @return A pre-order (top-down) visitor that delegates to the specified {@link Function}.
   * @throws IllegalArgumentException
   *           if the specified function is {@code null}.
   */
  public static <S> IDepthFirstVisitor<S> functionToPreOrderVisitor(Function<S, TreeVisitResult> visitor) {
    Ensure.notNull(visitor);
    return new DefaultDepthFirstVisitor<>() {
      @Override
      public TreeVisitResult preVisit(S element, int level, int index) {
        return visitor.apply(element);
      }
    };
  }

  /**
   * Wraps the specified {@link Function} to a pre-order {@link IDepthFirstVisitor}.
   *
   * @param visitor
   *          The {@link Function} to convert. Must not be {@code null}.
   * @param type
   *          The type for which the specified {@link Function} should be called. All other types are skipped. Must not
   *          be {@code null}.
   */
  public DepthFirstVisitorTypeAdapter(Function<U, TreeVisitResult> visitor, Class<U> type) {
    this(functionToPreOrderVisitor(visitor), type);
  }

  /**
   * Wraps the specified sub-type-visitor. Only elements of that sub-type are passed to the wrapped visitor. All other
   * elements are skipped.
   *
   * @param visitor
   *          The {@link Function} to convert. Must not be {@code null}.
   * @param type
   *          The type for which the specified {@link Function} should be called. All other types are skipped. Must not
   *          be {@code null}.
   */
  public DepthFirstVisitorTypeAdapter(IDepthFirstVisitor<U> visitor, Class<U> type) {
    m_type = Ensure.notNull(type);
    m_visitor = Ensure.notNull(visitor);
  }

  @Override
  public TreeVisitResult preVisit(T element, int level, int index) {
    return delegateToFunctionIfTypeMatches(element, e -> innerVisitor().preVisit(e, level, index), TreeVisitResult.CONTINUE);
  }

  @Override
  public boolean postVisit(T element, int level, int index) {
    return delegateToFunctionIfTypeMatches(element, e -> innerVisitor().postVisit(e, level, index), true);
  }

  @SuppressWarnings("unchecked")
  protected <S> S delegateToFunctionIfTypeMatches(T element, Function<U, S> function, S resultOnTypeMismatch) {
    if (type().isInstance(element)) {
      return function.apply((U) element);
    }
    return resultOnTypeMismatch;
  }

  public Class<U> type() {
    return m_type;
  }

  public IDepthFirstVisitor<U> innerVisitor() {
    return m_visitor;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + " for type '" + type().getName() + "'.";
  }
}
