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
package org.eclipse.scout.sdk.ui.internal.extensions.technology.jdbc;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.commons.TriState;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.compatibility.P2Utility;
import org.eclipse.scout.sdk.ui.extensions.technology.AbstractScoutTechnologyHandler;
import org.eclipse.scout.sdk.ui.extensions.technology.IScoutTechnologyResource;
import org.eclipse.scout.sdk.ui.internal.extensions.technology.IMarketplaceConstants;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.IScoutProject;

/**
 * <h3>{@link Oracle11g2JdbcManifestTechnologyHandler}</h3> ...
 * 
 * @author mvi
 * @since 3.8.0 13.02.2012
 */
public class Oracle11g2JdbcManifestTechnologyHandler extends AbstractScoutTechnologyHandler implements IMarketplaceConstants {

  private boolean m_newPluginsInstalled;

  public Oracle11g2JdbcManifestTechnologyHandler() {
  }

  @Override
  public boolean preSelectionChanged(boolean selected, IProgressMonitor monitor) throws CoreException {
    m_newPluginsInstalled = false;
    if (selected) {
      FeatureInstallResult result = ensureFeatureInstalled(SCOUT_ORACLE_JDBC_FEATURE, SCOUT_JDBC_FEATURE_URL, monitor, ORACLE_JDBC_PLUGIN, ORACLE_JDBC_FRAGMENT);
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
    selectionChangedManifest(resources, selected, ORACLE_JDBC_PLUGIN);
  }

  @Override
  public void postSelectionChanged(boolean selected, IProgressMonitor monitor) throws CoreException {
    TypeUtility.getPrimaryTypeHierarchy(TypeUtility.getType(RuntimeClasses.ISqlService)).invalidate();

    if (m_newPluginsInstalled) {
      P2Utility.promptForRestart();
    }
  }

  @Override
  public TriState getSelection(IScoutProject project) {
    return getSelectionManifest(project.getServerBundle(), ORACLE_JDBC_PLUGIN);
  }

  @Override
  public boolean isActive(IScoutProject project) {
    return project.getServerBundle() != null && project.getServerBundle().getProject().exists();
  }

  @Override
  protected void contributeResources(IScoutProject project, List<IScoutTechnologyResource> list) {
    contributeManifestFile(project.getServerBundle(), list);
  }
}
