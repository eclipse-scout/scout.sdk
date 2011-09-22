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

import org.eclipse.scout.sdk.ui.action.AbstractScoutHandler;
import org.eclipse.scout.sdk.ui.extensions.IContextMenuContributor;
import org.eclipse.scout.sdk.ui.menu.IContextMenuProvider;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;

public class DefaultMenuContributorExtension implements IContextMenuContributor {

  @Override
  public Class<? extends AbstractScoutHandler>[] getSupportedMenuActionsFor(IPage p) {
    if (p instanceof IContextMenuProvider) {
      return ((IContextMenuProvider) p).getSupportedMenuActions();
    }
    return null;
  }

  @Override
  public void prepareMenuAction(IPage p, AbstractScoutHandler menu) {
    if (p instanceof IContextMenuProvider) {
      ((IContextMenuProvider) p).prepareMenuAction(menu);
    }
  }
}
