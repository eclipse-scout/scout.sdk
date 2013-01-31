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
package org.eclipse.scout.sdk.ws.jaxws.util;

import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsSdk;

public class EclipseFileHandle implements IFileHandle<IFile> {

  private final IResource m_resource;

  public EclipseFileHandle(final IResource resource) {
    m_resource = resource;
  }

  @Override
  public IFileHandle<IFile> getParent() {
    return new EclipseFileHandle(m_resource.getParent());
  }

  @Override
  public InputStream getInputStream() {
    if (m_resource instanceof IFile) {
      try {
        return ((IFile) m_resource).getContents();
      }
      catch (final CoreException e) {
        JaxWsSdk.logError(String.format("Failed to get InputStream of file '%s'", m_resource.getName()), e);
      }
    }
    return null;
  }

  @Override
  public boolean exists() {
    return m_resource.exists();
  }

  @Override
  public String getName() {
    return m_resource.getName();
  }

  @Override
  public IPath getFullPath() {
    return m_resource.getLocation();
  }

  @Override
  public IFileHandle<IFile> getChild(final IPath path) {
    if (path == null) {
      return null;
    }
    final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
    final IFile child = root.getFile(m_resource.getFullPath().append(path));
    if (child.exists()) {
      return new EclipseFileHandle(child);
    }
    return null;
  }

  @Override
  public IFile getFile() {
    if (m_resource instanceof IFile) {
      return (IFile) m_resource;
    }
    return null;
  }

  @Override
  public int hashCode() {
    return m_resource.hashCode();
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    return m_resource.equals(((EclipseFileHandle) obj).m_resource);
  }
}
