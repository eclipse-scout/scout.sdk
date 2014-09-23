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
package org.eclipse.scout.sdk.operation.project;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.compatibility.PlatformVersionUtility;
import org.eclipse.scout.sdk.util.PropertyMap;
import org.osgi.framework.Version;

public abstract class AbstractScoutProjectNewOperation implements IScoutProjectNewOperation {

  public static final String UPDATE_SITE_URL_MARS = "http://download.eclipse.org/releases/mars";
  public static final String UPDATE_SITE_URL_LUNA = "http://download.eclipse.org/releases/luna";
  public static final String UPDATE_SITE_URL_JUNO_38 = "http://download.eclipse.org/eclipse/updates/3.8";
  public static final String UPDATE_SITE_URL_INDIGO = "http://download.eclipse.org/releases/indigo";

  private PropertyMap m_properties;

  protected AbstractScoutProjectNewOperation() {
  }

  @Override
  public void validate() {
    if (getProperties() == null) {
      throw new IllegalArgumentException("Properties may not be null.");
    }
    if (StringUtility.isNullOrEmpty(getProjectAlias())) {
      throw new IllegalArgumentException("The project alias may not be null.");
    }
    if (StringUtility.isNullOrEmpty(getProjectName())) {
      throw new IllegalArgumentException("the project name may not be null.");
    }
  }

  protected final void addCreatedProductFile(IFile file) {
    getCreatedProductFiles().add(file);
  }

  protected final void addCreatedBundle(IJavaProject javaProject) {
    getCreatedBundlesList().add(javaProject);
  }

  protected final IJavaProject getCreatedBundle(String bundleName) {
    for (IJavaProject p : getCreatedBundlesList()) {
      if (p.getElementName().equals(bundleName)) {
        return p;
      }
    }
    return null;
  }

  protected final boolean isNodeChecked(String nodeId) {
    return getCheckedNodeIds().contains(nodeId);
  }

  @SuppressWarnings("unchecked")
  protected final List<IJavaProject> getCreatedBundlesList() {
    return getProperties().getProperty(PROP_CREATED_BUNDLES, List.class);
  }

  @SuppressWarnings("unchecked")
  public final synchronized List<IFile> getCreatedProductFiles() {
    List<IFile> list = getProperties().getProperty(PROP_CREATED_PRODUCT_FILES, List.class);
    if (list == null) {
      list = new ArrayList<IFile>();
      getProperties().setProperty(PROP_CREATED_PRODUCT_FILES, list);
    }
    return list;
  }

  protected String getUpdateSiteUrl() {
    Version targetPlatformVersion = getTargetPlatformVersion();
    if (PlatformVersionUtility.isIndigo(targetPlatformVersion)) {
      return UPDATE_SITE_URL_INDIGO;
    }
    else if (PlatformVersionUtility.isLuna(targetPlatformVersion)) {
      return UPDATE_SITE_URL_LUNA;
    }
    else if (PlatformVersionUtility.isMars(targetPlatformVersion)) {
      return UPDATE_SITE_URL_MARS;
    }
    else {
      return UPDATE_SITE_URL_JUNO_38;
    }
  }

  @SuppressWarnings("unchecked")
  private Set<String> getCheckedNodeIds() {
    return getProperties().getProperty(PROP_PROJECT_CHECKED_NODES, Set.class);
  }

  protected final String getProjectAlias() {
    return getProperties().getProperty(PROP_PROJECT_ALIAS, String.class);
  }

  protected final String getProjectName() {
    return getProperties().getProperty(PROP_PROJECT_NAME, String.class);
  }

  protected final String getProjectNamePostfix() {
    return getProperties().getProperty(PROP_PROJECT_NAME_POSTFIX, String.class);
  }

  protected final String getTemplateName() {
    return getProperties().getProperty(PROP_SELECTED_TEMPLATE_NAME, String.class);
  }

  protected final boolean isUseDefaultJdtPrefs() {
    return getProperties().getProperty(PROP_USE_DEFAULT_JDT_PREFS, Boolean.class);
  }

  protected final boolean isKeepCurrentTarget() {
    return getProperties().getProperty(PROP_KEEP_CURRENT_TARGET, Boolean.class);
  }

  protected final String getPluginName(String pluginSuffix) {
    return getPluginName(getProjectName(), getProjectNamePostfix(), pluginSuffix);
  }

  public static String getPluginName(String name, String postfix, String pluginSuffix) {
    final String DELIM = ".";
    if (pluginSuffix == null) {
      pluginSuffix = "";
    }
    else {
      pluginSuffix = pluginSuffix.trim();
    }
    if (pluginSuffix.length() > 0 && !pluginSuffix.startsWith(DELIM)) {
      pluginSuffix = DELIM + pluginSuffix;
    }

    if (postfix == null) {
      postfix = "";
    }
    else {
      postfix = postfix.trim();
      if (postfix.length() > 0 && !postfix.startsWith(DELIM)) {
        postfix = DELIM + postfix;
      }
    }

    if (name == null) {
      name = "";
    }
    else {
      name = name.trim();
    }

    return name + pluginSuffix + postfix;
  }

  @Override
  public final void setProperties(PropertyMap properties) {
    m_properties = properties;
  }

  protected final PropertyMap getProperties() {
    return m_properties;
  }

  protected final Map<String, String> getStringProperties() {
    Map<String, Object> allProps = getProperties().getPropertiesMap();
    HashMap<String, String> ret = new HashMap<String, String>();
    for (Entry<String, Object> e : allProps.entrySet()) {
      if (e != null && !StringUtility.isNullOrEmpty(e.getKey()) && e.getValue() instanceof String) {
        ret.put(e.getKey(), (String) e.getValue());
      }
    }
    return ret;
  }

  protected final String getOsgiOs() {
    return getProperties().getProperty(PROP_OS, String.class);
  }

  protected final String getOsgiWs() {
    return getProperties().getProperty(PROP_WS, String.class);
  }

  protected final String getOsgiArch() {
    return getProperties().getProperty(PROP_ARCH, String.class);
  }

  protected final String getLocalHostName() {
    return getProperties().getProperty(PROP_LOCALHOST, String.class);
  }

  protected final String getCurrentDate() {
    return getProperties().getProperty(PROP_CURRENT_DATE, String.class);
  }

  protected final String getUserName() {
    return getProperties().getProperty(PROP_USER_NAME, String.class);
  }

  protected final String getExecutionEnvironment() {
    return getProperties().getProperty(PROP_EXEC_ENV, String.class);
  }

  protected final Version getTargetPlatformVersion() {
    return getProperties().getProperty(PROP_TARGET_PLATFORM_VERSION, Version.class);
  }

  protected final String getScoutProjectName() {
    if (StringUtility.isNullOrEmpty(getProjectNamePostfix())) {
      return getProjectName();
    }
    return getProjectName() + " (" + getProjectNamePostfix() + ")";
  }
}
