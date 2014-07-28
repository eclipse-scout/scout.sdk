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
package org.eclipse.scout.sdk.ui.view.outline;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;

/**
 * <h3>IScoutExplorerPart</h3>
 */
public interface IScoutExplorerPart extends IPageOutlineView, IDirtyManageable {
  /**
   * gets the current selection of the scout explorer tree.
   *
   * @return The structured selection of the tree.
   */
  IStructuredSelection getSelection();

  /**
   * sets the new selection of the scout explorer tree.
   *
   * @param selection
   *          the new selection containing the {@link IPage} instances that should be selected.
   */
  void setSelection(IStructuredSelection selection);

  /**
   * expands the scout explorer tree to project level and sets the selection to the first project in the tree.
   */
  void expandAndSelectProjectLevel();

  /**
   * gets the (invisible) root page. this page contains the project nodes.
   *
   * @return the invisible root page.
   */
  IPage getRootPage();
}
