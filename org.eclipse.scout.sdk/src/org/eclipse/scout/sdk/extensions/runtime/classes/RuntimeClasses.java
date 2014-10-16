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
package org.eclipse.scout.sdk.extensions.runtime.classes;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.holders.StringHolder;
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.internal.workspace.ScoutWorkspace;
import org.eclipse.scout.sdk.util.ScoutUtility;
import org.eclipse.scout.sdk.util.signature.SignatureCache;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.IScoutWorkspaceListener;
import org.eclipse.scout.sdk.workspace.ScoutWorkspaceEvent;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;

/**
 * <h3>{@link RuntimeClasses}</h3>
 *
 * @author Matthias Villiger
 * @since 3.8.0 22.11.2012
 */
public final class RuntimeClasses implements IRuntimeClasses {
  private static final String PREFERENCES_PREFIX = "default.super.types.";

  private static final String EXTENSION_POINT_NAME = "runtimeClasses";
  private static final String TAG_NAME = "element";
  private static final String ATTRIB_INTERFACE = "interface";
  private static final String TAG_DEFAULT_NAME = "default";
  private static final String ATTRIB_DEFAULT_PRIO = "priority";
  private static final String ATTRIB_DEFAULT_CLASS = "class";

  private static final Object LOCK = new Object();
  private static final Map<IScoutBundle, Map<String /* interfaceFqn */, StringHolder /* configured value */>> CONFIGURED_VALUES = new HashMap<IScoutBundle, Map<String, StringHolder>>();
  private static final Map<IScoutBundle, IPreferenceChangeListener> REGISTERED_LISTENERS = new HashMap<IScoutBundle, IPreferenceChangeListener>();
  private static volatile Map<String /* interfaceFqn */, TreeMap<Double, String> /* default values */> defaultValues = null;

  private RuntimeClasses() {
  }

  private static Map<String, TreeMap<Double, String>> getDefaults() {
    if (defaultValues == null) {
      synchronized (LOCK) {
        if (defaultValues == null) {
          Map<String, TreeMap<Double, String>> tmp = new HashMap<String, TreeMap<Double, String>>();
          IExtensionRegistry reg = Platform.getExtensionRegistry();
          IExtensionPoint xp = reg.getExtensionPoint(ScoutSdk.PLUGIN_ID, EXTENSION_POINT_NAME);
          IExtension[] extensions = xp.getExtensions();
          for (IExtension extension : extensions) {
            IConfigurationElement[] elements = extension.getConfigurationElements();
            for (IConfigurationElement element : elements) {
              if (TAG_NAME.equals(element.getName())) {
                String interf = element.getAttribute(ATTRIB_INTERFACE);
                TreeMap<Double, String> curDefaults = tmp.get(interf);
                if (curDefaults == null) {
                  curDefaults = new TreeMap<Double, String>();
                  tmp.put(interf, curDefaults);
                }

                for (IConfigurationElement defaultElement : element.getChildren(TAG_DEFAULT_NAME)) {
                  String def = defaultElement.getAttribute(ATTRIB_DEFAULT_CLASS);
                  Double prio = -parseDouble(defaultElement.getAttribute(ATTRIB_DEFAULT_PRIO));
                  String existing = curDefaults.put(prio, def);
                  if (existing != null) {
                    ScoutSdk.logWarning("Multiple super type definitions with the same priority found for interface '" + interf + "'.");
                  }
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

  private static Double parseDouble(String order) {
    if (StringUtility.hasText(order)) {
      try {
        return Double.valueOf(order);
      }
      catch (NumberFormatException e) {
        ScoutSdk.logWarning("Invalid numeric extension order: '" + order + "'.", e);
      }
    }
    return Double.valueOf(0);
  }

  private static String getConfiguredSuperTypeNameCached(IScoutBundle context, String interfaceFqn) {
    synchronized (LOCK) {
      Map<String, StringHolder> projectConfigs = CONFIGURED_VALUES.get(context);
      if (projectConfigs == null) {
        projectConfigs = new HashMap<String, StringHolder>();
        CONFIGURED_VALUES.put(context, projectConfigs);

        // first time we are reading properties for the given project -> we are starting to cache for that project
        // add listener to get informed when the settings change so that we can clear our cache for the project.
        P_PreferenceChangeListener listener = new P_PreferenceChangeListener(projectConfigs);
        REGISTERED_LISTENERS.put(context, listener); // remember in case we must remove it again later on
        context.getPreferences().addPreferenceChangeListener(listener);
      }
      StringHolder ret = projectConfigs.get(interfaceFqn);
      if (ret == null) {
        ret = new StringHolder(context.getPreferences().get(getPreferenceKey(interfaceFqn), null));
        projectConfigs.put(interfaceFqn, ret);
      }
      return ret.getValue();
    }
  }

  public static String getPreferenceKey(String interfaceFqn) {
    return PREFERENCES_PREFIX + interfaceFqn;
  }

  public static Map<String, String> getAllDefaults(IScoutBundle context) {
    Set<String> interfaces = getDefaults().keySet();
    HashMap<String, String> ret = new HashMap<String, String>(interfaces.size());
    for (String entry : interfaces) {
      String defaultClass = getDefaultSuperType(entry, context);
      if (defaultClass != null) {
        IType t = TypeUtility.getType(defaultClass);
        if (TypeUtility.exists(t) && ScoutTypeUtility.isOnClasspath(t, context)) {
          // some classes may not be available for a project (e.g. no ISwingEnvironment for a project with swt UI).
          ret.put(entry, defaultClass);
        }
      }
    }
    return ret;
  }

  public static IType getSuperType(IType interfaceType, IScoutBundle context) {
    return getSuperType(interfaceType.getFullyQualifiedName(), context);
  }

  public static IType getSuperType(IType interfaceType, IJavaProject context) {
    return getSuperType(interfaceType.getFullyQualifiedName(), context);
  }

  public static IType getSuperType(String interfaceFqn, IJavaProject context) {
    return TypeUtility.getType(getSuperTypeName(interfaceFqn, context));
  }

  public static IType getSuperType(String interfaceFqn, IScoutBundle context) {
    return TypeUtility.getType(getSuperTypeName(interfaceFqn, context));
  }

  public static String getSuperTypeName(IType interfaceType, IScoutBundle context) {
    return getSuperTypeName(interfaceType.getFullyQualifiedName(), context);
  }

  public static String getSuperTypeName(IType interfaceType, IJavaProject context) {
    return getSuperTypeName(interfaceType.getFullyQualifiedName(), context);
  }

  public static String getSuperTypeName(String interfaceFqn, IJavaProject jp) {
    IScoutBundle context = ScoutTypeUtility.getScoutBundle(jp);
    return getSuperTypeName(interfaceFqn, context);
  }

  public static String getSuperTypeName(String interfaceFqn, IScoutBundle context) {
    String config = getConfiguredSuperTypeNameCached(context, interfaceFqn);
    if (config == null) {
      return getDefaultSuperTypeNameInternal(interfaceFqn, context);
    }
    else {
      IType t = TypeUtility.getType(config);
      if (TypeUtility.exists(t)) {
        return config;
      }
      else {
        return getDefaultSuperTypeNameInternal(interfaceFqn, context);
      }
    }
  }

  private static String getDefaultSuperTypeNameInternal(String interfaceFqn, IScoutBundle context) {
    String defaultType = getDefaultSuperType(interfaceFqn, context);
    if (defaultType == null) {
      throw new IllegalArgumentException("No default super class for '" + interfaceFqn + "' found on classpath of project '" + context.getSymbolicName() + "'.");
    }
    return defaultType;
  }

  private static String getDefaultSuperType(String interfaceFqn, IScoutBundle context) {
    TreeMap<Double, String> defaults = getDefaults().get(interfaceFqn);
    if (defaults != null) {
      for (String fqn : defaults.values()) {
        IType t = TypeUtility.getType(fqn);
        if (TypeUtility.exists(t)) {
          if (TypeUtility.isOnClasspath(t, ScoutUtility.getJavaProject(context))) {
            return fqn;
          }
        }
      }
      // no default seems to be on the class path: return the first as a guess
      return defaults.firstEntry().getValue();
    }
    return null;
  }

  public static String getSuperTypeSignature(IType interfaceType, IJavaProject context) {
    return getSuperTypeSignature(interfaceType.getFullyQualifiedName(), context);
  }

  public static String getSuperTypeSignature(IType interfaceType, IScoutBundle context) {
    return getSuperTypeSignature(interfaceType.getFullyQualifiedName(), context);
  }

  public static String getSuperTypeSignature(String interfaceFqn, IScoutBundle context) {
    String superType = getSuperTypeName(interfaceFqn, context);
    return SignatureCache.createTypeSignature(superType);
  }

  public static String getSuperTypeSignature(String interfaceFqn, IJavaProject context) {
    String superType = getSuperTypeName(interfaceFqn, context);
    return SignatureCache.createTypeSignature(superType);
  }

  private static final class P_PreferenceChangeListener implements IPreferenceChangeListener {
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

  private static final class P_ScoutWorkspaceListener implements IScoutWorkspaceListener {
    @Override
    public void workspaceChanged(ScoutWorkspaceEvent event) {
      if (event.getType() == ScoutWorkspaceEvent.TYPE_BUNDLE_REMOVED) {
        IScoutBundle removed = event.getScoutElement();
        // A scout project is removed from the workspace: also remove all caches of this project
        CONFIGURED_VALUES.remove(removed);

        // there is still a listener attached to the project preferences -> remove and deregister
        IPreferenceChangeListener existingListener = REGISTERED_LISTENERS.remove(removed);
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
