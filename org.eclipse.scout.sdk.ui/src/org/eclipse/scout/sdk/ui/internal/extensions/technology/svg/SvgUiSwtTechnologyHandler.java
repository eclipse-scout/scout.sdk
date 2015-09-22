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
package org.eclipse.scout.sdk.ui.internal.extensions.technology.svg;

import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.commons.TriState;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.operation.project.SwtProductFileUpgradeOperation;
import org.eclipse.scout.sdk.ui.extensions.technology.AbstractScoutTechnologyHandler;
import org.eclipse.scout.sdk.ui.extensions.technology.IScoutTechnologyResource;
import org.eclipse.scout.sdk.util.pde.ProductFileModelHelper;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.ScoutBundleFilters;

public class SvgUiSwtTechnologyHandler extends AbstractScoutTechnologyHandler {

  public static final String[] SWT_SVG_PLUGIN = new String[]{"org.eclipse.scout.svg.ui.swt"};

  public SvgUiSwtTechnologyHandler() {
  }

  @Override
  public void selectionChanged(Set<IScoutTechnologyResource> resources, boolean selected, IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    for (IScoutTechnologyResource resource : resources) {
      if (isE4Product(resource.getResource())) {
        selectionChangedProductFile(resource, selected, SvgClientTechnologyHandler.SCOUT_ONLY_SVG_PLUGINS, SWT_SVG_PLUGIN, SvgClientTechnologyHandler.getAdditionalBatik17ScoutPlugins());
      }
      else {
        selectionChangedProductFile(resource, selected, SvgClientTechnologyHandler.SCOUT_ONLY_SVG_PLUGINS, SvgClientTechnologyHandler.CORE_SVG_PLUGINS, SWT_SVG_PLUGIN,
            SvgClientTechnologyHandler.getAdditionalBatik17CorePlugins(), SvgClientTechnologyHandler.getAdditionalBatik17ScoutPlugins());
      }
    }
  }

  private boolean isE4Product(IFile productFile) throws CoreException {
    ProductFileModelHelper pfmh = new ProductFileModelHelper(productFile);
    return pfmh.ProductFile.existsDependency(SwtProductFileUpgradeOperation.E4_UI_CSS_CORE_PLUGIN_ID);
  }

  @Override
  public boolean isActive(IScoutBundle project) {
    return project.getChildBundle(ScoutBundleFilters.getBundlesOfTypeFilter(IScoutBundle.TYPE_UI_SWT), false) != null;
  }

  @Override
  public TriState getSelection(IScoutBundle project) throws CoreException {
    return getSelectionProductFiles(new String[]{IRuntimeClasses.ScoutClientBundleId, IRuntimeClasses.ScoutUiSwtBundleId},
        SvgClientTechnologyHandler.SCOUT_ONLY_SVG_PLUGINS, SvgClientTechnologyHandler.CORE_SVG_PLUGINS, SWT_SVG_PLUGIN,
        SvgClientTechnologyHandler.getAdditionalBatik17CorePlugins(), SvgClientTechnologyHandler.getAdditionalBatik17ScoutPlugins());
  }

  @Override
  protected void contributeResources(IScoutBundle project, List<IScoutTechnologyResource> list) throws CoreException {
    contributeProductFiles(list, IRuntimeClasses.ScoutClientBundleId, IRuntimeClasses.ScoutUiSwtBundleId);
  }
}