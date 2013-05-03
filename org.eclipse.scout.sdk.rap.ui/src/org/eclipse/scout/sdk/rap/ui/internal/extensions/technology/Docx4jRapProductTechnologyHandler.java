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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.commons.TriState;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.rap.IScoutSdkRapConstants;
import org.eclipse.scout.sdk.ui.extensions.technology.AbstractScoutTechnologyHandler;
import org.eclipse.scout.sdk.ui.extensions.technology.IScoutTechnologyResource;
import org.eclipse.scout.sdk.ui.internal.extensions.technology.IMarketplaceConstants;
import org.eclipse.scout.sdk.ui.internal.extensions.technology.IOrbitConstants;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.ScoutBundleFilters;

/**
 * <h3>{@link Docx4jRapProductTechnologyHandler}</h3> ...
 * 
 * @author mvi
 * @since 3.9.0 01.05.2013
 */
public class Docx4jRapProductTechnologyHandler extends AbstractScoutTechnologyHandler implements IMarketplaceConstants, IOrbitConstants {

  @Override
  public void selectionChanged(IScoutTechnologyResource[] resources, boolean selected, IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    selectionChangedProductFiles(resources, selected, new String[]{XML_GRAPHICS_PLUGIN_NAME, APACHE_COMMONS_PLUGIN_NAME, APACHE_COMMONS_LOGGING_PLUGIN_NAME, DOCX4J_PLUGIN,
        DOCX4J_SCOUT_PLUGIN, DOCX4J_SCOUT_CLIENT_PLUGIN, LOGGING_BRIDGE_LOG4J_FRAGMENT});
  }

  @Override
  public TriState getSelection(IScoutBundle project) throws CoreException {
    return getSelectionProductFiles(getProductBundles(project),
        new String[]{RuntimeClasses.ScoutSharedBundleId, IScoutSdkRapConstants.ScoutUiRapBundleId},
        new String[]{XML_GRAPHICS_PLUGIN_NAME, APACHE_COMMONS_PLUGIN_NAME, APACHE_COMMONS_LOGGING_PLUGIN_NAME, DOCX4J_PLUGIN, DOCX4J_SCOUT_PLUGIN,
            DOCX4J_SCOUT_CLIENT_PLUGIN, LOGGING_BRIDGE_LOG4J_FRAGMENT});
  }

  @Override
  public boolean isActive(IScoutBundle project) {
    return project.getChildBundle(ScoutBundleFilters.getBundlesOfTypeFilter(IScoutSdkRapConstants.TYPE_UI_RAP), true) != null;
  }

  @Override
  protected void contributeResources(IScoutBundle project, List<IScoutTechnologyResource> list) throws CoreException {
    contributeProductFiles(getProductBundles(project), list, RuntimeClasses.ScoutSharedBundleId, IScoutSdkRapConstants.ScoutUiRapBundleId);
  }

  private IScoutBundle[] getProductBundles(IScoutBundle start) {
    return start.getChildBundles(ScoutBundleFilters.getBundlesOfTypeFilter(IScoutSdkRapConstants.TYPE_UI_RAP), false);
  }
}
