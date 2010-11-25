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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.pde.PdeUtility;
import org.eclipse.scout.sdk.typecache.IScoutWorkingCopyManager;

public class JavaToWsdlOperation implements IOperation {

  private String m_serviceClassname;
  private String[][] m_packageToNamespaceMap;
  private String m_publishUrl;
  private String m_namespace;
  private String m_wsdlAbsolutePath;
  private IProject m_project;

  @Override
  public String getOperationName() {

    return "Java 2 WSDL for '" + getWsdlAbsolutePath() + "'";
  }

  @Override
  public void validate() throws IllegalArgumentException {
    if (StringUtility.isNullOrEmpty(getServiceClassname())) {
      throw new IllegalArgumentException("service classname can not be null");
    }
    if (StringUtility.isNullOrEmpty(getPublishUrl())) {
      throw new IllegalArgumentException("publish url can not be null.");
    }
    if (StringUtility.isNullOrEmpty(getWsdlAbsolutePath())) {
      throw new IllegalArgumentException("path to wsdl can not be null.");
    }
    if (getProject() == null) {
      throw new IllegalArgumentException("project can not be null.");
    }
  }

  @Override
  public void run(IProgressMonitor monitor, IScoutWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    String serviceName = getServiceClassname().replaceAll("^(.*\\.)?([^.]+)$", "$2");
    //
    StringBuilder b = new StringBuilder();
    if (getPackageToNamespaceMap() != null) {
      for (String[] pair : getPackageToNamespaceMap()) {
        b.append(" -p\"");
        b.append(pair[0]);
        b.append("\" \"");
        b.append(pair[1]);
        b.append("\"");
      }
    }
    String pArg = b.toString();
    PdeUtility.launchLocalJavaApplicationAndWait(
        "BSI CASE - Axis Java2Wsdl",
        getProject().getName(),
        "org.apache.axis.wsdl.Java2WSDL",
        "-o \"" + getWsdlAbsolutePath() + "\" -l \"" + getPublishUrl() + "\" -n \"" + getNamespace() + "\" -P " + serviceName + "PortType -S " + serviceName + "SOAPService -s " + serviceName + "Port -w All -T 1.2 -A OPERATION -y RPC -u LITERAL" + pArg + " \"" + getServiceClassname() + "\"",
        true,
        monitor
        );

  }

  public void setServiceClassname(String serviceClassname) {
    m_serviceClassname = serviceClassname;
  }

  public String getServiceClassname() {
    return m_serviceClassname;
  }

  public void setPackageToNamespaceMap(String[][] packageToNamespaceMap) {
    m_packageToNamespaceMap = packageToNamespaceMap;
  }

  public String[][] getPackageToNamespaceMap() {
    return m_packageToNamespaceMap;
  }

  public void setPublishUrl(String publishUrl) {
    m_publishUrl = publishUrl;
  }

  public String getPublishUrl() {
    return m_publishUrl;
  }

  public void setNamespace(String namespace) {
    m_namespace = namespace;
  }

  public String getNamespace() {
    return m_namespace;
  }

  public void setWsdlAbsolutePath(String wsdlAbsolutePath) {
    m_wsdlAbsolutePath = wsdlAbsolutePath;
  }

  public String getWsdlAbsolutePath() {
    return m_wsdlAbsolutePath;
  }

  public void setProject(IProject project) {
    m_project = project;
  }

  public IProject getProject() {
    return m_project;
  }

}
