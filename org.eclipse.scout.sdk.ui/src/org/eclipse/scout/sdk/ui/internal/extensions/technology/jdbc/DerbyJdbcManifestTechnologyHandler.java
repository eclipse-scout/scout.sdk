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
package org.eclipse.scout.sdk.ui.internal.extensions.technology.jdbc;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.commons.TriState;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.ui.extensions.technology.AbstractScoutTechnologyHandler;
import org.eclipse.scout.sdk.ui.extensions.technology.IScoutTechnologyResource;
import org.eclipse.scout.sdk.ui.internal.extensions.technology.IMarketplaceConstants;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutProject;

/**
 * <h3>{@link DerbyJdbcManifestTechnologyHandler}</h3> ...
 * 
 * @author mvi
 * @since 3.8.0 13.02.2012
 */
public class DerbyJdbcManifestTechnologyHandler extends AbstractScoutTechnologyHandler implements IMarketplaceConstants {

  public DerbyJdbcManifestTechnologyHandler() {
  }

  @Override
  public void selectionChanged(IScoutTechnologyResource[] resources, boolean selected, IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    selectionChangedManifest(resources, selected, DERBY_JDBC_PLUGIN);
  }

  @Override
  public void postSelectionChanged(boolean selected, IProgressMonitor monitor) throws CoreException {
    TypeUtility.getPrimaryTypeHierarchy(TypeUtility.getType(RuntimeClasses.ISqlService)).invalidate();
  }

  @Override
  public TriState getSelection(IScoutProject project) {
    return getSelectionManifest(project.getServerBundle(), DERBY_JDBC_PLUGIN);
  }

  @Override
  public boolean isActive(IScoutProject project) {
    return project.getServerBundle() != null && project.getServerBundle().getProject().exists();
  }

  @Override
  protected void contributeResources(IScoutProject project, List<IScoutTechnologyResource> list) {
    contributeManifestFile(project.getServerBundle(), list);
  }
}
