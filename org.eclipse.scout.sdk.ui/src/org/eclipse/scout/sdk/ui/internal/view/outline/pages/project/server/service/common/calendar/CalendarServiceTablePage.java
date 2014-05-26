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
package org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.server.service.common.calendar;

import java.util.Set;

import org.eclipse.jdt.core.IType;
import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.operation.ITypeResolver;
import org.eclipse.scout.sdk.ui.action.IScoutHandler;
import org.eclipse.scout.sdk.ui.action.create.CalendarServiceNewAction;
import org.eclipse.scout.sdk.ui.action.validation.FormDataSqlBindingValidateAction;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;
import org.eclipse.scout.sdk.util.type.TypeComparators;
import org.eclipse.scout.sdk.util.type.TypeFilters;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ICachedTypeHierarchy;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeFilters;

/**
 * <h3>CalendarServiceTablePage</h3> ...
 */
public class CalendarServiceTablePage extends AbstractPage {

  private ICachedTypeHierarchy m_serviceHierarchy;

  public CalendarServiceTablePage(AbstractPage parent) {
    setParent(parent);
    setName(Texts.get("CalendarServices"));
    setImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Services));
  }

  @Override
  public void unloadPage() {
    if (m_serviceHierarchy != null) {
      m_serviceHierarchy.removeHierarchyListener(getPageDirtyListener());
      m_serviceHierarchy = null;
    }
    super.unloadPage();
  }

  @Override
  public void refresh(boolean clearCache) {
    if (clearCache && m_serviceHierarchy != null) {
      m_serviceHierarchy.invalidate();
    }
    super.refresh(clearCache);
  }

  @Override
  public String getPageId() {
    return IScoutPageConstants.CALENDAR_SERVICE_TABLE_PAGE;
  }

  @Override
  public boolean isFolder() {
    return true;
  }

  @Override
  protected void loadChildrenImpl() {
    for (IType service : resolveServices()) {
      Set<IType> interfaces = m_serviceHierarchy.getSuperInterfaces(service, TypeFilters.getElementNameFilter("I" + service.getElementName()));
      new CalendarServiceNodePage(this, service, CollectionUtility.firstElement(interfaces));
    }
  }

  protected Set<IType> resolveServices() {
    IType iCalendarService = TypeUtility.getType(IRuntimeClasses.ICalendarService);
    if (m_serviceHierarchy == null) {
      m_serviceHierarchy = TypeUtility.getPrimaryTypeHierarchy(iCalendarService);
      m_serviceHierarchy.addHierarchyListener(getPageDirtyListener());
    }
    return m_serviceHierarchy.getAllSubtypes(iCalendarService, ScoutTypeFilters.getTypesInScoutBundles(getScoutBundle()), TypeComparators.getTypeNameComparator());
  }

  @SuppressWarnings("unchecked")
  @Override
  public Class<? extends IScoutHandler>[] getSupportedMenuActions() {
    return new Class[]{FormDataSqlBindingValidateAction.class, CalendarServiceNewAction.class};
  }

  @Override
  public void prepareMenuAction(IScoutHandler menu) {
    if (menu instanceof FormDataSqlBindingValidateAction) {
      ((FormDataSqlBindingValidateAction) menu).setTyperesolver(new ITypeResolver() {
        @Override
        public Set<IType> getTypes() {
          return resolveServices();
        }
      });
    }
    else if (menu instanceof CalendarServiceNewAction) {
      ((CalendarServiceNewAction) menu).setScoutBundle(getScoutBundle());
    }
  }
}
