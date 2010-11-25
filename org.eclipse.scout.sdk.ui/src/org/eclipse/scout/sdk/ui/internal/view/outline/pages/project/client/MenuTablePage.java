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
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.action.WizardAction;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;
import org.eclipse.scout.sdk.ui.view.outline.pages.InnerTypePageDirtyListener;
import org.eclipse.scout.sdk.ui.wizard.menu.MenuNewWizard;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.type.SdkTypeUtility;

/**
 * <h3>MenuTablePage</h3> ...
 */
public class MenuTablePage extends AbstractPage {

  static final IType iMenuType = ScoutSdk.getType(RuntimeClasses.IMenu);
  private final IType m_declaringType;
  private InnerTypePageDirtyListener m_menuChangedListener;

  public MenuTablePage(IPage parentPage, IType menuDeclaringType) {
    m_declaringType = menuDeclaringType;
    setParent(parentPage);
    setName(Texts.get("MenuTablePage"));
  }

  @Override
  public void unloadPage() {
    super.unloadPage();
    if (m_menuChangedListener != null) {
      ScoutSdk.removeInnerTypeChangedListener(getDeclaringType(), m_menuChangedListener);
      m_menuChangedListener = null;
    }
  }

  @Override
  public String getPageId() {
    return IScoutPageConstants.MENU_TABLE_PAGE;
  }

  @Override
  public boolean isFolder() {
    return true;
  }

  /**
   * client bundle
   */
  @Override
  public IScoutBundle getScoutResource() {
    return (IScoutBundle) super.getScoutResource();
  }

  /**
   * @return the menuDeclaringType
   */
  public IType getDeclaringType() {
    return m_declaringType;
  }

  @Override
  public void loadChildrenImpl() {
    if (m_menuChangedListener == null) {
      m_menuChangedListener = new InnerTypePageDirtyListener(this, iMenuType);
      ScoutSdk.addInnerTypeChangedListener(getDeclaringType(), m_menuChangedListener);
    }
    IType[] menues = SdkTypeUtility.getMenus(getDeclaringType());
    for (IType menu : menues) {
      new MenuNodePage(this, menu);
    }
  }

  @Override
  public Action createNewAction() {
    MenuNewWizard wizard = new MenuNewWizard();
    wizard.initWizard(getDeclaringType());
    return new WizardAction(Texts.get("Action_newTypeX", "Menu"), JavaUI.getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJS_CLASS),
        wizard);
  }

}
