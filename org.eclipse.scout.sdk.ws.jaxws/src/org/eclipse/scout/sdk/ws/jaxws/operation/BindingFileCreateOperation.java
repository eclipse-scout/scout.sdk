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
/**
 *
 */
package org.eclipse.scout.sdk.ws.jaxws.operation;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.xmlparser.ScoutXmlDocument;
import org.eclipse.scout.commons.xmlparser.ScoutXmlDocument.ScoutXmlElement;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.util.log.ScoutStatus;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsRuntimeClasses;
import org.eclipse.scout.sdk.ws.jaxws.util.JaxWsSdkUtility;

public class BindingFileCreateOperation implements IOperation {

  private IScoutBundle m_bundle;
  private IPath m_projectRelativePath;
  private IFile m_wsdlLocation;
  private String m_schemaTargetNamespace;
  private boolean m_createGlobalBindingSection;
  private IFolder m_wsdlDestinationFolder;

  @Override
  public void validate() throws IllegalArgumentException {
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

    ScoutXmlDocument xmlDocument = new ScoutXmlDocument();

    xmlDocument.setXmlVersion("1.0");
    xmlDocument.setXmlEncoding("UTF-8");
    xmlDocument.setPrettyPrint(true);

    ScoutXmlElement rootXml = createBindingsRootNode(xmlDocument);
    xmlDocument.setRoot(rootXml);

    rootXml.setNamespace("jaxws", "http://java.sun.com/xml/ns/jaxws");
    rootXml.setNamespace("jaxb", "http://java.sun.com/xml/ns/jaxb");
    rootXml.setNamespace("wsdl", "http://schemas.xmlsoap.org/wsdl/");
    rootXml.setNamespace("xsd", "http://www.w3.org/2001/XMLSchema");
    rootXml.setNamespace("xjc", "http://java.sun.com/xml/ns/jaxb/xjc");
    rootXml.setName("jaxws:bindings");
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
      ScoutXmlElement globalBindingsXml = rootXml.addChild();
      globalBindingsXml.setName("jaxb:globalBindings");
      IType utcDateAdapterType = TypeUtility.getType(JaxWsRuntimeClasses.UtcDateAdapter);

      ScoutXmlElement bindingJavaTypeXml = globalBindingsXml.addChild();
      bindingJavaTypeXml.setName("xjc:javaType");
      bindingJavaTypeXml.setAttribute("name", Date.class.getName());
      bindingJavaTypeXml.setAttribute("xmlType", "xsd:date");
      bindingJavaTypeXml.setAttribute("adapter", utcDateAdapterType.getFullyQualifiedName());

      bindingJavaTypeXml = globalBindingsXml.addChild();
      bindingJavaTypeXml.setName("xjc:javaType");
      bindingJavaTypeXml.setAttribute("name", Date.class.getName());
      bindingJavaTypeXml.setAttribute("xmlType", "xsd:time");
      bindingJavaTypeXml.setAttribute("adapter", utcDateAdapterType.getFullyQualifiedName());

      bindingJavaTypeXml = globalBindingsXml.addChild();
      bindingJavaTypeXml.setName("xjc:javaType");
      bindingJavaTypeXml.setAttribute("name", Date.class.getName());
      bindingJavaTypeXml.setAttribute("xmlType", "xsd:dateTime");
      bindingJavaTypeXml.setAttribute("adapter", utcDateAdapterType.getFullyQualifiedName());
    }
    /*
     *  legacy: the following code dynamically creates adapters when stub is generated.
     *  They simply act as delegates to the actual adapters.
     *  If no specific package is configured (recommended approach), these adapters are
     *  placed in package org.w3._2001.xmlschema. Thereby, various adapters are named in
     *  sequence (Adapter1.java, Adapter2.java, ...).
     *  This causes problems as different stub overwrite the respective adapters.
     */
//    ScoutXmlElement bindingJavaTypeXml = globalBindingsXml.addChild();
//    bindingJavaTypeXml.setName("jaxb:javaType");
//    bindingJavaTypeXml.setAttribute("name", Date.class.getName());
//    bindingJavaTypeXml.setAttribute("xmlType", "xsd:dateTime");
//    bindingJavaTypeXml.setAttribute("parseMethod", "org.eclipse.scout.jaxws.adapters.DateAdapterISO8601UTC.parseDate");
//    bindingJavaTypeXml.setAttribute("printMethod", "org.eclipse.scout.jaxws.adapters.DateAdapterISO8601UTC.printDate");

    ByteArrayOutputStream os = new ByteArrayOutputStream();
    try {
      xmlDocument.write(os);
      InputStream is = new ByteArrayInputStream(os.toByteArray());
      bindingFile.setContents(is, true, false, monitor);
    }
    catch (IOException e) {
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

  private ScoutXmlElement createBindingsRootNode(ScoutXmlDocument xmlDocument) {
    // do not escape the quotation character because it used to specify XPath node constraint containing the targetNamespace definition
    return xmlDocument.new ScoutXmlElement("bindings") {

      private static final long serialVersionUID = 1L;

      @Override
      protected void writeEncoded(BufferedWriter bufferedWriter, String string) throws IOException {
        if (string == null) {
          return;
        }
        for (int i = 0; i < string.length(); i++) {
          char ch = string.charAt(i);

          if (ch == '\'') { // do not escape this character
            bufferedWriter.write(ch);
          }
          else {
            String escaped = ScoutXmlDocument.XML_ENTITIES.get(ch);
            if (escaped != null) {
              bufferedWriter.write(escaped);
            }
            else {
              bufferedWriter.write(ch);
            }
          }
        }
      }
    };
  }
}
