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
package org.eclipse.scout.sdk.ws.jaxws.swt.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.xml.namespace.QName;

import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.xmlparser.ScoutXmlDocument;
import org.eclipse.scout.commons.xmlparser.ScoutXmlDocument.ScoutXmlElement;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.ws.jaxws.resource.ResourceFactory;

public class SunJaxWsBean {

  public static final String XML_ENDPOINT = "endpoint";
  public static final String XML_ALIAS = "name";
  public static final String XML_SERVICE = "service";
  public static final String XML_PORT = "port";
  public static final String XML_WSDL = "wsdl";
  public static final String XML_IMPLEMENTATION = "implementation";
  public static final String XML_URL_PATTERN = "url-pattern";
  public static final String XML_HANDLER_CHAINS = "handler-chains";
  public static final String XML_HANDLER_CHAIN = "handler-chain";
  public static final String XML_HANDLER = "handler";
  public static final String XML_HANDLER_CLASS = "handler-class";
  public static final String NS_ENDPOINT = "http://java.sun.com/xml/ns/jax-ws/ri/runtime";
  public static final String NS_HANDLER_CHAINS = "http://java.sun.com/xml/ns/javaee";

  public static final String XML_HANDLER_FILTER_PROTOCOL = "protocol-bindings";
  public static final String XML_HANDLER_FILTER_SERVICE = "service-name-pattern";
  public static final String XML_HANDLER_FILTER_PORT = "port-name-pattern";

  private ScoutXmlElement m_xml;

  public SunJaxWsBean(ScoutXmlElement xml) {
    m_xml = xml;
  }

  public ScoutXmlElement getXml() {
    return m_xml;
  }

  public void setXml(ScoutXmlElement xml) {
    m_xml = xml;
  }

  public String getAlias() {
    return m_xml.getAttribute(XML_ALIAS, null);
  }

  public void setAlias(String alias) {
    m_xml.removeAttribute(XML_ALIAS);
    m_xml.setAttribute(XML_ALIAS, alias);
  }

  public String getImplementation() {
    return m_xml.getAttribute(XML_IMPLEMENTATION, null);
  }

  public void setImplementation(String implementation) {
    m_xml.removeAttribute(XML_IMPLEMENTATION);
    m_xml.setAttribute(XML_IMPLEMENTATION, implementation);
  }

  public String getService() {
    return m_xml.getAttribute(XML_SERVICE, null);
  }

  public QName getServiceQNameSafe() {
    try {
      return QName.valueOf(getService());
    }
    catch (Exception e) {
      return null;
    }
  }

  public void setService(String service) {
    m_xml.removeAttribute(XML_SERVICE);
    m_xml.setAttribute(XML_SERVICE, service);
  }

  public String getPort() {
    return m_xml.getAttribute(XML_PORT, null);
  }

  public QName getPortQNameSafe() {
    try {
      return QName.valueOf(getPort());
    }
    catch (Exception e) {
      return null;
    }
  }

  public void setPort(String port) {
    m_xml.removeAttribute(XML_PORT);
    m_xml.setAttribute(XML_PORT, port);
  }

  public String getWsdl() {
    return m_xml.getAttribute(XML_WSDL, null);
  }

  public void setWsdl(String wsdl) {
    m_xml.removeAttribute(XML_WSDL);
    m_xml.setAttribute(XML_WSDL, wsdl);
  }

  public String getUrlPattern() {
    return m_xml.getAttribute(XML_URL_PATTERN, null);
  }

  public void setUrlPattern(String urlPattern) {
    m_xml.removeAttribute(XML_URL_PATTERN);
    m_xml.setAttribute(XML_URL_PATTERN, urlPattern);
  }

  public List<ScoutXmlElement> getHandlerChains() {
    ScoutXmlElement xmlChains = m_xml.getChild(toQualifiedName(SunJaxWsBean.XML_HANDLER_CHAINS));

    if (xmlChains == null) {
      return new ArrayList<ScoutXmlElement>(0);
    }

    List<ScoutXmlElement> children = xmlChains.getChildren(toQualifiedName(SunJaxWsBean.XML_HANDLER_CHAIN));
    if (children == null) {
      return new ArrayList<ScoutXmlElement>(0);
    }

    List<ScoutXmlElement> chains = new ArrayList<ScoutXmlElement>();
    for (ScoutXmlElement child : children) {
      chains.add(child);
    }
    return chains;
  }

  public void setHandlerChains(List<ScoutXmlElement> xmlHandlerChains) {
    if (m_xml.hasChild(toQualifiedName(SunJaxWsBean.XML_HANDLER_CHAINS))) {
      m_xml.removeChildren(toQualifiedName(SunJaxWsBean.XML_HANDLER_CHAINS));
    }
    for (ScoutXmlElement chain : xmlHandlerChains) {
      m_xml.addChild(chain);
    }
  }

  public String toQualifiedName(String elementName) {
    return StringUtility.join(":", m_xml.getRoot().getNamePrefix(), elementName);
  }

  public void visitHandlers(IHandlerVisitor visitor) {
    String handlerXmlElementName = toQualifiedName(SunJaxWsBean.XML_HANDLER);
    String handlerClazzXmlElementName = toQualifiedName(SunJaxWsBean.XML_HANDLER_CLASS);

    for (ScoutXmlElement xmlHandlerChain : getHandlerChains()) {
      List<ScoutXmlElement> children = xmlHandlerChain.getChildren(handlerXmlElementName);
      if (children == null || children.size() == 0) {
        continue;
      }

      List<ScoutXmlElement> handlers = new LinkedList<ScoutXmlElement>();
      for (ScoutXmlElement child : children) {
        handlers.add(child);
      }
      for (int i = 0; i < handlers.size(); i++) {
        ScoutXmlElement xmlHandler = handlers.get(i);

        ScoutXmlElement xmlHandlerClazzElement = xmlHandler.getChild(handlerClazzXmlElementName);
        String fqn = null;
        if (xmlHandlerClazzElement != null) {
          fqn = xmlHandlerClazzElement.getText();
        }
        if (!visitor.visit(xmlHandler, fqn, i, handlers.size())) {
          return;
        }
      }
    }
  }

  public void visitHandlers(ScoutXmlElement xmlHandlerChain, IHandlerVisitor visitor) {
    String handlerXmlElementName = toQualifiedName(SunJaxWsBean.XML_HANDLER);
    String handlerClazzXmlElementName = toQualifiedName(SunJaxWsBean.XML_HANDLER_CLASS);

    List<ScoutXmlElement> children = xmlHandlerChain.getChildren(handlerXmlElementName);
    if (children == null || children.size() == 0) {
      return;
    }

    List<ScoutXmlElement> handlers = new LinkedList<ScoutXmlElement>();
    for (ScoutXmlElement child : children) {
      handlers.add((ScoutXmlElement) child);
    }
    for (int i = 0; i < handlers.size(); i++) {
      ScoutXmlElement xmlHandler = handlers.get(i);

      ScoutXmlElement xmlHandlerClazzElement = xmlHandler.getChild(handlerClazzXmlElementName);
      String fqn = null;
      if (xmlHandlerClazzElement != null) {
        fqn = xmlHandlerClazzElement.getText();
      }
      if (!visitor.visit(xmlHandler, fqn, i, handlers.size())) {
        return;
      }
    }
  }

  public boolean swapHandler(ScoutXmlElement xmlHandlerChain, int oldIndex, int newIndex) {
    String handlerXmlElementName = toQualifiedName(SunJaxWsBean.XML_HANDLER);
    List<ScoutXmlElement> handlerChildren = xmlHandlerChain.getChildren(handlerXmlElementName);
    try {
      Collections.swap(handlerChildren, oldIndex, newIndex);

      // update chain in memory
      xmlHandlerChain.removeChildren(handlerChildren);
      xmlHandlerChain.addChildren(handlerChildren);
      return true;
    }
    catch (IndexOutOfBoundsException e) {
      return false;
    }
  }

  /**
   * Reloads the entry from disk
   * 
   * @param bundle
   * @return true if successful, false otherwise
   */
  public boolean reload(IScoutBundle bundle) {
    ScoutXmlDocument newDocument = ResourceFactory.getSunJaxWsResource(bundle).loadXml();
    if (newDocument == null || newDocument.getRoot() == null) {
      return false;
    }

    ScoutXmlElement rootXml = newDocument.getRoot();
    if (rootXml == null || !rootXml.hasChild(toQualifiedName(SunJaxWsBean.XML_ENDPOINT))) {
      return false;
    }

    ScoutXmlElement xml = rootXml.getChild(toQualifiedName(SunJaxWsBean.XML_ENDPOINT), SunJaxWsBean.XML_ALIAS, getAlias());
    if (xml == null) {
      return false;
    }
    setXml(xml);
    return true;
  }

  public static SunJaxWsBean load(IScoutBundle bundle, String alias) {
    if (!StringUtility.hasText(alias)) {
      return null;
    }
    ScoutXmlDocument document = ResourceFactory.getSunJaxWsResource(bundle).loadXml();
    if (document == null || document.getRoot() == null) {
      return null;
    }

    ScoutXmlElement rootXml = document.getRoot();
    if (rootXml == null || !rootXml.hasChild(SunJaxWsBean.XML_ENDPOINT)) {
      return null;
    }

    ScoutXmlElement xml = rootXml.getChild(StringUtility.join(":", rootXml.getRoot().getNamePrefix(), SunJaxWsBean.XML_ENDPOINT), SunJaxWsBean.XML_ALIAS, alias);
    if (xml == null) {
      return null;
    }

    return new SunJaxWsBean(xml);
  }

  public static interface IHandlerVisitor {

    public boolean visit(ScoutXmlElement xmlHandlerElement, String fullyQualifiedName, int handlerIndex, int handlerCount);
  }
}
