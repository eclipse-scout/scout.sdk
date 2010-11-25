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
package org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.page;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.operation.util.wellform.WellformPagesOperation;
import org.eclipse.scout.sdk.ui.action.WellformAction;
import org.eclipse.scout.sdk.ui.action.WizardAction;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;
import org.eclipse.scout.sdk.ui.wizard.page.PageNewWizard;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.type.TypeComparators;
import org.eclipse.scout.sdk.workspace.type.TypeFilters;
import org.eclipse.scout.sdk.workspace.typecache.ICachedTypeHierarchy;

/**
 * <h3>AllPagesTablePage</h3> ...
 */
public class AllPagesTablePage extends AbstractPage {
  final IType iPage = ScoutSdk.getType(RuntimeClasses.IPage);

  private ICachedTypeHierarchy m_cachedTypeHierarchy;

  public AllPagesTablePage(IPage parent) {
    setParent(parent);
    setName("All pages");
  }

  @Override
  public void unloadPage() {
    if (m_cachedTypeHierarchy != null) {
      m_cachedTypeHierarchy.removeHierarchyListener(getPageDirtyListener());
      m_cachedTypeHierarchy = null;
    }
  }

  @Override
  public void refresh(boolean clearCache) {
    if (clearCache && m_cachedTypeHierarchy != null) {
      m_cachedTypeHierarchy.invalidate();
    }
    super.refresh(clearCache);
  }

  @Override
  public String getPageId() {
    return IScoutPageConstants.ALL_PAGES_TABLE_PAGE;
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
  public void loadChildrenImpl() {
    if (m_cachedTypeHierarchy == null) {
      m_cachedTypeHierarchy = ScoutSdk.getPrimaryTypeHierarchy(iPage);
      m_cachedTypeHierarchy.addHierarchyListener(getPageDirtyListener());
    }
    IType[] allPages = m_cachedTypeHierarchy.getAllClasses(TypeFilters.getClassesInProject(getScoutResource().getJavaProject()), TypeComparators.getTypeNameComparator());
    PageNodePageHelper.createRepresentationFor(this, allPages, m_cachedTypeHierarchy);
  }

  @Override
  public void fillContextMenu(IMenuManager manager) {
    super.fillContextMenu(manager);
    manager.add(new Separator());
    manager.add(new WellformAction(getOutlineView().getSite().getShell(), "Wellform all pages...", new WellformPagesOperation(getScoutResource())));
  }

  @Override
  public Action createNewAction() {
    PageNewWizard wizard = new PageNewWizard(getScoutResource());
    return new WizardAction(Texts.get("Action_newTypeX", "Page"), JavaUI.getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJS_CLASS), wizard);
  }
}
