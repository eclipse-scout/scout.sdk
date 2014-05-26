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

import java.util.Set;

import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.operation.ITypeResolver;
import org.eclipse.scout.sdk.operation.util.wellform.WellformPagesOperation;
import org.eclipse.scout.sdk.ui.action.IScoutHandler;
import org.eclipse.scout.sdk.ui.action.WellformAction;
import org.eclipse.scout.sdk.ui.action.create.PageNewAction;
import org.eclipse.scout.sdk.ui.action.dto.TypeResolverPageDataAction;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;
import org.eclipse.scout.sdk.util.type.TypeComparators;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ICachedTypeHierarchy;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeFilters;

/**
 * <h3>AllPagesTablePage</h3> ...
 */
public class AllPagesTablePage extends AbstractPage {
  private ICachedTypeHierarchy m_cachedTypeHierarchy;

  public AllPagesTablePage(IPage parent) {
    setParent(parent);
    setName(Texts.get("AllPages"));
    setImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Pages));
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

  @Override
  protected void loadChildrenImpl() {
    if (m_cachedTypeHierarchy == null) {
      IType iPage = TypeUtility.getType(IRuntimeClasses.IPage);
      m_cachedTypeHierarchy = TypeUtility.getPrimaryTypeHierarchy(iPage);
      m_cachedTypeHierarchy.addHierarchyListener(getPageDirtyListener());
    }
    Set<IType> allPages = m_cachedTypeHierarchy.getAllClasses(ScoutTypeFilters.getTypesInScoutBundles(getScoutBundle()), TypeComparators.getTypeNameComparator());

    PageNodePageHelper.createRepresentationFor(this, allPages, m_cachedTypeHierarchy);
  }

  @SuppressWarnings("unchecked")
  @Override
  public Class<? extends IScoutHandler>[] getSupportedMenuActions() {
    return new Class[]{WellformAction.class, PageNewAction.class, TypeResolverPageDataAction.class};
  }

  @Override
  public void prepareMenuAction(IScoutHandler menu) {
    if (menu instanceof WellformAction) {
      WellformAction action = (WellformAction) menu;
      action.init(getScoutBundle());
      action.setOperation(new WellformPagesOperation(getScoutBundle()));
    }
    else if (menu instanceof PageNewAction) {
      ((PageNewAction) menu).init(getScoutBundle());
    }
    else if (menu instanceof TypeResolverPageDataAction) {
      ((TypeResolverPageDataAction) menu).init(new ITypeResolver() {
        @Override
        public Set<IType> getTypes() {
          IType iPageWithTable = TypeUtility.getType(IRuntimeClasses.IPageWithTable);
          ICachedTypeHierarchy pageWithTableHierarchy = TypeUtility.getPrimaryTypeHierarchy(iPageWithTable);
          return pageWithTableHierarchy.getAllSubtypes(iPageWithTable, ScoutTypeFilters.getTypesInScoutBundles(getScoutBundle()));
        }
      }, getScoutBundle());
    }
  }
}
