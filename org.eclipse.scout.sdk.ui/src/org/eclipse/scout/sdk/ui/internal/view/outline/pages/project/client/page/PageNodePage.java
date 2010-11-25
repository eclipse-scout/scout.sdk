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
package org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.page;

import org.eclipse.jdt.core.IType;
import org.eclipse.jface.action.Action;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.operation.util.TypeDeleteOperation;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.action.delete.DeleteAction;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractScoutTypePage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;
import org.eclipse.scout.sdk.ui.view.outline.pages.basic.beanproperty.BeanPropertyTablePage;
import org.eclipse.scout.sdk.workspace.IScoutBundle;

/**
 * <h3>PageWithTableNodePage</h3> ...
 */
public class PageNodePage extends AbstractScoutTypePage {
  final IType iTable = ScoutSdk.getType(RuntimeClasses.ITable);

  public PageNodePage(IPage parent, IType type) {
    setParent(parent);
    setType(type);
    setImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.IMG_PAGE_WITH_NODES));
  }

  @Override
  public String getPageId() {
    return IScoutPageConstants.PAGE_WITH_TABLE_NODE_PAGE;
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
  protected void loadChildrenImpl() {
    new BeanPropertyTablePage(this, getType());
  }

  @Override
  public Action createDeleteAction() {
    TypeDeleteOperation op = new TypeDeleteOperation(getType());
    return new DeleteAction(getName(), getOutlineView().getSite().getShell(), op);
  }

}
