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
package org.eclipse.scout.sdk.operation.axis;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.operation.IDeleteOperation;
import org.eclipse.scout.sdk.typecache.IScoutWorkingCopyManager;
import org.eclipse.ui.ide.undo.DeleteResourcesOperation;

public class AxisWebServiceConsumerDeleteOperation implements IDeleteOperation {

  private IFile[] m_files;
  private IFile m_undeployFile;
  private IProject m_serverProject;

  @Override
  public String getOperationName() {
    return "Delete Axis Webservice...";
  }

  @Override
  public void validate() throws IllegalArgumentException {
    if (getServerProject() == null) {
      throw new IllegalArgumentException("Server project can not be null.");
    }
    if (getFiles() == null) {
      throw new IllegalArgumentException("Files can not be null.");
    }
  }

  @Override
  public void run(IProgressMonitor monitor, IScoutWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    if (getUndeployFile() != null && getUndeployFile().exists()) {
      WebServiceUndeployOperation undeployOperation = new WebServiceUndeployOperation();
      undeployOperation.setProject(getServerProject());
      undeployOperation.setRole("client");
      undeployOperation.setUndeployFile(getUndeployFile());
      undeployOperation.run(monitor, workingCopyManager);
    }
    try {
      new DeleteResourcesOperation(m_files, getOperationName(), true).execute(monitor, null);
      if (getUndeployFile() != null && getUndeployFile().exists()) {
        new DeleteResourcesOperation(new IResource[]{getUndeployFile()}, getOperationName(), true).execute(monitor, null);
      }
    }
    catch (ExecutionException e) {
      ScoutSdk.logError("can not delete webservice.", e);
    }
    finally {
      getServerProject().refreshLocal(IResource.DEPTH_ONE, monitor);
    }
  }

  public void setFiles(IFile[] files) {
    m_files = files;
  }

  public IFile[] getFiles() {
    return m_files;
  }

  public void setUndeployFile(IFile undeployFile) {
    m_undeployFile = undeployFile;
  }

  public IFile getUndeployFile() {
    return m_undeployFile;
  }

  public void setServerProject(IProject serverProject) {
    m_serverProject = serverProject;
  }

  public IProject getServerProject() {
    return m_serverProject;
  }

}
