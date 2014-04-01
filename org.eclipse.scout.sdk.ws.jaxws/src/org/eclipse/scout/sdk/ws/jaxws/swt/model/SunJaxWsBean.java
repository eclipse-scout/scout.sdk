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

import java.util.Collections;
import java.util.List;

import javax.xml.namespace.QName;

import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.ws.jaxws.resource.ResourceFactory;
import org.eclipse.scout.sdk.ws.jaxws.util.JaxWsSdkUtility;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

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

  private Element m_xml;

  public SunJaxWsBean(Element xml) {
    m_xml = xml;
  }

  public Element getXml() {
    return m_xml;
  }

  public void setXml(Element xml) {
    m_xml = xml;
  }

  public String getAlias() {
    return JaxWsSdkUtility.getXmlAttribute(m_xml, XML_ALIAS, null);
  }

  public void setAlias(String alias) {
    m_xml.removeAttribute(XML_ALIAS);
    m_xml.setAttribute(XML_ALIAS, alias);
  }

  public String getImplementation() {
    return JaxWsSdkUtility.getXmlAttribute(m_xml, XML_IMPLEMENTATION, null);
  }

  public void setImplementation(String implementation) {
    m_xml.removeAttribute(XML_IMPLEMENTATION);
    m_xml.setAttribute(XML_IMPLEMENTATION, implementation);
  }

  public String getService() {
    return JaxWsSdkUtility.getXmlAttribute(m_xml, XML_SERVICE, null);
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
    return JaxWsSdkUtility.getXmlAttribute(m_xml, XML_PORT, null);
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
    return JaxWsSdkUtility.getXmlAttribute(m_xml, XML_WSDL, null);
  }

  public void setWsdl(String wsdl) {
    m_xml.removeAttribute(XML_WSDL);
    m_xml.setAttribute(XML_WSDL, wsdl);
  }

  public String getUrlPattern() {
    return JaxWsSdkUtility.getXmlAttribute(m_xml, XML_URL_PATTERN, null);
  }

  public void setUrlPattern(String urlPattern) {
    m_xml.removeAttribute(XML_URL_PATTERN);
    m_xml.setAttribute(XML_URL_PATTERN, urlPattern);
  }

  public List<Element> getHandlerChains() {
    Element xmlChains = JaxWsSdkUtility.getFirstChildElementByTagName(m_xml, toQualifiedName(SunJaxWsBean.XML_HANDLER_CHAINS));
    if (xmlChains == null) {
      return Collections.emptyList();
    }
    return JaxWsSdkUtility.getChildElements(xmlChains.getChildNodes(), toQualifiedName(SunJaxWsBean.XML_HANDLER_CHAIN));
  }

  public void setHandlerChains(List<Element> xmlHandlerChains) {
    List<Element> childElements = JaxWsSdkUtility.getChildElements(m_xml.getChildNodes(), toQualifiedName(SunJaxWsBean.XML_HANDLER_CHAINS));
    for (Element existing : childElements) {
      m_xml.removeChild(existing);
    }
    for (Element chain : xmlHandlerChains) {
      m_xml.appendChild(chain);
    }
  }

  public String toQualifiedName(String elementName) {
    return StringUtility.join(":", JaxWsSdkUtility.getXmlPrefix(m_xml.getOwnerDocument().getDocumentElement()), elementName);
  }

  public void visitHandlers(IHandlerVisitor visitor) {
    for (Element xmlHandlerChain : getHandlerChains()) {
      visitHandlers(xmlHandlerChain, visitor);
    }
  }

  public void visitHandlers(Element xmlHandlerChain, IHandlerVisitor visitor) {
    String handlerXmlElementName = toQualifiedName(SunJaxWsBean.XML_HANDLER);
    String handlerClazzXmlElementName = toQualifiedName(SunJaxWsBean.XML_HANDLER_CLASS);

    List<Element> children = JaxWsSdkUtility.getChildElements(xmlHandlerChain.getChildNodes(), handlerXmlElementName);
    if (children == null || children.size() == 0) {
      return;
    }

    for (int i = 0; i < children.size(); i++) {
      Element xmlHandler = children.get(i);

      Element xmlHandlerClazzElement = JaxWsSdkUtility.getFirstChildElementByTagName(xmlHandler, handlerClazzXmlElementName);
      String fqn = null;
      if (xmlHandlerClazzElement != null) {
        fqn = xmlHandlerClazzElement.getTextContent();
      }
      if (!visitor.visit(xmlHandler, fqn, i, children.size())) {
        return;
      }
    }
  }

  public boolean swapHandler(Element xmlHandlerChain, int oldIndex, int newIndex) {
    String handlerXmlElementName = toQualifiedName(SunJaxWsBean.XML_HANDLER);
    List<Element> handlerChildren = JaxWsSdkUtility.getChildElements(xmlHandlerChain.getChildNodes(), handlerXmlElementName);
    try {
      Collections.swap(handlerChildren, oldIndex, newIndex);

      // update chain in memory
      for (Element e : handlerChildren) {
        xmlHandlerChain.removeChild(e);
      }
      for (Element e : handlerChildren) {
        xmlHandlerChain.appendChild(e);
      }
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
    Document newDocument = ResourceFactory.getSunJaxWsResource(bundle).loadXml();
    if (newDocument == null) {
      return false;
    }

    Element rootXml = newDocument.getDocumentElement();
    if (rootXml == null) {
      return false;
    }

    List<Element> endpoints = JaxWsSdkUtility.getChildElements(rootXml.getChildNodes(), toQualifiedName(SunJaxWsBean.XML_ENDPOINT));
    if (endpoints.size() < 1) {
      return false;
    }

    List<Element> childElementsWithAttributes = JaxWsSdkUtility.getChildElementsWithAttributes(rootXml, toQualifiedName(SunJaxWsBean.XML_ENDPOINT), SunJaxWsBean.XML_ALIAS, getAlias());

    if (childElementsWithAttributes.size() < 1) {
      return false;
    }
    setXml(childElementsWithAttributes.get(0));
    return true;
  }

  public static SunJaxWsBean load(IScoutBundle bundle, String alias) {
    if (!StringUtility.hasText(alias)) {
      return null;
    }
    Document document = ResourceFactory.getSunJaxWsResource(bundle).loadXml();
    if (document == null) {
      return null;
    }

    Element rootXml = document.getDocumentElement();
    if (rootXml == null) {
      return null;
    }

    String xmlEndpoint = StringUtility.join(":", JaxWsSdkUtility.getXmlPrefix(rootXml), SunJaxWsBean.XML_ENDPOINT);
    List<Element> xml = JaxWsSdkUtility.getChildElementsWithAttributes(rootXml, xmlEndpoint, SunJaxWsBean.XML_ALIAS, alias);
    if (xml.size() < 1) {
      return null;
    }

    return new SunJaxWsBean(xml.get(0));
  }

  public static interface IHandlerVisitor {

    public boolean visit(Element xmlHandlerElement, String fullyQualifiedName, int handlerIndex, int handlerCount);
  }
}
