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

import org.eclipse.scout.sdk.ScoutSdkCore;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.action.IScoutHandler;
import org.eclipse.scout.sdk.ui.action.create.ScoutProjectNewAction;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;
import org.eclipse.scout.sdk.workspace.IScoutWorkspaceListener;
import org.eclipse.scout.sdk.workspace.ScoutWorkspaceEvent;

public class ProjectsTablePage extends AbstractPage {

  private final IScoutWorkspaceListener m_workspaceListener = new IScoutWorkspaceListener() {
    @Override
    public void workspaceChanged(ScoutWorkspaceEvent event) {
      switch (event.getType()) {
        case ScoutWorkspaceEvent.TYPE_BUNDLE_ADDED:
        case ScoutWorkspaceEvent.TYPE_BUNDLE_CHANGED:
        case ScoutWorkspaceEvent.TYPE_BUNDLE_REMOVED:
          markStructureDirty();
          break;
      }
    }
  }; // end IScoutWorkspaceListener

  private final ScoutBundleTreeModel m_uiModel;

  public ProjectsTablePage(IPage parent) {
    setParent(parent);
    setName(Texts.get("RootNodeName"));
    ScoutSdkCore.getScoutWorkspace().addWorkspaceListener(m_workspaceListener);
    m_uiModel = new ScoutBundleTreeModel();
    m_uiModel.build(false);
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
  public void refresh(boolean clearCache) {
    m_uiModel.build(true);
    super.refresh(clearCache);
  }

  @Override
  public void loadChildrenImpl() {
    for (ScoutBundleNodeGroup g : m_uiModel.getRoots()) {
      new BundleNodeGroupTablePage(this, g);
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public Class<? extends IScoutHandler>[] getSupportedMenuActions() {
    return new Class[]{ScoutProjectNewAction.class};
  }

  @Override
  public boolean isFolder() {
    return true;
  }
}
