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
package org.eclipse.scout.sdk.ui.extensions;

import org.eclipse.scout.sdk.ui.action.AbstractScoutHandler;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;

public interface IContextMenuContributor {

  /**
   * Gets an array of all menu action implementations that can be available for the given page.
   * 
   * @param page
   *          The page for which the available menus should be returned.
   * @return The list of supported menu classes.
   */
  Class<? extends AbstractScoutHandler>[] getSupportedMenuActionsFor(IPage page);

  /**
   * Prepares the given context menu instance called on the given page.<br>
   * The preparation of menus is called before the <code>AbstractScoutHandler.isVisible()</code>.<br>
   * Usually this methods fills the menu handler with all arguments required.<br>
   * It is guaranteed that only menus are passed to this method that are marked as supported by the current handler.
   * 
   * @param page
   *          The page on which the menu should be shown.
   * @param menu
   *          The menu instance (unprepared) that can be filled with parameters.
   */
  void prepareMenuAction(IPage page, AbstractScoutHandler menu);
}
