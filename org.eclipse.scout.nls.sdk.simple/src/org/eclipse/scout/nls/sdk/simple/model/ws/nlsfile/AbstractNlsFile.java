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
package org.eclipse.scout.nls.sdk.simple.model.ws.nlsfile;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.scout.commons.beans.BasicPropertySupport;
import org.eclipse.scout.nls.sdk.internal.NlsCore;
import org.eclipse.scout.nls.sdk.simple.internal.NlsSdkSimple;

public abstract class AbstractNlsFile {

  private static final String PROP_NLS_TYPE_NAME = "nlsTypeName";
  public static final String MANIFEST_CLASS = "Nls-Class";

  private final BasicPropertySupport m_propertySupport;
  private final IProject m_project;

  protected AbstractNlsFile(IFile file) {
    m_propertySupport = new BasicPropertySupport(this);
    m_project = file.getProject();
    parseInput(file);
  }

  public abstract boolean isReadOnly();

  public static AbstractNlsFile loadNlsFile(IFile file) throws CoreException {
    AbstractNlsFile nlsFile = null;
    if (file != null && file.exists()) {
      if (file.isReadOnly()) {
        nlsFile = new PlatformNlsFile(file);
      }
      else {
        nlsFile = new WorkspaceNlsFile(file);
      }
    }
    return nlsFile;
  }

  protected void parseInput(IFile file) {
    Properties props = new Properties();
    InputStream io = null;
    try {
      io = file.getContents();
      props.load(io);
    }
    catch (Exception e) {
      NlsCore.logWarning("could not open stream to read NLS file :'" + file.getFullPath() + "'", e);
    }
    finally {
      if (io != null) {
        try {
          io.close();
        }
        catch (IOException e) {
          NlsSdkSimple.logWarning("could not close input stream of '" + file.getFullPath() + "'.", e);
        }
      }
    }
    m_propertySupport.setPropertyString(PROP_NLS_TYPE_NAME, props.getProperty(MANIFEST_CLASS));
  }

  public void addPropertyChangeListener(PropertyChangeListener listener) {
    m_propertySupport.addPropertyChangeListener(listener);
  }

  public void removePropertyChangeListener(PropertyChangeListener listener) {
    m_propertySupport.removePropertyChangeListener(listener);
  }

  /**
   * @return the fully qualified class name (e.g. java.lang.String)
   */
  public String getNlsTypeName() {
    return m_propertySupport.getPropertyString(PROP_NLS_TYPE_NAME);
  }

  public IProject getProject() {
    return m_project;
  }
}
