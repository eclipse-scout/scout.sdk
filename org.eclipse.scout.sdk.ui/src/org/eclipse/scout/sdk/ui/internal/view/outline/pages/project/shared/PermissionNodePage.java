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
import org.eclipse.scout.sdk.ui.action.IScoutHandler;
import org.eclipse.scout.sdk.ui.action.ShowJavaReferencesAction;
import org.eclipse.scout.sdk.ui.action.delete.DeleteAction;
import org.eclipse.scout.sdk.ui.action.rename.TypeRenameAction;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractScoutTypePage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;
import org.eclipse.scout.sdk.util.SdkProperties;

public class PermissionNodePage extends AbstractScoutTypePage {

  public PermissionNodePage(IPage parent, IType type) {
    super(SdkProperties.SUFFIX_PERMISSION);
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

  @SuppressWarnings("unchecked")
  @Override
  public Class<? extends IScoutHandler>[] getSupportedMenuActions() {
    return new Class[]{TypeRenameAction.class, ShowJavaReferencesAction.class, DeleteAction.class};
  }

  @Override
  public void prepareMenuAction(IScoutHandler menu) {
    if (menu instanceof DeleteAction) {
      DeleteAction action = (DeleteAction) menu;
      action.addType(getType());
      action.setName(getName());
      action.setImage(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.PermissionRemove));
    }
  }
}
