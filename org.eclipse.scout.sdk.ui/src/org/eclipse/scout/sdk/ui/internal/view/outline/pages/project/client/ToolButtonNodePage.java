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
package org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client;

import org.eclipse.jdt.core.IType;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractScoutTypePage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;
import org.eclipse.scout.sdk.workspace.IScoutBundle;

public class ToolButtonNodePage extends AbstractScoutTypePage {

  public ToolButtonNodePage(IPage parentPage, IType toolbutton) {
    super();
    setParent(parentPage);
    setType(toolbutton);
    setImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Button));
  }

  @Override
  public String getPageId() {
    return IScoutPageConstants.TOOL_BUTTON_NODE_PAGE;
  }

  @Override
  protected String getMethodNameForTranslatedText() {
    return "getConfiguredText";
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
    new ToolButtonTablePage(this, getType());
  }

  @Override
  public Action createEditAction() {
    // find out the exact menu type.

    // return new EditAction(new EntityEditOrder(new ToolEntity(getType())));
    return null;
  }

  @Override
  public Action createRenameAction() {
    // XXX
    // return new RenameAction(new MenuRenameOrder(getType()));
    return null;
  }

  @Override
  public Action createDeleteAction() {
    return null;
    // return new DeleteAction(new MenuDeleteOrder(getType()));
  }

  @Override
  public Action createMoveAction(int moveOperation) {
    // TypeMoveOrder o=new TypeMoveOrder(getType());
    // o.setMoveOperation(moveOperation);
    // o.setTypeFilter(new DescendantTypeFilter(ITool.class));
    // return new RunAction(o);
    return null;
  }

  @Override
  public void fillContextMenu(IMenuManager manager) {
    super.fillContextMenu(manager);
    // manager.add(new Separator());
    // manager.add(createMoveAction(IMoveTypes.UP));
    // manager.add(createMoveAction(IMoveTypes.DOWN));
    // manager.add(new Separator());
    // manager.add(createMoveAction(IMoveTypes.TOP));
    // manager.add(createMoveAction(IMoveTypes.BOTTOM));
  }

}
