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
import org.eclipse.scout.sdk.ui.action.delete.LookupCallDeleteAction;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractScoutTypePage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;
import org.eclipse.scout.sdk.ui.view.outline.pages.basic.beanproperty.BeanPropertyTablePage;
import org.eclipse.scout.sdk.workspace.IScoutBundle;

public class LookupCallNodePage extends AbstractScoutTypePage {

  public LookupCallNodePage(AbstractPage parent, IType type) {
    setParent(parent);
    setType(type);
    
  }

  @Override
  public String getPageId() {
    return IScoutPageConstants.LOOKUP_CALL_NODE_PAGE;
  }

  @Override
  public void loadChildrenImpl() {
    new BeanPropertyTablePage(this, getType());
  }

  /**
   * shared bundle
   */
  @Override
  public IScoutBundle getScoutResource() {
    return (IScoutBundle) super.getScoutResource();
  }

  @Override
  public Action createDeleteAction() {
    return new LookupCallDeleteAction(getType(), getOutlineView().getSite().getShell());
  }

  /*
   * XXX
   * @Override
   * public Action createRenameAction(){
   * return new ProcessAction(Texts.get("Action_renameX", getType().getSimpleName()), Icons.getDescriptor(Icons.IMG_TOOL_RENAME),new FormRenameProcess(getType()));
   * }
   */

  /*
   * XXX
   * @Override
   * public Action createDeleteAction(){
   * ProcessAction a = new ProcessAction(Texts.get("Action_deleteTypeX", getType().getSimpleName()),
   * JavaUI.getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJS_CFILE),
   * new FormDeleteProcess(getType(), getBsiCaseProjectGroup()));
   * return a;
   * }
   */

  @Override
  public boolean isFolder() {
    return false;
  }
}
