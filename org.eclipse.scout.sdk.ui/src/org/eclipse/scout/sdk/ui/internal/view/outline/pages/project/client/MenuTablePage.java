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
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.ui.action.IScoutHandler;
import org.eclipse.scout.sdk.ui.action.create.MenuNewAction;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;
import org.eclipse.scout.sdk.ui.view.outline.pages.InnerTypePageDirtyListener;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.TypeCacheAccessor;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;

/**
 * <h3>MenuTablePage</h3> ...
 */
public class MenuTablePage extends AbstractPage {

  final IType iMenuType = TypeUtility.getType(RuntimeClasses.IMenu);
  private final IType m_declaringType;
  private InnerTypePageDirtyListener m_menuChangedListener;

  public MenuTablePage(IPage parentPage, IType menuDeclaringType) {
    m_declaringType = menuDeclaringType;
    setParent(parentPage);
    setName(Texts.get("MenuTablePage"));
    setImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Menu));
  }

  @Override
  public void unloadPage() {
    super.unloadPage();
    if (m_menuChangedListener != null) {
      TypeCacheAccessor.getJavaResourceChangedEmitter().removeInnerTypeChangedListener(getDeclaringType(), m_menuChangedListener);
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
      TypeCacheAccessor.getJavaResourceChangedEmitter().addInnerTypeChangedListener(getDeclaringType(), m_menuChangedListener);
    }
    IType[] menues = ScoutTypeUtility.getMenus(getDeclaringType());
    for (IType menu : menues) {
      new MenuNodePage(this, menu);
    }
  }

  @Override
  public void prepareMenuAction(IScoutHandler menu) {
    if (menu instanceof MenuNewAction) {
      MenuNewAction action = (MenuNewAction) menu;
      action.setType(getDeclaringType());
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public Class<? extends IScoutHandler>[] getSupportedMenuActions() {
    return new Class[]{MenuNewAction.class};
  }
}
