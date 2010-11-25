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
package org.eclipse.scout.sdk.operation.axis;

import java.io.File;
import java.net.URI;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.pde.PdeUtility;
import org.eclipse.scout.sdk.typecache.IScoutWorkingCopyManager;

public class WsdlToJavaOperation implements IOperation {

  private URI m_wsdlUri;
  private IProject m_project;
  private File m_sourceDir;
  private String m_implClassQName;
  private String m_username;
  private String m_password;

  @Override
  public String getOperationName() {
    return "WSDL to Java...";
  }

  @Override
  public void validate() throws IllegalArgumentException {
    if (getWsdlUri() == null) {
      throw new IllegalArgumentException("wsdl URI can not be null.");
    }
    if (getProject() == null) {
      throw new IllegalArgumentException("project can not be null.");
    }
  }

  @Override
  public void run(IProgressMonitor monitor, IScoutWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    String implClassArg = "";
    if (!StringUtility.isNullOrEmpty(getImplClassQName())) {
      implClassArg = " -c \"" + getImplClassQName() + "\"";
    }
    String authentication = "";
    if (!StringUtility.isNullOrEmpty(getUsername()) && getPassword() != null) {
      authentication = " -U " + getUsername() + " -P " + getPassword();
    }
    PdeUtility.launchLocalJavaApplicationAndWait("BSI CASE - Axis Wsdl2Java",
        getProject().getName(),
        "org.apache.axis.wsdl.WSDL2Java", "-o \"" + getSourceDir().getAbsolutePath() + "\" -d Request -a -s -S false " + implClassArg + authentication + " \"" + getWsdlUri().toString() + "\"",
        true, monitor);
  }

  public void setWsdlUri(URI wsdlUri) {
    m_wsdlUri = wsdlUri;
  }

  public URI getWsdlUri() {
    return m_wsdlUri;
  }

  public void setProject(IProject project) {
    m_project = project;
  }

  public IProject getProject() {
    return m_project;
  }

  public void setSourceDir(File sourceDir) {
    m_sourceDir = sourceDir;
  }

  public File getSourceDir() {
    return m_sourceDir;
  }

  public void setImplClassQName(String implClassQName) {
    m_implClassQName = implClassQName;
  }

  public String getImplClassQName() {
    return m_implClassQName;
  }

  public void setUsername(String username) {
    m_username = username;
  }

  public String getUsername() {
    return m_username;
  }

  public void setPassword(String password) {
    m_password = password;
  }

  public String getPassword() {
    return m_password;
  }

}
