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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsSdk;

public class JavaFileHandle implements IFileHandle<File> {

  private final File m_file;

  public JavaFileHandle(final File file) {
    m_file = file;
  }

  @Override
  public IFileHandle<File> getParent() {
    return new JavaFileHandle(m_file.getParentFile());
  }

  @Override
  public InputStream getInputStream() {
    if (m_file.isFile()) {
      try {
        return new FileInputStream(m_file);
      }
      catch (final FileNotFoundException e) {
        JaxWsSdk.logError(String.format("Failed to get InputStream of file '%s'", m_file.getName()), e);
      }
    }
    return null;
  }

  @Override
  public File getFile() {
    return m_file;
  }

  @Override
  public boolean exists() {
    return m_file.exists();
  }

  @Override
  public String getName() {
    return m_file.getName();
  }

  @Override
  public IPath getFullPath() {
    return new Path(m_file.getPath());
  }

  @Override
  public IFileHandle<File> getChild(final IPath path) {
    if (path == null) {
      return null;
    }
    final File child = getFullPath().append(path).toFile();
    if (child.exists()) {
      return new JavaFileHandle(child);
    }
    return null;
  }

  @Override
  public int hashCode() {
    return m_file.hashCode();
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    return m_file.equals(((JavaFileHandle) obj).m_file);
  }
}
