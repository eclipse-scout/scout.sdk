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
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.operation.util.wellform.WellformOutlinesOperation;
import org.eclipse.scout.sdk.ui.action.WellformAction;
import org.eclipse.scout.sdk.ui.action.WizardAction;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;
import org.eclipse.scout.sdk.ui.wizard.outline.OutlineNewWizard;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.type.TypeComparators;
import org.eclipse.scout.sdk.workspace.type.TypeFilters;
import org.eclipse.scout.sdk.workspace.typecache.ICachedTypeHierarchy;

/**
 * <h3>OutlineTablePage</h3> ...
 */
public class OutlineTablePage extends AbstractPage {
  final IType iOutline = ScoutSdk.getType(RuntimeClasses.IOutline);
  private ICachedTypeHierarchy m_outlineHierarchy;

  public OutlineTablePage(AbstractPage parent) {
    setParent(parent);
    setName(Texts.get("AllOutlinesTablePage"));
  }

  @Override
  public void unloadPage() {
    if (m_outlineHierarchy != null) {
      m_outlineHierarchy.removeHierarchyListener(getPageDirtyListener());
      m_outlineHierarchy = null;
    }
    super.unloadPage();
  }

  @Override
  public void refresh(boolean clearCache) {
    if (clearCache && m_outlineHierarchy != null) {
      m_outlineHierarchy.invalidate();
    }
    super.refresh(clearCache);
  }

  @Override
  public String getPageId() {
    return IScoutPageConstants.OUTLINE_TABLE_PAGE;
  }

  /**
   * client bundle
   */
  @Override
  public IScoutBundle getScoutResource() {
    return (IScoutBundle) super.getScoutResource();
  }

  @Override
  public boolean isFolder() {
    return true;
  }

  @Override
  public void loadChildrenImpl() {
    if (m_outlineHierarchy == null) {
      m_outlineHierarchy = ScoutSdk.getPrimaryTypeHierarchy(iOutline);
      m_outlineHierarchy.addHierarchyListener(getPageDirtyListener());
    }
    IType[] outlines = m_outlineHierarchy.getAllSubtypes(iOutline, TypeFilters.getClassesInProject(getScoutResource().getJavaProject()), TypeComparators.getTypeNameComparator());
    for (IType outline : outlines) {
      new OutlineNodePage(this, outline);
    }
  }

  @Override
  public void fillContextMenu(IMenuManager manager) {
    super.fillContextMenu(manager);
    manager.add(new Separator());
    manager.add(new WellformAction(getOutlineView().getSite().getShell(), "Wellform all outlines...", new WellformOutlinesOperation(getScoutResource())));
  }

  @Override
  public Action createNewAction() {
    OutlineNewWizard wizard = new OutlineNewWizard(getScoutResource());
    return new WizardAction(Texts.get("Action_newTypeX", "Outline"), JavaUI.getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJS_CLASS),
        wizard);
  }
}
