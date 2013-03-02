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
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.operation.util.wellform.WellformPagesOperation;
import org.eclipse.scout.sdk.ui.action.IScoutHandler;
import org.eclipse.scout.sdk.ui.action.WellformAction;
import org.eclipse.scout.sdk.ui.action.create.PageNewAction;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;
import org.eclipse.scout.sdk.util.type.TypeComparators;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ICachedTypeHierarchy;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeFilters;

/**
 * <h3>AllPagesTablePage</h3> ...
 */
public class AllPagesTablePage extends AbstractPage {
  final IType iPage = TypeUtility.getType(RuntimeClasses.IPage);

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
      m_cachedTypeHierarchy = TypeUtility.getPrimaryTypeHierarchy(iPage);
      m_cachedTypeHierarchy.addHierarchyListener(getPageDirtyListener());
    }
    IType[] allPages = m_cachedTypeHierarchy.getAllClasses(ScoutTypeFilters.getTypesInScoutBundles(getScoutResource()), TypeComparators.getTypeNameComparator());
    PageNodePageHelper.createRepresentationFor(this, allPages, m_cachedTypeHierarchy);
  }

  @SuppressWarnings("unchecked")
  @Override
  public Class<? extends IScoutHandler>[] getSupportedMenuActions() {
    return new Class[]{WellformAction.class, PageNewAction.class};
  }

  @Override
  public void prepareMenuAction(IScoutHandler menu) {
    if (menu instanceof WellformAction) {
      WellformAction action = (WellformAction) menu;
      action.setScoutBundle(getScoutResource());
      action.setOperation(new WellformPagesOperation(getScoutResource()));
      action.setLabel(Texts.get("Wellform all Pages..."));
    }
    else if (menu instanceof PageNewAction) {
      ((PageNewAction) menu).init(getScoutResource());
    }
  }
}
