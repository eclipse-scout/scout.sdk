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
package org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.shared;

import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.ui.action.IScoutHandler;
import org.eclipse.scout.sdk.ui.action.create.LookupCallNewAction;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;
import org.eclipse.scout.sdk.util.type.ITypeFilter;
import org.eclipse.scout.sdk.util.type.TypeComparators;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ICachedTypeHierarchy;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeFilters;

public class LookupCallTablePage extends AbstractPage {
  private ICachedTypeHierarchy m_lookupCallHierarchy;

  public LookupCallTablePage(AbstractPage parent) {
    setParent(parent);
    setName(Texts.get("LookupCallTablePage"));
    setImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.LookupCalls));
  }

  @Override
  public void unloadPage() {
    if (m_lookupCallHierarchy != null) {
      m_lookupCallHierarchy.removeHierarchyListener(getPageDirtyListener());
      m_lookupCallHierarchy = null;
    }
    super.unloadPage();
  }

  @Override
  public void refresh(boolean clearCache) {
    if (clearCache && m_lookupCallHierarchy != null) {
      m_lookupCallHierarchy.invalidate();
    }
    super.refresh(clearCache);
  }

  @Override
  public String getPageId() {
    return IScoutPageConstants.LOOKUP_CALL_TABLE_PAGE;
  }

  @Override
  public boolean isFolder() {
    return true;
  }

  @Override
  public void loadChildrenImpl() {
    IType iLookupCall = TypeUtility.getType(IRuntimeClasses.ILookupCall);

    if (m_lookupCallHierarchy == null) {
      m_lookupCallHierarchy = TypeUtility.getPrimaryTypeHierarchy(iLookupCall);
      m_lookupCallHierarchy.addHierarchyListener(getPageDirtyListener());
    }
    ITypeFilter filter = ScoutTypeFilters.getTypesInScoutBundles(getScoutBundle());
    IType[] lookupCallTypes = m_lookupCallHierarchy.getAllSubtypes(iLookupCall, filter, TypeComparators.getTypeNameComparator());
    for (IType type : lookupCallTypes) {
      new LookupCallNodePage(this, type);
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public Class<? extends IScoutHandler>[] getSupportedMenuActions() {
    return new Class[]{LookupCallNewAction.class};
  }

  @Override
  public void prepareMenuAction(IScoutHandler menu) {
    ((LookupCallNewAction) menu).setScoutBundle(getScoutBundle());
  }
}
