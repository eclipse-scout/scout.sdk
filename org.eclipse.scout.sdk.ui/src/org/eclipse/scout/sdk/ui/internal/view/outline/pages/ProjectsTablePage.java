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
package org.eclipse.scout.sdk.ui.internal.view.outline.pages;

import org.eclipse.scout.sdk.ScoutSdkCore;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.action.IScoutHandler;
import org.eclipse.scout.sdk.ui.action.ImportPluginAction;
import org.eclipse.scout.sdk.ui.action.create.ScoutProjectNewAction;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.ProjectNodePage;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;
import org.eclipse.scout.sdk.workspace.IScoutProject;
import org.eclipse.scout.sdk.workspace.IScoutWorkspaceListener;
import org.eclipse.scout.sdk.workspace.ScoutWorkspaceEvent;

public class ProjectsTablePage extends AbstractPage {

  private IScoutWorkspaceListener m_workspaceListener = new IScoutWorkspaceListener() {
    @Override
    public void worspaceChanged(ScoutWorkspaceEvent event) {
      switch (event.getType()) {
        case ScoutWorkspaceEvent.TYPE_PROJECT_ADDED:
                case ScoutWorkspaceEvent.TYPE_PROJECT_REMOVED:
                case ScoutWorkspaceEvent.TYPE_PROJECT_CHANGED:
                  markStructureDirty();
                  break;
              }
            }
  }; // end IScoutWorkspaceListener

  public ProjectsTablePage(IPage parent) {
    setParent(parent);
    setName(Texts.get("RootNodeName"));
    ScoutSdkCore.getScoutWorkspace().addWorkspaceListener(m_workspaceListener);
  }

  @Override
  public void unloadPage() {
    ScoutSdkCore.getScoutWorkspace().removeWorkspaceListener(m_workspaceListener);
  }

  @Override
  public String getPageId() {
    return IScoutPageConstants.PROJECT_TABLE_PAGE;
  }

  @Override
  public boolean isInitiallyLoaded() {
    return true;
  }

  @Override
  public void loadChildrenImpl() {
    IScoutProject[] rootProjects = ScoutSdkCore.getScoutWorkspace().getRootProjects();
    ScoutSdkUi.logInfo("INVISIBLE ROOT NODE: found " + rootProjects.length + " root projects.");
    for (IScoutProject group : rootProjects) {
      new ProjectNodePage(this, group);
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public Class<? extends IScoutHandler>[] getSupportedMenuActions() {
    return new Class[]{ScoutProjectNewAction.class, ImportPluginAction.class};
  }

  @Override
  public boolean isFolder() {
    return true;
  }
}
