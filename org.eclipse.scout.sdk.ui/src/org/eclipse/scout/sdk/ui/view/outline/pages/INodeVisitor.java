/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.ui.view.outline.pages;

/**
 * <h3>IPageVisitor</h3> The page visitor is used to visit all nodes of the Outline Tree.
 */
public interface INodeVisitor {
  /**
   * continues visiting the tree (load children if not loaded)
   */
  public static final int CONTINUE = 1;
  /**
   * continues visiting the tree only on this branch (load children if not loaded)
   */
  public static final int CONTINUE_BRANCH = 2;
  /**
   * stop visiting the tree; used when the mission is completed.
   */
  public static final int CANCEL = 0;
  /**
   * continues visiting with siblings of the parent node, aboard subtree.
   */
  public static final int CANCEL_SUBTREE = 3;

  /**
   * @param page
   * @return one of {@link INodeVisitor#CONTINUE}; {@link INodeVisitor#CANCEL}; {@link INodeVisitor#CANCEL_SUBTREE}
   */
  int visit(IPage page);
}
