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
import org.eclipse.scout.sdk.ScoutIdeProperties;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.action.delete.MemberListDeleteAction;
import org.eclipse.scout.sdk.ui.action.rename.TypeRenameAction;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractScoutTypePage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;
import org.eclipse.scout.sdk.workspace.IScoutBundle;

/**
 * <h3>OutlineNodePage</h3> ...
 */
public class OutlineNodePage extends AbstractScoutTypePage {

  public OutlineNodePage(IPage parentPage, IType outlineType) {
    setParent(parentPage);
    setType(outlineType);
    setImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Outline));
  }

  @Override
  public String getPageId() {
    return IScoutPageConstants.OUTLINE_NODE_PAGE;
  }

  @Override
  protected String getMethodNameForTranslatedText() {
    return "getConfiguredTitle";
  }

  @Override
  public boolean isFolder() {
    return false;
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
    new OutlinePageChildPageTablePage(this, getType());
  }

  @Override
  public Action createRenameAction() {
    return new TypeRenameAction(getOutlineView().getSite().getShell(), "Rename...", getType(), ScoutIdeProperties.SUFFIX_OUTLINE);
  }

  @Override
  public Action createDeleteAction() {
    MemberListDeleteAction action = new MemberListDeleteAction(Texts.get("Action_deleteTypeX", getName()), getOutlineView().getSite().getShell());
    action.setTypesToDelete(new IMember[]{getType()});
    action.setImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.OutlineRemove));
    return action;
  }

}
