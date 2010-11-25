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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.operation.IDeleteOperation;
import org.eclipse.scout.sdk.pde.BuildProperties;
import org.eclipse.scout.sdk.pde.PdeUtility;
import org.eclipse.scout.sdk.typecache.IScoutWorkingCopyManager;
import org.eclipse.scout.sdk.util.ApacheAxisUtility;

public class AxisWebServiceConsumerNewOperation implements IDeleteOperation {

  private IFile[] m_files;
  private IFile m_deployFile;
  private IFile m_contentDescFile;
  private IProject m_serverProject;
  private HashMap<IPath, File> m_pathToTempDir;

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
    // create a content description file for this service
    StringBuilder buf = new StringBuilder();
    int fileIndex = 0;
    for (IFile ifile : m_files) {
      if (ifile.getName().endsWith(".java")) {
        IPath path = ifile.getFullPath();
        path = path.removeFirstSegments(path.matchingFirstSegments(getServerProject().getFullPath()));
        buf.append("file" + fileIndex + "=" + path.toString() + "\n");
        fileIndex++;
      }
    }
    PdeUtility.createFile(getContentDescFile(), new ByteArrayInputStream(buf.toString().getBytes()), monitor);
    // copy files
    for (IFile ifile : m_files) {
      File tmpFile = getPathToTempDir().get(ifile.getFullPath());
      FileInputStream in = null;
      try {
        in = new FileInputStream(tmpFile);
        PdeUtility.createFile(ifile, in, monitor);
      }
      catch (FileNotFoundException e) {
        ScoutSdk.logWarning("file not found.", e);
      }
      finally {
        try {
          if (in != null) {
            in.close();
          }
        }
        catch (IOException e) {
          // void
        }
      }
    }
    // add client-config.wsdd to build.properties
    try {
      PdeUtility.addBuildPropertiesFiles(getServerProject().getProject(), BuildProperties.PROP_BIN_INCLUDES, new String[]{"client-config.wsdd"});
    }
    catch (IOException e) {
      ScoutSdk.logError("could not add build proeprty file.", e);
    }
    ApacheAxisUtility.runDeploy(getDeployFile().getLocation().toFile(), "client", getServerProject(), monitor);
    // refresh project
    getServerProject().refreshLocal(IResource.DEPTH_INFINITE, monitor);

  }

  public void setFiles(IFile[] files) {
    m_files = files;
  }

  public IFile[] getFiles() {
    return m_files;
  }

  public void setDeployFile(IFile undeployFile) {
    m_deployFile = undeployFile;
  }

  public IFile getDeployFile() {
    return m_deployFile;
  }

  public void setServerProject(IProject serverProject) {
    m_serverProject = serverProject;
  }

  public IProject getServerProject() {
    return m_serverProject;
  }

  public void setContentDescFile(IFile contentDescFile) {
    m_contentDescFile = contentDescFile;
  }

  public IFile getContentDescFile() {
    return m_contentDescFile;
  }

  public void setPathToTempDir(HashMap<IPath, File> pathToTempDir) {
    m_pathToTempDir = pathToTempDir;
  }

  public HashMap<IPath, File> getPathToTempDir() {
    return m_pathToTempDir;
  }

}
