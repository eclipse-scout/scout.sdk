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
import org.eclipse.scout.commons.xmlparser.ScoutXmlDocument;
import org.eclipse.scout.commons.xmlparser.ScoutXmlDocument.ScoutXmlElement;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.ws.jaxws.resource.ResourceFactory;

public class BuildJaxWsBean {

  public static final String XML_PROVIDER = "provider";
  public static final String XML_CONSUMER = "consumer";
  public static final String XML_ALIAS = "name";
  public static final String XML_PROPERTY = "property";
  public static final String XML_PROPERTY_NAME = "name";
  public static final String XML_PROPERTY_VALUE = "value";
  public static final String XML_WSDL = "wsdl";

  private ScoutXmlElement m_xml;

  public BuildJaxWsBean(ScoutXmlElement xml) {
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
    return m_xml.getAttribute(XML_WSDL, null);
  }

  public Map<String, List<String>> getPropertiers() {
    Map<String, List<String>> properties = new HashMap<String, List<String>>();

    List children = m_xml.getChildren(XML_PROPERTY);
    for (Object child : children) {
      ScoutXmlElement xmlProperty = (ScoutXmlElement) child;
      String name = xmlProperty.getAttribute(XML_PROPERTY_NAME, null);
      if (!StringUtility.isNullOrEmpty(name)) {
        String value = xmlProperty.getAttribute(XML_PROPERTY_VALUE, null);
        if (!properties.containsKey(name)) {
          properties.put(name, new LinkedList<String>());
        }
        properties.get(name).add(value);
      }
    }
    return properties;
  }

  public void setProperties(Map<String, List<String>> properties) {
    m_xml.removeChildren(XML_PROPERTY);

    if (properties == null || properties.size() == 0) {
      return;
    }
    for (Entry<String, List<String>> property : properties.entrySet()) {
      String name = property.getKey();
      if (property.getValue() == null || property.getValue().size() == 0) {
        ScoutXmlElement xmlProperty = m_xml.addChild(XML_PROPERTY);
        xmlProperty.setAttribute(XML_PROPERTY_NAME, property.getKey());
      }
      else {
        for (String value : property.getValue()) {
          ScoutXmlElement xmlProperty = m_xml.addChild(XML_PROPERTY);
          xmlProperty.setAttribute(XML_PROPERTY_NAME, name);
          xmlProperty.setAttribute(XML_PROPERTY_VALUE, value);
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
    ScoutXmlDocument newDocument = ResourceFactory.getBuildJaxWsResource(bundle).loadXml();
    if (newDocument == null || newDocument.getRoot() == null) {
      return false;
    }

    ScoutXmlElement rootXml = newDocument.getRoot();
    if (rootXml == null || !rootXml.hasChild(BuildJaxWsBean.XML_PROVIDER)) {
      return false;
    }

    ScoutXmlElement xml = newDocument.getRoot().getChild(BuildJaxWsBean.XML_PROVIDER, BuildJaxWsBean.XML_ALIAS, getAlias());
    if (xml == null) {
      return false;
    }

    setXml(xml);
    return true;
  }

  public static BuildJaxWsBean load(IScoutBundle bundle, String alias) {
    if (!StringUtility.hasText(alias)) {
      return null;
    }
    ScoutXmlDocument document = ResourceFactory.getBuildJaxWsResource(bundle).loadXml();
    if (document == null || document.getRoot() == null) {
      return null;
    }

    ScoutXmlElement rootXml = document.getRoot();
    if (rootXml == null || !rootXml.hasChild(BuildJaxWsBean.XML_PROVIDER)) {
      return null;
    }

    ScoutXmlElement xml = document.getRoot().getChild(BuildJaxWsBean.XML_PROVIDER, BuildJaxWsBean.XML_ALIAS, alias);
    if (xml == null) {
      return null;
    }

    return new BuildJaxWsBean(xml);
  }
}
