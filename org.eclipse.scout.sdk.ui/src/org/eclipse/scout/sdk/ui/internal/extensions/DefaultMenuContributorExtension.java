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
package org.eclipse.scout.sdk.ui.internal.extensions;

import java.util.Set;

import org.eclipse.scout.sdk.ui.action.IScoutHandler;
import org.eclipse.scout.sdk.ui.extensions.IContextMenuContributor;
import org.eclipse.scout.sdk.ui.menu.IContextMenuHolder;

public class DefaultMenuContributorExtension implements IContextMenuContributor {

  @Override
  public Set<Class<? extends IScoutHandler>> getSupportedMenuActionsFor(Object o) {
    if (o instanceof IContextMenuHolder) {
      return ((IContextMenuHolder) o).getSupportedMenuActions();
    }
    return null;
  }
}
