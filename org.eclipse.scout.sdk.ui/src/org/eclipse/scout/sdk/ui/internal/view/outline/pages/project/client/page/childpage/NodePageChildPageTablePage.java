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
package org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.page.childpage;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.action.IScoutHandler;
import org.eclipse.scout.sdk.ui.action.PageLinkAction;
import org.eclipse.scout.sdk.ui.action.create.PageNewAction;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.page.PageNodePageHelper;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;
import org.eclipse.scout.sdk.util.jdt.IJavaResourceChangedListener;
import org.eclipse.scout.sdk.util.jdt.JdtEvent;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ICachedTypeHierarchy;
import org.eclipse.scout.sdk.util.typecache.TypeCacheAccessor;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;

/**
 * <h3>NodePageChildPageTablePage</h3> all child pages of a page with nodes
 */
public class NodePageChildPageTablePage extends AbstractPage {
  static final String execCreateChildPages = "execCreateChildPages";

  private final IType m_nodePageType;
  final IType iPage = TypeUtility.getType(RuntimeClasses.IPage);
  private ICachedTypeHierarchy m_iPageTypeHierarchy;
  private P_MethodListener m_methodListener;

  /**
   * @param parent
   * @param type
   *          a subtype of AbstractPageWithNodes
   */
  public NodePageChildPageTablePage(IPage parent, IType nodePageType) {
    super();
    m_nodePageType = nodePageType;
    setParent(parent);
    setName(Texts.get("ChildPages"));
    setImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Pages));
  }

  @Override
  public String getPageId() {
    return IScoutPageConstants.NODE_PAGE_CHILD_PAGE_TABLE_PAGE;
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

  @Override
  public void unloadPage() {
    if (m_iPageTypeHierarchy != null) {
      m_iPageTypeHierarchy.removeHierarchyListener(getPageDirtyListener());
      m_iPageTypeHierarchy = null;
    }
    if (m_methodListener != null) {
      TypeCacheAccessor.getJavaResourceChangedEmitter().removeMethodChangedListener(getNodePageType(), m_methodListener);
      m_methodListener = null;
    }
    super.unloadPage();
  }

  @Override
  protected void loadChildrenImpl() {
    if (m_iPageTypeHierarchy == null) {
      m_iPageTypeHierarchy = TypeUtility.getPrimaryTypeHierarchy(iPage);
      m_iPageTypeHierarchy.addHierarchyListener(getPageDirtyListener());
    }
    if (m_methodListener == null) {
      m_methodListener = new P_MethodListener();
      TypeCacheAccessor.getJavaResourceChangedEmitter().addMethodChangedListener(getNodePageType(), m_methodListener);
    }
    IMethod createChildPagesMethods = TypeUtility.getMethod(getNodePageType(), execCreateChildPages);
    if (TypeUtility.exists(createChildPagesMethods)) {
      PageNodePageHelper.createRepresentationFor(this, ScoutTypeUtility.getNewTypeOccurencesInMethod(createChildPagesMethods), m_iPageTypeHierarchy);
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
      ((PageLinkAction) menu).init(getScoutResource(), getNodePageType());
    }
    else if (menu instanceof PageNewAction) {
      ((PageNewAction) menu).init(getScoutResource(), getNodePageType());
    }
  }

  public IType getNodePageType() {
    return m_nodePageType;
  }

  private class P_MethodListener implements IJavaResourceChangedListener {
    @Override
    public void handleEvent(JdtEvent event) {
      if (event.getElement().getElementName().equals(execCreateChildPages)) {
        markStructureDirty();
      }
    }
  } // end class P_MethodListener
}
