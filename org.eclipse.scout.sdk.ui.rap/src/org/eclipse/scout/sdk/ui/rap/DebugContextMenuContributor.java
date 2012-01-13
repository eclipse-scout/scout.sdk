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
package org.eclipse.scout.sdk.ui.rap;

import org.eclipse.scout.sdk.ui.action.AbstractScoutHandler;
import org.eclipse.scout.sdk.ui.extensions.IContextMenuContributor;
import org.eclipse.scout.sdk.ui.rap.internal.ModifyXmlAction;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.workspace.IScoutBundle;

public class DebugContextMenuContributor implements IContextMenuContributor {

  public DebugContextMenuContributor() {
  }

  @SuppressWarnings("unchecked")
  @Override
  public Class<? extends AbstractScoutHandler>[] getSupportedMenuActionsFor(IPage page) {
    return new Class[]{ModifyXmlAction.class};
  }

  @Override
  public void prepareMenuAction(IPage page, AbstractScoutHandler menu) {
    if (menu instanceof ModifyXmlAction) {
      ((ModifyXmlAction) menu).setServerProject(((IScoutBundle) ((AbstractPage) page).getScoutResource()).getJavaProject());
    }
  }

}
