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

import java.util.Set;

import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.operation.ITypeResolver;
import org.eclipse.scout.sdk.operation.util.wellform.WellformServerBundleOperation;
import org.eclipse.scout.sdk.ui.action.IScoutHandler;
import org.eclipse.scout.sdk.ui.action.WellformAction;
import org.eclipse.scout.sdk.ui.action.create.ClassIdNewAction;
import org.eclipse.scout.sdk.ui.action.create.ScoutBundleNewAction;
import org.eclipse.scout.sdk.ui.action.validation.FormDataSqlBindingValidateAction;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.library.LibrariesTablePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.AbstractBundleNodeTablePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.ScoutBundleNode;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.server.service.ServerServicesTablePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.server.service.common.CommonServicesNodePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.server.service.lookup.LookupServiceTablePage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ICachedTypeHierarchy;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeFilters;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;

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
  protected void loadChildrenImpl() {
    super.loadChildrenImpl();

    if (m_serverSessionHierarchy == null) {
      IType iServerSession = TypeUtility.getType(IRuntimeClasses.IServerSession);
      m_serverSessionHierarchy = TypeUtility.getPrimaryTypeHierarchy(iServerSession);
      m_serverSessionHierarchy.addHierarchyListener(getPageDirtyListener());
    }

    try {
      for (IType serverSession : ScoutTypeUtility.getServerSessionTypes(getScoutBundle())) {
        new ServerSessionNodePage(this, serverSession);
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

  protected Set<IType> resolveServices() {
    IType iService = TypeUtility.getType(IRuntimeClasses.IService);
    ICachedTypeHierarchy serviceHierarchy = TypeUtility.getPrimaryTypeHierarchy(iService);
    return serviceHierarchy.getAllSubtypes(iService, ScoutTypeFilters.getTypesInScoutBundles(getScoutBundle()));
  }

  @SuppressWarnings("unchecked")
  @Override
  public Class<? extends IScoutHandler>[] getSupportedMenuActions() {
    return new Class[]{WellformAction.class, FormDataSqlBindingValidateAction.class, ScoutBundleNewAction.class, ClassIdNewAction.class};
  }

  @Override
  public void prepareMenuAction(IScoutHandler menu) {
    if (menu instanceof WellformAction) {
      WellformAction action = (WellformAction) menu;
      action.init(getScoutBundle());
      action.setOperation(new WellformServerBundleOperation(getScoutBundle()));
    }
    else if (menu instanceof FormDataSqlBindingValidateAction) {
      ((FormDataSqlBindingValidateAction) menu).setTyperesolver(new ITypeResolver() {
        @Override
        public Set<IType> getTypes() {
          return resolveServices();
        }
      });
    }
    else if (menu instanceof ScoutBundleNewAction) {
      ((ScoutBundleNewAction) menu).setScoutProject(getScoutBundle());
    }
    else if (menu instanceof ClassIdNewAction) {
      ((ClassIdNewAction) menu).setScoutBundle(getScoutBundle());
    }
  }
}
