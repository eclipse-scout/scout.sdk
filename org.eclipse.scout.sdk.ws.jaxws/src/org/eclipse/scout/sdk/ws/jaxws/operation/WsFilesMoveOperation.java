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

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsSdk;
import org.eclipse.scout.sdk.ws.jaxws.resource.IResourceListener;
import org.eclipse.scout.sdk.ws.jaxws.resource.ResourceFactory;
import org.eclipse.scout.sdk.ws.jaxws.resource.XmlResource;
import org.eclipse.scout.sdk.ws.jaxws.swt.model.BuildJaxWsBean;
import org.eclipse.scout.sdk.ws.jaxws.swt.model.SunJaxWsBean;
import org.eclipse.scout.sdk.ws.jaxws.swt.wizard.page.ElementBean;
import org.eclipse.scout.sdk.ws.jaxws.util.JaxWsSdkUtility;
import org.eclipse.scout.sdk.ws.jaxws.util.PathNormalizer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class WsFilesMoveOperation implements IOperation {

  public static final int ID_WSDL_FILE = 1 << 1;
  public static final int ID_REF_WSDL = 1 << 2;
  public static final int ID_REF_XSD = 1 << 3;
  public static final int ID_WSDL_SUNJAXWS_REGISTRATION = 1 << 4;
  public static final int ID_WSDL_BUILDJAXWS_REGISTRATION = 1 << 5;
  public static final int ID_BINDING_FILE = 1 << 6;

  private IScoutBundle m_bundle;
  private SunJaxWsBean m_sunJaxWsBean;
  private BuildJaxWsBean m_buildJaxWsBean;
  private List<ElementBean> m_elements;
  private IFolder m_destination;

  @Override
  public void validate() throws IllegalArgumentException {
    if (m_bundle == null) {
      throw new IllegalArgumentException("No bundle set");
    }
    if (m_buildJaxWsBean == null) {
      throw new IllegalArgumentException("No buildJaxWsBean set");
    }
    if (m_elements == null) {
      throw new IllegalArgumentException("No elements set");
    }
    if (m_destination == null) {
      throw new IllegalArgumentException("No destination path set");
    }
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    JaxWsSdk.getDefault().getMarkerQueueManager().suspend();
    try {
      String wsdlFileName = null;
      for (ElementBean element : m_elements) {
        switch (element.getId()) {
          case ID_WSDL_FILE:
          case ID_REF_WSDL:
          case ID_REF_XSD: {
            moveFile((IFile) element.getResource(), m_destination, monitor);
            break;
          }
          case ID_WSDL_SUNJAXWS_REGISTRATION: {
            wsdlFileName = new Path(m_sunJaxWsBean.getWsdl()).lastSegment();
            m_sunJaxWsBean.setWsdl(PathNormalizer.toWsdlPath(m_destination.getProjectRelativePath().append(wsdlFileName).toString()));
            ResourceFactory.getSunJaxWsResource(m_bundle).storeXml(m_sunJaxWsBean.getXml().getOwnerDocument(), IResourceListener.EVENT_SUNJAXWS_WSDL_CHANGED, monitor, m_sunJaxWsBean.getAlias());
            break;
          }
          case ID_WSDL_BUILDJAXWS_REGISTRATION: {
            wsdlFileName = new Path(m_buildJaxWsBean.getWsdl()).lastSegment();
            m_buildJaxWsBean.setWsdl(PathNormalizer.toWsdlPath(m_destination.getProjectRelativePath().append(wsdlFileName).toString()));
            ResourceFactory.getBuildJaxWsResource(m_bundle).storeXml(m_buildJaxWsBean.getXml().getOwnerDocument(), IResourceListener.EVENT_BUILDJAXWS_WSDL_CHANGED, monitor, m_buildJaxWsBean.getAlias());
            break;
          }
          case ID_BINDING_FILE: {
            IFile bindingFile = (IFile) element.getResource();

            XmlResource xmlResource = new XmlResource(m_bundle);
            xmlResource.setFile(bindingFile);
            Document xmlDocument = xmlResource.loadXml();
            String namespacePrefix = JaxWsSdkUtility.getXmlPrefix(xmlDocument.getOwnerDocument().getDocumentElement());
            String fqn = StringUtility.join(":", namespacePrefix, "bindings");
            Element xmlBindings = JaxWsSdkUtility.getChildElement(xmlDocument.getDocumentElement().getChildNodes(), fqn);
            if (xmlBindings.hasAttribute("wsdlLocation")) {
              xmlBindings.removeAttribute("wsdlLocation");
              xmlBindings.setAttribute("wsdlLocation", (String) element.getData());
              xmlResource.storeXml(xmlDocument, IResourceListener.EVENT_UNKNOWN, monitor);
            }
            break;
          }
        }
      }

      // rebuild webservice stub
      if (wsdlFileName != null) {
        WsStubGenerationOperation op = new WsStubGenerationOperation();
        op.setBundle(m_bundle);
        op.setAlias(m_buildJaxWsBean.getAlias());
        op.setProperties(m_buildJaxWsBean.getPropertiers());
        op.setWsdlFileName(wsdlFileName);
        op.setWsdlFolder(m_destination);
        op.run(monitor, workingCopyManager);
      }
    }
    finally {
      JaxWsSdk.getDefault().getMarkerQueueManager().resume();
    }
  }

  private void moveFile(IFile file, IFolder destinationFolderPath, IProgressMonitor monitor) throws CoreException {
    if (file == null || !file.exists()) {
      return;
    }
    try {
      // ensure folder to be created
      JaxWsSdkUtility.getFolder(m_bundle, destinationFolderPath.getProjectRelativePath(), true);
      // move file
      file.move(destinationFolderPath.getFullPath().append(file.getName()), true, true, monitor);
    }
    catch (Exception e) {
      JaxWsSdk.logError(e);
    }
  }

  @Override
  public String getOperationName() {
    return WsFilesMoveOperation.class.getName();
  }

  public IScoutBundle getBundle() {
    return m_bundle;
  }

  public void setBundle(IScoutBundle bundle) {
    m_bundle = bundle;
  }

  public SunJaxWsBean getSunJaxWsBean() {
    return m_sunJaxWsBean;
  }

  public void setSunJaxWsBean(SunJaxWsBean sunJaxWsBean) {
    m_sunJaxWsBean = sunJaxWsBean;
  }

  public BuildJaxWsBean getBuildJaxWsBean() {
    return m_buildJaxWsBean;
  }

  public void setBuildJaxWsBean(BuildJaxWsBean buildJaxWsBean) {
    m_buildJaxWsBean = buildJaxWsBean;
  }

  public IFolder getDestination() {
    return m_destination;
  }

  public void setDestination(IFolder destination) {
    m_destination = destination;
  }

  public List<ElementBean> getElements() {
    return m_elements;
  }

  public void setElements(List<ElementBean> elements) {
    m_elements = elements;
  }
}
