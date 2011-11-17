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
import java.util.Map;

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
import org.eclipse.scout.sdk.ws.jaxws.swt.model.BuildJaxWsBean;
import org.eclipse.scout.sdk.ws.jaxws.swt.wizard.page.WebserviceEnum;

public class BuildJaxWsEntryCreateOperation implements IOperation {

  private IScoutBundle m_bundle;

  private String m_alias;
  private String m_wsdlFile;
  private Map<String, List<String>> m_buildProperties;

  private WebserviceEnum m_webserviceEnum;
  private BuildJaxWsBean m_createdBuildJaxWsBean;

  public BuildJaxWsEntryCreateOperation(WebserviceEnum webserviceEnum) {
    m_webserviceEnum = webserviceEnum;
  }

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
    ScoutXmlDocument xmlDocument = ResourceFactory.getBuildJaxWsResource(m_bundle).loadXml();

    ScoutXmlElement xml;
    if (m_webserviceEnum == WebserviceEnum.Provider) {
      xml = xmlDocument.getRoot().addChild(BuildJaxWsBean.XML_PROVIDER);
    }
    else {
      xml = xmlDocument.getRoot().addChild(BuildJaxWsBean.XML_CONSUMER);
    }

    BuildJaxWsBean bean = new BuildJaxWsBean(xml);
    bean.setAlias(m_alias);
    if (m_webserviceEnum == WebserviceEnum.Consumer) {
      bean.setWsdl(m_wsdlFile); // if provider, this is stored in sun-jaxws.xml
    }
    bean.setProperties(m_buildProperties);
    m_createdBuildJaxWsBean = bean;

    ResourceFactory.getBuildJaxWsResource(m_bundle).storeXml(m_createdBuildJaxWsBean.getXml().getDocument(), m_alias, IResourceListener.EVENT_BUILDJAXWS_ENTRY_ADDED, monitor);
  }

  @Override
  public String getOperationName() {
    return BuildJaxWsEntryCreateOperation.class.getName();
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

  public Map<String, List<String>> getBuildProperties() {
    return m_buildProperties;
  }

  public void setBuildProperties(Map<String, List<String>> buildProperties) {
    m_buildProperties = buildProperties;
  }

  /**
   * only used for webservice consumer
   * 
   * @return
   */
  public String getWsdlFile() {
    return m_wsdlFile;
  }

  /**
   * only used for webservice consumer
   * 
   * @param wsdlFile
   */
  public void setWsdlFile(String wsdlFile) {
    m_wsdlFile = wsdlFile;
  }

  /**
   * After execution, to get the created {@link BuildJaxWsBean}.
   * 
   * @return
   */
  public BuildJaxWsBean getCreatedBuildJaxWsBean() {
    return m_createdBuildJaxWsBean;
  }
}
