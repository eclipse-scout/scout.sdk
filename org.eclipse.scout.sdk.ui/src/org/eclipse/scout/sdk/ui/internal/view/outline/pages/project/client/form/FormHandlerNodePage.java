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
package org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.form;

import org.eclipse.jdt.core.IType;
import org.eclipse.jface.action.Action;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractScoutTypePage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;

public class FormHandlerNodePage extends AbstractScoutTypePage {

  public FormHandlerNodePage(AbstractPage parent, IType type) {
    super();
    setParent(parent);
    setType(type);
    setImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.FormHandler));
  }

  @Override
  public String getPageId() {
    return IScoutPageConstants.FORM_HANDLER_NODE_PAGE;
  }

  @Override
  public Action createRenameAction() {
    // XXX
    return null;
    // return new RenameAction(new FormHandlerRenameOrder(getType()));
  }

  @Override
  public Action createDeleteAction() {
    // XXX
    return null;
    // return new DeleteAction(new FormHandlerDeleteOrder(getType()));
  }

}
