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

import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.ws.jaxws.util.JaxWsSdkUtility;

public class SourceFolderCreateOrUpdateOperation implements IOperation {

  private IScoutBundle m_bundle;
  private IPath m_sourceFolder;

  public IScoutBundle getBundle() {
    return m_bundle;
  }

  public void setBundle(IScoutBundle bundle) {
    m_bundle = bundle;
  }

  public IPath getSourceFolder() {
    return m_sourceFolder;
  }

  public void setSourceFolder(IPath sourceFolder) {
    m_sourceFolder = sourceFolder;
  }

  @Override
  public void validate() {
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    IFolder folder = JaxWsSdkUtility.getFolder(m_bundle, m_sourceFolder.makeRelativeTo(m_bundle.getProject().getFullPath()), true);
    List<IClasspathEntry> classpathEntries = new LinkedList<>();
    for (IClasspathEntry entry : m_bundle.getJavaProject().getRawClasspath()) {
      if (entry.getPath().equals(folder.getFullPath())) {
        return; // source folder exists already
      }
      classpathEntries.add(entry);
    }
    classpathEntries.add(JavaCore.newSourceEntry(folder.getFullPath()));
    m_bundle.getJavaProject().setRawClasspath(classpathEntries.toArray(new IClasspathEntry[classpathEntries.size()]), m_bundle.getJavaProject().getOutputLocation(), monitor);
  }

  @Override
  public String getOperationName() {
    return SourceFolderCreateOrUpdateOperation.class.getName();
  }
}
