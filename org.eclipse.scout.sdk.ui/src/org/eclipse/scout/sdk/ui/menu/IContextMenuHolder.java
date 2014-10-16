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

import java.util.Set;

import org.eclipse.scout.sdk.ui.action.IScoutHandler;

public interface IContextMenuHolder {

  /**
   * defines which context menus can be available on the current holder
   *
   * @return
   */
  Set<Class<? extends IScoutHandler>> getSupportedMenuActions();
}
