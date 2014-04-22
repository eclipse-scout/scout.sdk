/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
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
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.commons.TriState;
import org.eclipse.scout.sdk.ui.extensions.technology.AbstractScoutTechnologyHandler;
import org.eclipse.scout.sdk.ui.extensions.technology.IScoutTechnologyResource;
import org.eclipse.scout.sdk.ui.extensions.technology.ScoutTechnologyResource;
import org.eclipse.scout.sdk.ui.internal.extensions.technology.IMarketplaceConstants;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.ScoutBundleFilters;

/**
 * <h3>{@link Oracle11g2JdbcTargetTechnologyHandler}</h3>
 * 
 * @author Matthias Villiger
 * @since 3.10.0 09.04.2014
 */
public class Oracle11g2JdbcTargetTechnologyHandler extends AbstractScoutTechnologyHandler implements IMarketplaceConstants {

  @Override
  public boolean preSelectionChanged(Set<IScoutTechnologyResource> resources, boolean selected, IProgressMonitor monitor) throws CoreException {
    if (!closeTargetEditors(resources)) {
      return false;
    }
    return showLicenseDialog(selected, monitor, new String[]{SCOUT_ORACLE_JDBC_FEATURE}, new String[]{SCOUT_JDBC_FEATURE_URL});
  }

  @Override
  public void selectionChanged(Set<IScoutTechnologyResource> resources, boolean selected, IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    selectionChangedTargetFiles(resources, selected, monitor,
        new String[]{SCOUT_ORACLE_JDBC_FEATURE}, new String[]{null},
        new String[]{SCOUT_JDBC_FEATURE_URL});
  }

  @Override
  public TriState getSelection(IScoutBundle project) throws CoreException {
    List<ScoutTechnologyResource> targetFiles = getTargetFiles();
    return getSelectionTargetFileContainsFeature(targetFiles, SCOUT_ORACLE_JDBC_FEATURE);
  }

  @Override
  public boolean isActive(IScoutBundle project) {
    return project.getChildBundle(ScoutBundleFilters.getBundlesOfTypeFilter(IScoutBundle.TYPE_SERVER), false) != null;
  }

  @Override
  protected void contributeResources(IScoutBundle project, List<IScoutTechnologyResource> list) throws CoreException {
    list.addAll(getTargetFiles());
  }
}
