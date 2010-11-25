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

import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;

public interface IScoutSdkExtension {

  /**
   * This method is called after the page has added their children, but before the page filtered and published the new
   * children.
   * If this extension wishes to add additional children to the page it can do so by calling for example new
   * MyChildPage({@link AbstractPage})
   */
  void contributePageChildren(IPage page);

  /**
   * This method is called after the page has added their menus in
   * {@link AbstractPage#fillContextMenu(org.eclipse.jface.action.IMenuManager)} If this extension wishes to add
   * additional menus to the page it can do so by calling
   * {@link IContributionManager#add(org.eclipse.jface.action.IAction)}
   */
  void contributePageMenus(IMenuManager manager, IPage page);

}
