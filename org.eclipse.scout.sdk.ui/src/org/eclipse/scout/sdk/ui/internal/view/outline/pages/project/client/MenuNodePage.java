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

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.action.Action;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.ScoutIdeProperties;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.action.WizardAction;
import org.eclipse.scout.sdk.ui.action.delete.MemberListDeleteAction;
import org.eclipse.scout.sdk.ui.action.rename.TypeRenameAction;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractScoutTypePage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;
import org.eclipse.scout.sdk.ui.view.outline.pages.InnerTypePageDirtyListener;
import org.eclipse.scout.sdk.ui.wizard.menu.MenuNewWizard;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.type.TypeComparators;
import org.eclipse.scout.sdk.workspace.type.TypeFilters;
import org.eclipse.scout.sdk.workspace.type.TypeUtility;

public class MenuNodePage extends AbstractScoutTypePage {
  static final IType iMenuType = ScoutSdk.getType(RuntimeClasses.IMenu);

  private InnerTypePageDirtyListener m_menuChangedListener;

  public MenuNodePage(IPage parentPage, IType menuType) {
    super();
    setParent(parentPage);
    setType(menuType);

  }

  @Override
  public String getPageId() {
    return IScoutPageConstants.MENU_NODE_PAGE;
  }

  @Override
  protected String getMethodNameForTranslatedText() {
    return "getConfiguredText";
  }

  /**
   * client bundle
   */
  @Override
  public IScoutBundle getScoutResource() {
    return (IScoutBundle) super.getScoutResource();
  }

  @Override
  public void unloadPage() {
    super.unloadPage();
    if (m_menuChangedListener != null) {
      ScoutSdk.removeInnerTypeChangedListener(getType(), m_menuChangedListener);
      m_menuChangedListener = null;
    }
  }

  @Override
  public void loadChildrenImpl() {
    if (m_menuChangedListener == null) {
      m_menuChangedListener = new InnerTypePageDirtyListener(this, iMenuType);
      ScoutSdk.addInnerTypeChangedListener(getType(), m_menuChangedListener);
    }
    // recursively add children
    IType[] menus = TypeUtility.getInnerTypes(getType(), TypeFilters.getSubtypeFilter(iMenuType), TypeComparators.getOrderAnnotationComparator());
    for (IType menu : menus) {
      new MenuNodePage(this, menu);
    }
  }

  @Override
  public Action createEditAction() {
    // find out the exact menu type.
    // try {
    // if (JdtUtility.isDescendant(getType(), ICheckBoxMenu.class)){
    // return new EditAction(new EntityEditOrder(new CheckBoxMenuEntity(getType())));
    // }
    // else{
    // return new EditAction(new EntityEditOrder(new MenuEntity(getType())));
    // }
    // }
    // catch (JavaModelException e) {
    // BsiCaseCore.reportError(e);
    // }
    return null;
  }

  @Override
  public Action createRenameAction() {
    return new TypeRenameAction(getOutlineView().getSite().getShell(), "Rename...", getType(), ScoutIdeProperties.SUFFIX_MENU);
  }

  @Override
  public Action createDeleteAction() {
    MemberListDeleteAction action = new MemberListDeleteAction(Texts.get("Action_deleteTypeX", getType().getElementName()), getOutlineView().getSite().getShell());
    action.addMemberToDelete(getType());
    return action;
  }

  @Override
  public Action createNewAction() {
    MenuNewWizard wizard = new MenuNewWizard();
    wizard.initWizard(getType());
    return new WizardAction(Texts.get("Action_newTypeX", "Menu"), JavaUI.getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJS_CLASS),
        wizard);
  }

}
