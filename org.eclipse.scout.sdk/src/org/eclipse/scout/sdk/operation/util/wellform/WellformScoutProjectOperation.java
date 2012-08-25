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
package org.eclipse.scout.sdk.operation.util.wellform;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutProject;

/**
 * <h3>{@link WellformScoutProjectOperation}</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 20.07.2011
 */
public class WellformScoutProjectOperation implements IOperation {

  private final IScoutProject m_project;

  public WellformScoutProjectOperation(IScoutProject project) {
    m_project = project;
  }

  @Override
  public String getOperationName() {
    StringBuilder builder = new StringBuilder();
    builder.append("Wellform '" + getProject().getProjectName() + "'...");
    return builder.toString();
  }

  @Override
  public void validate() throws IllegalArgumentException {
    if (getProject() == null) {
      throw new IllegalArgumentException("Project can not be null.");
    }
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    processProject(getProject(), monitor, workingCopyManager);
  }

  private void processProject(IScoutProject project, IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) {
    if (project.getClientBundle() != null) {
      WellformClientBundleOperation op = new WellformClientBundleOperation(project.getClientBundle());
      try {
        op.validate();
        op.run(monitor, workingCopyManager);
      }
      catch (Exception e) {
        ScoutSdk.logError("could not wellform '" + project.getClientBundle().getBundleName() + "'.", e);
      }
    }
    if (project.getSharedBundle() != null) {
      WellformSharedBundleOperation op = new WellformSharedBundleOperation(project.getSharedBundle());
      try {
        op.validate();
        op.run(monitor, workingCopyManager);
      }
      catch (Exception e) {
        ScoutSdk.logError("could not wellform '" + project.getSharedBundle().getBundleName() + "'.", e);
      }
    }
    if (project.getServerBundle() != null) {
      WellformServerBundleOperation op = new WellformServerBundleOperation(project.getServerBundle());
      try {
        op.validate();
        op.run(monitor, workingCopyManager);
      }
      catch (Exception e) {
        ScoutSdk.logError("could not wellform '" + project.getServerBundle().getBundleName() + "'.", e);
      }
    }
    // sub projects
    for (IScoutProject subProject : project.getSubProjects()) {
      processProject(subProject, monitor, workingCopyManager);
    }
  }

  /**
   * @return the project
   */
  public IScoutProject getProject() {
    return m_project;
  }

}
