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

import java.util.Set;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.ScoutSdkCore;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
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
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;

/**
 * <h3>NodePageChildPageTablePage</h3> all child pages of a page with nodes
 */
public class NodePageChildPageTablePage extends AbstractPage {
  public static final String EXEC_CREATE_CHILD_PAGES = "execCreateChildPages";

  private final IType m_nodePageType;
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

  @Override
  public void unloadPage() {
    if (m_iPageTypeHierarchy != null) {
      m_iPageTypeHierarchy.removeHierarchyListener(getPageDirtyListener());
      m_iPageTypeHierarchy = null;
    }
    if (m_methodListener != null) {
      ScoutSdkCore.getJavaResourceChangedEmitter().removeMethodChangedListener(getNodePageType(), m_methodListener);
      m_methodListener = null;
    }
    super.unloadPage();
  }

  @Override
  protected void loadChildrenImpl() {
    IType iPage = TypeUtility.getType(IRuntimeClasses.IPage);

    if (m_iPageTypeHierarchy == null) {
      m_iPageTypeHierarchy = TypeUtility.getPrimaryTypeHierarchy(iPage);
      m_iPageTypeHierarchy.addHierarchyListener(getPageDirtyListener());
    }
    if (m_methodListener == null) {
      m_methodListener = new P_MethodListener();
      ScoutSdkCore.getJavaResourceChangedEmitter().addMethodChangedListener(getNodePageType(), m_methodListener);
    }
    IMethod createChildPagesMethods = TypeUtility.getMethod(getNodePageType(), EXEC_CREATE_CHILD_PAGES);
    if (TypeUtility.exists(createChildPagesMethods)) {
      PageNodePageHelper.createRepresentationFor(this, ScoutTypeUtility.getNewTypeOccurencesInMethod(createChildPagesMethods), m_iPageTypeHierarchy);
    }
  }

  @Override
  public Set<Class<? extends IScoutHandler>> getSupportedMenuActions() {
    return newSet(PageLinkAction.class, PageNewAction.class);
  }

  public IType getNodePageType() {
    return m_nodePageType;
  }

  private class P_MethodListener implements IJavaResourceChangedListener {
    @Override
    public void handleEvent(JdtEvent event) {
      if (event.getElement().getElementName().equals(EXEC_CREATE_CHILD_PAGES)) {
        markStructureDirty();
      }
    }
  } // end class P_MethodListener
}
