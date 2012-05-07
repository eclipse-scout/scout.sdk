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
package org.eclipse.scout.sdk.operation.template;

import java.io.ByteArrayInputStream;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.scout.commons.IOUtility;
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.util.log.ScoutStatus;
import org.eclipse.scout.sdk.util.resources.ResourceUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.osgi.framework.Bundle;

public class InstallTextFileOperation implements IOperation {

  protected final String m_srcPath;
  protected final String m_dstPath;
  protected final IProject m_dstProject;
  private final Bundle m_sourceBoundle;
  private final Map<String, String> m_properties;
  private IFile m_createdFile;

  public InstallTextFileOperation(String srcPath, String dstPath, IProject dstProject) {
    this(srcPath, dstPath, dstProject, null);
  }

  public InstallTextFileOperation(String srcPath, String dstPath, IProject dstProject, Map<String, String> properties) {
    this(srcPath, dstPath, Platform.getBundle(ScoutSdk.PLUGIN_ID), dstProject, properties);
  }

  public InstallTextFileOperation(String srcPath, String dstPath, Bundle sourceBoundle, IProject dstProject, Map<String, String> properties) {
    m_srcPath = srcPath;
    m_dstPath = dstPath;
    m_sourceBoundle = sourceBoundle;
    m_dstProject = dstProject;
    m_properties = properties;
  }

  @Override
  public String getOperationName() {
    return "Install file " + getSrcPath() + " to /" + m_dstProject.getName() + "/" + getDstPath();
  }

  @Override
  public void validate() throws IllegalArgumentException {
    if (getDstProject() == null) {
      throw new IllegalArgumentException("project can not be null.");
    }
    if (getSrcPath() == null) {
      throw new IllegalArgumentException("source path can not be null.");
    }
    if (getDstPath() == null) {
      throw new IllegalArgumentException("destination path can not be null.");
    }
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    try {
      String s = new String(IOUtility.getContent(FileLocator.openStream(m_sourceBoundle, new Path(getSrcPath()), false)), "UTF-8");
      if (getProperties() != null) {
        for (Map.Entry<String, String> e : getProperties().entrySet()) {
          s = s.replace("@@" + e.getKey() + "@@", e.getValue());
        }
      }

      // check that all variables have been substituted
      Matcher m = Pattern.compile("@@([^@]+)@@").matcher(s);
      if (m.find()) {
        throw new CoreException(new ScoutStatus("Missing tag replacement for tag " + m.group(1) + " in template " + getSrcPath()));
      }

      // create file
      m_createdFile = m_dstProject.getFile(m_dstPath);
      if (m_createdFile.exists()) {
        m_createdFile.delete(true, false, monitor);
      }
      else {
        ResourceUtility.mkdirs(m_createdFile, monitor);
      }

      m_createdFile.create(new ByteArrayInputStream(s.getBytes()), true, monitor);
    }
    catch (Exception e) {
      ScoutSdk.logError("could not install text file.", e);
    }
  }

  public String getSrcPath() {
    return m_srcPath;
  }

  public String getDstPath() {
    return m_dstPath;
  }

  public IProject getDstProject() {
    return m_dstProject;
  }

  public IFile getCreatedFile() {
    return m_createdFile;
  }

  protected Map<String, String> getProperties() {
    return m_properties;
  }
}
