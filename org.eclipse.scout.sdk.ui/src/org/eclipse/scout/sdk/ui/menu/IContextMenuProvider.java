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
package org.eclipse.scout.sdk.ui.menu;

import org.eclipse.scout.sdk.ui.action.AbstractScoutHandler;

public interface IContextMenuProvider {

  /**
   * defines which context menu handlers can be available on the current provider
   * 
   * @return
   */
  Class<? extends AbstractScoutHandler>[] getSupportedMenuActions();

  /**
   * is called for each menu that is supported by this provider and supports the current selection count.
   * 
   * @param menu
   */
  void prepareMenuAction(AbstractScoutHandler menu);
}
