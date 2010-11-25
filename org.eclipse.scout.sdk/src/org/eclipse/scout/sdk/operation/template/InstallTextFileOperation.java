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

import java.io.File;
import java.io.FileWriter;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.scout.commons.IOUtility;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.ScoutStatus;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.typecache.IScoutWorkingCopyManager;

public class InstallTextFileOperation implements IOperation {

  protected final String m_srcPath;
  protected final String m_dstPath;
  protected final IProject m_dstProject;
  private final ITemplateVariableSet m_templateBinding;

  public InstallTextFileOperation(String srcPath, String dstPath, IProject dstProject) {
    this(srcPath, dstPath, dstProject, TemplateVariableSet.createNew(dstProject));
  }

  public InstallTextFileOperation(String srcPath, String dstPath, IProject dstProject, ITemplateVariableSet templateBinding) {
    m_srcPath = srcPath;
    m_dstPath = dstPath;
    m_dstProject = dstProject;
    m_templateBinding = templateBinding;
  }

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
    if (getDstProject() == null) {
      throw new IllegalArgumentException("destination path can not be null.");
    }

  }

  public void run(IProgressMonitor monitor, IScoutWorkingCopyManager workingCopyManager) throws CoreException {
    try {
      String s = new String(IOUtility.getContent(FileLocator.openStream(Platform.getBundle(ScoutSdk.PLUGIN_ID), new Path(getSrcPath()), false)), "UTF-8");
      for (Map.Entry<String, String> e : m_templateBinding.entrySet()) {
        s = s.replace("@@" + e.getKey() + "@@", e.getValue());
      }
      Matcher m = Pattern.compile("@@([^@]+)@@").matcher(s);
      if (m.find()) {
        throw new CoreException(new ScoutStatus("Missing tag replacement for tag " + m.group(1) + " in template " + getSrcPath()));
      }
      File f = new File(new File(m_dstProject.getLocation().toOSString()), m_dstPath);
      f.getParentFile().mkdirs();
      IOUtility.writeContent(new FileWriter(f), s);
      m_dstProject.getFile(new Path(m_dstPath)).refreshLocal(IResource.DEPTH_ZERO, monitor);
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

  public ITemplateVariableSet getTemplateBinding() {
    return m_templateBinding;
  }

}
