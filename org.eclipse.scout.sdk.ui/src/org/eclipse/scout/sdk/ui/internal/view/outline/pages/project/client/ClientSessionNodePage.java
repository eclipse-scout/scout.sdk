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

import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.action.Action;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.action.delete.MemberListDeleteAction;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractScoutTypePage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;
import org.eclipse.scout.sdk.workspace.IScoutBundle;

public class ClientSessionNodePage extends AbstractScoutTypePage {

  public ClientSessionNodePage(IPage parent, IType type) {
    setParent(parent);
    setType(type);
    setName(type.getElementName());
    setImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.ClientSession));
  }

  @Override
  public String getPageId() {
    return IScoutPageConstants.CLIENT_SESSION_NODE_PAGE;
  }

  /**
   * client bundle
   */
  @Override
  public IScoutBundle getScoutResource() {
    return (IScoutBundle) super.getScoutResource();
  }

  @Override
  public boolean isInitiallyLoaded() {
    return true;
  }

  @Override
  public void loadChildrenImpl() {
  }

  @Override
  public boolean isFolder() {
    return false;
  }

  @Override
  public Action createDeleteAction() {
    MemberListDeleteAction action = new MemberListDeleteAction("Delete ClientSession", getOutlineView().getSite().getShell());
    action.setImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.ClientSessionRemove));
    action.setTypesToDelete(new IMember[]{getType()});
    return action;
  }
}
