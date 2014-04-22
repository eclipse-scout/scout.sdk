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
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.rap.IScoutSdkRapConstants;
import org.eclipse.scout.sdk.ui.extensions.technology.AbstractScoutTechnologyHandler;
import org.eclipse.scout.sdk.ui.extensions.technology.IScoutTechnologyResource;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.ScoutBundleFilters;

/**
 * <h3>{@link FileChooserRapProductTechnologyHandler}</h3> ...
 * 
 * @author Matthias Villiger
 * @since 3.8.0 13.04.2012
 */
public class FileChooserRapProductTechnologyHandler extends AbstractScoutTechnologyHandler {

  public static final String[] RAP_FILE_CHOOSER_PLUGINS = new String[]{"org.eclipse.scout.rt.ui.rap.incubator.filechooser",
      "org.apache.commons.fileupload", "org.apache.commons.io", "org.eclipse.rap.rwt.supplemental.filedialog", "org.eclipse.rap.rwt.supplemental.fileupload"};

  @Override
  public void selectionChanged(Set<IScoutTechnologyResource> resources, boolean selected, IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    selectionChangedProductFiles(resources, selected, RAP_FILE_CHOOSER_PLUGINS);
  }

  @Override
  public TriState getSelection(IScoutBundle project) throws CoreException {
    return getSelectionProductFiles(new String[]{IRuntimeClasses.ScoutClientBundleId, IScoutSdkRapConstants.ScoutUiRapBundleId}, RAP_FILE_CHOOSER_PLUGINS);
  }

  @Override
  public boolean isActive(IScoutBundle project) {
    return project.getChildBundle(ScoutBundleFilters.getBundlesOfTypeFilter(IScoutSdkRapConstants.TYPE_UI_RAP), false) != null;
  }

  @Override
  protected void contributeResources(IScoutBundle project, List<IScoutTechnologyResource> list) throws CoreException {
    contributeProductFiles(list, IRuntimeClasses.ScoutClientBundleId, IScoutSdkRapConstants.ScoutUiRapBundleId);
  }
}
