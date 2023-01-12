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

/**
 * Controls how the tree visiting should continue.
 *
 * @since 7.1
 * @see IBreadthFirstVisitor
 * @see IDepthFirstVisitor
 * @see ITreeTraversal
 * @see TreeTraversals
 * @see TreeVisitResult#CONTINUE
 * @see TreeVisitResult#TERMINATE
 * @see TreeVisitResult#SKIP_SUBTREE
 * @see TreeVisitResult#SKIP_SIBLINGS
 */
public enum TreeVisitResult {
  /**
   * Normally continue visiting with the next element. Nothing will be skipped.
   */
  CONTINUE,

  /**
   * Abort the whole visiting. May be used if the visitor finishes the operation before all elements have been visited.
   */
  TERMINATE,

  /**
   * Continue without visiting the child elements of the current element.
   */
  SKIP_SUBTREE,

  /**
   * All siblings of the current element are skipped. The current element is visited completely (including children).
   */
  SKIP_SIBLINGS
}
