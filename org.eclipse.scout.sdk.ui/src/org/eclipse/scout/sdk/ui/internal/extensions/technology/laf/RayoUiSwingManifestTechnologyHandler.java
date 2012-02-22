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
package org.eclipse.scout.sdk.ui.internal.extensions.technology.laf;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.commons.TriState;
import org.eclipse.scout.sdk.compatibility.P2Utility;
import org.eclipse.scout.sdk.ui.extensions.technology.AbstractScoutTechnologyHandler;
import org.eclipse.scout.sdk.ui.extensions.technology.IScoutTechnologyResource;
import org.eclipse.scout.sdk.ui.internal.extensions.technology.IMarketplaceConstants;
import org.eclipse.scout.sdk.workspace.IScoutProject;

/**
 * <h3>{@link RayoUiSwingManifestTechnologyHandler}</h3> ...
 * 
 * @author mvi
 * @since 3.8.0 19.02.2012
 */
public class RayoUiSwingManifestTechnologyHandler extends AbstractScoutTechnologyHandler implements IMarketplaceConstants {

  private boolean m_newPluginsInstalled;

  @Override
  public boolean preSelectionChanged(boolean selected, IProgressMonitor monitor) throws CoreException {
    m_newPluginsInstalled = false;
    if (selected) {
      FeatureInstallResult result = ensureFeatureInstalled(SCOUT_RAYO_LAF_FEATURE, SCOUT_RAYO_FEATURE_URL, monitor, RAYO_LAF_PLUGIN, RAYO_LAF_FRAGMENT);
      if (FeatureInstallResult.LicenseNotAccepted.equals(result)) {
        return false; // abort processing if the installation would be necessary but the license was not accepted.
      }
      else if (FeatureInstallResult.InstallationSuccessful.equals(result)) {
        m_newPluginsInstalled = true; // remember if we have installed new plugins so that we can ask for a restart later on.
      }
    }
    return true;
  }

  @Override
  public void selectionChanged(IScoutTechnologyResource[] resources, boolean selected, IProgressMonitor monitor) throws CoreException {
    selectionChangedManifest(resources, selected, RAYO_LAF_PLUGIN);
  }

  @Override
  public void postSelectionChanged(boolean selected, IProgressMonitor monitor) throws CoreException {
    if (m_newPluginsInstalled) {
      P2Utility.promptForRestart();
    }
  }

  @Override
  public TriState getSelection(IScoutProject project) {
    return getSelectionManifest(project.getUiSwingBundle(), RAYO_LAF_PLUGIN);
  }

  @Override
  public boolean isActive(IScoutProject project) {
    return project.getUiSwingBundle() != null && project.getUiSwingBundle().getProject().exists();
  }

  @Override
  protected void contributeResources(IScoutProject project, List<IScoutTechnologyResource> list) {
    contributeManifestFile(project.getUiSwingBundle(), list);
  }
}
