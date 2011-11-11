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
package org.eclipse.scout.sdk.ws.jaxws.swt.wizard;

import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.commons.xmlparser.ScoutXmlDocument.ScoutXmlElement;
import org.eclipse.scout.sdk.typecache.IScoutWorkingCopyManager;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizard;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.ws.jaxws.Texts;
import org.eclipse.scout.sdk.ws.jaxws.resource.IResourceListener;
import org.eclipse.scout.sdk.ws.jaxws.resource.ResourceFactory;
import org.eclipse.scout.sdk.ws.jaxws.swt.model.SunJaxWsBean;
import org.eclipse.scout.sdk.ws.jaxws.swt.wizard.page.HandlerChainFilterWizardPage;
import org.eclipse.scout.sdk.ws.jaxws.swt.wizard.page.HandlerChainFilterWizardPage.FilterTypeEnum;

public class HandlerChainFilterWizard extends AbstractWorkspaceWizard {

  private HandlerChainFilterWizardPage m_wizardPage;

  private IScoutBundle m_bundle;
  private SunJaxWsBean m_sunJaxWsBean;
  private ScoutXmlElement m_xmlHandlerChain;

  private FilterTypeEnum m_filterTypeEnum;
  private String m_namespacePrefix;
  private String m_namespace;
  private String m_pattern;

  public HandlerChainFilterWizard() {
    setWindowTitle(Texts.get("HandlerChainFilter"));
  }

  public void init(IScoutBundle bundle, SunJaxWsBean sunJaxWsBean, ScoutXmlElement xmlHandlerChain) {
    m_bundle = bundle;
    m_sunJaxWsBean = sunJaxWsBean;
    m_xmlHandlerChain = xmlHandlerChain;

    ScoutXmlElement xmlFilterProtocol = m_xmlHandlerChain.getChild(toQualifiedName(SunJaxWsBean.XML_HANDLER_FILTER_PROTOCOL));
    ScoutXmlElement xmlFilterService = m_xmlHandlerChain.getChild(toQualifiedName(SunJaxWsBean.XML_HANDLER_FILTER_SERVICE));
    ScoutXmlElement xmlFilterPort = m_xmlHandlerChain.getChild(toQualifiedName(SunJaxWsBean.XML_HANDLER_FILTER_PORT));

    if (xmlFilterProtocol != null) {
      m_filterTypeEnum = FilterTypeEnum.ProtocolFilter;
      m_pattern = xmlFilterProtocol.getText();
    }
    else if (xmlFilterService != null || xmlFilterPort != null) {
      ScoutXmlElement xmlFilter;
      if (xmlFilterService != null) {
        xmlFilter = xmlFilterService;
        m_filterTypeEnum = FilterTypeEnum.ServiceFilter;
      }
      else {
        xmlFilter = xmlFilterPort;
        m_filterTypeEnum = FilterTypeEnum.PortFilter;
      }
      m_pattern = xmlFilter.getText();
      Map namespaces = xmlFilter.getNamespaces();
      if (namespaces != null && namespaces.size() > 0) {
        Entry ns = (Entry) namespaces.entrySet().iterator().next();
        m_namespacePrefix = (String) ns.getKey();
        m_namespace = (String) ns.getValue();
      }
    }
    else {
      m_filterTypeEnum = FilterTypeEnum.NoFilter;
    }
  }

  @Override
  public void addPages() {
    m_wizardPage = new HandlerChainFilterWizardPage(m_bundle);
    m_wizardPage.setFilterType(m_filterTypeEnum);
    m_wizardPage.setNamespacePrefix(m_namespacePrefix);
    m_wizardPage.setNamespace(m_namespace);
    m_wizardPage.setPattern(m_pattern);
    addPage(m_wizardPage);
  }

  @Override
  protected boolean beforeFinish() throws CoreException {
    m_filterTypeEnum = m_wizardPage.getFilterType();
    m_namespacePrefix = m_wizardPage.getNamespacePrefix();
    m_namespace = m_wizardPage.getNamespace();
    m_pattern = m_wizardPage.getPattern();
    return true;
  }

  @Override
  protected boolean performFinish(IProgressMonitor monitor, IScoutWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    // workaround as {@link ScoutXmlElement#removeChildren(Collection)} not works properly
    while (m_xmlHandlerChain.hasChild(toQualifiedName(SunJaxWsBean.XML_HANDLER_FILTER_PROTOCOL))) {
      m_xmlHandlerChain.removeChild(toQualifiedName(SunJaxWsBean.XML_HANDLER_FILTER_PROTOCOL));
    }
    while (m_xmlHandlerChain.hasChild(toQualifiedName(SunJaxWsBean.XML_HANDLER_FILTER_SERVICE))) {
      m_xmlHandlerChain.removeChild(toQualifiedName(SunJaxWsBean.XML_HANDLER_FILTER_SERVICE));
    }
    while (m_xmlHandlerChain.hasChild(toQualifiedName(SunJaxWsBean.XML_HANDLER_FILTER_PORT))) {
      m_xmlHandlerChain.removeChild(toQualifiedName(SunJaxWsBean.XML_HANDLER_FILTER_PORT));
    }

    switch (m_filterTypeEnum) {
      case ProtocolFilter:
        ScoutXmlElement protocolFilter = m_xmlHandlerChain.addChild();
        m_xmlHandlerChain.removeChild(protocolFilter);
        m_xmlHandlerChain.addChild(protocolFilter, 0);
        protocolFilter.setName(toQualifiedName(SunJaxWsBean.XML_HANDLER_FILTER_PROTOCOL));
        protocolFilter.addText(m_pattern);
        break;
      case ServiceFilter:
        ScoutXmlElement serviceFilter = m_xmlHandlerChain.addChild();
        m_xmlHandlerChain.removeChild(serviceFilter);
        m_xmlHandlerChain.addChild(serviceFilter, 0);
        serviceFilter.setName(toQualifiedName(SunJaxWsBean.XML_HANDLER_FILTER_SERVICE));
        serviceFilter.addText(m_pattern);
        serviceFilter.setNamespace(m_namespacePrefix, m_namespace);
        break;
      case PortFilter:
        ScoutXmlElement portFilter = m_xmlHandlerChain.addChild();
        m_xmlHandlerChain.removeChild(portFilter);
        m_xmlHandlerChain.addChild(portFilter, 0);
        portFilter.setName(toQualifiedName(SunJaxWsBean.XML_HANDLER_FILTER_PORT));
        portFilter.addText(m_pattern);
        portFilter.setNamespace(m_namespacePrefix, m_namespace);
        break;
    }

    // persist
    ResourceFactory.getSunJaxWsResource(m_bundle).storeXmlAsync(m_xmlHandlerChain.getDocument(), m_sunJaxWsBean.getAlias(), IResourceListener.EVENT_SUNJAXWS_HANDLER_CHANGED);
    return true;
  }

  private String toQualifiedName(String name) {
    return m_sunJaxWsBean.toQualifiedName(name);
  }
}
