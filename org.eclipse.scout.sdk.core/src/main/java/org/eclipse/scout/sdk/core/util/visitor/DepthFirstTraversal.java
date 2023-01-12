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

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.util.Ensure;

/**
 * <h3>{@link DepthFirstTraversal}</h3>
 * <p>
 * Pre-Order/Post-Order traversal implementation.
 *
 * @since 8.0.0
 */
public class DepthFirstTraversal<T> implements ITreeTraversal<T> {

  private final IDepthFirstVisitor<T> m_visitor;
  private final Function<T, Stream<? extends T>> m_childrenSupplier;

  protected DepthFirstTraversal(IDepthFirstVisitor<T> visitor, Function<T, Stream<? extends T>> childrenSupplier) {
    m_visitor = visitor;
    m_childrenSupplier = childrenSupplier;
  }

  @Override
  public TreeVisitResult traverse(T root) {
    return doVisitInternal(Ensure.notNull(root), 0, 0);
  }

  protected TreeVisitResult doVisitInternal(T toVisit, int level, int index) {
    var nextAction = m_visitor.preVisit(toVisit, level, index);
    if (nextAction == TreeVisitResult.TERMINATE) {
      return TreeVisitResult.TERMINATE;
    }

    if (nextAction != TreeVisitResult.SKIP_SUBTREE) {
      var childResult = visitChildren(toVisit, level + 1);
      if (childResult == TreeVisitResult.TERMINATE) {
        return TreeVisitResult.TERMINATE;
      }
    }

    var continueVisit = m_visitor.postVisit(toVisit, level, index);
    if (!continueVisit) {
      return TreeVisitResult.TERMINATE;
    }

    return nextAction;
  }

  protected TreeVisitResult visitChildren(T parent, int level) {
    var children = m_childrenSupplier.apply(parent);
    if (children == null) {
      return TreeVisitResult.CONTINUE;
    }

    var index = new AtomicInteger();
    return children
        .map(child -> visitChild(child, level, index.getAndIncrement()))
        .filter(DepthFirstTraversal::isAbortResult)
        .map(DepthFirstTraversal::mapNextActionForParent)
        .findAny()
        .orElse(TreeVisitResult.CONTINUE);
  }

  protected static TreeVisitResult mapNextActionForParent(TreeVisitResult nextAction) {
    if (nextAction == TreeVisitResult.SKIP_SIBLINGS) {
      return TreeVisitResult.CONTINUE; // skip this loop only
    }
    return nextAction;
  }

  protected static boolean isAbortResult(TreeVisitResult nextAction) {
    return nextAction == TreeVisitResult.TERMINATE
        || nextAction == TreeVisitResult.SKIP_SIBLINGS;
  }

  protected TreeVisitResult visitChild(T child, int level, int index) {
    if (child == null) {
      return TreeVisitResult.CONTINUE;
    }

    return doVisitInternal(child, level, index);
  }
}
