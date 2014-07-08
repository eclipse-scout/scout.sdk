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
import org.eclipse.scout.sdk.compatibility.P2Utility;
import org.eclipse.scout.sdk.ui.extensions.technology.AbstractScoutTechnologyHandler;
import org.eclipse.scout.sdk.ui.extensions.technology.IScoutTechnologyResource;
import org.eclipse.scout.sdk.ui.internal.extensions.technology.IMarketplaceConstants;
import org.eclipse.scout.sdk.ui.internal.extensions.technology.IOrbitConstants;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.ScoutBundleFilters;

/**
 * <h3>{@link Docx4jManifestTechnologyHandler}</h3>
 * 
 * @author Matthias Villiger
 * @since 3.9.0 23.04.2013
 */
public class Docx4jManifestTechnologyHandler extends AbstractScoutTechnologyHandler implements IMarketplaceConstants, IOrbitConstants {

  private boolean m_newPluginsInstalled;

  public Docx4jManifestTechnologyHandler() {
  }

  @Override
  public boolean preSelectionChanged(Set<IScoutTechnologyResource> resources, boolean selected, IProgressMonitor monitor) throws CoreException {
    m_newPluginsInstalled = false;
    if (selected) {
      FeatureInstallResult result = ensureFeaturesInstalled(new String[]{XML_GRAPHICS_FEATURE_NAME, DOCX4J_CORE_FEATURE, DOCX4J_LIBS_FEATURE, DOCX4J_SDK_FEATURE},
          new String[]{ORBIT_UPDATESITE_URL, SCOUT_DOCX4J_FEATURE_URL, SCOUT_DOCX4J_FEATURE_URL, SCOUT_DOCX4J_FEATURE_URL}, monitor,
          new String[]{XML_GRAPHICS_PLUGIN_NAME, APACHE_COMMONS_PLUGIN_NAME, APACHE_COMMONS_LOGGING_PLUGIN_NAME},
          new String[]{DOCX4J_SCOUT_PLUGIN},
          new String[]{DOCX4J_PLUGIN,},
          new String[]{DOCX4J_SDK_PLUGIN});
      if (FeatureInstallResult.LICENSE_NOT_ACCEPTED.equals(result)) {
        return false; // abort processing if the installation would be necessary but the license was not accepted.
      }
      else if (FeatureInstallResult.INSTALLATION_SUCCESSFUL.equals(result)) {
        m_newPluginsInstalled = true; // remember if we have installed new plug-in so that we can ask for a restart later on.
      }
    }
    return true;
  }

  @Override
  public void selectionChanged(Set<IScoutTechnologyResource> resources, boolean selected, IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    for (IScoutTechnologyResource r : resources) {
      if (IScoutBundle.TYPE_CLIENT.equals(r.getBundle().getType())) {
        selectionChangedManifest(r, selected, DOCX4J_PLUGIN, DOCX4J_SCOUT_PLUGIN, DOCX4J_SCOUT_CLIENT_PLUGIN);
      }
      else {
        selectionChangedManifest(r, selected, DOCX4J_PLUGIN, DOCX4J_SCOUT_PLUGIN);
      }
    }
  }

  @Override
  public void postSelectionChanged(boolean selected, IProgressMonitor monitor) throws CoreException {
    if (m_newPluginsInstalled) {
      P2Utility.promptForRestart();
    }
  }

  @Override
  public TriState getSelection(IScoutBundle project) throws CoreException {
    return getSelectionManifests(project.getChildBundles(ScoutBundleFilters.getBundlesOfTypeFilter(IScoutBundle.TYPE_SHARED), true),
        DOCX4J_PLUGIN, DOCX4J_SCOUT_PLUGIN);
  }

  @Override
  public boolean isActive(IScoutBundle project) {
    return project.getChildBundle(ScoutBundleFilters.getBundlesOfTypeFilter(IScoutBundle.TYPE_SHARED, IScoutBundle.TYPE_CLIENT), true) != null;
  }

  @Override
  protected void contributeResources(IScoutBundle project, List<IScoutTechnologyResource> list) throws CoreException {
    Set<IScoutBundle> childBundles = project.getChildBundles(ScoutBundleFilters.getBundlesOfTypeFilter(IScoutBundle.TYPE_SHARED, IScoutBundle.TYPE_CLIENT), true);
    contributeManifestFiles(childBundles, list);
  }
}
