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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.pde.PdeUtility;
import org.eclipse.scout.sdk.typecache.IScoutWorkingCopyManager;

public class WebServiceUndeployOperation implements IOperation {

  private IFile m_undeployFile;
  private IProject m_project;
  private String m_role;

  @Override
  public String getOperationName() {
    return "Undeploy " + m_undeployFile.getFullPath().toString();
  }

  @Override
  public void validate() throws IllegalArgumentException {
    if (getUndeployFile() == null || !getUndeployFile().exists()) {
      throw new IllegalArgumentException("undeploy file can not be null.");
    }
    if (getProject() == null) {
      throw new IllegalArgumentException("project can not be null.");
    }

  }

  @Override
  public void run(IProgressMonitor monitor, IScoutWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    PdeUtility.launchLocalJavaApplicationAndWait(
        "Scout - Undeploy WSDD",
        getProject().getName(),
        "org.apache.axis.utils.Admin",
        getRole() + " \"" + getUndeployFile().getLocation().toOSString() + "\"",
        true,
        monitor
        );

  }

  public void setUndeployFile(IFile undeployFile) {
    m_undeployFile = undeployFile;
  }

  public IFile getUndeployFile() {
    return m_undeployFile;
  }

  public void setProject(IProject project) {
    m_project = project;
  }

  public IProject getProject() {
    return m_project;
  }

  public void setRole(String role) {
    m_role = role;
  }

  public String getRole() {
    return m_role;
  }

}
