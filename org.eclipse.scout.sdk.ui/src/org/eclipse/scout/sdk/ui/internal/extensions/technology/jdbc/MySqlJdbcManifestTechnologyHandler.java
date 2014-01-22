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
import org.eclipse.scout.sdk.compatibility.P2Utility;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.ui.extensions.technology.AbstractScoutTechnologyHandler;
import org.eclipse.scout.sdk.ui.extensions.technology.IScoutTechnologyResource;
import org.eclipse.scout.sdk.ui.internal.extensions.technology.IMarketplaceConstants;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.ScoutBundleFilters;

/**
 * <h3>{@link MySqlJdbcManifestTechnologyHandler}</h3> ...
 * 
 * @author Matthias Villiger
 * @since 3.8.0 13.02.2012
 */
public class MySqlJdbcManifestTechnologyHandler extends AbstractScoutTechnologyHandler implements IMarketplaceConstants {

  public MySqlJdbcManifestTechnologyHandler() {
  }

  private boolean m_newPluginsInstalled;

  @Override
  public boolean preSelectionChanged(boolean selected, IProgressMonitor monitor) throws CoreException {
    m_newPluginsInstalled = false;
    if (selected) {
      FeatureInstallResult result = ensureFeatureInstalled(SCOUT_MYSQL_JDBC_FEATURE, SCOUT_JDBC_FEATURE_URL, monitor, MY_SQL_JDBC_PLUGIN, MY_SQL_JDBC_FRAGMENT);
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
  public void selectionChanged(IScoutTechnologyResource[] resources, boolean selected, IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    selectionChangedManifest(resources, selected, MY_SQL_JDBC_PLUGIN);
  }

  @Override
  public void postSelectionChanged(boolean selected, IProgressMonitor monitor) throws CoreException {
    TypeUtility.getPrimaryTypeHierarchy(TypeUtility.getType(IRuntimeClasses.ISqlService)).invalidate();

    if (m_newPluginsInstalled) {
      P2Utility.promptForRestart();
    }
  }

  @Override
  public TriState getSelection(IScoutBundle project) {
    return getSelectionManifests(getServerBundlesBelow(project), MY_SQL_JDBC_PLUGIN);
  }

  @Override
  public boolean isActive(IScoutBundle project) {
    return project.getChildBundle(ScoutBundleFilters.getBundlesOfTypeFilter(IScoutBundle.TYPE_SERVER), false) != null;
  }

  @Override
  protected void contributeResources(IScoutBundle project, List<IScoutTechnologyResource> list) {
    contributeManifestFiles(getServerBundlesBelow(project), list);
  }

  private IScoutBundle[] getServerBundlesBelow(IScoutBundle start) {
    return start.getChildBundles(ScoutBundleFilters.getBundlesOfTypeFilter(IScoutBundle.TYPE_SERVER), true);
  }
}
