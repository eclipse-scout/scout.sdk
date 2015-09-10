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
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.lang3.Validate;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.scout.sdk.core.util.CoreUtils;
import org.eclipse.scout.sdk.s2e.internal.S2ESdkActivator;
import org.eclipse.scout.sdk.s2e.log.ScoutStatus;

/**
 * <h3>{@link ResourceWriteOperation}</h3>
 * <p>
 * Change the content of a resource file
 *
 * @author imo
 * @since 5.1.0
 */
public class ResourceWriteOperation implements IWorkspaceBlockingOperation {
  private final IFile m_file;
  private final String m_content;

  public ResourceWriteOperation(IFile file, String content) {
    if (file == null) {
      throw new IllegalArgumentException("file is null");
    }
    m_file = file;
    m_content = content;
  }

  public ResourceWriteOperation(IFolder resourceFolder, String packageName, String fileName, String content) {
    if (resourceFolder == null) {
      throw new IllegalArgumentException("resourceFolder is null");
    }
    IFolder folder = packageName != null ? resourceFolder.getFolder(packageName.replace('.', '/')) : resourceFolder;
    m_file = folder.getFile(fileName);
    m_content = content;
  }

  @Override
  public String getOperationName() {
    return "Change " + m_file.getProjectRelativePath();
  }

  public IFile getFile() {
    return m_file;
  }

  @Override
  public void validate() {
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    Validate.notNull(workingCopyManager);
    String newSource = m_content;

    // compare
    String oldSource = getExistingContent(m_file);
    if (isSourceEquals(oldSource, newSource)) {
      oldSource = null;
      return;
    }
    oldSource = null;

    if (monitor.isCanceled()) {
      return;
    }

    // write new source
    try {
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
      m_file.refreshLocal(1, monitor);
    }
    catch (Exception e) {
      S2ESdkActivator.logError("could not store '" + m_file.getProjectRelativePath() + "'.", e);
    }
  }

  private static String getExistingContent(IFile f) throws CoreException {
    if (!f.exists()) {
      return null;
    }
    String charsetName = f.getCharset();
    try (InputStream contents = f.getContents()) {
      return CoreUtils.inputStreamToString(contents, charsetName).toString();
    }
    catch (IOException e) {
      throw new CoreException(new ScoutStatus("Unable to read file '" + f.getFullPath().toOSString() + "'.", e));
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

  private static boolean isSourceEquals(String source1, String source2) {
    if (source1 == null && source2 == null) {
      return true;
    }
    else if (source1 == null) {
      return false;
    }
    else if (source2 == null) {
      return false;
    }
    if (source1.length() != source2.length()) {
      return false;
    }
    return source1.equals(source2);
  }

  public static IFolder findPrimaryResourceFolder(IJavaProject project) {
    return ((IProject) project.getResource()).getFolder("src/main/resources");
  }
}
