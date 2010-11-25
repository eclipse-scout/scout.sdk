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

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.commons.xmlparser.SimpleXmlElement;

/**
 * representation of the feature.xml file
 */
public class FeatureXml extends AbstractConfigFile {
  private SimpleXmlElement m_xml;

  public FeatureXml(IProject p) throws CoreException {
    super(p, "feature.xml");
    m_xml = new SimpleXmlElement("feature");
    m_xml.setAttribute("id", p.getName());
    m_xml.setAttribute("label", "Feature");
    m_xml.setAttribute("version", "1.0.0");
    m_xml.setAttribute("provider-name", "BSI Business Systems Integration AG");
    //
    SimpleXmlElement e = new SimpleXmlElement("description");
    e.setAttribute("url", "http://www.example.com/description");
    e.setContent("[Enter Feature Description here.]");
    m_xml.addChild(e);
    //
    e = new SimpleXmlElement("copyright");
    e.setAttribute("url", "http://www.example.com/description");
    e.setContent("[Enter Copyright Description here.]");
    m_xml.addChild(e);
    //
    e = new SimpleXmlElement("license");
    e.setAttribute("url", "http://www.example.com/description");
    e.setContent("[Enter License Description here.]");
    m_xml.addChild(e);
    loadXmlInternal(m_xml);
  }

  @Override
  public boolean store(IProgressMonitor p) throws CoreException {
    return storeXmlInternal(m_xml, p);
  }

  public SimpleXmlElement getDescription(boolean autoCreate) {
    SimpleXmlElement e = m_xml.getChild("description");
    if (e == null && autoCreate) {
      e = new SimpleXmlElement("description");
      m_xml.addChild(e);
    }
    return e;
  }

  public SimpleXmlElement getCopyright(boolean autoCreate) {
    SimpleXmlElement e = m_xml.getChild("copyright");
    if (e == null && autoCreate) {
      e = new SimpleXmlElement("copyright");
      m_xml.addChild(e);
    }
    return e;
  }

  public SimpleXmlElement getLicense(boolean autoCreate) {
    SimpleXmlElement e = m_xml.getChild("license");
    if (e == null && autoCreate) {
      e = new SimpleXmlElement("license");
      m_xml.addChild(e);
    }
    return e;
  }

  public Set<SimpleXmlElement> getFeatures() {
    HashSet<SimpleXmlElement> set = new HashSet<SimpleXmlElement>();
    for (SimpleXmlElement e : m_xml.getChildren("includes")) {
      set.add(e);
    }
    return set;
  }

  public SimpleXmlElement addFeature(String symbolicName) {
    SimpleXmlElement e = getFeature(symbolicName, true);
    return e;
  }

  public SimpleXmlElement getFeature(String symbolicName, boolean autoCreate) {
    SimpleXmlElement e = null;
    for (SimpleXmlElement xml : m_xml.getChildren("includes")) {
      if (xml.getStringAttribute("id", "").equals(symbolicName)) {
        e = xml;
        break;
      }
    }
    if (e == null && autoCreate) {
      e = new SimpleXmlElement("includes");
      e.setAttribute("id", symbolicName);
      e.setAttribute("version", "0.0.0");
      m_xml.addChild(e);
    }
    return e;
  }

  public Set<SimpleXmlElement> getPlugins() {
    HashSet<SimpleXmlElement> set = new HashSet<SimpleXmlElement>();
    for (SimpleXmlElement e : m_xml.getChildren("plugin")) {
      set.add(e);
    }
    return set;
  }

  public SimpleXmlElement addPlugin(String symbolicName) {
    return addPlugin(symbolicName, false, false);
  }

  public SimpleXmlElement addPlugin(String symbolicName, boolean fragment, boolean unpack) {
    SimpleXmlElement e = getPlugin(symbolicName, true);
    e.setAttribute("id", symbolicName);
    e.setAttribute("unpack", "" + unpack);
    e.removeAttribute("fragment");
    if (fragment) e.setAttribute("fragment", "true");
    return e;
  }

  public SimpleXmlElement getPlugin(String symbolicName, boolean autoCreate) {
    SimpleXmlElement e = null;
    for (SimpleXmlElement xml : m_xml.getChildren("plugin")) {
      if (xml.getStringAttribute("id", "").equals(symbolicName)) {
        e = xml;
        break;
      }
    }
    if (e == null && autoCreate) {
      e = new SimpleXmlElement("plugin");
      e.setAttribute("id", symbolicName);
      e.setAttribute("download-size", "0");
      e.setAttribute("install-size", "0");
      e.setAttribute("version", "0.0.0");
      e.setAttribute("unpack", "false");
      m_xml.addChild(e);
    }
    return e;
  }

}
