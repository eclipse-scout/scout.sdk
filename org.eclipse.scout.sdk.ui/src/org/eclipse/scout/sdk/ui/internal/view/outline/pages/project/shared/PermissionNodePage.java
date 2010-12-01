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
package org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.shared;

import org.eclipse.jdt.core.IType;
import org.eclipse.jface.action.Action;
import org.eclipse.scout.sdk.ScoutIdeProperties;
import org.eclipse.scout.sdk.operation.util.TypeDeleteOperation;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.action.delete.DeleteAction;
import org.eclipse.scout.sdk.ui.action.rename.TypeRenameAction;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractScoutTypePage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;

public class PermissionNodePage extends AbstractScoutTypePage {

  public PermissionNodePage(IPage parent, IType type) {
    setParent(parent);
    setType(type);
    // String name=type.getSimpleName();
    // setName(name.substring(0, name.lastIndexOf("Permission")));

    setImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Permission));
  }

  @Override
  public String getPageId() {
    return IScoutPageConstants.PERMISSION_NODE_PAGE;
  }

  @Override
  public void loadChildrenImpl() {
    // XXX addChild(new PermissionLevelTablePage()); //better solved in attributes view
  }

  @Override
  public boolean isFolder() {
    return false;
  }

  @Override
  public Action createDeleteAction() {
    DeleteAction action = new DeleteAction("Delete...", ScoutSdkUi.getShell(), new TypeDeleteOperation(getType()));
    action.setImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.PermissionRemove));
    return action;
  }

  @Override
  public Action createRenameAction() {
    return new TypeRenameAction(getOutlineView().getSite().getShell(), "Rename...", getType(), ScoutIdeProperties.SUFFIX_PERMISSION);
  }

}
