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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.XmlUtility;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.ws.jaxws.resource.ResourceFactory;
import org.eclipse.scout.sdk.ws.jaxws.swt.wizard.page.WebserviceEnum;
import org.eclipse.scout.sdk.ws.jaxws.util.JaxWsSdkUtility;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class BuildJaxWsBean {

  public static final String XML_PROVIDER = "provider";
  public static final String XML_CONSUMER = "consumer";
  public static final String XML_ALIAS = "name";
  public static final String XML_PROPERTY = "property";
  public static final String XML_PROPERTY_NAME = "name";
  public static final String XML_PROPERTY_VALUE = "value";
  public static final String XML_WSDL = "wsdl";

  private Element m_xml;
  private WebserviceEnum m_webserviceEnum;

  public BuildJaxWsBean(Element xml, WebserviceEnum webserviceEnum) {
    m_xml = xml;
    m_webserviceEnum = webserviceEnum;
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

  /**
   * is only used if consumer build entry
   *
   * @param wsdl
   */
  public void setWsdl(String wsdl) {
    m_xml.removeAttribute(XML_WSDL);
    m_xml.setAttribute(XML_WSDL, wsdl);
  }

  /**
   * is only used if consumer build entry
   *
   * @return
   */
  public String getWsdl() {
    return JaxWsSdkUtility.getXmlAttribute(m_xml, XML_WSDL, null);
  }

  public Map<String, List<String>> getPropertiers() {
    Map<String, List<String>> properties = new HashMap<>();

    List<Element> children = XmlUtility.getChildElements(m_xml, XML_PROPERTY);
    for (Element xmlProperty : children) {
      String name = JaxWsSdkUtility.getXmlAttribute(xmlProperty, XML_PROPERTY_NAME, null);
      if (!StringUtility.isNullOrEmpty(name)) {
        String value = JaxWsSdkUtility.getXmlAttribute(xmlProperty, XML_PROPERTY_VALUE, null);
        if (!properties.containsKey(name)) {
          properties.put(name, new LinkedList<String>());
        }
        properties.get(name).add(value);
      }
    }
    return properties;
  }

  public void setProperties(Map<String, List<String>> properties) {
    JaxWsSdkUtility.removeAllChildElements(m_xml, XML_PROPERTY);
    if (properties == null || properties.size() == 0) {
      return;
    }
    for (Entry<String, List<String>> property : properties.entrySet()) {
      String name = property.getKey();
      if (property.getValue() == null || property.getValue().size() == 0) {
        Element xmlProperty = m_xml.getOwnerDocument().createElement(XML_PROPERTY);
        xmlProperty.setAttribute(XML_PROPERTY_NAME, property.getKey());
        m_xml.appendChild(xmlProperty);
      }
      else {
        for (String value : property.getValue()) {
          Element xmlProperty = m_xml.getOwnerDocument().createElement(XML_PROPERTY);
          xmlProperty.setAttribute(XML_PROPERTY_NAME, name);
          xmlProperty.setAttribute(XML_PROPERTY_VALUE, value);
          m_xml.appendChild(xmlProperty);
        }
      }
    }
  }

  /**
   * Reloads the entry from disk
   *
   * @param bundle
   * @return true if successful, false otherwise
   */
  public boolean reload(IScoutBundle bundle) {
    Document newDocument = ResourceFactory.getBuildJaxWsResource(bundle).loadXml();
    if (newDocument == null) {
      return false;
    }

    Element rootXml = newDocument.getDocumentElement();
    if (rootXml == null) {
      return false;
    }

    String nodeName;
    if (m_webserviceEnum == WebserviceEnum.PROVIDER) {
      nodeName = BuildJaxWsBean.XML_PROVIDER;
    }
    else {
      nodeName = BuildJaxWsBean.XML_CONSUMER;
    }
    List<Element> xml = XmlUtility.getChildElementsWithAttributes(rootXml, nodeName, BuildJaxWsBean.XML_ALIAS, getAlias());
    if (xml.size() < 1) {
      return false;
    }

    setXml(xml.get(0));
    return true;
  }

  public static BuildJaxWsBean load(IScoutBundle bundle, String alias, WebserviceEnum webserviceEnum) {
    if (!StringUtility.hasText(alias)) {
      return null;
    }
    Document document = ResourceFactory.getBuildJaxWsResource(bundle).loadXml();
    if (document == null) {
      return null;
    }

    String nodeName;
    if (webserviceEnum == WebserviceEnum.PROVIDER) {
      nodeName = BuildJaxWsBean.XML_PROVIDER;
    }
    else {
      nodeName = BuildJaxWsBean.XML_CONSUMER;
    }

    Element rootXml = document.getDocumentElement();
    if (rootXml == null) {
      return null;
    }

    List<Element> xml = XmlUtility.getChildElementsWithAttributes(rootXml, nodeName, BuildJaxWsBean.XML_ALIAS, alias);
    if (xml.size() < 1) {
      return null;
    }

    return new BuildJaxWsBean(xml.get(0), webserviceEnum);
  }
}
