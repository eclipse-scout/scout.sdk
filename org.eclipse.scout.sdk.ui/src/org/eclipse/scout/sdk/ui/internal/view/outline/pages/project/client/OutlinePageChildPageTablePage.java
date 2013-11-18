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

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.ScoutSdkCore;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.ui.action.IScoutHandler;
import org.eclipse.scout.sdk.ui.action.PageLinkAction;
import org.eclipse.scout.sdk.ui.action.create.PageNewAction;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.page.PageNodePageHelper;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.page.childpage.NodePageChildPageTablePage;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;
import org.eclipse.scout.sdk.util.jdt.IJavaResourceChangedListener;
import org.eclipse.scout.sdk.util.jdt.JdtEvent;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;

/**
 * <h3>OutlinePageChildPageTablePage</h3> all child pages of a outline
 */
public class OutlinePageChildPageTablePage extends AbstractPage {
  private final IType m_outlineType;
  private P_MethodListener m_methodListener;

  /**
   * @param parent
   * @param type
   *          a subtype of AbstractOutline
   */
  public OutlinePageChildPageTablePage(IPage parent, IType outlineType) {
    m_outlineType = outlineType;
    setParent(parent);
    setName(Texts.get("ChildPages"));
    setImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Outlines));
  }

  @Override
  public String getPageId() {
    return IScoutPageConstants.OUTLINE_PAGE_CHILD_PAGE_TABLE_PAGE;
  }

  @Override
  public boolean isFolder() {
    return true;
  }

  @Override
  public void unloadPage() {
    if (m_methodListener != null) {
      ScoutSdkCore.getJavaResourceChangedEmitter().removeMethodChangedListener(getOutlineType(), m_methodListener);
    }
    super.unloadPage();
  }

  @Override
  protected void loadChildrenImpl() {
    if (m_methodListener == null) {
      m_methodListener = new P_MethodListener();
      ScoutSdkCore.getJavaResourceChangedEmitter().addMethodChangedListener(getOutlineType(), m_methodListener);
    }
    IMethod createChildPagesMethod = TypeUtility.getMethod(getOutlineType(), NodePageChildPageTablePage.EXEC_CREATE_CHILD_PAGES);
    if (TypeUtility.exists(createChildPagesMethod)) {
      IType iPage = TypeUtility.getType(RuntimeClasses.IPage);
      PageNodePageHelper.createRepresentationFor(this, ScoutTypeUtility.getNewTypeOccurencesInMethod(createChildPagesMethod), TypeUtility.getPrimaryTypeHierarchy(iPage));
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public Class<? extends IScoutHandler>[] getSupportedMenuActions() {
    return new Class[]{PageLinkAction.class, PageNewAction.class};
  }

  @Override
  public void prepareMenuAction(IScoutHandler menu) {
    if (menu instanceof PageLinkAction) {
      ((PageLinkAction) menu).init(getScoutBundle(), getOutlineType());
    }
    else if (menu instanceof PageNewAction) {
      ((PageNewAction) menu).init(getScoutBundle(), getOutlineType());
    }
  }

  public IType getOutlineType() {
    return m_outlineType;
  }

  private class P_MethodListener implements IJavaResourceChangedListener {
    @Override
    public void handleEvent(JdtEvent event) {
      if (event.getElement().getElementName().equals(NodePageChildPageTablePage.EXEC_CREATE_CHILD_PAGES)) {
        markStructureDirty();
      }
    }
  }
}
