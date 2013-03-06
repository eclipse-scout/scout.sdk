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
package org.eclipse.scout.sdk.ui.internal.view.outline.pages.library;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.pde.core.plugin.IPluginImport;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.core.plugin.PluginRegistry;
import org.eclipse.pde.internal.core.IPluginModelListener;
import org.eclipse.pde.internal.core.PDECore;
import org.eclipse.pde.internal.core.PluginModelDelta;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.ui.action.IScoutHandler;
import org.eclipse.scout.sdk.ui.action.library.LibraryBundleLinkAction;
import org.eclipse.scout.sdk.ui.action.library.LibraryBundleNewAction;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;
import org.eclipse.scout.sdk.util.pde.PluginModelHelper;
import org.eclipse.scout.sdk.workspace.IScoutBundle;

/**
 * <h3>{@link LibrariesTablePage}</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 3.8.0 28.02.2012
 */
@SuppressWarnings("restriction")
public class LibrariesTablePage extends AbstractPage {

  private org.eclipse.scout.sdk.ui.internal.view.outline.pages.library.LibrariesTablePage.P_PluginModelListener m_dirtyListener;
  private final IScoutBundle m_ownerBundle;

  public LibrariesTablePage(IPage parent) {
    this(parent, null);
  }

  public LibrariesTablePage(IPage parent, IScoutBundle ownerBundle) {
    m_ownerBundle = ownerBundle;
    setParent(parent);
    setName(Texts.get("Libraries"));
    setImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Libraries));
  }

  @Override
  public boolean isFolder() {
    return true;
  }

  @Override
  public String getPageId() {
    return IScoutPageConstants.LIBRARIES_TABLE_PAGE;
  }

  @Override
  public void unloadPage() {
    super.unloadPage();
    if (m_dirtyListener != null) {
      PDECore.getDefault().getModelManager().removePluginModelListener(m_dirtyListener);
    }
  }

  @Override
  public IScoutBundle getScoutBundle() {
    return m_ownerBundle;
  }

  @Override
  protected void loadChildrenImpl() {
    if (m_dirtyListener == null) {
      m_dirtyListener = new P_PluginModelListener();
      PDECore.getDefault().getModelManager().addPluginModelListener(m_dirtyListener);
    }
    try {
      // find library projects
      List<IPluginModelBase> libraries = new ArrayList<IPluginModelBase>(3);
      if (getScoutBundle() != null) {
        PluginModelHelper helper = new PluginModelHelper(getScoutBundle().getProject());
        IPluginImport[] allDependencies = helper.Manifest.getAllDependencies();
        Set<String> dependencyIds = new HashSet<String>(allDependencies.length);
        for (IPluginImport dependency : allDependencies) {
          dependencyIds.add(dependency.getId());
        }
        for (IPluginModelBase potLib : getAllLibrariesInWorkspace()) {
          if (dependencyIds.contains(potLib.getPluginBase().getId())) {
            libraries.add(potLib);
          }
        }
      }
      else {
        libraries.addAll(getAllLibrariesInWorkspace());
      }

      for (IPluginModelBase pluginModel : libraries) {
        new LibraryNodePage(this, pluginModel);
      }
    }
    catch (CoreException e) {
      ScoutSdkUi.logWarning("Could not load library table page.", e);
    }
  }

  private Collection<IPluginModelBase> getAllLibrariesInWorkspace() throws CoreException {
    IPluginModelBase[] workspaceModels = PluginRegistry.getWorkspaceModels();
    List<IPluginModelBase> libraries = new ArrayList<IPluginModelBase>();
    for (IPluginModelBase model : workspaceModels) {
      IProject project = model.getUnderlyingResource().getProject();
      if (project != null && project.exists() && project.isOpen() && project.hasNature(ScoutSdk.LIBRARY_NATURE_ID)) {
        libraries.add(model);
      }
    }

    return libraries;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Class<? extends IScoutHandler>[] getSupportedMenuActions() {
    return new Class[]{LibraryBundleNewAction.class, LibraryBundleLinkAction.class};
  }

  @Override
  public void prepareMenuAction(IScoutHandler menu) {
    if (menu instanceof LibraryBundleNewAction) {
      ((LibraryBundleNewAction) menu).setOwnerBundle(getScoutBundle());
    }
    else if (menu instanceof LibraryBundleLinkAction) {
      ((LibraryBundleLinkAction) menu).setLibraryUserBundle(getScoutBundle());
    }
    super.prepareMenuAction(menu);
  }

  /**
  *
  */
  private final class P_PluginModelListener implements IPluginModelListener {
    @Override
    public void modelsChanged(PluginModelDelta delta) {
      markStructureDirty();
    }
  }
}
