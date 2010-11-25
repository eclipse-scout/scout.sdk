/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.pde;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.commons.xmlparser.SimpleXmlElement;

/**
 * representation of the plugin.xml file
 */
public class PluginXml extends AbstractConfigFile {
  private SimpleXmlElement m_xml;

  public PluginXml(IProject p) throws CoreException {
    super(p, "plugin.xml");
    m_xml = new SimpleXmlElement("plugin");
    loadXmlInternal(m_xml);
  }

  @Override
  public boolean store(IProgressMonitor p) throws CoreException {
    return storeXmlInternal(m_xml, p);
  }

  public SimpleXmlElement getApplicationExtension() {
    return getExtension("org.eclipse.core.runtime.applications");
  }

  public SimpleXmlElement getProductExtension() {
    return getExtension("org.eclipse.core.runtime.products");
  }

  public SimpleXmlElement createApplicationExtension(String appId, String name, String runClassName) {
    SimpleXmlElement e = getApplicationExtension();
    e = new SimpleXmlElement("extension");
    e.setAttribute("point", "org.eclipse.core.runtime.applications");
    e.setAttribute("id", appId);
    e.setAttribute("name", name);
    SimpleXmlElement appXml = new SimpleXmlElement("application");
    SimpleXmlElement runXml = new SimpleXmlElement("run");
    runXml.setAttribute("class", runClassName);
    e.addChild(appXml);
    appXml.addChild(runXml);
    // remove old
    SimpleXmlElement old = getApplicationExtension();
    if (old != null) m_xml.removeChild(old);
    // add new
    m_xml.addChild(e);
    return e;
  }

  public SimpleXmlElement createProductExtension(String productId, String application, String name) {
    SimpleXmlElement e = new SimpleXmlElement("extension");
    e.setAttribute("point", "org.eclipse.core.runtime.products");
    e.setAttribute("id", productId);
    SimpleXmlElement prodXml = new SimpleXmlElement("product");
    prodXml.setAttribute("application", application);
    prodXml.setAttribute("name", name);
    e.addChild(prodXml);
    SimpleXmlElement propertyXml = new SimpleXmlElement("property");
    propertyXml.setAttribute("name", "appName");
    propertyXml.setAttribute("value", name);
    prodXml.addChild(propertyXml);
    // remove old
    SimpleXmlElement old = getProductExtension();
    if (old != null) m_xml.removeChild(old);
    // add new
    m_xml.addChild(e);
    return e;
  }

  public SimpleXmlElement getExtension(String extensionPoint) {
    for (SimpleXmlElement e : m_xml.getChildren("extension")) {
      if (e.getStringAttribute("point", "").equalsIgnoreCase(extensionPoint)) {
        return e;
      }
    }
    return null;
  }

  public SimpleXmlElement getOrCreateExtension(String extensionPoint) {
    for (SimpleXmlElement e : m_xml.getChildren("extension")) {
      if (e.getStringAttribute("point", "").equalsIgnoreCase(extensionPoint)) {
        return e;
      }
    }
    SimpleXmlElement e = new SimpleXmlElement("extension");
    e.setAttribute("point", extensionPoint);
    m_xml.addChild(e);
    return e;
  }

  public Collection<SimpleXmlElement> getExtensions(String extensionPointId) {
    ArrayList<SimpleXmlElement> list = new ArrayList<SimpleXmlElement>();
    for (SimpleXmlElement e : m_xml.getChildren("extension")) {
      if (e.getStringAttribute("point", "").equalsIgnoreCase(extensionPointId)) {
        list.add(e);
      }
    }
    return list;
  }

}
