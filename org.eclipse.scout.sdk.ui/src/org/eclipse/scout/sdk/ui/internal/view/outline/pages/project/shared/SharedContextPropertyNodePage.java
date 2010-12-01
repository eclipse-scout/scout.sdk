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

import java.util.ArrayList;

import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jface.action.Action;
import org.eclipse.scout.sdk.NamingUtility;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.action.delete.MemberListDeleteAction;
import org.eclipse.scout.sdk.ui.action.rename.PropertyBeansRenameAction;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;
import org.eclipse.scout.sdk.workspace.member.IPropertyBean;

public class SharedContextPropertyNodePage extends AbstractPage {
  private final IPropertyBean m_serverDesc;
  private final IPropertyBean m_clientDesc;

  public SharedContextPropertyNodePage(IPage parent, IPropertyBean clientDesc, IPropertyBean serverDesc) {
    setParent(parent);
    m_clientDesc = clientDesc;
    m_serverDesc = serverDesc;
    IPropertyBean desc = getServerDesc();
    if (desc == null) {
      desc = getClientDesc();
    }
    setName(NamingUtility.toVariableName(desc.getBeanName()) + " (" + Signature.getSignatureSimpleName(desc.getBeanSignature()) + ")");
    setImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Variable));
  }

  @Override
  public String getPageId() {
    return IScoutPageConstants.SHARED_CONTEXT_PROPERTY_NODE_PAGE;
  }

  @Override
  public Action createRenameAction() {
    ArrayList<IPropertyBean> descs = new ArrayList<IPropertyBean>();
    if (getClientDesc() != null) {
      descs.add(getClientDesc());
    }
    if (getServerDesc() != null) {
      descs.add(getServerDesc());
    }
    return new PropertyBeansRenameAction(getOutlineView().getSite().getShell(), "Rename...", descs.toArray(new IPropertyBean[descs.size()]));
  }

  @Override
  public Action createDeleteAction() {
    MemberListDeleteAction action = new MemberListDeleteAction("Delete " + getName(), ScoutSdkUi.getShell());
    if (getServerDesc() != null) {
      for (IMember m : getServerDesc().getAllMembers()) {
        action.addMemberToDelete(m);
      }
    }
    if (getClientDesc() != null) {
      for (IMember m : getClientDesc().getAllMembers()) {
        action.addMemberToDelete(m);
      }
    }
    action.setImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.VariableRemove));
    return action;
  }

  public IPropertyBean getServerDesc() {
    return m_serverDesc;
  }

  public IPropertyBean getClientDesc() {
    return m_clientDesc;
  }

}
