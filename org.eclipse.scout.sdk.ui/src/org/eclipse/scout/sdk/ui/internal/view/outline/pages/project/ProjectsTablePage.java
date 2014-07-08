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

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.core.IType;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.scout.sdk.ScoutSdkCore;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.operation.ITypeResolver;
import org.eclipse.scout.sdk.operation.util.wellform.WellformClientBundleOperation;
import org.eclipse.scout.sdk.ui.action.IScoutHandler;
import org.eclipse.scout.sdk.ui.action.WellformAction;
import org.eclipse.scout.sdk.ui.action.create.ScoutProjectNewAction;
import org.eclipse.scout.sdk.ui.action.dto.TypeResolverFormDataAction;
import org.eclipse.scout.sdk.ui.action.dto.TypeResolverPageDataAction;
import org.eclipse.scout.sdk.ui.extensions.bundle.ScoutBundleExtensionPoint;
import org.eclipse.scout.sdk.ui.extensions.bundle.ScoutBundleUiExtension;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.internal.view.outline.ScoutExplorerSettingsSupport;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ICachedTypeHierarchy;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.IScoutWorkspaceListener;
import org.eclipse.scout.sdk.workspace.ScoutBundleComparators;
import org.eclipse.scout.sdk.workspace.ScoutBundleFilters;
import org.eclipse.scout.sdk.workspace.ScoutWorkspaceEvent;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.PlatformUI;

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
          ScoutExplorerSettingsSupport.PREF_SHOW_BINARY_BUNDLES_KEY.equals(event.getProperty()) ||
          ScoutExplorerSettingsSupport.PREF_HIDDEN_BUNDLES_TYPES.equals(event.getProperty())) {
        markStructureDirty();
      }
      else if (ScoutExplorerSettingsSupport.PREF_HIDDEN_WORKING_SETS.equals(event.getProperty()) ||
          ScoutExplorerSettingsSupport.PREF_WORKING_SETS_ORDER.equals(event.getProperty())) {
        if (ScoutExplorerSettingsSupport.BundlePresentation.WORKING_SET.equals(ScoutExplorerSettingsSupport.get().getBundlePresentation())) {
          markStructureDirty();
        }
      }
    }
  }; // end IPropertyChangeListener

  private final IPropertyChangeListener m_workingSetConfigChangeListener = new IPropertyChangeListener() {
    @Override
    public void propertyChange(PropertyChangeEvent event) {
      if (ScoutExplorerSettingsSupport.BundlePresentation.WORKING_SET.equals(ScoutExplorerSettingsSupport.get().getBundlePresentation())) {
        markStructureDirty();
      }
    }
  }; // end IPropertyChangeListener

  public ProjectsTablePage(IPage parent) {
    setParent(parent);
    setName(Texts.get("RootNodeName"));
    ScoutSdkCore.getScoutWorkspace().addWorkspaceListener(m_workspaceListener);
    ScoutSdkUi.getDefault().getPreferenceStore().addPropertyChangeListener(m_explorerConfigChangeListener);
    PlatformUI.getWorkbench().getWorkingSetManager().addPropertyChangeListener(m_workingSetConfigChangeListener);
  }

  @Override
  public void unloadPage() {
    ScoutSdkCore.getScoutWorkspace().removeWorkspaceListener(m_workspaceListener);
    ScoutSdkUi.getDefault().getPreferenceStore().removePropertyChangeListener(m_explorerConfigChangeListener);
    PlatformUI.getWorkbench().getWorkingSetManager().removePropertyChangeListener(m_workingSetConfigChangeListener);
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
  @SuppressWarnings("restriction")
  public void refresh(boolean clearCache) {
    if (clearCache) {
      org.eclipse.scout.sdk.internal.workspace.ScoutWorkspace.getInstance().rebuildGraph();
      // here the graph is rebuilt asynchronously. on completion the table page is refreshed because m_workspaceListener is fired.
    }
    else {
      super.refresh(clearCache);
    }
  }

  @Override
  protected void loadChildrenImpl() {
    if (ScoutExplorerSettingsSupport.BundlePresentation.FLAT.equals(ScoutExplorerSettingsSupport.get().getBundlePresentation())) {
      // flat display
      Set<IScoutBundle> allBundles = ScoutSdkCore.getScoutWorkspace().getBundleGraph().getBundles(ScoutExplorerSettingsBundleFilter.get(), ScoutBundleComparators.getSymbolicNameAscComparator());
      for (IScoutBundle b : allBundles) {
        createBundlePage(this, b);
      }
    }
    else if (ScoutExplorerSettingsSupport.BundlePresentation.HIERARCHICAL.equals(ScoutExplorerSettingsSupport.get().getBundlePresentation())) {
      // hierarchical display
      for (IScoutBundle root : ScoutSdkCore.getScoutWorkspace().getBundleGraph().getBundles(
          ScoutBundleFilters.getFilteredRootBundlesFilter(ScoutExplorerSettingsBundleFilter.get()), ScoutBundleComparators.getSymbolicNameAscComparator())) {
        createBundlePage(this, root);
      }
    }
    else if (ScoutExplorerSettingsSupport.BundlePresentation.WORKING_SET.equals(ScoutExplorerSettingsSupport.get().getBundlePresentation())) {
      // show working sets
      for (IWorkingSet ws : ScoutExplorerSettingsSupport.get().getScoutWorkingSets(false)) {
        new ScoutWorkingSetTablePage(this, ws);
      }
    }
    else {
      ScoutBundleTreeModel uiModel = new ScoutBundleTreeModel();
      uiModel.build();
      if (ScoutExplorerSettingsSupport.BundlePresentation.FLAT_GROUPS.equals(ScoutExplorerSettingsSupport.get().getBundlePresentation())) {
        // flat grouped
        HashSet<ScoutBundleNodeGroup> collector = new HashSet<ScoutBundleNodeGroup>();
        collectAllBundleGroupsRec(collector, uiModel.getRoots());
        for (ScoutBundleNodeGroup g : collector) {
          new BundleNodeGroupTablePage(this, g);
        }
      }
      else {
        // grouped display
        for (ScoutBundleNodeGroup g : uiModel.getRoots()) {
          new BundleNodeGroupTablePage(this, g);
        }
      }
    }
  }

  private void collectAllBundleGroupsRec(Set<ScoutBundleNodeGroup> collector, ScoutBundleNodeGroup[] groups) {
    for (ScoutBundleNodeGroup g : groups) {
      collector.add(g);
      collectAllBundleGroupsRec(collector, g.getChildGroups().toArray(new ScoutBundleNodeGroup[g.getChildGroups().size()]));
    }
  }

  public static void createBundlePage(IPage parentPage, IScoutBundle b) {
    if (b != null) {
      ScoutBundleUiExtension childExt = ScoutBundleExtensionPoint.getExtension(b.getType());
      if (childExt != null) {
        ScoutBundleNode rootNode = new ScoutBundleNode(b, childExt);
        rootNode.createBundlePage(parentPage);
      }
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public Class<? extends IScoutHandler>[] getSupportedMenuActions() {
    return new Class[]{ScoutProjectNewAction.class, TypeResolverFormDataAction.class, TypeResolverPageDataAction.class, WellformAction.class};
  }

  @Override
  public void prepareMenuAction(IScoutHandler menu) {
    super.prepareMenuAction(menu);
    if (menu instanceof TypeResolverFormDataAction) {
      ((TypeResolverFormDataAction) menu).init(new ITypeResolver() {
        @Override
        public Set<IType> getTypes() {
          IType iForm = TypeUtility.getType(IRuntimeClasses.IForm);
          ICachedTypeHierarchy formHierarchy = TypeUtility.getPrimaryTypeHierarchy(iForm);
          return formHierarchy.getAllSubtypes(iForm);
        }
      }, null);
    }
    else if (menu instanceof TypeResolverPageDataAction) {
      ((TypeResolverPageDataAction) menu).init(new ITypeResolver() {
        @Override
        public Set<IType> getTypes() {
          IType iPageWithTable = TypeUtility.getType(IRuntimeClasses.IPageWithTable);
          ICachedTypeHierarchy pageWithTableHierarchy = TypeUtility.getPrimaryTypeHierarchy(iPageWithTable);
          return pageWithTableHierarchy.getAllSubtypes(iPageWithTable);
        }
      }, getScoutBundle());
    }
    else if (menu instanceof WellformAction) {
      WellformAction action = (WellformAction) menu;
      Set<IScoutBundle> clients = ScoutSdkCore.getScoutWorkspace().getBundleGraph().getBundles(ScoutBundleFilters.getBundlesOfTypeFilter(IScoutBundle.TYPE_CLIENT));
      action.setOperation(new WellformClientBundleOperation(clients));
      action.init(getScoutBundle());
    }
  }

  @Override
  public boolean isFolder() {
    return true;
  }
}
