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
package org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.table;

import org.eclipse.jface.action.Action;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.MenuTablePage;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractScoutTypePage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;
import org.eclipse.scout.sdk.workspace.IScoutBundle;

public class TableNodePage extends AbstractScoutTypePage {

  public TableNodePage() {
    super();
    setImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Table));
  }

  @Override
  public String getPageId() {
    return IScoutPageConstants.TABLE_NODE_PAGE;
  }

  @Override
  protected String getMethodNameForTranslatedText() {
    return "getConfiguredTitle";
  }

  /**
   * client bundle
   */
  @Override
  public IScoutBundle getScoutResource() {
    return (IScoutBundle) super.getScoutResource();
  }

  @Override
  public void loadChildrenImpl() {
    new MenuTablePage(this, getType());
    new ColumnTablePage(this, getType());
  }

  @Override
  public Action createEditAction() {
    // XXX
    return null;
    // return new EditAction(new EntityEditOrder(new TableEntity(getType())));
  }

  @Override
  public Action createRenameAction() {
    // XXX
    return null;
    // return new RenameAction(new TableRenameOrder(getType()));
  }

  // XXX add new/delete orders

}
