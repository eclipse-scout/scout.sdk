/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.s2e.workspace;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.apache.commons.lang3.Validate;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.sdk.core.util.SdkLog;

/**
 * <h3>{@link ResourceWriteOperation}</h3>
 * <p>
 * Change the content of a resource file
 *
 * @author Ivan Motsch
 * @since 5.1.0
 */
public class ResourceWriteOperation implements IOperation {
  private final IFile m_file;
  private final String m_content;

  public ResourceWriteOperation(IFile file, String content) {
    m_file = file;
    m_content = content;
  }

  public ResourceWriteOperation(IFolder resourceFolder, String packageName, String fileName, String content) {
    if (resourceFolder == null) {
      m_file = null;
    }
    else {
      IFolder folder = packageName != null ? resourceFolder.getFolder(packageName.replace('.', '/')) : resourceFolder;
      m_file = folder.getFile(fileName);
    }
    m_content = content;
  }

  @Override
  public String getOperationName() {
    return "Write " + m_file.getProjectRelativePath();
  }

  public IFile getFile() {
    return m_file;
  }

  @Override
  public void validate() {
    if (m_file == null) {
      throw new IllegalArgumentException("file is null");
    }
    if (m_content == null) {
      throw new IllegalArgumentException("content is null");
    }
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    Validate.notNull(workingCopyManager);
    monitor.subTask("Write " + m_file.getProjectRelativePath());
    String newSource = m_content;

    try {
      // compare
      String oldSource = CompilationUnitWriteOperation.getContentOfFile(m_file);
      if (CompilationUnitWriteOperation.isSourceEqual(oldSource, newSource)) {
        oldSource = null;
        return;
      }
      oldSource = null;

      if (monitor.isCanceled()) {
        return;
      }

      // write new source
      String charsetName = m_file.getCharset();
      try (InputStream stream = new ByteArrayInputStream(newSource.getBytes(charsetName))) {
        if (!m_file.exists()) {
          mkdirs(m_file.getParent(), monitor);
          m_file.create(stream, true, monitor);
        }
        else {
          m_file.setContents(stream, true, true, monitor);
        }
      }
      m_file.refreshLocal(IResource.DEPTH_ONE, monitor);
    }
    catch (Exception e) {
      SdkLog.error("could not store '" + m_file.getProjectRelativePath() + "'.", e);
    }
  }

  private static void mkdirs(IContainer dir, IProgressMonitor monitor) throws CoreException {
    if (dir.getType() != IResource.FOLDER) {
      return;
    }
    if (!dir.getParent().exists()) {
      mkdirs(dir.getParent(), monitor);
    }
    if (!dir.exists()) {
      ((IFolder) dir).create(true, false, monitor);
    }
  }
}
