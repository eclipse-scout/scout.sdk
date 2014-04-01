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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizard;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.ws.jaxws.Texts;
import org.eclipse.scout.sdk.ws.jaxws.resource.IResourceListener;
import org.eclipse.scout.sdk.ws.jaxws.resource.ResourceFactory;
import org.eclipse.scout.sdk.ws.jaxws.swt.model.SunJaxWsBean;
import org.eclipse.scout.sdk.ws.jaxws.swt.wizard.page.HandlerChainFilterWizardPage;
import org.eclipse.scout.sdk.ws.jaxws.swt.wizard.page.HandlerChainFilterWizardPage.FilterTypeEnum;
import org.eclipse.scout.sdk.ws.jaxws.util.JaxWsSdkUtility;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class HandlerChainFilterWizard extends AbstractWorkspaceWizard {

  private HandlerChainFilterWizardPage m_wizardPage;

  private IScoutBundle m_bundle;
  private SunJaxWsBean m_sunJaxWsBean;
  private Element m_xmlHandlerChain;

  private FilterTypeEnum m_filterTypeEnum;
  private String m_namespacePrefix;
  private String m_namespace;
  private String m_pattern;

  public HandlerChainFilterWizard() {
    setWindowTitle(Texts.get("HandlerChainFilter"));
  }

  @SuppressWarnings("null")
  public void init(IScoutBundle bundle, SunJaxWsBean sunJaxWsBean, Element xmlHandlerChain) {
    m_bundle = bundle;
    m_sunJaxWsBean = sunJaxWsBean;
    m_xmlHandlerChain = xmlHandlerChain;

    Element xmlFilterProtocol = JaxWsSdkUtility.getChildElement(m_xmlHandlerChain.getChildNodes(), toQualifiedName(SunJaxWsBean.XML_HANDLER_FILTER_PROTOCOL));
    Element xmlFilterService = JaxWsSdkUtility.getChildElement(m_xmlHandlerChain.getChildNodes(), toQualifiedName(SunJaxWsBean.XML_HANDLER_FILTER_SERVICE));
    Element xmlFilterPort = JaxWsSdkUtility.getChildElement(m_xmlHandlerChain.getChildNodes(), toQualifiedName(SunJaxWsBean.XML_HANDLER_FILTER_PORT));

    if (xmlFilterProtocol != null) {
      m_filterTypeEnum = FilterTypeEnum.ProtocolFilter;
      m_pattern = xmlFilterProtocol.getTextContent();
    }
    else if (xmlFilterService != null || xmlFilterPort != null) {
      Element xmlFilter;
      if (xmlFilterService != null) {
        xmlFilter = xmlFilterService;
        m_filterTypeEnum = FilterTypeEnum.ServiceFilter;
      }
      else {
        xmlFilter = xmlFilterPort;
        m_filterTypeEnum = FilterTypeEnum.PortFilter;
      }
      m_pattern = xmlFilter.getTextContent();
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
  protected boolean performFinish(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    JaxWsSdkUtility.removeAllChildElements(m_xmlHandlerChain, toQualifiedName(SunJaxWsBean.XML_HANDLER_FILTER_PROTOCOL));
    JaxWsSdkUtility.removeAllChildElements(m_xmlHandlerChain, toQualifiedName(SunJaxWsBean.XML_HANDLER_FILTER_SERVICE));
    JaxWsSdkUtility.removeAllChildElements(m_xmlHandlerChain, toQualifiedName(SunJaxWsBean.XML_HANDLER_FILTER_PORT));

    Node firstChild = null;
    switch (m_filterTypeEnum) {
      case ProtocolFilter:
        Element protocolFilter = m_xmlHandlerChain.getOwnerDocument().createElement(toQualifiedName(SunJaxWsBean.XML_HANDLER_FILTER_PROTOCOL));
        firstChild = m_xmlHandlerChain.getFirstChild();
        if (firstChild == null) {
          m_xmlHandlerChain.appendChild(protocolFilter);
        }
        else {
          m_xmlHandlerChain.insertBefore(firstChild, protocolFilter);
        }
        protocolFilter.setTextContent(m_pattern);
        break;
      case ServiceFilter:
        Element serviceFilter = m_xmlHandlerChain.getOwnerDocument().createElement(toQualifiedName(SunJaxWsBean.XML_HANDLER_FILTER_SERVICE));
        firstChild = m_xmlHandlerChain.getFirstChild();
        if (firstChild == null) {
          m_xmlHandlerChain.appendChild(serviceFilter);
        }
        else {
          m_xmlHandlerChain.insertBefore(firstChild, serviceFilter);
        }
        serviceFilter.setTextContent(m_pattern);
        serviceFilter.setPrefix(m_namespacePrefix);
        break;
      case PortFilter:
        Element portFilter = m_xmlHandlerChain.getOwnerDocument().createElement(toQualifiedName(SunJaxWsBean.XML_HANDLER_FILTER_PORT));
        firstChild = m_xmlHandlerChain.getFirstChild();
        if (firstChild == null) {
          m_xmlHandlerChain.appendChild(portFilter);
        }
        else {
          m_xmlHandlerChain.insertBefore(firstChild, portFilter);
        }
        portFilter.setTextContent(m_pattern);
        portFilter.setPrefix(m_namespacePrefix);
        break;
    }

    // persist
    ResourceFactory.getSunJaxWsResource(m_bundle).storeXmlAsync(m_xmlHandlerChain.getOwnerDocument(), IResourceListener.EVENT_SUNJAXWS_HANDLER_CHANGED, m_sunJaxWsBean.getAlias());
    return true;
  }

  private String toQualifiedName(String name) {
    return m_sunJaxWsBean.toQualifiedName(name);
  }
}
