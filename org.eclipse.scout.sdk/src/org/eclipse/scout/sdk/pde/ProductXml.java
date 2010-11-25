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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.scout.commons.xmlparser.SimpleXmlElement;

/**
 * representation of a *.product xml file
 */
public class ProductXml extends AbstractConfigFile {
  private SimpleXmlElement m_xml;

  public ProductXml(IFile file) throws CoreException {
    super(file);
    m_xml = new SimpleXmlElement("product");
    loadXmlInternal(m_xml);
  }

  @Override
  public boolean store(IProgressMonitor p) throws CoreException {
    return storeXmlInternal(m_xml, p);
  }

  public String getProductId() {
    return m_xml.getStringAttribute("id");
  }

  public SimpleXmlElement getRootElement() {
    return m_xml;
  }

  public void addPlugin(String name) {
    addPlugin(name, false);
  }

  public void addPlugin(String symbolicName, boolean fragment) {
    SimpleXmlElement pluginXml = getPluginXml(symbolicName, true);
    if (fragment) pluginXml.setAttribute("fragment", "true");
  }

  public void removePlugin(String symbolicName) {
    SimpleXmlElement pluginXml = getPluginXml(symbolicName, false);
    if (pluginXml != null) {
      pluginXml.getParent().removeChild(pluginXml);
    }
  }

  public SimpleXmlElement getConfigIniXml(boolean autoCreate) {
    SimpleXmlElement configIniXml = m_xml.getChild("configIni");
    if (configIniXml == null && autoCreate) {
      configIniXml = new SimpleXmlElement("configIni");
      m_xml.addChild(configIniXml);
    }
    return configIniXml;
  }

  public Set<String> getPlugins() {
    HashSet<String> set = new HashSet<String>();
    for (SimpleXmlElement pluginXml : getPluginsXml(true).getChildren("plugin")) {
      set.add(pluginXml.getStringAttribute("id"));
    }
    return set;
  }

  private SimpleXmlElement getPluginsXml(boolean autoCreate) {
    SimpleXmlElement pluginsXml = m_xml.getChild("plugins");
    if (pluginsXml == null && autoCreate) {
      pluginsXml = new SimpleXmlElement("plugins");
      m_xml.addChild(pluginsXml);
    }
    return pluginsXml;
  }

  private SimpleXmlElement getPluginXml(String symbolicName, boolean autoCreate) {
    SimpleXmlElement pluginsXml = m_xml.getChild("plugins");
    if (pluginsXml == null && autoCreate) {
      pluginsXml = new SimpleXmlElement("plugins");
      m_xml.addChild(pluginsXml);
    }
    if (pluginsXml != null) {
      for (SimpleXmlElement pluginXml : pluginsXml.getChildren("plugin")) {
        if (pluginXml.getStringAttribute("id", "").equals(symbolicName)) {
          return pluginXml;
        }
      }
      if (autoCreate) {
        SimpleXmlElement pluginXml = new SimpleXmlElement("plugin");
        pluginXml.setAttribute("id", symbolicName);
        pluginsXml.addChild(pluginXml);
        return pluginXml;
      }
    }
    return null;
  }

  public String getVmArgs() {
    SimpleXmlElement e = getVmArgsXml(false);
    if (e != null) {
      return e.getContent();
    }
    else {
      return null;
    }
  }

  public String getVm() {
    SimpleXmlElement e = getVmXml(false);
    if (e != null) {
      for (SimpleXmlElement x : e.getChildren()) {
        String s = x.getContent();
        if (s != null && s.startsWith(IJavaLaunchConfigurationConstants.ATTR_JRE_CONTAINER_PATH)) {
          return s;
        }
      }
    }
    return null;
  }

  public void setVmArgs(String vmArgs) {
    SimpleXmlElement e = getVmArgsXml(true);
    e.setContent(vmArgs);
  }

  private SimpleXmlElement getVmArgsXml(boolean autoCreate) {
    SimpleXmlElement launcherArgsXml = m_xml.getChild("launcherArgs");
    if (launcherArgsXml == null && autoCreate) {
      launcherArgsXml = new SimpleXmlElement("launcherArgs");
      m_xml.addChild(launcherArgsXml);
    }
    if (launcherArgsXml != null) {
      SimpleXmlElement vmArgsXml = launcherArgsXml.getChild("vmArgs");
      if (vmArgsXml == null && autoCreate) {
        vmArgsXml = new SimpleXmlElement("vmArgs");
        launcherArgsXml.addChild(vmArgsXml);
      }
      if (vmArgsXml != null) {
        return vmArgsXml;
      }
    }
    return null;
  }

  private SimpleXmlElement getVmXml(boolean autoCreate) {
    SimpleXmlElement vmXml = m_xml.getChild("vm");
    if (vmXml == null && autoCreate) {
      vmXml = new SimpleXmlElement("vm");
      m_xml.addChild(vmXml);
    }
    return vmXml;
  }

}
