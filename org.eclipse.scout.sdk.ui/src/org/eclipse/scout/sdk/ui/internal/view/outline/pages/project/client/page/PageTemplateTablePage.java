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

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.operation.ITypeResolver;
import org.eclipse.scout.sdk.ui.action.IScoutHandler;
import org.eclipse.scout.sdk.ui.action.dto.TypeResolverPageDataAction;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;
import org.eclipse.scout.sdk.util.type.ITypeFilter;
import org.eclipse.scout.sdk.util.type.TypeComparators;
import org.eclipse.scout.sdk.util.type.TypeFilters;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ICachedTypeHierarchy;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeFilters;

/**
 * <h3>{@link PageTemplateTablePage}</h3>
 *
 * @author Matthias Villiger
 * @since 4.1.0 22.07.2014
 */
public class PageTemplateTablePage extends AbstractPage implements ITypeResolver {
  private ICachedTypeHierarchy m_pageHierarchy;

  public PageTemplateTablePage(IPage parent) {
    setParent(parent);
    setName(Texts.get("Pages"));
    setImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.PageTemplate));
  }

  @Override
  public void refresh(boolean clearCache) {
    if (clearCache && m_pageHierarchy != null) {
      m_pageHierarchy.invalidate();
    }
    super.refresh(clearCache);
  }

  @Override
  public void unloadPage() {
    if (m_pageHierarchy != null) {
      m_pageHierarchy.removeHierarchyListener(getPageDirtyListener());
      m_pageHierarchy = null;
    }
    super.unloadPage();
  }

  @Override
  protected void loadChildrenImpl() {
    PageNodePageHelper.createRepresentationFor(this, getTypes(), m_pageHierarchy);
  }

  @Override
  public Set<IType> getTypes() {
    IType iPage = TypeUtility.getType(IRuntimeClasses.IPage);
    if (m_pageHierarchy == null) {
      m_pageHierarchy = TypeUtility.getPrimaryTypeHierarchy(iPage);
      m_pageHierarchy.addHierarchyListener(getPageDirtyListener());
    }

    ITypeFilter filter = TypeFilters.getMultiTypeFilterAnd(
        ScoutTypeFilters.getInScoutBundles(getScoutBundle()),
        TypeFilters.getFlagsFilter(Flags.AccAbstract | Flags.AccPublic)
        );
    return m_pageHierarchy.getAllSubtypes(iPage, filter, TypeComparators.getTypeNameComparator());
  }

  @Override
  public Set<Class<? extends IScoutHandler>> getSupportedMenuActions() {
    return newSet(TypeResolverPageDataAction.class);
  }

  @Override
  public String getPageId() {
    return IScoutPageConstants.PAGE_TEMPLATE_TABLE_PAGE;
  }

  @Override
  public boolean isFolder() {
    return true;
  }
}
