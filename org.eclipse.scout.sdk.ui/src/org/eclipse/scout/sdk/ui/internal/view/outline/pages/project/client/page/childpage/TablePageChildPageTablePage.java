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
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.page.PageWithTableNodePage;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;
import org.eclipse.scout.sdk.util.jdt.IJavaResourceChangedListener;
import org.eclipse.scout.sdk.util.jdt.JdtEvent;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ICachedTypeHierarchy;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;

/**
 * <h3>TablePageChildPageTablePage</h3> all child pages of a page with table
 */
public class TablePageChildPageTablePage extends AbstractPage {
  private ICachedTypeHierarchy m_iPageTypeHierarchy;
  private P_MethodListener m_methodListener;

  private final IType m_tablePageType;

  /**
   * @param parent
   * @param type
   *          a subtype of AbstractPageWithNodes
   */
  public TablePageChildPageTablePage(IPage parent, IType tablePageType) {
    m_tablePageType = tablePageType;
    setParent(parent);
    setName(Texts.get("ChildPage"));
    setImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Pages));
  }

  @Override
  public boolean isFolder() {
    return true;
  }

  @Override
  public String getPageId() {
    return IScoutPageConstants.TABLE_PAGE_CHILD_PAGE_TABLE_PAGE;
  }

  @Override
  public void unloadPage() {
    if (m_iPageTypeHierarchy != null) {
      m_iPageTypeHierarchy.removeHierarchyListener(getPageDirtyListener());
      m_iPageTypeHierarchy = null;
    }
    if (m_methodListener != null) {
      ScoutSdkCore.getJavaResourceChangedEmitter().removeMethodChangedListener(getTablePageType(), m_methodListener);
      m_methodListener = null;
    }
    super.unloadPage();
  }

  @Override
  protected void loadChildrenImpl() {
    if (m_iPageTypeHierarchy == null) {
      IType iPage = TypeUtility.getType(IRuntimeClasses.IPage);
      m_iPageTypeHierarchy = TypeUtility.getPrimaryTypeHierarchy(iPage);
      m_iPageTypeHierarchy.addHierarchyListener(getPageDirtyListener());
    }
    if (m_methodListener == null) {
      m_methodListener = new P_MethodListener();
      ScoutSdkCore.getJavaResourceChangedEmitter().addMethodChangedListener(getTablePageType(), m_methodListener);
    }
    IMethod createChildPageMethod = TypeUtility.getMethod(getTablePageType(), PageWithTableNodePage.METHOD_EXEC_CREATE_CHILD_PAGE);
    if (TypeUtility.exists(createChildPageMethod)) {
      PageNodePageHelper.createRepresentationFor(this, ScoutTypeUtility.getNewTypeOccurencesInMethod(createChildPageMethod), m_iPageTypeHierarchy);
    }
  }

  @Override
  public Set<Class<? extends IScoutHandler>> getSupportedMenuActions() {
    IMethod createChildPageMethod = TypeUtility.getMethod(getTablePageType(), PageWithTableNodePage.METHOD_EXEC_CREATE_CHILD_PAGE);
    if (!TypeUtility.exists(createChildPageMethod)) {
      return newSet(PageLinkAction.class, PageNewAction.class);
    }
    return null;
  }

  public IType getTablePageType() {
    return m_tablePageType;
  }

  private class P_MethodListener implements IJavaResourceChangedListener {
    @Override
    public void handleEvent(JdtEvent event) {
      if (event.getElement().getElementName().equals(PageWithTableNodePage.METHOD_EXEC_CREATE_CHILD_PAGE)) {
        markStructureDirty();
      }
    }
  } // end class P_MethodListener
}
