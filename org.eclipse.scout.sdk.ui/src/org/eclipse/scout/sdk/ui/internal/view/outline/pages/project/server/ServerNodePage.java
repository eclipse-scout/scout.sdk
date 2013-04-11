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
package org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.server;

import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.operation.util.wellform.WellformServerBundleOperation;
import org.eclipse.scout.sdk.ui.action.IScoutHandler;
import org.eclipse.scout.sdk.ui.action.WellformAction;
import org.eclipse.scout.sdk.ui.action.create.ScoutBundleNewAction;
import org.eclipse.scout.sdk.ui.action.validation.FormDataSqlBindingValidateAction;
import org.eclipse.scout.sdk.ui.action.validation.ITypeResolver;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.library.LibrariesTablePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.AbstractBundleNodeTablePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.ScoutBundleNode;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.server.service.ServerServicesTablePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.server.service.common.CommonServicesNodePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.server.service.lookup.LookupServiceTablePage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;
import org.eclipse.scout.sdk.util.type.ITypeFilter;
import org.eclipse.scout.sdk.util.type.TypeComparators;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ICachedTypeHierarchy;
import org.eclipse.scout.sdk.util.typecache.IPrimaryTypeTypeHierarchy;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeFilters;

/**
 * <h3>ServerNodePage</h3> ...
 */
public class ServerNodePage extends AbstractBundleNodeTablePage {
  private ICachedTypeHierarchy m_serverSessionHierarchy;

  public ServerNodePage(IPage parent, ScoutBundleNode node) {
    super(parent, node);
  }

  @Override
  public String getPageId() {
    return IScoutPageConstants.SERVER_NODE_PAGE;
  }

  @Override
  public void unloadPage() {
    if (m_serverSessionHierarchy != null) {
      m_serverSessionHierarchy.removeHierarchyListener(getPageDirtyListener());
      m_serverSessionHierarchy = null;
    }
  }

  @Override
  public void loadChildrenImpl() {
    super.loadChildrenImpl();

    IType iServerSession = TypeUtility.getType(RuntimeClasses.IServerSession);

    if (m_serverSessionHierarchy == null) {
      m_serverSessionHierarchy = TypeUtility.getPrimaryTypeHierarchy(iServerSession);
      m_serverSessionHierarchy.addHierarchyListener(getPageDirtyListener());
    }

    try {
      ITypeFilter filter = ScoutTypeFilters.getTypesInScoutBundles(getScoutBundle());
      IType[] serverSessions = m_serverSessionHierarchy.getAllSubtypes(iServerSession, filter, TypeComparators.getTypeNameComparator());
      if (serverSessions.length > 1) {
        ScoutSdkUi.logError("The server bundle '" + getScoutBundle().getSymbolicName() + "' can have in maximum 1 server session.");
      }
      else if (serverSessions.length == 1) {
        new ServerSessionNodePage(this, serverSessions[0]);
      }
    }
    catch (Exception e) {
      ScoutSdkUi.logWarning("Error occured during loading '" + ServerSessionNodePage.class.getSimpleName() + "' node in bundle '" + getScoutBundle().getSymbolicName() + "'.", e);
    }
    try {
      new ServerServicesTablePage(this);
    }
    catch (Exception e) {
      ScoutSdkUi.logWarning("Error occured during loading '" + ServerServicesTablePage.class.getSimpleName() + "' node in bundle '" + getScoutBundle().getSymbolicName() + "'.", e);
    }
    try {
      new LookupServiceTablePage(this);
    }
    catch (Exception e) {
      ScoutSdkUi.logWarning("Error occured during loading '" + LookupServiceTablePage.class.getSimpleName() + "' node in bundle '" + getScoutBundle().getSymbolicName() + "'.", e);
    }
    try {
      new CommonServicesNodePage(this);
    }
    catch (Exception e) {
      ScoutSdkUi.logWarning("Error occured during loading '" + CommonServicesNodePage.class.getSimpleName() + "' node in bundle '" + getScoutBundle().getSymbolicName() + "'.", e);
    }
    try {
      new LibrariesTablePage(this, getScoutBundle());
    }
    catch (Exception e) {
      ScoutSdkUi.logWarning("Error occured while loading '" + LibrariesTablePage.class.getSimpleName() + "' node in bundle '" + getScoutBundle().getSymbolicName() + "'.", e);
    }
  }

  protected IType[] resolveServices() {
    IType iService = TypeUtility.getType(RuntimeClasses.IService);
    IPrimaryTypeTypeHierarchy serviceHierarchy = TypeUtility.getPrimaryTypeHierarchy(iService);
    IType[] services = serviceHierarchy.getAllSubtypes(iService, ScoutTypeFilters.getTypesInScoutBundles(getScoutBundle()));
    return services;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Class<? extends IScoutHandler>[] getSupportedMenuActions() {
    return new Class[]{WellformAction.class, FormDataSqlBindingValidateAction.class, ScoutBundleNewAction.class};
  }

  @Override
  public void prepareMenuAction(IScoutHandler menu) {
    if (menu instanceof WellformAction) {
      WellformAction action = (WellformAction) menu;
      action.setScoutBundle(getScoutBundle());
      action.setOperation(new WellformServerBundleOperation(getScoutBundle()));
    }
    else if (menu instanceof FormDataSqlBindingValidateAction) {
      ((FormDataSqlBindingValidateAction) menu).setTyperesolver(new ITypeResolver() {
        @Override
        public IType[] getTypes() {
          return resolveServices();
        }
      });
    }
    else if (menu instanceof ScoutBundleNewAction) {
      ((ScoutBundleNewAction) menu).setScoutProject(getScoutBundle());
    }
  }
}
