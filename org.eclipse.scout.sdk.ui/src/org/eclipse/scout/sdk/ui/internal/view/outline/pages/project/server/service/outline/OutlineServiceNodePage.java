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
package org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.server.service.outline;

import org.eclipse.jdt.core.IType;
import org.eclipse.jface.action.Action;
import org.eclipse.scout.sdk.ScoutIdeProperties;
import org.eclipse.scout.sdk.ui.action.delete.ServiceDeleteAction;
import org.eclipse.scout.sdk.ui.action.rename.ServiceRenameAction;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;
import org.eclipse.scout.sdk.ui.view.outline.pages.project.server.service.AbstractServiceNodePage;

/**
 * Page representing a service implementation
 */
public class OutlineServiceNodePage extends AbstractServiceNodePage {

  public OutlineServiceNodePage(AbstractPage parent, IType type, IType interfaceType) {
    super(parent, type, interfaceType);
  }

  @Override
  public String getPageId() {
    return IScoutPageConstants.OUTLINE_SERVICE_NODE_PAGE;
  }

  @Override
  public Action createRenameAction() {
    return new ServiceRenameAction(getOutlineView().getSite().getShell(), "Rename...", getType(), getInterfaceType(), ScoutIdeProperties.SUFFIX_OUTLINE_SERVICE);
  }

  @Override
  public Action createDeleteAction() {
    return new ServiceDeleteAction(getOutlineView().getSite().getShell(), getInterfaceType(), getType());
  }
  /*
   * @Override
   * public Action createDeleteAction(){
   * return new ProcessAction(Texts.get("Action_deleteTypeX", getName()), Icons.getDescriptor(Icons.IMG_TOOL_DELETE),
   * new ServiceDeleteProcess(getType()));
   * }
   * @Override
   * public Action createNewAction() {
   * return new ProcessAction(Texts.get("Action_newTypeX", "Service Operation"), Icons.getDescriptor(Icons.IMG_TOOL_ADD),
   * new ServiceOperationNewProcess(getType(), m_interfaceType, getBsiCaseProjectGroup()));
   * }
   */
}
