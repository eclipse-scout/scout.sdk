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
package org.eclipse.scout.sdk.s2e.nls.internal.simpleProject;

import java.io.InputStream;
import java.util.Properties;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.scout.sdk.s2e.nls.NlsCore;

public abstract class AbstractNlsFile {

  public static final String MANIFEST_CLASS = "Nls-Class";

  private final IProject m_project;
  private String m_nlsTypeName;

  protected AbstractNlsFile(IFile file) {
    m_project = file.getProject();
    parseInput(file);
  }

  public abstract boolean isReadOnly();

  public static AbstractNlsFile loadNlsFile(IFile file) {
    if (file == null || !file.exists()) {
      return null;
    }

    if (file.isReadOnly()) {
      return new PlatformNlsFile(file);
    }
    return new WorkspaceNlsFile(file);
  }

  protected void parseInput(IFile file) {
    Properties props = new Properties();
    try (InputStream io = file.getContents()) {
      props.load(io);
    }
    catch (Exception e) {
      NlsCore.logWarning("could not open stream to read NLS file :'" + file.getFullPath() + "'", e);
    }
    m_nlsTypeName = props.getProperty(MANIFEST_CLASS);
  }

  /**
   * @return the fully qualified class name (e.g. java.lang.String)
   */
  public String getNlsTypeName() {
    return m_nlsTypeName;
  }

  public IProject getProject() {
    return m_project;
  }
}
