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
import org.eclipse.scout.sdk.operation.util.wellform.WellformScoutTypeOperation;
import org.eclipse.scout.sdk.ui.action.IScoutHandler;
import org.eclipse.scout.sdk.ui.action.ShowJavaReferencesAction;
import org.eclipse.scout.sdk.ui.action.WellformAction;
import org.eclipse.scout.sdk.ui.action.delete.DeleteAction;
import org.eclipse.scout.sdk.ui.action.rename.TypeRenameAction;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.MenuTablePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.page.childpage.NodePageChildPageTablePage;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractScoutTypePage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;
import org.eclipse.scout.sdk.ui.view.outline.pages.basic.beanproperty.BeanPropertyTablePage;
import org.eclipse.scout.sdk.workspace.IScoutBundle;

/**
 * <h3>PageWithNodeNodePage</h3> ...
 */
public class PageWithNodeNodePage extends AbstractScoutTypePage {

  /**
   * @param parent
   * @param type
   *          a subtype of AbstractPageWithNodes
   */
  public PageWithNodeNodePage(IPage parent, IType type) {
    setParent(parent);
    setType(type);
    setImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.PageNode));
  }

  @Override
  public String getPageId() {
    return IScoutPageConstants.PAGE_WITH_NODE_NODE_PAGE;
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
    new MenuTablePage(this, getType());
    new NodePageChildPageTablePage(this, getType());
  }

  @SuppressWarnings("unchecked")
  @Override
  public Class<? extends IScoutHandler>[] getSupportedMenuActions() {
    return new Class[]{TypeRenameAction.class, ShowJavaReferencesAction.class, WellformAction.class, DeleteAction.class};
  }

  @Override
  public void prepareMenuAction(IScoutHandler menu) {
    super.prepareMenuAction(menu);
    if (menu instanceof WellformAction) {
      ((WellformAction) menu).setOperation(new WellformScoutTypeOperation(getType(), true));
    }
    else if (menu instanceof DeleteAction) {
      DeleteAction action = (DeleteAction) menu;
      action.addType(getType());
      action.setName(getName());
    }
  }
}
