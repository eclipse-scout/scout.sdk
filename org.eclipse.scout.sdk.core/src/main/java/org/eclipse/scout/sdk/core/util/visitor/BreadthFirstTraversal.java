/*******************************************************************************
 * Copyright (c) 2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.core.util.visitor;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
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
      P_BreadthFirstNode<T> node = dek.poll();
      TreeVisitResult nextAction = m_visitor.visit(node.m_element, node.m_level, node.m_index);
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
    Stream<? extends T> children = m_childrenSupplier.apply(node.m_element);
    if (children == null) {
      return;
    }

    AtomicInteger index = new AtomicInteger();
    children.forEach(child -> enqueueChild(child, dek, node, index.getAndIncrement()));
  }

  protected void enqueueChild(T child, Deque<P_BreadthFirstNode<T>> dek, P_BreadthFirstNode<T> node, int index) {
    if (child == null) {
      return;
    }
    enqueue(dek, child, node.m_level + 1, index);
  }

  protected void enqueue(Deque<P_BreadthFirstNode<T>> dek, T element, int level, int index) {
    P_BreadthFirstNode<T> e = new P_BreadthFirstNode<>(element, level, index);
    dek.addLast(e);
  }

  protected void removeQueuedSiblings(Iterable<P_BreadthFirstNode<T>> dek) {
    Iterator<P_BreadthFirstNode<T>> iterator = dek.iterator();
    while (iterator.hasNext()) {
      P_BreadthFirstNode<T> siblingCandidate = iterator.next();
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
