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
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.ui.action.AbstractScoutHandler;
import org.eclipse.scout.sdk.ui.action.ShowJavaReferencesAction;
import org.eclipse.scout.sdk.ui.action.create.MenuNewAction;
import org.eclipse.scout.sdk.ui.action.delete.MemberListDeleteAction;
import org.eclipse.scout.sdk.ui.action.rename.TypeRenameAction;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractScoutTypePage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;
import org.eclipse.scout.sdk.ui.view.outline.pages.InnerTypePageDirtyListener;
import org.eclipse.scout.sdk.util.SdkProperties;
import org.eclipse.scout.sdk.util.type.TypeFilters;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.TypeCacheAccessor;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeComparators;

public class MenuNodePage extends AbstractScoutTypePage {
  static final IType iMenuType = TypeUtility.getType(RuntimeClasses.IMenu);

  private InnerTypePageDirtyListener m_menuChangedListener;

  public MenuNodePage(IPage parentPage, IType menuType) {
    super(SdkProperties.SUFFIX_MENU);
    setParent(parentPage);
    setType(menuType);
    setImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Menu));

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
      TypeCacheAccessor.getJavaResourceChangedEmitter().removeInnerTypeChangedListener(getType(), m_menuChangedListener);
      m_menuChangedListener = null;
    }
  }

  @Override
  public void loadChildrenImpl() {
    if (m_menuChangedListener == null) {
      m_menuChangedListener = new InnerTypePageDirtyListener(this, iMenuType);
      TypeCacheAccessor.getJavaResourceChangedEmitter().addInnerTypeChangedListener(getType(), m_menuChangedListener);
    }
    // recursively add children
    IType[] menus = TypeUtility.getInnerTypes(getType(), TypeFilters.getSubtypeFilter(iMenuType), ScoutTypeComparators.getOrderAnnotationComparator());
    for (IType menu : menus) {
      new MenuNodePage(this, menu);
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public Class<? extends AbstractScoutHandler>[] getSupportedMenuActions() {
    return new Class[]{TypeRenameAction.class, ShowJavaReferencesAction.class, MemberListDeleteAction.class, MenuNewAction.class};
  }

  @Override
  public void prepareMenuAction(AbstractScoutHandler menu) {
    super.prepareMenuAction(menu);
    if (menu instanceof MemberListDeleteAction) {
      MemberListDeleteAction action = (MemberListDeleteAction) menu;
      action.addMemberToDelete(getType());
      action.setImage(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.MenuRemove));
    }
    else if (menu instanceof MenuNewAction) {
      ((MenuNewAction) menu).setType(getType());
    }
  }
}
