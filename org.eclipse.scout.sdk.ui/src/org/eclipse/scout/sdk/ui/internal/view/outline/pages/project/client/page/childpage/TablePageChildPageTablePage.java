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
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.jdt.IJavaResourceChangedListener;
import org.eclipse.scout.sdk.jdt.JdtEvent;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.action.WizardAction;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.page.PageNodePageHelper;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;
import org.eclipse.scout.sdk.ui.wizard.page.PageLinkWizard;
import org.eclipse.scout.sdk.ui.wizard.page.PageNewWizard;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.typecache.ICachedTypeHierarchy;

/**
 * <h3>TablePageChildPageTablePage</h3> all child pages of a page with table
 */
public class TablePageChildPageTablePage extends AbstractPage {
  static final String execCreateChildPage = "execCreateChildPage";

  final IType iPage = ScoutSdk.getType(RuntimeClasses.IPage);
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
    setName("Child Page");
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
      ScoutSdk.removeMethodChangedListener(getTablePageType(), m_methodListener);
      m_methodListener = null;
    }
    super.unloadPage();
  }

  @Override
  protected void loadChildrenImpl() {
    if (m_iPageTypeHierarchy == null) {
      m_iPageTypeHierarchy = ScoutSdk.getPrimaryTypeHierarchy(iPage);
      m_iPageTypeHierarchy.addHierarchyListener(getPageDirtyListener());
    }
    if (m_methodListener == null) {
      m_methodListener = new P_MethodListener();
      ScoutSdk.addMethodChangedListener(getTablePageType(), m_methodListener);
    }
    IMethod createChildPageMethod = TypeUtility.getMethod(getTablePageType(), "execCreateChildPage");
    if (TypeUtility.exists(createChildPageMethod)) {
      PageNodePageHelper.createRepresentationFor(this, TypeUtility.getNewTypeOccurencesInMethod(createChildPageMethod), m_iPageTypeHierarchy);
    }
  }

  @Override
  public void fillContextMenu(IMenuManager manager) {
    super.fillContextMenu(manager);
    IMethod createChildPageMethod = TypeUtility.getMethod(getTablePageType(), "execCreateChildPage");
    if (!TypeUtility.exists(createChildPageMethod)) {
      // new action
      PageNewWizard wizard = new PageNewWizard(getScoutResource());
      wizard.setHolderType(getTablePageType());
      manager.add(new WizardAction(Texts.get("Action_newTypeX", "Page"), ScoutSdkUi.getImageDescriptor(ScoutSdkUi.PageAdd), wizard));
      // link action
      PageLinkWizard linkWizard = new PageLinkWizard(getScoutResource());
      linkWizard.setHolderType(getTablePageType());
      linkWizard.setHolderEnabled(false);
      manager.add(new WizardAction("Add Page...", ScoutSdkUi.getImageDescriptor(ScoutSdkUi.PageLink), linkWizard));
    }
  }

  public IType getTablePageType() {
    return m_tablePageType;
  }

  private class P_MethodListener implements IJavaResourceChangedListener {
    @Override
    public void handleEvent(JdtEvent event) {
      if (event.getElement().getElementName().equals(execCreateChildPage)) {
        markStructureDirty();
      }
    }
  } // end class P_MethodListener
}
