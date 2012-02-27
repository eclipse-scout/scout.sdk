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
package org.eclipse.scout.sdk.operation.form.formdata;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutProject;

/**
 * <h3>{@link ScoutProjectUpdateFormDataOperation}</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 20.07.2011
 */
public class ScoutProjectUpdateFormDataOperation implements IOperation {

  private final IScoutProject m_project;

  public ScoutProjectUpdateFormDataOperation(IScoutProject project) {
    m_project = project;

  }

  @Override
  public String getOperationName() {
    return "Update form data of '" + getProject().getProjectName() + "'...";
  }

  @Override
  public void validate() throws IllegalArgumentException {
    if (getProject() == null) {
      throw new IllegalArgumentException("Scout project can not be null.");
    }
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    processProject(getProject(), monitor, workingCopyManager);
  }

  private void processProject(IScoutProject project, IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) {
    if (getProject().getClientBundle() != null) {
      ClientBundleUpdateFormDataOperation updateOp = new ClientBundleUpdateFormDataOperation(getProject().getClientBundle());
      updateOp.validate();
      try {
        updateOp.run(monitor, workingCopyManager);
      }
      catch (Exception e) {
        ScoutSdk.logError("could not update form data of bundle '" + getProject().getClientBundle() + "'");
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
