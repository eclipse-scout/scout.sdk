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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.commons.TriState;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.ui.extensions.technology.AbstractScoutTechnologyHandler;
import org.eclipse.scout.sdk.ui.extensions.technology.IScoutTechnologyResource;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.internal.extensions.technology.IMarketplaceConstants;
import org.eclipse.scout.sdk.util.pde.ProductFileModelHelper;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.ScoutBundleFilters;

/**
 * <h3>{@link RayoUiSwingProdTechnologyHandler}</h3> ...
 * 
 * @author Matthias Villiger
 * @since 3.8.0 19.02.2012
 */
public class RayoUiSwingProdTechnologyHandler extends AbstractScoutTechnologyHandler implements IMarketplaceConstants {

  private static final String SCOUT_LAF_KEY = "scout.laf";
  private static final String RAYO_LAF_NAME = "com.bsiag.scout.rt.ui.swing.laf.rayo.Rayo";
  private static final String RAYO_LAF_FRAME_KEY = "scout.laf.useLafFrameAndDialog";
  private static final String RAYO_LAF_FRAME_NAME = "true";

  @Override
  public void selectionChanged(IScoutTechnologyResource[] resources, boolean selected, IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    selectionChangedProductFiles(resources, selected, new String[]{RAYO_LAF_PLUGIN, RAYO_LAF_FRAGMENT});

    for (IScoutTechnologyResource res : resources) {
      ProductFileModelHelper pfmh = new ProductFileModelHelper(res.getResource());
      if (selected) {
        pfmh.ConfigurationFile.setEntry(SCOUT_LAF_KEY, RAYO_LAF_NAME);
        pfmh.ConfigurationFile.setEntry(RAYO_LAF_FRAME_KEY, RAYO_LAF_FRAME_NAME);
      }
      else {
        pfmh.ConfigurationFile.removeEntry(SCOUT_LAF_KEY);
        pfmh.ConfigurationFile.removeEntry(RAYO_LAF_FRAME_KEY);
      }
      pfmh.save();
    }
  }

  @Override
  public TriState getSelection(IScoutBundle project) throws CoreException {
    TriState ret = getSelectionProductFiles(new String[]{IRuntimeClasses.ScoutClientBundleId, IRuntimeClasses.ScoutUiSwingBundleId},
        new String[]{RAYO_LAF_PLUGIN, RAYO_LAF_FRAGMENT});

    List<P_TechProductFile> productFiles = getFilteredProductFiles(new String[]{IRuntimeClasses.ScoutClientBundleId, IRuntimeClasses.ScoutUiSwingBundleId});
    if (productFiles.size() == 0) {
      return null;
    }

    for (int i = 0; i < productFiles.size(); i++) {
      TriState tmp = TriState.parseTriState(isRayoLafEnabledInConfigIni(productFiles.get(i).productFile));
      if (ret != tmp) {
        return TriState.UNDEFINED;
      }
    }
    return ret;
  }

  @Override
  public boolean isActive(IScoutBundle project) {
    return project.getChildBundle(ScoutBundleFilters.getBundlesOfTypeFilter(IScoutBundle.TYPE_UI_SWING), false) != null;
  }

  @Override
  protected void contributeResources(IScoutBundle project, List<IScoutTechnologyResource> list) throws CoreException {
    contributeProductFiles(list, IRuntimeClasses.ScoutClientBundleId, IRuntimeClasses.ScoutUiSwingBundleId);
  }

  private boolean isRayoLafEnabledInConfigIni(IFile productFile) {
    try {
      ProductFileModelHelper pfmh = new ProductFileModelHelper(productFile);
      String scoutLaf = pfmh.ConfigurationFile.getEntry(SCOUT_LAF_KEY);
      return RAYO_LAF_NAME.equals(scoutLaf);
    }
    catch (CoreException e) {
      ScoutSdkUi.logError("cannot parse product file: " + productFile, e);
      return false;
    }
  }
}
