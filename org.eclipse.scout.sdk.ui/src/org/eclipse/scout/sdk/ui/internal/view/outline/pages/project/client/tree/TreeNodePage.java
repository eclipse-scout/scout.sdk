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
package org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.tree;

import org.eclipse.jdt.core.IType;
import org.eclipse.jface.action.Action;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.action.delete.MemberListDeleteAction;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractScoutTypePage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;
import org.eclipse.scout.sdk.workspace.IScoutBundle;

public class TreeNodePage extends AbstractScoutTypePage {

  private final boolean m_hasDeleteAction;

  public TreeNodePage(AbstractPage parent, IType type) {
    this(parent, type, false);
  }

  public TreeNodePage(AbstractPage parent, IType type, boolean hasDeleteAction) {
    super();
    m_hasDeleteAction = hasDeleteAction;
    setParent(parent);
    setType(type);
    setImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Tree));

  }

  @Override
  public String getPageId() {
    return IScoutPageConstants.TREE_NODE_PAGE;
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
  public Action createDeleteAction() {
    MemberListDeleteAction action = null;
    if (m_hasDeleteAction) {
      action = new MemberListDeleteAction(Texts.get("Action_deleteTypeX", getType().getElementName()), getOutlineView().getSite().getShell());
      action.addMemberToDelete(getType());
      action.setImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.TreeRemove));
    }
    return action;
  }
}
