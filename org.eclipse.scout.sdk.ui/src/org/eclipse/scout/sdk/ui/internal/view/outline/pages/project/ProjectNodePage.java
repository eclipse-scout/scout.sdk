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
package org.eclipse.scout.sdk.ui.internal.view.outline.pages.project;

import org.eclipse.jdt.core.IType;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.ScoutSdkCore;
import org.eclipse.scout.sdk.operation.form.formdata.ScoutProjectUpdateFormDataOperation;
import org.eclipse.scout.sdk.operation.util.wellform.WellformScoutProjectOperation;
import org.eclipse.scout.sdk.ui.action.FormDataUpdateAction;
import org.eclipse.scout.sdk.ui.action.IScoutHandler;
import org.eclipse.scout.sdk.ui.action.ImportPluginAction;
import org.eclipse.scout.sdk.ui.action.OrganizeAllImportsAction;
import org.eclipse.scout.sdk.ui.action.WellformAction;
import org.eclipse.scout.sdk.ui.action.export.ExportEarAction;
import org.eclipse.scout.sdk.ui.action.validation.FormDataSqlBindingValidateAction;
import org.eclipse.scout.sdk.ui.action.validation.ITypeResolver;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.ClientNodePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.server.ServerNodePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.shared.SharedNodePage;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;
import org.eclipse.scout.sdk.ui.view.outline.pages.project.IProjectNodePage;
import org.eclipse.scout.sdk.util.type.ITypeFilter;
import org.eclipse.scout.sdk.util.type.TypeFilters;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IPrimaryTypeTypeHierarchy;
import org.eclipse.scout.sdk.workspace.IScoutProject;
import org.eclipse.scout.sdk.workspace.IScoutWorkspaceListener;
import org.eclipse.scout.sdk.workspace.ScoutWorkspaceEvent;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeFilters;

public class ProjectNodePage extends AbstractPage implements IProjectNodePage {

  private IScoutProject m_scoutProject;
  final IType iService = TypeUtility.getType(RuntimeClasses.IService);

  private IScoutWorkspaceListener m_workspaceListener = new IScoutWorkspaceListener() {
    @Override
    public void worspaceChanged(ScoutWorkspaceEvent event) {
      if (event.getType() == ScoutWorkspaceEvent.TYPE_PROJECT_CHANGED) {
        if (CompareUtility.equals(event.getScoutElement(), m_scoutProject)) {
          markStructureDirty();
        }
      }
    }
  }; // end IScoutWorkspaceListener

  public ProjectNodePage(IPage parent, IScoutProject p) {
    setParent(parent);
    m_scoutProject = p;
    setName(p.getProjectName());
    setImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.ScoutProject));
    ScoutSdkCore.getScoutWorkspace().addWorkspaceListener(m_workspaceListener);
  }

  @Override
  public void unloadPage() {
    ScoutSdkCore.getScoutWorkspace().removeWorkspaceListener(m_workspaceListener);
  }

  @Override
  public String getPageId() {
    return IScoutPageConstants.PROJECT_NODE_PAGE;
  }

  @Override
  public boolean isFolder() {
    return true;
  }

  @Override
  public boolean isInitiallyLoaded() {
    return true;
  }

  @Override
  public IScoutProject getScoutResource() {
    return m_scoutProject;
  }

  @Override
  public void loadChildrenImpl() {
    // ui swing
    try {
      if (getScoutResource().getUiSwingBundle() != null) {
        new UiSwingNodePage(this, getScoutResource().getUiSwingBundle());
      }
    }
    catch (Exception e) {
      ScoutSdkUi.logWarning("Error during creating node page for UI swing bundle in projct '" + getScoutResource().getProjectName() + "'.", e);
    }
    // ui swt
    try {
      if (getScoutResource().getUiSwtBundle() != null) {
        new UiSwtNodePage(this, getScoutResource().getUiSwtBundle());
      }
    }
    catch (Exception e) {
      ScoutSdkUi.logWarning("Error during creating node page for UI SWT bundle in projct '" + getScoutResource().getProjectName() + "'.", e);
    }
    // client
    try {
      if (getScoutResource().getClientBundle() != null) {
        new ClientNodePage(this, getScoutResource().getClientBundle());
      }
    }
    catch (Exception e) {
      ScoutSdkUi.logWarning("Error during creating node page for Client Bundle in projct '" + getScoutResource().getProjectName() + "'.", e);
    }
    // shared
    try {
      if (getScoutResource().getSharedBundle() != null) {
        new SharedNodePage(this, getScoutResource().getSharedBundle());
      }
    }
    catch (Exception e) {
      ScoutSdkUi.logWarning("Error during creating node page for Shared Bundle in projct '" + getScoutResource().getProjectName() + "'.", e);
    }
    // server
    try {
      if (getScoutResource().getServerBundle() != null) {
        new ServerNodePage(this, getScoutResource().getServerBundle());
      }
    }
    catch (Exception e) {
      ScoutSdkUi.logWarning("Error during creating node page for Server Bundle in projct '" + getScoutResource().getProjectName() + "'.", e);
    }
    // sub projects
    for (IScoutProject subProject : getScoutResource().getSubProjects()) {
      try {
        new ProjectNodePage(this, subProject);
      }
      catch (Exception e) {
        ScoutSdkUi.logWarning("Error during creating node page for Project '" + subProject.getProjectName() + "'.", e);
      }
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public Class<? extends IScoutHandler>[] getSupportedMenuActions() {
    return new Class[]{ImportPluginAction.class, OrganizeAllImportsAction.class, WellformAction.class,
        FormDataUpdateAction.class, FormDataSqlBindingValidateAction.class/*, ExportEarAction.class*/};
  }

  @Override
  public void prepareMenuAction(IScoutHandler menu) {
    if (menu instanceof ImportPluginAction) {
      ((ImportPluginAction) menu).setScoutProject(getScoutResource());
    }
    else if (menu instanceof OrganizeAllImportsAction) {
      ((OrganizeAllImportsAction) menu).setScoutProject(getScoutResource());
    }
    else if (menu instanceof WellformAction) {
      ((WellformAction) menu).setOperation(new WellformScoutProjectOperation(getScoutResource()));
    }
    else if (menu instanceof FormDataUpdateAction) {
      ((FormDataUpdateAction) menu).setOperation(new ScoutProjectUpdateFormDataOperation(getScoutResource()));
    }
    else if (menu instanceof FormDataSqlBindingValidateAction) {
      ((FormDataSqlBindingValidateAction) menu).setTyperesolver(new ITypeResolver() {
        @Override
        public IType[] getTypes() {
          return resolveServices();
        }
      });
    }
    else if (menu instanceof ExportEarAction) {
      ((ExportEarAction) menu).setScoutProject(getScoutResource());
    }
  }

  protected IType[] resolveServices() {
    IPrimaryTypeTypeHierarchy serviceHierarchy = TypeUtility.getPrimaryTypeHierarchy(iService);
    ITypeFilter filter = TypeFilters.getMultiTypeFilter(TypeFilters.getClassFilter(),
        ScoutTypeFilters.getTypesInScoutProject(getScoutResource(), true));
    IType[] services = serviceHierarchy.getAllSubtypes(iService, filter);
    return services;
  }
}
