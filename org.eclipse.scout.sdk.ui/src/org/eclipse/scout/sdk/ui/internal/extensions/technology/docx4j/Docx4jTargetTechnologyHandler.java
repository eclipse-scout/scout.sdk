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
package org.eclipse.scout.sdk.ui.internal.extensions.technology.docx4j;

import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.commons.TriState;
import org.eclipse.scout.sdk.ui.extensions.technology.AbstractScoutTechnologyHandler;
import org.eclipse.scout.sdk.ui.extensions.technology.IScoutTechnologyResource;
import org.eclipse.scout.sdk.ui.extensions.technology.ScoutTechnologyResource;
import org.eclipse.scout.sdk.ui.internal.extensions.technology.IMarketplaceConstants;
import org.eclipse.scout.sdk.ui.internal.extensions.technology.IOrbitConstants;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.ScoutBundleFilters;

/**
 * <h3>{@link Docx4jTargetTechnologyHandler}</h3>
 *
 * @author Matthias Villiger
 * @since 3.10.0 09.04.2014
 */
public class Docx4jTargetTechnologyHandler extends AbstractScoutTechnologyHandler implements IMarketplaceConstants, IOrbitConstants {

  @Override
  public boolean preSelectionChanged(Set<IScoutTechnologyResource> resources, boolean selected, IProgressMonitor monitor) throws CoreException {
    if (!closeTargetEditors(resources)) {
      return false;
    }
    return showLicenseDialog(selected, monitor, new String[]{LOGGING_BRIDGE_FEATURE, XML_GRAPHICS_FEATURE_NAME, DOCX4J_CORE_FEATURE, DOCX4J_CLIENT_FEATURE},
        new String[]{SCOUT_LOGGING_BRIDGE_FEATURE_URL, ORBIT_UPDATESITE_URL, SCOUT_DOCX4J_FEATURE_URL, SCOUT_DOCX4J_FEATURE_URL});
  }

  @Override
  public void selectionChanged(Set<IScoutTechnologyResource> resources, boolean selected, IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    selectionChangedTargetFiles(resources, selected, monitor,
        new String[]{LOGGING_BRIDGE_FEATURE, XML_GRAPHICS_FEATURE_NAME, DOCX4J_CORE_FEATURE, DOCX4J_CLIENT_FEATURE, DOCX4J_LIBS_FEATURE},
        new String[]{null, null, null, null, null},
        new String[]{SCOUT_LOGGING_BRIDGE_FEATURE_URL, ORBIT_UPDATESITE_URL, SCOUT_DOCX4J_FEATURE_URL, SCOUT_DOCX4J_FEATURE_URL, SCOUT_DOCX4J_FEATURE_URL});
  }

  @Override
  public TriState getSelection(IScoutBundle project) throws CoreException {
    List<ScoutTechnologyResource> targetFiles = getTargetFiles();
    return getSelectionTargetFileContainsFeature(targetFiles, LOGGING_BRIDGE_FEATURE, XML_GRAPHICS_FEATURE_NAME, DOCX4J_CORE_FEATURE, DOCX4J_CLIENT_FEATURE);
  }

  @Override
  public boolean isActive(IScoutBundle project) {
    return project.getChildBundle(ScoutBundleFilters.getBundlesOfTypeFilter(IScoutBundle.TYPE_SHARED, IScoutBundle.TYPE_CLIENT), true) != null;
  }

  @Override
  protected void contributeResources(IScoutBundle project, List<IScoutTechnologyResource> list) throws CoreException {
    list.addAll(getTargetFiles());
  }
}
