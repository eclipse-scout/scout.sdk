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

import java.util.Set;

import org.eclipse.scout.sdk.ui.action.IScoutHandler;

public interface IContextMenuContributor {

  /**
   * Gets a {@link Set} of all menu action implementations that can be available on the given object
   *
   * @param selection
   *          The object that is currently selected and for which the available menus should be returend.
   * @return The {@link Set} of supported menu classes.
   */
  Set<Class<? extends IScoutHandler>> getSupportedMenuActionsFor(Object selection);
}
