/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.util.visitor;

import java.util.function.Function;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.util.Ensure;

/**
 * Factory to create {@link ITreeTraversal} instances.
 * <p>
 * Depending on the type of visitor a <i>Depth-First</i> (DFS) or a <i>Breadth-First</i> (BFS) strategy is returned.<br>
 * A traversal is called <i>Depth-First</i> if the search tree is deepened as much as possible on each child before
 * going to the next sibling (top-down=pre-order or bottom-up=post-order).<br>
 * A traversal is called <i>Breadth-First</i> if a level is traversed completely before deepened to the next level (also
 * known as level-order).
 *
 * @since 7.1
 * @see IDepthFirstVisitor
 * @see IBreadthFirstVisitor
 * @see ITreeTraversal
 * @see TreeVisitResult
 */
public final class TreeTraversals {

  private TreeTraversals() {
  }

  /**
   * Creates a new {@link ITreeTraversal} using a <i>Breadth-First</i> traversal strategy.
   *
   * @param visitor
   *          The {@link IBreadthFirstVisitor} to use during the traversal. Must not be {@code null}.
   * @param childrenSupplier
   *          A {@link Function} that returns the child elements of a given parent. Must not be {@code null}.<br>
   *          The result of the {@link Function} itself may be {@code null}. The element passed to the {@link Function}
   *          is never {@code null}.
   * @return A <i>Breadth-First</i> {@link ITreeTraversal} that calls the specified {@link IBreadthFirstVisitor} and
   *         uses the specified {@link Function} to calculate child elements. Never returns {@code null}.
   * @throws IllegalArgumentException
   *           if one of the arguments is {@code null}.
   * @see IBreadthFirstVisitor
   * @see ITreeTraversal
   */
  public static <T> ITreeTraversal<T> create(IBreadthFirstVisitor<T> visitor, Function<T, Stream<? extends T>> childrenSupplier) {
    return new BreadthFirstTraversal<>(Ensure.notNull(visitor), Ensure.notNull(childrenSupplier));
  }

  /**
   * Creates a new {@link ITreeTraversal} using a <i>Depth-First</i> traversal strategy.
   *
   * @param visitor
   *          The {@link IDepthFirstVisitor} to use during the traversal. Must not be {@code null}.
   * @param childrenSupplier
   *          A {@link Function} that returns the child elements of a given parent. Must not be {@code null}.<br>
   *          The result of the {@link Function} itself may be {@code null}. The element passed to the {@link Function}
   *          is never {@code null}.
   * @return A <i>Depth-First</i> {@link ITreeTraversal} that calls the specified {@link IDepthFirstVisitor} and uses
   *         the specified {@link Function} to calculate child elements. Never returns {@code null}.
   * @throws IllegalArgumentException
   *           if one of the arguments is {@code null}.
   * @see IDepthFirstVisitor
   * @see ITreeTraversal
   */
  public static <T> ITreeTraversal<T> create(IDepthFirstVisitor<T> visitor, Function<T, Stream<? extends T>> childrenSupplier) {
    return new DepthFirstTraversal<>(Ensure.notNull(visitor), Ensure.notNull(childrenSupplier));
  }
}
