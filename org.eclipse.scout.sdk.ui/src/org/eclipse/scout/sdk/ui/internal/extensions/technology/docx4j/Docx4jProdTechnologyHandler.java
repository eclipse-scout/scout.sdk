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
package org.eclipse.scout.sdk.ui.internal.extensions.technology.docx4j;

import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.commons.TriState;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.ui.extensions.technology.AbstractScoutTechnologyHandler;
import org.eclipse.scout.sdk.ui.extensions.technology.IScoutTechnologyResource;
import org.eclipse.scout.sdk.ui.internal.extensions.technology.IMarketplaceConstants;
import org.eclipse.scout.sdk.ui.internal.extensions.technology.IOrbitConstants;
import org.eclipse.scout.sdk.util.pde.ProductFileModelHelper;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.ScoutBundleFilters;

/**
 * <h3>{@link Docx4jProdTechnologyHandler}</h3>
 * 
 * @author Matthias Villiger
 * @since 3.9.0 23.04.2013
 */
public class Docx4jProdTechnologyHandler extends AbstractScoutTechnologyHandler implements IMarketplaceConstants, IOrbitConstants {

  @Override
  public void selectionChanged(Set<IScoutTechnologyResource> resources, boolean selected, IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    for (IScoutTechnologyResource r : resources) {
      ProductFileModelHelper h = new ProductFileModelHelper(r.getResource());
      if (h.ProductFile.existsDependency(IRuntimeClasses.ScoutClientBundleId)) {
        selectionChangedProductFile(r, selected, new String[]{XML_GRAPHICS_PLUGIN_NAME, APACHE_COMMONS_PLUGIN_NAME, APACHE_COMMONS_LOGGING_PLUGIN_NAME, DOCX4J_PLUGIN,
            DOCX4J_SCOUT_PLUGIN, DOCX4J_SCOUT_CLIENT_PLUGIN, LOGGING_BRIDGE_LOG4J_FRAGMENT});
      }
      else {
        selectionChangedProductFile(r, selected, new String[]{XML_GRAPHICS_PLUGIN_NAME, APACHE_COMMONS_PLUGIN_NAME, APACHE_COMMONS_LOGGING_PLUGIN_NAME, DOCX4J_PLUGIN,
            DOCX4J_SCOUT_PLUGIN, LOGGING_BRIDGE_LOG4J_FRAGMENT});
      }
    }
  }

  @Override
  public TriState getSelection(IScoutBundle project) throws CoreException {
    return getSelectionProductFiles(new String[]{IRuntimeClasses.ScoutSharedBundleId},
        new String[]{XML_GRAPHICS_PLUGIN_NAME, APACHE_COMMONS_PLUGIN_NAME, APACHE_COMMONS_LOGGING_PLUGIN_NAME, DOCX4J_PLUGIN, DOCX4J_SCOUT_PLUGIN,
            LOGGING_BRIDGE_LOG4J_FRAGMENT});
  }

  @Override
  public boolean isActive(IScoutBundle project) {
    return project.getChildBundle(ScoutBundleFilters.getBundlesOfTypeFilter(IScoutBundle.TYPE_SERVER, IScoutBundle.TYPE_UI_SWING, IScoutBundle.TYPE_UI_SWT), true) != null;
  }

  @Override
  protected void contributeResources(IScoutBundle project, List<IScoutTechnologyResource> list) throws CoreException {
    contributeProductFiles(list, IRuntimeClasses.ScoutSharedBundleId, IRuntimeClasses.ScoutUiSwingBundleId);
    contributeProductFiles(list, IRuntimeClasses.ScoutSharedBundleId, IRuntimeClasses.ScoutUiSwtBundleId);
    contributeProductFiles(list, IRuntimeClasses.ScoutSharedBundleId, IRuntimeClasses.ScoutServerBundleId);
  }
}