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
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.action.OrganizeAllImportsAction;
import org.eclipse.scout.sdk.ui.action.WizardAction;
import org.eclipse.scout.sdk.ui.action.validation.FormDataSqlBindingValidateAction;
import org.eclipse.scout.sdk.ui.action.validation.ITypeResolver;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.ClientNodePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.server.ServerNodePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.shared.SharedNodePage;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;
import org.eclipse.scout.sdk.ui.view.outline.pages.project.IProjectNodePage;
import org.eclipse.scout.sdk.ui.wizard.bundle.BundleImportWizard;
import org.eclipse.scout.sdk.workspace.IScoutProject;
import org.eclipse.scout.sdk.workspace.IScoutWorkspaceListener;
import org.eclipse.scout.sdk.workspace.ScoutWorkspaceEvent;
import org.eclipse.scout.sdk.workspace.type.ITypeFilter;
import org.eclipse.scout.sdk.workspace.type.TypeFilters;
import org.eclipse.scout.sdk.workspace.typecache.IPrimaryTypeTypeHierarchy;

public class ProjectNodePage extends AbstractPage implements IProjectNodePage {

  private IScoutProject m_scoutProject;
  final IType iService = ScoutSdk.getType(RuntimeClasses.IService);

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
    ScoutSdk.getScoutWorkspace().addWorkspaceListener(m_workspaceListener);
  }

  @Override
  public void unloadPage() {
    ScoutSdk.getScoutWorkspace().removeWorkspaceListener(m_workspaceListener);
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

  @Override
  public void fillContextMenu(IMenuManager manager) {
    super.fillContextMenu(manager);
    manager.add(new OrganizeAllImportsAction(getScoutResource()));
    manager.add(new WizardAction("Import Plugin...", ScoutSdkUi.getImageDescriptor(ScoutSdkUi.SharedBundleAdd), new BundleImportWizard(getScoutResource())));
    manager.add(new Separator());
    manager.add(new FormDataSqlBindingValidateAction(new ITypeResolver() {
      @Override
      public IType[] getTypes() {
        return resolveServices();
      }
    }));
    /*
     * LaunchConfigurationQueryOrder lc=new LaunchConfigurationQueryOrder();
     * lc.setLaunchConfigurationName("google.product");
     * manager.add(new RunAction(lc));
     */
  }

  protected IType[] resolveServices() {
    IPrimaryTypeTypeHierarchy serviceHierarchy = ScoutSdk.getPrimaryTypeHierarchy(iService);
    ITypeFilter filter = TypeFilters.getMultiTypeFilter(TypeFilters.getClassFilter(),
        TypeFilters.getTypesInScoutProject(getScoutResource(), true));
    IType[] services = serviceHierarchy.getAllSubtypes(iService, filter);
    return services;
  }
}
