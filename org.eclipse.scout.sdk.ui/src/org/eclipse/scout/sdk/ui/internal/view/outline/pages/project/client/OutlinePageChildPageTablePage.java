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
import org.eclipse.jface.action.Action;
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

/**
 * <h3>OutlinePageChildPageTablePage</h3> all child pages of a outline
 */
public class OutlinePageChildPageTablePage extends AbstractPage {
  final IType iPage = ScoutSdk.getType(RuntimeClasses.IPage);

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
    setName("Child Pages");
  }

  @Override
  public String getPageId() {
    return IScoutPageConstants.OUTLINE_PAGE_CHILD_PAGE_TABLE_PAGE;
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
    if (m_methodListener != null) {
      ScoutSdk.removeMethodChangedListener(getOutlineType(), m_methodListener);
    }
    super.unloadPage();
  }

  /*
   * (non-Javadoc)
   * @see org.eclipse.scout.sde.ui.view.outline.pages.AbstractPage#loadChildrenImpl()
   */
  @Override
  protected void loadChildrenImpl() {
    if (m_methodListener == null) {
      m_methodListener = new P_MethodListener();
      ScoutSdk.addMethodChangedListener(getOutlineType(), m_methodListener);
    }
    IMethod createChildPagesMethod = TypeUtility.getMethod(getOutlineType(), "execCreateChildPages");
    if (TypeUtility.exists(createChildPagesMethod)) {
      PageNodePageHelper.createRepresentationFor(this, TypeUtility.getNewTypeOccurencesInMethod(createChildPagesMethod), ScoutSdk.getPrimaryTypeHierarchy(iPage));
    }
  }

  @Override
  public Action createNewAction() {
    PageNewWizard wizard = new PageNewWizard(getScoutResource());
    wizard.setHolderType(getOutlineType());
    return new WizardAction(Texts.get("Action_newTypeX", "Page"), ScoutSdkUi.getImageDescriptor(ScoutSdkUi.IMG_TOOL_ADD), wizard);

  }

  @Override
  public void fillContextMenu(IMenuManager manager) {
    super.fillContextMenu(manager);
    PageLinkWizard wizard = new PageLinkWizard(getScoutResource());
    wizard.setHolderType(getOutlineType());
    wizard.setHolderEnabled(false);
    manager.add(new WizardAction("Add Page", ScoutSdkUi.getImageDescriptor(ScoutSdkUi.IMG_TOOL_ADD), wizard));
  }

  public IType getOutlineType() {
    return m_outlineType;
  }

  private class P_MethodListener implements IJavaResourceChangedListener {
    @Override
    public void handleEvent(JdtEvent event) {
      if (event.getElement().getElementName().equals("execCreateChildPages")) {
        markStructureDirty();
      }
    }
  }
}
