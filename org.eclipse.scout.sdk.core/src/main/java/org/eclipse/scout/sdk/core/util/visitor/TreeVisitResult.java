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
