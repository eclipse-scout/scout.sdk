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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Date;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.util.log.ScoutStatus;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsRuntimeClasses;
import org.eclipse.scout.sdk.ws.jaxws.util.JaxWsSdkUtility;
import org.w3c.dom.Element;

public class BindingFileCreateOperation implements IOperation {

  private IScoutBundle m_bundle;
  private IPath m_projectRelativePath;
  private IFile m_wsdlLocation;
  private String m_schemaTargetNamespace;
  private boolean m_createGlobalBindingSection;
  private IFolder m_wsdlDestinationFolder;

  @Override
  public void validate() {
    if (m_bundle == null) {
      throw new IllegalArgumentException("no bundle set");
    }

    if (m_projectRelativePath == null) {
      throw new IllegalArgumentException("no projectRelativePath set");
    }

    if (m_wsdlDestinationFolder == null) {
      throw new IllegalArgumentException("WSDL destination path must not be null");
    }
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    IFile bindingFile = JaxWsSdkUtility.getFile(m_bundle, m_projectRelativePath, true);

    org.w3c.dom.Document xmlDocument = JaxWsSdkUtility.createNewXmlDocument("jaxws:bindings");
    Element rootXml = xmlDocument.getDocumentElement();
    rootXml.setAttribute("xmlns:jaxws", "http://java.sun.com/xml/ns/jaxws");
    rootXml.setAttribute("xmlns:jaxb", "http://java.sun.com/xml/ns/jaxb");
    rootXml.setAttribute("xmlns:wsdl", "http://schemas.xmlsoap.org/wsdl/");
    rootXml.setAttribute("xmlns:xsd", "http://www.w3.org/2001/XMLSchema");
    rootXml.setAttribute("xmlns:xjc", "http://java.sun.com/xml/ns/jaxb/xjc");
    rootXml.setAttribute("version", "2.0");
    if (m_wsdlLocation != null) {
      IPath wsdlFolderProjectRelativePath = m_wsdlDestinationFolder.getProjectRelativePath().makeRelativeTo(JaxWsSdkUtility.getParentFolder(m_bundle, bindingFile).getProjectRelativePath());
      rootXml.setAttribute("wsdlLocation", wsdlFolderProjectRelativePath.append(m_wsdlLocation.getName()).toString());
    }
    if (StringUtility.hasText(m_schemaTargetNamespace)) {
      rootXml.setAttribute("node", "wsdl:definitions/wsdl:types/xsd:schema[@targetNamespace='" + m_schemaTargetNamespace + "']");
    }
    else {
      rootXml.setAttribute("node", "wsdl:definitions/wsdl:types/xsd:schema");
    }

    if (isCreateGlobalBindingSection()) {
      Element globalBindingsXml = xmlDocument.createElement("jaxb:globalBindings");
      rootXml.appendChild(globalBindingsXml);

      Element bindingJavaTypeXml = xmlDocument.createElement("xjc:javaType");
      bindingJavaTypeXml.setAttribute("name", Date.class.getName());
      bindingJavaTypeXml.setAttribute("xmlType", "xsd:date");
      bindingJavaTypeXml.setAttribute("adapter", JaxWsRuntimeClasses.UtcDateAdapter);
      globalBindingsXml.appendChild(bindingJavaTypeXml);

      bindingJavaTypeXml = xmlDocument.createElement("xjc:javaType");
      bindingJavaTypeXml.setAttribute("name", Date.class.getName());
      bindingJavaTypeXml.setAttribute("xmlType", "xsd:time");
      bindingJavaTypeXml.setAttribute("adapter", JaxWsRuntimeClasses.UtcDateAdapter);
      globalBindingsXml.appendChild(bindingJavaTypeXml);

      bindingJavaTypeXml = xmlDocument.createElement("xjc:javaType");
      bindingJavaTypeXml.setAttribute("name", Date.class.getName());
      bindingJavaTypeXml.setAttribute("xmlType", "xsd:dateTime");
      bindingJavaTypeXml.setAttribute("adapter", JaxWsRuntimeClasses.UtcDateAdapter);
      globalBindingsXml.appendChild(bindingJavaTypeXml);
    }

    try {
      InputStream is = new ByteArrayInputStream(JaxWsSdkUtility.getXmlContent(xmlDocument).getBytes("UTF-8"));
      bindingFile.setContents(is, true, false, monitor);
    }
    catch (UnsupportedEncodingException e) {
      throw new CoreException(new ScoutStatus(IStatus.ERROR, "could not create binding file.", e));
    }
  }

  @Override
  public String getOperationName() {
    return BindingFileCreateOperation.class.getName();
  }

  public IScoutBundle getBundle() {
    return m_bundle;
  }

  public void setBundle(IScoutBundle bundle) {
    m_bundle = bundle;
  }

  public IPath getProjectRelativePath() {
    return m_projectRelativePath;
  }

  public void setProjectRelativePath(IPath projectRelativePath) {
    m_projectRelativePath = projectRelativePath;
  }

  public IFile getWsdlLocation() {
    return m_wsdlLocation;
  }

  public void setWsdlLocation(IFile wsdlLocation) {
    m_wsdlLocation = wsdlLocation;
  }

  public String getSchemaTargetNamespace() {
    return m_schemaTargetNamespace;
  }

  public void setSchemaTargetNamespace(String schemaTargetNamespace) {
    m_schemaTargetNamespace = schemaTargetNamespace;
  }

  public boolean isCreateGlobalBindingSection() {
    return m_createGlobalBindingSection;
  }

  public void setCreateGlobalBindingSection(boolean createGlobalBindingSection) {
    m_createGlobalBindingSection = createGlobalBindingSection;
  }

  public IFolder getWsdlDestinationFolder() {
    return m_wsdlDestinationFolder;
  }

  public void setWsdlDestinationFolder(IFolder wsdlDestinationFolder) {
    m_wsdlDestinationFolder = wsdlDestinationFolder;
  }
}
