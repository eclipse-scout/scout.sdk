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
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.operation.util.wellform.WellformOutlinesOperation;
import org.eclipse.scout.sdk.ui.action.IScoutHandler;
import org.eclipse.scout.sdk.ui.action.WellformAction;
import org.eclipse.scout.sdk.ui.action.create.OutlineNewAction;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;
import org.eclipse.scout.sdk.util.type.TypeComparators;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ICachedTypeHierarchy;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeFilters;

/**
 * <h3>OutlineTablePage</h3> ...
 */
public class OutlineTablePage extends AbstractPage {
  private final IType iOutline = TypeUtility.getType(RuntimeClasses.IOutline);
  private ICachedTypeHierarchy m_outlineHierarchy;

  public OutlineTablePage(AbstractPage parent) {
    setParent(parent);
    setName(Texts.get("AllOutlinesTablePage"));
    setImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Outlines));
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

  @Override
  public boolean isFolder() {
    return true;
  }

  @Override
  public void loadChildrenImpl() {
    if (m_outlineHierarchy == null) {
      m_outlineHierarchy = TypeUtility.getPrimaryTypeHierarchy(iOutline);
      m_outlineHierarchy.addHierarchyListener(getPageDirtyListener());
    }
    IType[] outlines = m_outlineHierarchy.getAllSubtypes(iOutline, ScoutTypeFilters.getTypesInScoutBundles(getScoutBundle()), TypeComparators.getTypeNameComparator());
    for (IType outline : outlines) {
      new OutlineNodePage(this, outline);
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public Class<? extends IScoutHandler>[] getSupportedMenuActions() {
    return new Class[]{OutlineNewAction.class, WellformAction.class};
  }

  @Override
  public void prepareMenuAction(IScoutHandler menu) {
    if (menu instanceof OutlineNewAction) {
      ((OutlineNewAction) menu).init(getScoutBundle());
    }
    else if (menu instanceof WellformAction) {
      WellformAction action = (WellformAction) menu;
      action.setLabel(Texts.get("WellformAllOutlines"));
      action.setScoutBundle(getScoutBundle());
      action.setOperation(new WellformOutlinesOperation(getScoutBundle()));
    }
  }
}
