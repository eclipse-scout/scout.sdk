/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Daniel Wiehl (BSI Business Systems Integration AG) - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.ws.jaxws.operation;

import java.io.File;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.util.log.ScoutStatus;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsSdk;
import org.eclipse.scout.sdk.ws.jaxws.util.JavaFileHandle;
import org.eclipse.scout.sdk.ws.jaxws.util.JaxWsSdkUtility;

public class ExternalFileCopyOperation implements IOperation {

  private IScoutBundle m_bundle;
  private boolean m_overwrite;
  private File m_externalFile;
  private IPath m_workspacePath;
  private IFile m_fileCopied;

  @Override
  public void validate() {
    if (m_bundle == null) {
      throw new IllegalArgumentException("No bundle provided.");
    }
    if (m_externalFile == null) {
      throw new IllegalArgumentException("No file to copy provided.");
    }
    if (m_workspacePath == null) {
      throw new IllegalArgumentException("No workspace path provided.");
    }
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    IProject tempProject = null;
    try {
      IWorkspace workspace = ResourcesPlugin.getWorkspace();
      tempProject = workspace.getRoot().getProject("temp.project");
      if (!tempProject.exists()) {
        tempProject.create(new NullProgressMonitor());
      }
      if (!tempProject.isOpen()) {
        tempProject.open(new NullProgressMonitor());
      }

      IPath path = new JavaFileHandle(m_externalFile).getFullPath();
      IFile link = tempProject.getFile(path.lastSegment());
      link.createLink(path, IResource.NONE, new NullProgressMonitor());

      if (link.exists() && link.isAccessible()) {
        IProject project = m_bundle.getProject();
        IPath projectPath = project.getFullPath();

        IPath relativePath = m_workspacePath.append(new Path(link.getName()));
        IFile targetFile = project.getFile(relativePath);
        if (targetFile.exists()) {
          if (m_overwrite) {
            targetFile.delete(true, true, new NullProgressMonitor());
          }
          else {
            throw new CoreException(new ScoutStatus("File '" + targetFile.getFullPath().toString() + "' already exists."));
          }
        }

        // create folder if not exist yet
        JaxWsSdkUtility.getFolder(m_bundle, m_workspacePath, true);
        IPath destination = projectPath.append(relativePath);
        link.copy(destination, true, new NullProgressMonitor());
        m_fileCopied = m_bundle.getProject().getFile(destination.makeRelativeTo(projectPath));
      }
      else {
        throw new CoreException(new ScoutStatus("Temporary link '" + link.getFullPath().toString() + "' is not accessible."));
      }
    }
    finally {
      if (tempProject != null && tempProject.exists()) {
        try {
          tempProject.delete(true, new NullProgressMonitor());
        }
        catch (CoreException e) {
          JaxWsSdk.logError("Could not cleanup temporary project for copying file.", e);
        }
      }
    }
  }

  @Override
  public String getOperationName() {
    return ExternalFileCopyOperation.class.getName();
  }

  public IScoutBundle getBundle() {
    return m_bundle;
  }

  public void setBundle(IScoutBundle bundle) {
    m_bundle = bundle;
  }

  public File getExternalFile() {
    return m_externalFile;
  }

  public void setExternalFile(File externalFile) {
    m_externalFile = externalFile;
  }

  public IPath getWorkspacePath() {
    return m_workspacePath;
  }

  public void setWorkspacePath(IPath workspacePath) {
    m_workspacePath = workspacePath;
  }

  public IFile getFileCopied() {
    return m_fileCopied;
  }

  public boolean isOverwrite() {
    return m_overwrite;
  }

  public void setOverwrite(boolean overwrite) {
    m_overwrite = overwrite;
  }
}
