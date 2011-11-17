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

import javax.xml.namespace.QName;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.xmlparser.ScoutXmlDocument;
import org.eclipse.scout.commons.xmlparser.ScoutXmlDocument.ScoutXmlElement;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.ws.jaxws.resource.IResourceListener;
import org.eclipse.scout.sdk.ws.jaxws.resource.ResourceFactory;
import org.eclipse.scout.sdk.ws.jaxws.swt.model.SunJaxWsBean;

public class SunJaxWsEntryCreateOperation implements IOperation {

  private IScoutBundle m_bundle;

  private String m_alias;
  private String m_implTypeQualifiedName;
  private QName m_serviceQName;
  private QName m_portQName;
  private String m_urlPattern;
  private String m_wsdlFile;

  private SunJaxWsBean m_createdSunJaxWsBean;

  @Override
  public void validate() throws IllegalArgumentException {
    if (m_bundle == null) {
      throw new IllegalArgumentException("bundle not set");
    }

    if (!StringUtility.hasText(m_alias)) {
      throw new IllegalArgumentException("alias must not be empty");
    }
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    ScoutXmlDocument xmlDocument = ResourceFactory.getSunJaxWsResource(m_bundle).loadXml();
    String namespacePrefix = xmlDocument.getRoot().getNamePrefix();

    ScoutXmlElement endpointXml = xmlDocument.getRoot().addChild(StringUtility.join(":", namespacePrefix, SunJaxWsBean.XML_ENDPOINT));
    SunJaxWsBean bean = new SunJaxWsBean(endpointXml);
    bean.setAlias(m_alias);
    bean.setImplementation(m_implTypeQualifiedName);
    if (m_serviceQName != null) {
      bean.setService(m_serviceQName.toString());
    }
    if (m_portQName != null) {
      bean.setPort(m_portQName.toString());
    }
    bean.setUrlPattern(m_urlPattern);
    bean.setWsdl(m_wsdlFile);
    m_createdSunJaxWsBean = bean;

    ResourceFactory.getSunJaxWsResource(m_bundle).storeXml(m_createdSunJaxWsBean.getXml().getDocument(), m_alias, IResourceListener.EVENT_SUNJAXWS_ENTRY_ADDED, monitor);
  }

  @Override
  public String getOperationName() {
    return SunJaxWsEntryCreateOperation.class.getName();
  }

  public IScoutBundle getBundle() {
    return m_bundle;
  }

  public void setBundle(IScoutBundle bundle) {
    m_bundle = bundle;
  }

  public String getAlias() {
    return m_alias;
  }

  public void setAlias(String alias) {
    m_alias = alias;
  }

  public String getImplQName() {
    return m_implTypeQualifiedName;
  }

  public void setImplTypeQualifiedName(String implTypeQualifiedName) {
    m_implTypeQualifiedName = implTypeQualifiedName;
  }

  public QName getServiceQName() {
    return m_serviceQName;
  }

  public void setServiceQName(QName serviceQName) {
    m_serviceQName = serviceQName;
  }

  public QName getPortQName() {
    return m_portQName;
  }

  public void setPortQName(QName portQName) {
    m_portQName = portQName;
  }

  public String getUrlPattern() {
    return m_urlPattern;
  }

  public void setUrlPattern(String urlPattern) {
    m_urlPattern = urlPattern;
  }

  public String getWsdlFileName() {
    return m_wsdlFile;
  }

  public void setWsdlFile(String wsdlFile) {
    m_wsdlFile = wsdlFile;
  }

  /**
   * After execution, to get the created {@link SunJaxWsBean}.
   * 
   * @return
   */
  public SunJaxWsBean getCreatedSunJaxWsBean() {
    return m_createdSunJaxWsBean;
  }
}
