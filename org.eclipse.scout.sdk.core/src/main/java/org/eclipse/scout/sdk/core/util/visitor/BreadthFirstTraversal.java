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

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.util.Ensure;

/**
 * <h3>{@link BreadthFirstTraversal}</h3>
 * <p>
 * Level-Order traversal implementation.
 *
 * @since 8.0.0
 */
public class BreadthFirstTraversal<T> implements ITreeTraversal<T> {

  private final IBreadthFirstVisitor<T> m_visitor;
  private final Function<T, Stream<? extends T>> m_childrenSupplier;

  protected BreadthFirstTraversal(IBreadthFirstVisitor<T> visitor, Function<T, Stream<? extends T>> childrenSupplier) {
    m_visitor = visitor;
    m_childrenSupplier = childrenSupplier;
  }

  @Override
  public TreeVisitResult traverse(T root) {
    Ensure.notNull(root);
    Deque<P_BreadthFirstNode<T>> dek = new ArrayDeque<>();
    enqueue(dek, root, 0, 0);

    while (!dek.isEmpty()) {
      var node = dek.poll();
      var nextAction = m_visitor.visit(node.m_element, node.m_level, node.m_index);
      if (nextAction == TreeVisitResult.TERMINATE) {
        return TreeVisitResult.TERMINATE;
      }
      if (nextAction == TreeVisitResult.SKIP_SIBLINGS) {
        // remove all heads with same level until first next level
        removeQueuedSiblings(dek);
      }

      if (nextAction != TreeVisitResult.SKIP_SUBTREE) {
        // nextAction can only be 'continue' here
        enqueueChildren(dek, node);
      }
    }
    return TreeVisitResult.CONTINUE;
  }

  protected void enqueueChildren(Deque<P_BreadthFirstNode<T>> dek, P_BreadthFirstNode<T> node) {
    var children = m_childrenSupplier.apply(node.m_element);
    if (children == null) {
      return;
    }

    var index = new AtomicInteger();
    children.forEach(child -> enqueueChild(child, dek, node, index.getAndIncrement()));
  }

  protected void enqueueChild(T child, Deque<P_BreadthFirstNode<T>> dek, P_BreadthFirstNode<T> node, int index) {
    if (child == null) {
      return;
    }
    enqueue(dek, child, node.m_level + 1, index);
  }

  protected void enqueue(Deque<P_BreadthFirstNode<T>> dek, T element, int level, int index) {
    var e = new P_BreadthFirstNode<>(element, level, index);
    dek.addLast(e);
  }

  protected void removeQueuedSiblings(Iterable<P_BreadthFirstNode<T>> dek) {
    var iterator = dek.iterator();
    while (iterator.hasNext()) {
      var siblingCandidate = iterator.next();
      if (siblingCandidate.m_index > 0) {
        iterator.remove();
      }
      else {
        return;
      }
    }
  }

  protected static class P_BreadthFirstNode<T> {
    private final int m_level;
    private final T m_element;
    private final int m_index;

    protected P_BreadthFirstNode(T element, int level, int index) {
      m_element = element;
      m_level = level;
      m_index = index;
    }
  }
}
