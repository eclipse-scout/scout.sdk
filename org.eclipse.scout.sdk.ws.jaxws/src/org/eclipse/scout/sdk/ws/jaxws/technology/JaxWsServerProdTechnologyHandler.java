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
package org.eclipse.scout.sdk.ws.jaxws.technology;

import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.commons.TriState;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.ui.extensions.technology.AbstractScoutTechnologyHandler;
import org.eclipse.scout.sdk.ui.extensions.technology.IScoutTechnologyResource;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.ScoutBundleFilters;

public class JaxWsServerProdTechnologyHandler extends AbstractScoutTechnologyHandler {

  @Override
  public void selectionChanged(Set<IScoutTechnologyResource> resources, boolean selected, IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    selectionChangedProductFiles(resources, selected, new String[]{JaxWsServerManifestTechnologyHandler.JAXWS_RUNTIME_PLUGIN});
  }

  @Override
  public TriState getSelection(IScoutBundle project) throws CoreException {
    return getSelectionProductFiles(new String[]{IRuntimeClasses.ScoutServerBundleId}, new String[]{JaxWsServerManifestTechnologyHandler.JAXWS_RUNTIME_PLUGIN});
  }

  @Override
  protected void contributeResources(IScoutBundle project, List<IScoutTechnologyResource> list) throws CoreException {
    contributeProductFiles(list, IRuntimeClasses.ScoutServerBundleId);
  }

  @Override
  public boolean isActive(IScoutBundle project) {
    return project.getChildBundle(ScoutBundleFilters.getBundlesOfTypeFilter(IScoutBundle.TYPE_SERVER), false) != null;
  }
}