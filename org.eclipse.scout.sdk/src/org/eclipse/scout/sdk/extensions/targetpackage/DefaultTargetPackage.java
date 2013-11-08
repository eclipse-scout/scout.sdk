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
package org.eclipse.scout.sdk.extensions.targetpackage;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.holders.StringHolder;
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.internal.workspace.ScoutWorkspace;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.IScoutWorkspaceListener;
import org.eclipse.scout.sdk.workspace.ScoutWorkspaceEvent;

/**
 * <h3>{@link DefaultTargetPackage}</h3>
 * 
 * @author Matthias Villiger
 * @since 3.9.0 17.12.2012
 */
public final class DefaultTargetPackage implements IDefaultTargetPackage {

  public final static String PROP_USE_LEGACY_TARGET_PACKAGE = ScoutSdk.PLUGIN_ID + ".propDefaultTargetPackageConfigEnabled";

  private final static String PREFERENCES_PREFIX = "default.target.package.";
  private final static String PACKAGE_PREFIX = ".";

  private final static String EXTENSION_POINT_NAME = "targetPackage";
  private final static String TAG_NAME = "targetPackage";
  private final static String ATTRIB_DEFAULT_PACKAGE = "default";
  private final static String ATTRIB_ID = "id";
  private final static String ATTRIB_TYPE = "bundleType";

  private static Map<String /* packageId */, TargetPackageEntry> defaultValues = null;
  private static boolean isPackageConfigurationEnabled;
  private final static Object lock = new Object();
  private final static Map<IScoutBundle, Map<String /* packageId */, StringHolder /* configured value */>> configuredValues = new HashMap<IScoutBundle, Map<String, StringHolder>>();
  private final static Map<IScoutBundle, IPreferenceChangeListener> registeredListeners = new HashMap<IScoutBundle, IPreferenceChangeListener>();

  private DefaultTargetPackage() {
  }

  private static Map<String, TargetPackageEntry> getDefaults() {
    if (defaultValues == null) {
      synchronized (lock) {
        if (defaultValues == null) {
          Map<String, TargetPackageEntry> tmp = new HashMap<String, TargetPackageEntry>();
          IExtensionRegistry reg = Platform.getExtensionRegistry();
          IExtensionPoint xp = reg.getExtensionPoint(ScoutSdk.PLUGIN_ID, EXTENSION_POINT_NAME);
          IExtension[] extensions = xp.getExtensions();
          for (IExtension extension : extensions) {
            IConfigurationElement[] elements = extension.getConfigurationElements();
            for (IConfigurationElement element : elements) {
              if (TAG_NAME.equals(element.getName())) {
                String def = element.getAttribute(ATTRIB_DEFAULT_PACKAGE);
                String id = element.getAttribute(ATTRIB_ID);
                String type = element.getAttribute(ATTRIB_TYPE);
                if (StringUtility.hasText(id) && def != null && StringUtility.hasText(type)) {
                  def = def.trim();
                  id = id.trim();
                  type = type.trim();
                  if (def.startsWith(PACKAGE_PREFIX)) {
                    def = def.substring(PACKAGE_PREFIX.length());
                  }
                  TargetPackageEntry existing = tmp.put(id, new TargetPackageEntry(id, def, type));
                  if (existing != null) {
                    ScoutSdk.logWarning("Multiple default package definitions found for id '" + id + "'.");
                  }
                }
                else {
                  ScoutSdk.logWarning("Invalid default package entry.");
                }
              }
            }
          }
          defaultValues = tmp;

          P_ScoutWorkspaceListener l = new P_ScoutWorkspaceListener();
          ScoutWorkspace.getInstance().addWorkspaceListener(l);
        }
      }
    }
    return defaultValues;
  }

  public static Set<TargetPackageEntry> getAllDefaults() {
    Map<String, TargetPackageEntry> def = getDefaults();
    HashSet<TargetPackageEntry> ret = new HashSet<TargetPackageEntry>(def.size());
    for (TargetPackageEntry entry : def.values()) {
      ret.add(entry);
    }
    return ret;
  }

  /**
   * Gets if users can specify the target package or if the legacy packages should be used.
   * 
   * @return true if the new mode is enabled in the settings. false if the legacy mode is active.
   */
  public static boolean isPackageConfigurationEnabled() {
    synchronized (lock) {
      return isPackageConfigurationEnabled;
    }
  }

  /**
   * Sets if users can specify the target package or if the legacy packages should be used.
   * 
   * @param enabled
   *          true if the new mode should be used (package can be chosen). false if the legacy mode should be active.
   */
  public static void setIsPackageConfigurationEnabled(boolean enabled) {
    synchronized (lock) {
      isPackageConfigurationEnabled = enabled;
    }
  }

  public static String get(IScoutBundle context, String packageId) {
    if (isPackageConfigurationEnabled()) {
      // users can specify the target package: use the configured defaults
      String config = getConfiguredDefaultPackageCached(context, packageId);
      if (config != null) {
        return config;
      }
    }
    else {
      String legacyPackage = getLegacyPackages(packageId);
      if (legacyPackage != null) {
        return legacyPackage;
      }
    }
    return getDefaults().get(packageId).getDefaultSuffix();
  }

  /**
   * exceptions for target packages: the new location differs from the packages used in older scout sdk versions.<br>
   * To keep backwards compatibility with old scout projects a legacy support can be enabled. in this case these old
   * packages are used.
   * 
   * @param packageId
   * @return
   */
  private static String getLegacyPackages(String packageId) {
    // legacy support: users cannot specify the target package: use pre-defined packages
    if (IDefaultTargetPackage.CLIENT_FORMS.equals(packageId)) {
      return "ui.forms";
    }
    else if (IDefaultTargetPackage.CLIENT_SEARCHFORMS.equals(packageId)) {
      return "ui.searchforms";
    }
    else if (IDefaultTargetPackage.SHARED_SERVICES.equals(packageId)) {
      return "services.process";
    }
    else if (IDefaultTargetPackage.SERVER_SERVICES.equals(packageId)) {
      return "services.process";
    }
    return null;
  }

  public static String getPreferenceKey(String packageId) {
    return PREFERENCES_PREFIX + packageId;
  }

  private static String getConfiguredDefaultPackageCached(IScoutBundle context, String packageId) {
    synchronized (lock) {
      Map<String, StringHolder> projectConfigs = configuredValues.get(context);
      if (projectConfigs == null) {
        projectConfigs = new HashMap<String, StringHolder>();
        configuredValues.put(context, projectConfigs);

        // first time we are reading properties for the given project -> we are starting to cache for that project
        // add listener to get informed when the settings change so that we can clear our cache for the project.
        P_PreferenceChangeListener listener = new P_PreferenceChangeListener(projectConfigs);
        registeredListeners.put(context, listener); // remember in case we must remove it again later on
        context.getPreferences().addPreferenceChangeListener(listener);
      }
      StringHolder ret = projectConfigs.get(packageId);
      if (ret == null) {
        String configuredValue = context.getPreferences().get(getPreferenceKey(packageId), null);
        if (!StringUtility.hasText(configuredValue)) {
          configuredValue = null;
        }
        else {
          configuredValue = configuredValue.trim();
          if (configuredValue.startsWith(PACKAGE_PREFIX)) {
            configuredValue = configuredValue.substring(1);
          }
        }
        ret = new StringHolder(configuredValue);
        projectConfigs.put(packageId, ret);
      }
      return ret.getValue();
    }
  }

  private static class P_PreferenceChangeListener implements IPreferenceChangeListener {
    private Map<String, StringHolder> m_projectConfig;

    private P_PreferenceChangeListener(Map<String, StringHolder> projectConfigs) {
      m_projectConfig = projectConfigs;
    }

    @Override
    public void preferenceChange(PreferenceChangeEvent event) {
      String key = event.getKey();
      if (key != null && key.startsWith(PREFERENCES_PREFIX)) {
        m_projectConfig.remove(key.substring(PREFERENCES_PREFIX.length())); // will be lazily filled again on next use
      }
    }
  }

  private static class P_ScoutWorkspaceListener implements IScoutWorkspaceListener {
    @Override
    public void workspaceChanged(ScoutWorkspaceEvent event) {
      if (event.getType() == ScoutWorkspaceEvent.TYPE_BUNDLE_REMOVED) {
        IScoutBundle removed = event.getScoutElement();
        // A scout project is removed from the workspace: also remove all caches of this project
        configuredValues.remove(removed);

        // there is still a listener attached to the project preferences -> remove and deregister
        IPreferenceChangeListener existingListener = registeredListeners.remove(removed);
        if (existingListener != null) {
          try {
            removed.getPreferences().removePreferenceChangeListener(existingListener);
          }
          catch (IllegalStateException e) {
            //nop
          }
        }
      }
    }
  }
}
