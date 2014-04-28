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
package org.eclipse.scout.sdk.rap.ui.internal.extensions.technology;

import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.commons.TriState;
import org.eclipse.scout.sdk.rap.IScoutSdkRapConstants;
import org.eclipse.scout.sdk.ui.extensions.technology.AbstractScoutTechnologyHandler;
import org.eclipse.scout.sdk.ui.extensions.technology.IScoutTechnologyResource;
import org.eclipse.scout.sdk.ui.extensions.technology.ScoutTechnologyResource;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.ScoutBundleFilters;
import org.osgi.framework.Version;

/**
 * <h3>{@link FileChooserRapTargetTechnologyHandler}</h3>
 * 
 * @author Matthias Villiger
 * @since 3.8.0 10.04.2012
 */
public class FileChooserRapTargetTechnologyHandler extends AbstractScoutTechnologyHandler {

  private static final String RAP_INCUBATOR_UPDATE_SITE_URL = "http://download.eclipse.org/rt/rap/incubator/2.3/fileupload/";
  private static final String RAP_INCUBATOR_FEATURE_NAME = "org.eclipse.rap.fileupload.feature.feature.group";
  private static final Version RAP_INCUBATOR_FEATURE_VERSION = new Version(2, 3, 0, "20140426-0831"); // the supported version. must be present at the update site

  private static final String SCOUT_INCUBATOR_FEATURE_NAME = "org.eclipse.scout.rt.ui.rap.incubator.filechooser.source.feature.group";
  private static final String SCOUT_INCUBATOR_UPDATE_SITE_URL = "http://download.eclipse.org/scout/releases/4.0";

  @Override
  public boolean preSelectionChanged(Set<IScoutTechnologyResource> resources, boolean selected, IProgressMonitor monitor) throws CoreException {
    if (!closeTargetEditors(resources)) {
      return false;
    }
    return showLicenseDialog(selected, monitor, new String[]{RAP_INCUBATOR_FEATURE_NAME, SCOUT_INCUBATOR_FEATURE_NAME},
        new String[]{RAP_INCUBATOR_UPDATE_SITE_URL, SCOUT_INCUBATOR_UPDATE_SITE_URL});
  }

  @Override
  public void selectionChanged(Set<IScoutTechnologyResource> resources, boolean selected, IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    selectionChangedTargetFiles(resources, selected, monitor,
        new String[]{RAP_INCUBATOR_FEATURE_NAME, SCOUT_INCUBATOR_FEATURE_NAME}, new String[]{RAP_INCUBATOR_FEATURE_VERSION.toString(), null},
        new String[]{RAP_INCUBATOR_UPDATE_SITE_URL, SCOUT_INCUBATOR_UPDATE_SITE_URL});
  }

  @Override
  public TriState getSelection(IScoutBundle project) throws CoreException {
    List<ScoutTechnologyResource> targetFiles = getTargetFiles();
    return getSelectionTargetFileContainsFeature(targetFiles, SCOUT_INCUBATOR_FEATURE_NAME, RAP_INCUBATOR_FEATURE_NAME);
  }

  @Override
  public boolean isActive(IScoutBundle project) {
    return project.getChildBundle(ScoutBundleFilters.getBundlesOfTypeFilter(IScoutSdkRapConstants.TYPE_UI_RAP), false) != null;
  }

  @Override
  protected void contributeResources(IScoutBundle project, List<IScoutTechnologyResource> list) throws CoreException {
    list.addAll(getTargetFiles());
  }
}
