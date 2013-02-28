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
package org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.server.service;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.ui.action.IScoutHandler;
import org.eclipse.scout.sdk.ui.action.create.ProcessServiceNewAction;
import org.eclipse.scout.sdk.ui.action.validation.FormDataSqlBindingValidateAction;
import org.eclipse.scout.sdk.ui.action.validation.ITypeResolver;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;
import org.eclipse.scout.sdk.util.type.TypeComparators;
import org.eclipse.scout.sdk.util.type.TypeFilters;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ICachedTypeHierarchy;
import org.eclipse.scout.sdk.workspace.IScoutBundle;

public class ServerServicesTablePage extends AbstractPage {

  private final IType iService = TypeUtility.getType(RuntimeClasses.IService);

  private final IType iSqlService = TypeUtility.getType(RuntimeClasses.ISqlService);
  private final IType iBookmarkStorageService = TypeUtility.getType(RuntimeClasses.IBookmarkStorageService);
  private final IType iCalendarService = TypeUtility.getType(RuntimeClasses.ICalendarService);
  private final IType iSMTPService = TypeUtility.getType(RuntimeClasses.ISMTPService);
  private final IType iAccessControlService = TypeUtility.getType(RuntimeClasses.IAccessControlService);
  private final IType iLookupService = TypeUtility.getType(RuntimeClasses.ILookupService);

  private ICachedTypeHierarchy m_serviceHierarchy;

  public ServerServicesTablePage(AbstractPage parent) {
    setParent(parent);
    setImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Services));
    setName(Texts.get("ServerServicesNodePage"));
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
    return IScoutPageConstants.SERVER_SERVICE_TABLE_PAGE;
  }

  @Override
  public boolean isFolder() {
    return true;
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
    for (IType service : resolveServices()) {
      IType serviceInterface = null;
      IType[] interfaces = m_serviceHierarchy.getSuperInterfaces(service, TypeFilters.getElementNameFilter("I" + service.getElementName()));
      if (interfaces.length > 0) {
        serviceInterface = interfaces[0];
      }
      new ServerServicesNodePage(this, service, serviceInterface);
    }
  }

  protected IType[] resolveServices() {
    if (m_serviceHierarchy == null) {
      m_serviceHierarchy = TypeUtility.getPrimaryTypeHierarchy(iService);
      m_serviceHierarchy.addHierarchyListener(getPageDirtyListener());
    }

    IJavaProject javaProject = getScoutResource().getJavaProject();
    IType[] sqlServices = m_serviceHierarchy.getAllSubtypes(iSqlService, TypeFilters.getClassesInProject(javaProject));
    IType[] bookmarkServices = m_serviceHierarchy.getAllSubtypes(iBookmarkStorageService, TypeFilters.getClassesInProject(javaProject));
    IType[] calendarServices = m_serviceHierarchy.getAllSubtypes(iCalendarService, TypeFilters.getClassesInProject(javaProject));
    IType[] smtpServices = m_serviceHierarchy.getAllSubtypes(iSMTPService, TypeFilters.getClassesInProject(javaProject));
    IType[] accessControlServices = m_serviceHierarchy.getAllSubtypes(iAccessControlService, TypeFilters.getClassesInProject(javaProject));
    IType[] lookupServices = m_serviceHierarchy.getAllSubtypes(iLookupService, TypeFilters.getClassesInProject(javaProject));

    IType[] services = m_serviceHierarchy.getAllSubtypes(iService,
        TypeFilters.getMultiTypeFilter(
            TypeFilters.getClassesInProject(javaProject),
            TypeFilters.getNotInTypes(sqlServices, bookmarkServices, calendarServices, smtpServices, accessControlServices, lookupServices)
            ), TypeComparators.getTypeNameComparator());

    return services;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Class<? extends IScoutHandler>[] getSupportedMenuActions() {
    return new Class[]{FormDataSqlBindingValidateAction.class, ProcessServiceNewAction.class};
  }

  @Override
  public void prepareMenuAction(IScoutHandler menu) {
    if (menu instanceof FormDataSqlBindingValidateAction) {
      ((FormDataSqlBindingValidateAction) menu).setTyperesolver(new ITypeResolver() {
        @Override
        public IType[] getTypes() {
          return resolveServices();
        }
      });
    }
    else if (menu instanceof ProcessServiceNewAction) {
      ((ProcessServiceNewAction) menu).setScoutBundle(getScoutResource());
    }
  }
}
