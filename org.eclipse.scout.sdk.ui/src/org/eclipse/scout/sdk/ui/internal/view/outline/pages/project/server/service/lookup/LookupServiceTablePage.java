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

import java.util.Set;

import org.eclipse.jdt.core.IType;
import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.operation.ITypeResolver;
import org.eclipse.scout.sdk.ui.action.IScoutHandler;
import org.eclipse.scout.sdk.ui.action.create.LookupServiceNewAction;
import org.eclipse.scout.sdk.ui.action.validation.FormDataSqlBindingValidateAction;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;
import org.eclipse.scout.sdk.util.type.TypeComparators;
import org.eclipse.scout.sdk.util.type.TypeFilters;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ICachedTypeHierarchy;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeFilters;

public class LookupServiceTablePage extends AbstractPage {

  private ICachedTypeHierarchy m_serviceHierarchy;

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

  @Override
  protected void loadChildrenImpl() {
    for (IType service : resolveAllLookupServices()) {
      Set<IType> interfaces = m_serviceHierarchy.getSuperInterfaces(service, TypeFilters.getElementNameFilter("I" + service.getElementName()));
      new LookupServiceNodePage(this, service, CollectionUtility.firstElement(interfaces));
    }
  }

  protected Set<IType> resolveAllLookupServices() {
    IType iLookupService = TypeUtility.getType(IRuntimeClasses.ILookupService);

    if (m_serviceHierarchy == null) {
      m_serviceHierarchy = TypeUtility.getPrimaryTypeHierarchy(iLookupService);
      m_serviceHierarchy.addHierarchyListener(getPageDirtyListener());
    }
    return m_serviceHierarchy.getAllSubtypes(iLookupService, ScoutTypeFilters.getClassesInScoutBundles(getScoutBundle()), TypeComparators.getTypeNameComparator());
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
        public Set<IType> getTypes() {
          return resolveAllLookupServices();
        }
      });
    }
    else if (menu instanceof LookupServiceNewAction) {
      ((LookupServiceNewAction) menu).setScoutBundle(getScoutBundle());
    }
  }
}
