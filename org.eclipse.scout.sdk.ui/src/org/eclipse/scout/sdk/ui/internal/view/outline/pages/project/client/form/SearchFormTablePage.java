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

import java.util.Set;

import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.operation.ITypeResolver;
import org.eclipse.scout.sdk.ui.action.IScoutHandler;
import org.eclipse.scout.sdk.ui.action.WellformAction;
import org.eclipse.scout.sdk.ui.action.create.SearchFormNewAction;
import org.eclipse.scout.sdk.ui.action.dto.TypeResolverFormDataAction;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;
import org.eclipse.scout.sdk.util.type.TypeComparators;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ICachedTypeHierarchy;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeFilters;

/**
 * <h3>SearchFormTablePage</h3>
 */
public class SearchFormTablePage extends AbstractPage implements ITypeResolver {
  private ICachedTypeHierarchy m_searchFormHierarchy;

  public SearchFormTablePage(AbstractPage parent) {
    setParent(parent);
    setName(Texts.get("SearchFormTablePage"));
    setImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.SearchForms));
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

  @Override
  protected void loadChildrenImpl() {
    for (IType searchForm : getTypes()) {
      FormNodePage form = new FormNodePage(this, searchForm);
      form.setImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.SearchForm));
    }
  }

  @Override
  public Set<IType> getTypes() {
    IType iSearchForm = TypeUtility.getType(IRuntimeClasses.ISearchForm);

    if (m_searchFormHierarchy == null) {
      m_searchFormHierarchy = TypeUtility.getPrimaryTypeHierarchy(iSearchForm);
      m_searchFormHierarchy.addHierarchyListener(getPageDirtyListener());
    }
    return m_searchFormHierarchy.getAllSubtypes(iSearchForm, ScoutTypeFilters.getClassesInScoutBundles(getScoutBundle()), TypeComparators.getTypeNameComparator());
  }

  @Override
  public Set<Class<? extends IScoutHandler>> getSupportedMenuActions() {
    return newSet(WellformAction.class, SearchFormNewAction.class, TypeResolverFormDataAction.class);
  }
}
