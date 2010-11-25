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
package org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.form;

import org.eclipse.jdt.core.IType;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.operation.util.wellform.WellformSearchFormsOperation;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.action.WellformAction;
import org.eclipse.scout.sdk.ui.action.WizardAction;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;
import org.eclipse.scout.sdk.ui.wizard.form.SearchFormNewWizard;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.type.TypeComparators;
import org.eclipse.scout.sdk.workspace.type.TypeFilters;
import org.eclipse.scout.sdk.workspace.typecache.ICachedTypeHierarchy;

/**
 * <h3>SearchFormTablePage</h3> ...
 */
public class SearchFormTablePage extends AbstractPage {
  final IType iSearchForm = ScoutSdk.getType(RuntimeClasses.ISearchForm);
  private ICachedTypeHierarchy m_searchFormHierarchy;

  public SearchFormTablePage(AbstractPage parent) {
    setParent(parent);
    setName(Texts.get("SearchFormTablePage"));
  }

  @Override
  public void unloadPage() {
    if (m_searchFormHierarchy != null) {
      m_searchFormHierarchy.removeHierarchyListener(getPageDirtyListener());
      m_searchFormHierarchy = null;
    }
    super.unloadPage();
  }

  @Override
  public void refresh(boolean clearCache) {
    if (clearCache && m_searchFormHierarchy != null) {
      m_searchFormHierarchy.invalidate();
    }
    super.refresh(clearCache);
  }

  @Override
  public boolean isFolder() {
    return true;
  }

  @Override
  public String getPageId() {
    return IScoutPageConstants.SEARCH_TABLE_PAGE;
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
    if (m_searchFormHierarchy == null) {
      m_searchFormHierarchy = ScoutSdk.getPrimaryTypeHierarchy(iSearchForm);
      m_searchFormHierarchy.addHierarchyListener(getPageDirtyListener());
    }
    IType[] searchForms = m_searchFormHierarchy.getAllSubtypes(iSearchForm, TypeFilters.getClassesInProject(getScoutResource().getJavaProject()), TypeComparators.getTypeNameComparator());
    for (IType searchForm : searchForms) {
      new FormNodePage(this, searchForm);
    }
  }

  @Override
  public void markStructureDirty() {
    // TODO Auto-generated method stub
    super.markStructureDirty();
  }

  @Override
  public void fillContextMenu(IMenuManager manager) {
    super.fillContextMenu(manager);
    manager.add(new Separator());
    manager.add(new WellformAction(getOutlineView().getSite().getShell(), "Wellform all search forms...", new WellformSearchFormsOperation(getScoutResource())));
  }

  @Override
  public Action createNewAction() {
    return new WizardAction(Texts.get("Action_newTypeX", "Search Form"), ScoutSdkUi.getImageDescriptor(ScoutSdkUi.IMG_TOOL_ADD),
        new SearchFormNewWizard(getScoutResource()));
  }

}
