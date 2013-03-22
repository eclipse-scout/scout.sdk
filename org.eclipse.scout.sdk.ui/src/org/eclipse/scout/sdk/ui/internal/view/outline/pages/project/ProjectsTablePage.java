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

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.scout.sdk.ScoutSdkCore;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.internal.workspace.ScoutWorkspace;
import org.eclipse.scout.sdk.ui.action.IScoutHandler;
import org.eclipse.scout.sdk.ui.action.create.ScoutProjectNewAction;
import org.eclipse.scout.sdk.ui.extensions.bundle.ScoutBundleUiExtension;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.internal.extensions.bundle.ScoutBundleExtensionPoint;
import org.eclipse.scout.sdk.ui.internal.view.outline.ScoutExplorerSettingsSupport;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.IScoutWorkspaceListener;
import org.eclipse.scout.sdk.workspace.ScoutBundleFilters;
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

  private final IPropertyChangeListener m_explorerConfigChangeListener = new IPropertyChangeListener() {
    @Override
    public void propertyChange(PropertyChangeEvent event) {
      if (ScoutExplorerSettingsSupport.PREF_BUNDLE_DISPLAY_STYLE_KEY.equals(event.getProperty()) ||
          ScoutExplorerSettingsSupport.PREF_SHOW_FRAGMENTS_KEY.equals(event.getProperty()) ||
          ScoutExplorerSettingsSupport.PREF_SHOW_BINARY_BUNDLES_KEY.equals(event.getProperty())) {
        markStructureDirty();
      }
    }
  }; // end IPropertyChangeListener

  public ProjectsTablePage(IPage parent) {
    setParent(parent);
    setName(Texts.get("RootNodeName"));
    ScoutSdkCore.getScoutWorkspace().addWorkspaceListener(m_workspaceListener);
    ScoutSdkUi.getDefault().getPreferenceStore().addPropertyChangeListener(m_explorerConfigChangeListener);
  }

  @Override
  public void unloadPage() {
    ScoutSdkCore.getScoutWorkspace().removeWorkspaceListener(m_workspaceListener);
    ScoutSdkUi.getDefault().getPreferenceStore().removePropertyChangeListener(m_explorerConfigChangeListener);
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
    if (clearCache) {
      ScoutWorkspace.getInstance().rebuildGraph();
    }
    super.refresh(clearCache);
  }

  @Override
  public void loadChildrenImpl() {
    if (ScoutExplorerSettingsSupport.BundlePresentation.Flat.equals(ScoutExplorerSettingsSupport.get().getBundlePresentation())) {
      // flat display
      IScoutBundle[] allBundles = ScoutSdkCore.getScoutWorkspace().getBundleGraph().getBundles(ScoutExplorerSettingsBundleFilter.get());
      for (IScoutBundle b : allBundles) {
        createBundlePage(b);
      }
    }
    else if (ScoutExplorerSettingsSupport.BundlePresentation.Hierarchical.equals(ScoutExplorerSettingsSupport.get().getBundlePresentation())) {
      // hierarchical display
      for (IScoutBundle root : ScoutSdkCore.getScoutWorkspace().getBundleGraph().getBundles(
          ScoutBundleFilters.getFilteredRootBundlesFilter(ScoutExplorerSettingsBundleFilter.get()))) {
        createBundlePage(root);
      }
    }
    else {
      // grouped display
      ScoutBundleTreeModel uiModel = new ScoutBundleTreeModel();
      uiModel.build();
      for (ScoutBundleNodeGroup g : uiModel.getRoots()) {
        new BundleNodeGroupTablePage(this, g);
      }
    }
  }

  private void createBundlePage(IScoutBundle b) {
    ScoutBundleUiExtension childExt = ScoutBundleExtensionPoint.getExtension(b.getType());
    if (childExt != null) {
      ScoutBundleNode rootNode = new ScoutBundleNode(b, childExt);
      rootNode.createBundlePage(this);
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
