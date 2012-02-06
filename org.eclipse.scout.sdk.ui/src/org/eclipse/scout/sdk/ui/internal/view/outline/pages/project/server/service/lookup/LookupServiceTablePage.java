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
package org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.server.service.lookup;

import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.action.IScoutHandler;
import org.eclipse.scout.sdk.ui.action.create.LookupServiceNewAction;
import org.eclipse.scout.sdk.ui.action.validation.FormDataSqlBindingValidateAction;
import org.eclipse.scout.sdk.ui.action.validation.ITypeResolver;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;
import org.eclipse.scout.sdk.util.type.TypeComparators;
import org.eclipse.scout.sdk.util.type.TypeFilters;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ICachedTypeHierarchy;
import org.eclipse.scout.sdk.util.typecache.ITypeHierarchyChangedListener;
import org.eclipse.scout.sdk.workspace.IScoutBundle;

public class LookupServiceTablePage extends AbstractPage {

  final IType iLookupService = TypeUtility.getType(RuntimeClasses.ILookupService);
  ICachedTypeHierarchy m_serviceHierarchy;

  public LookupServiceTablePage(AbstractPage parent) {
    setParent(parent);
    setName(Texts.get("LookupServiceTablePage"));
    setImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Services));
  }

  @Override
  public void unloadPage() {
    if (m_serviceHierarchy != null) {
      m_serviceHierarchy.removeHierarchyListener(getPageDirtyListener());
    }
  }

  @Override
  public String getPageId() {
    return IScoutPageConstants.LOOKUP_SERVICE_TABLE_PAGE;
  }

  @Override
  public boolean isFolder() {
    return true;
  }

  @Override
  public void refresh(boolean clearCache) {
    if (clearCache && m_serviceHierarchy != null) {
      m_serviceHierarchy.invalidate();
    }
    super.refresh(clearCache);
  }

  /**
   * server bundle
   */
  @Override
  public IScoutBundle getScoutResource() {
    return (IScoutBundle) super.getScoutResource();
  }

  @Override
  public void loadChildrenImpl() {
    for (IType service : resolveAllLookupServices()) {
      new LookupServiceNodePage(this, service, iLookupService);
    }
  }

  protected IType[] resolveAllLookupServices() {
    if (m_serviceHierarchy == null) {
      m_serviceHierarchy = TypeUtility.getPrimaryTypeHierarchy(iLookupService);
      m_serviceHierarchy.addHierarchyListener(getPageDirtyListener());
    }
    IType[] services = m_serviceHierarchy.getAllSubtypes(iLookupService, TypeFilters.getClassesInProject(getScoutResource().getJavaProject()), TypeComparators.getTypeNameComparator());
    return services;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Class<? extends IScoutHandler>[] getSupportedMenuActions() {
    return new Class[]{FormDataSqlBindingValidateAction.class, LookupServiceNewAction.class};
  }

  @Override
  public void prepareMenuAction(IScoutHandler menu) {
    if (menu instanceof FormDataSqlBindingValidateAction) {
      ((FormDataSqlBindingValidateAction) menu).setTyperesolver(new ITypeResolver() {
        @Override
        public IType[] getTypes() {
          return resolveAllLookupServices();
        }
      });
    }
    else if (menu instanceof LookupServiceNewAction) {
      ((LookupServiceNewAction) menu).setScoutBundle(getScoutResource());
    }
  }

  private class P_HierarchyListener implements ITypeHierarchyChangedListener {
    @Override
    public void handleEvent(int eventType, IType type) {
      switch (eventType) {
        case PRE_TYPE_REMOVING:
        case POST_TYPE_ADDING:
        case POST_TYPE_CHANGED:
          if (m_serviceHierarchy.isSubtype(iLookupService, type)) {
            markStructureDirty();
          }
          break;
      }
    }
  }
}
