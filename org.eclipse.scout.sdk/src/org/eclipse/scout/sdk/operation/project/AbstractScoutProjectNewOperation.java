package org.eclipse.scout.sdk.operation.project;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.ScoutSdkCore;
import org.eclipse.scout.sdk.util.PropertyMap;
import org.eclipse.scout.sdk.workspace.IScoutProject;
import org.osgi.framework.Version;

public abstract class AbstractScoutProjectNewOperation implements IScoutProjectNewOperation {

  private PropertyMap m_properties;

  protected AbstractScoutProjectNewOperation() {
  }

  @Override
  public void validate() throws IllegalArgumentException {
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
  protected List<IJavaProject> getCreatedBundlesList() {
    return getProperties().getProperty(PROP_CREATED_BUNDLES, List.class);
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

  protected final String getPluginName(String pluginSuffix) {
    final String DELIM = ".";
    pluginSuffix = pluginSuffix.trim();
    if (pluginSuffix.length() > 0 && !pluginSuffix.startsWith(DELIM)) {
      pluginSuffix = DELIM + pluginSuffix;
    }

    String postfix = getProjectNamePostfix();
    if (postfix == null) {
      postfix = "";
    }
    else {
      postfix = postfix.trim();
      if (postfix.length() > 0 && !postfix.startsWith(DELIM)) {
        postfix = DELIM + postfix;
      }
    }

    return getProjectName().trim() + pluginSuffix + postfix;
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

  protected final Version getTargetPlatformVersion() {
    return getProperties().getProperty(PROP_TARGET_PLATFORM_VERSION, Version.class);
  }

  protected final String getScoutProjectName() {
    if (StringUtility.isNullOrEmpty(getProjectNamePostfix())) {
      return getProjectName();
    }
    else {
      return getProjectName() + " (" + getProjectNamePostfix() + ")";
    }
  }

  protected final IScoutProject getScoutProject() {
    return ScoutSdkCore.getScoutWorkspace().findScoutProject(getScoutProjectName());
  }
}
